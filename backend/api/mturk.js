const express = require('express');
const AWS = require('aws-sdk');
const mongo = require('../services/mongo');
const ObjectId = require('mongodb').ObjectId;
//const endpoint = 'https://mturk-requester-sandbox.us-east-1.amazonaws.com';
const endpoint_default = 'https://mturk-requester.us-east-1.amazonaws.com';
var endpoint = endpoint_default;
const region = 'us-east-1';
const app = express();

let mturk;

app.post('/login', async (req, res) => {
  let data = req.body;

  let accessKeyId = data.awsAccessKeyId;
  let secretAccessKey = data.awsSecretAccessKey;
  console.log("endpoint requested " + data.endpoint);
  endpoint = data.endpoint || endpoint;
  console.log("endpoint in use will be " + endpoint);
  
  console.log('accessKeyId', accessKeyId || '');
  console.log('secretAccessKey', secretAccessKey || '');

  if (!accessKeyId || !secretAccessKey) {
    return res.send({
      success: false,
      message: 'Your Access-Key-ID or your Secret-Access-Key is empty'
    });
  }

  try {
    // TODO sandbox/live switch.
    AWS.config.update({
      region,
      endpoint,
      accessKeyId,
      secretAccessKey,
      sslEnabled: 'true'
    });

    mturk = new AWS.MTurk();
    let balance = await getBalance(mturk);

    return res.send({
      success: true,
      balance: balance,
      token: accessKeyId,
      message: 'You can pass'
    });
  } catch (e) {
    console.log(e);
    return res.send({
      success: false,
      message: 'Your Access-Key-ID or your Secret-Access-Key is wrong'
    });
  }
});

app.post('/addExperiment', async (req, res) => {
  let data = req.body;
  let result = await mongo.insertData(data);

  if (result) {
    return res.send({
      success: true,
      message: 'added experiment',
      data: {
        id: result.insertedId
      }
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/saveExperiment', async (req, res) => {
  let data = req.body;
  let id = data.id;

  delete data.experiment._id;
  data.experiment.endpoint = endpoint.indexOf("sandbox") == -1 ? "production" : "sandbox";
  const { awardQualificationName, awardQualificationDescription, awardQualificationId } = data.experiment;

  if (awardQualificationName && awardQualificationDescription) {
    let result = await createQualificationType(awardQualificationName, awardQualificationDescription).catch(err => ({ error: err }));

    if (!result.error) {
      console.log(result.QualificationType.QualificationTypeId);
      data.experiment.awardQualificationId = result.QualificationType.QualificationTypeId;
    }
    // else {
    //   return res.send({
    //     success: false,
    //     message: result.error.message,
    //     error: result.error.code
    //   });
    // }
  }

  let result = await mongo.updateData({ _id: ObjectId(id) }, data.experiment);

  if (result) {
    return res.send({
      success: true,
      message: 'Saved settings',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

const group = key => array => {
  return array.reduce(
    (objectsByKeyValue, obj) => ({
      ...objectsByKeyValue,
      [obj[key]]: (objectsByKeyValue[obj[key]] || []).concat(obj)
    }),
    {}
  );
};

app.post('/getExperiments', async (req, res) => {
  let data = req.body;
  let search_for = endpoint.indexOf("sandbox") == -1? "production" : "sandbox";
  console.log("Only searching for " + search_for);
  let id = data.id ? { _id: ObjectId(data.id), endpoint: search_for } : {endpoint: search_for};
  let groupBy = data.groupBy || null;
  let result = await mongo.findData(id);

  if (result) {
    for (var i = 0; i < result.length; i++) {
      let hits = result[i].hits;      
      let exp = {};

      exp.available = 0;
      exp.pending = 0;
      exp.waitingForApproval = 0;
      exp.completed = 0;
      exp.maxAssignments = 0;

      console.log("requesting HIT list");
      let res = (await listHITs({MaxResults: 100}).catch(err => ({ error: err })));
      let mHITList = [];
      if (!res.error) {
        mHITList = res.HITs;
        console.log("Got HITList with " + mHITList.length + " entries");
      } else {
        console.log("error while requesting HITlist: " + res.error);
      }

      
      let mHITs = {}
      for (var j = 0; j<mHITList.length; j++) {
        var hitDetail = mHITList[j];
        mHITs[hitDetail.HITId] = hitDetail;
      }
      console.log("HITlist split into HITs");

      for (var j = 0; hits && j < hits.length; j++) {
        let hit = hits[j];

        console.log("Searching for HIT " + JSON.stringify(hit) , hit.id);
        let mHIT = {}
        if (mHITs.hasOwnProperty(hit.id)) {
          mHIT = mHITs[hit.id];
          console.log("HIT found with id " + mHIT.HITId);
        } else {
          console.log("HIT not found in list, request via getHIT")
          mHIT = (await getHIT({id: hit.id}).catch(err => ({ error: err }))).HIT;
        }


        if (mHIT) {
          let hitID = mHIT.HITId;
          let maxAssignments = mHIT.MaxAssignments;
          let available = mHIT.NumberOfAssignmentsAvailable;
          let pending = mHIT.NumberOfAssignmentsPending;
          let completed = mHIT.NumberOfAssignmentsCompleted;
          let creationTime = mHIT.CreationTime;
          let title = mHIT.Title;
          let waitingForApproval =
            maxAssignments - available - completed - pending;
          let status = mHIT.HITStatus;

          mHIT = {
            id: hitID,
            title: title,
            available: `${available} / ${maxAssignments}`,
            pending: `${pending} / ${maxAssignments}`,
            waitingForApproval: `${waitingForApproval} / ${maxAssignments}`,
            completed: `${completed} / ${maxAssignments}`,
            creationTime: creationTime,
            status: status
          };

          result[i].hits[j] = mHIT;

          exp.available = exp.available + available;
          exp.pending = exp.pending + pending;
          exp.waitingForApproval = exp.waitingForApproval + waitingForApproval;
          exp.completed = exp.completed + completed;
          exp.maxAssignments = exp.maxAssignments + maxAssignments;

          result[i].available = `${exp.available} / ${exp.maxAssignments}`;
          result[i].pending = `${exp.pending} / ${exp.maxAssignments}`;
          result[i].waitingForApproval = `${exp.waitingForApproval} / ${
            exp.maxAssignments
            }`;
          result[i].completed = `${exp.completed} / ${exp.maxAssignments}`;
        } else {
          result[i].hits = [];
          result[i].available = `${exp.available} / ${exp.maxAssignments}`;
          result[i].pending = `${exp.pending} / ${exp.maxAssignments}`;
          result[i].waitingForApproval = `${exp.waitingForApproval} / ${
            exp.maxAssignments
            }`;
          result[i].completed = `${exp.completed} / ${exp.maxAssignments}`;
        }
      }
    }
    let mResult = (groupBy) ? group(groupBy)(result) : result;
    
    return res.send({
      success: true,
      endpoint: search_for,
      message: `found ${result.length} experiments`,
      data: mResult
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/deleteExperiment', async (req, res) => {
  let data = req.body;
  let id = data.id || {};
  let result = await mongo.removeData({ _id: ObjectId(id) });

  if (result) {
    return res.send({
      success: true,
      message: 'removed element',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/createHIT', async (req, res) => {
  let data = req.body;
  let result = await createHIT(data).catch(err => ({ error: err }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'created new HIT',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: result.error.message,
      error: result.error.code
    });
  }
});

app.post('/getHIT', async (req, res) => {
  let data = req.body;
  let result = await getHIT(data).catch(err => ({ error: err }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'got HIT',
      data: result.HIT
    });
  } else {
    return res.send({
      success: false,
      message: result.error.message,
      error: result.error.code
    });
  }
});

app.post('/deleteHIT', async (req, res) => {
  let data = req.body;
  let result = await deleteHIT(data).catch(err => ({ error: err }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'deleted HIT',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: result.error.message,
      error: result.error.code
    });
  }
});

app.post('/approveAssignment', async (req, res) => {
  let data = req.body;
  let result = await approveAssignment(data).catch(err => ({ error: err }));
  console.log(result);
  if (!result.error) {
    return res.send({
      success: true,
      message: 'approved assignment',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: result.error.errors,
      error: 'Something went wrong'
    });
  }
});

app.post('/qualifyWorker', async (req, res) => {
  let data = req.body;
  let result = await qualify(data).catch(err => ({ error: err }));
  console.log(result);
  if (!result.error) {
    return res.send({
      success: true,
      message: 'Qualification assigned',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: result.error.errors,
      error: 'Something went wrong'
    });
  }
});

app.post('/rejectAssignment', async (req, res) => {
  let data = req.body;
  let result = await rejectAssignment(data).catch(err => ({ error: err }));
  console.log(result);

  if (!result.error) {
    return res.send({
      success: true,
      message: 'rejected assignment',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/listAssignments', async (req, res) => {
  let data = req.body;
  let result = await listAssignments(data).catch(err => ({ error: err }));
  let groupBy = data.groupBy;
  console.log(result);

  if (!result.error) {
    return res.send({
      success: true,
      message: 'got all assignments',
      data: groupBy ? group(groupBy)(result.Assignments) : result.Assignments
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/createQualificationType', async (req, res) => {
  let data = req.body;
  let result = await createQualificationType(data).catch(err => ({
    error: err
  }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'qualification generated',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: result.error.message,
      error: result.error.code
    });
  }
});

const connectToMturk = () => {
  AWS.config.update({
    region,
    endpoint,
    sslEnabled: 'true'
  });

  mturk = new AWS.MTurk();
};

const getBalance = () => {
  return new Promise((resolve, reject) => {
    mturk.getAccountBalance({}, (err, data) => {
      if (err) {
        reject(err);
      }
      balance = data && data.AvailableBalance;
      resolve(balance);
    });
  });
};

const createHIT = async params => {
  connectToMturk();
  const lifetimeInSeconds = params['hitExpiresAfter (days)'] * 60 * 60 * 24;
  console.log(`params`, lifetimeInSeconds);

  let createHITOptions = {
    AssignmentDurationInSeconds: params.assignmentDurationInMinutes * 60,
    Description: params.description,
    Reward: params.rewardPerAssignment,
    Title: params.title,
    AutoApprovalDelayInSeconds: lifetimeInSeconds,
    Keywords: params.keywords,
    LifetimeInSeconds: lifetimeInSeconds,
    MaxAssignments: params.assignmentsPerHit,
    Question: `<?xml version="1.0" encoding="UTF-8"?><ExternalQuestion xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd"><ExternalURL>${
      params.entrypoint
      }</ExternalURL><FrameHeight>0</FrameHeight></ExternalQuestion>`
  };
  let requirements = {
    QualificationRequirements: []
  }
  if (params.defaultRequirements) {
    requirements = {
      
      QualificationRequirements: [
        {
          Comparator: 'GreaterThanOrEqualTo',
          QualificationTypeId: '000000000000000000L0',
          IntegerValues: [95]
        },
        {
          QualificationTypeId: '00000000000000000071',
          Comparator: 'EqualTo',
          LocaleValues: [
            {
              Country: 'US'
            }
          ]
        },
        {
          QualificationTypeId: '00000000000000000040',
          Comparator: 'GreaterThanOrEqualTo',
          IntegerValues: [1000]
        }
      ]
    };
  }
  console.log("Guard is " + params.guardHitbyQualification);
  if (params.guardHitByQualification) {
    requirements.QualificationRequirements.push(
      {
        QualificationTypeId: params.awardQualificationId,
        Comparator: 'DoesNotExist',
      }
    );
  }
  createHITOptions = Object.assign(createHITOptions, requirements);
  return new Promise((resolve, reject) => {
    mturk.createHIT(createHITOptions, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const getHIT = ({ id }) => {
  connectToMturk();

  let params = {
    HITId: id
  };
  // TODO show error as toast.
  return new Promise((resolve, reject) => {
    mturk.getHIT(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const listHITs = params => {
  connectToMturk();

  // TODO show error as toast.
  return new Promise((resolve, reject) => {
    mturk.listHITs(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const deleteHIT = ({ id }) => {
  connectToMturk();

  var params = {
    HITId: id
  };
  return new Promise((resolve, reject) => {
    mturk.deleteHIT(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const createQualificationType = (name, description) => {
  connectToMturk();

  let params = {
    Description: description,
    Name: name,
    QualificationTypeStatus: 'Active'
  };

  return new Promise((resolve, reject) => {
    mturk.createQualificationType(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const listAssignments = ({ id }) => {
  connectToMturk();

  let params = {
    HITId: id
  };
  return new Promise((resolve, reject) => {
    mturk.listAssignmentsForHIT(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const qualify = ({ awardQualificationID, workerID }) => {
  console.log("Qualifying worker " + workerID + " with " + awardQualificationID);
  let params = {
    QualificationTypeId: awardQualificationID,
    WorkerId: workerID,
    SendNotification: false
  };
  return new Promise((resolve, reject) => {
    mturk.associateQualificationWithWorker(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const approveAssignment = ({ id, feedback = '', awardQualificationID, workerID }) => {
  connectToMturk();

  let params = {
    AssignmentId: id,
    RequesterFeedback: feedback
  };

  return new Promise((resolve, reject) => {
    mturk.approveAssignment(params, async (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        awardQualificationID && await qualify({ awardQualificationID, workerID });
        resolve(data);
      }
    });
  });
};

const rejectAssignment = ({ id, feedback }) => {
  connectToMturk();

  let params = {
    AssignmentId: id,
    RequesterFeedback: feedback
  };
  return new Promise((resolve, reject) => {
    mturk.rejectAssignment(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

const contactWorkers = ({ subject, message, workerIDs }) => {
  var params = {
    MessageText: message,
    Subject: subject,
    WorkerIds: workerIDs
  };
  return new Promise((resolve, reject) => {
    mturk.notifyWorkers(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
};

module.exports = app;
