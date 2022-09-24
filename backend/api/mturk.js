const express = require('express');
const AWS = require('aws-sdk');
const mongo = require('../services/mongo');
const { uuid } = require('uuidv4');

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
    loadScheduledHITs()
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

// dev only
app.post('/listHITs', async (req, res) => {
  let result = (await listHITs({MaxResults: 100}).catch(err => ({ error: err })));
  if (result) {
    return res.send({
      success: true,
      message: 'added experiment',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
})


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
    else {
      return res.send({
        success: false,
        message: result.error.message,
        error: result.error.code
      });
    }
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

        console.log("Searching for HIT ", hit.HITId);
        let mHIT = {}
        if (hit.HITStatus == 'pending' || 'failed') {
          result[i].hits[j] = hit
        }
        else {
          if (mHITs.hasOwnProperty(hit.HITId)) {
            mHIT = mHITs[hit.HITId];
            console.log("HIT found with id " + mHIT.HITId);
          } else {
            console.log("HIT not found in list, request via getHIT")
            mHIT = (await getHIT({HITId: hit.HITId}).catch(err => ({ error: err }))).HIT;
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
            let HITStatus = mHIT.HITStatus;

            mHIT = {
              HITId: hitID,
              title: title,
              available: `${available} / ${maxAssignments}`,
              pending: `${pending} / ${maxAssignments}`,
              waitingForApproval: `${waitingForApproval} / ${maxAssignments}`,
              completed: `${completed} / ${maxAssignments}`,
              creationTime: creationTime,
              HITStatus: HITStatus
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

  const createHITOptions = parseHITForCreation(data)
  let result
  const now = Date.now()
  // delay to utc
  const target = Date.parse(data.scheduledDateTime) + 7200000 
  const delay =  target - now 
  if (data.scheduledDateTime == '0' || delay < 0) {
    result = await createHIT(createHITOptions).catch(err => ({ error: err }));
  }
  else {
    const HITId = uuid()
    //hack since nodejs.settimeout doesnt return a number but instead a complex object which cant be sent vie res.send
    const timeout = +setTimeout(() => {scheduleHIT({HIT: createHITOptions, HITId})}, delay) 
    result = {HIT: {...createHITOptions, HITStatus: 'pending', scheduledDateTime: data.scheduledDateTime, HITId, timeoutId: timeout}, error: false}
    
  }
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

app.post('/expireHIT', async (req,res) => {
  const data = req.body
  const result = await expireHIT(data).catch(err => ({ error: err}));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'expired HIT',
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

app.post('/deleteHIT', async (req, res) => {
  const HITId = req.body.HITId;
  let result = await deleteHIT({HITId}).catch(err => ({ error: err }));
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

app.post('/approveAssignments', async (req, res) => {
  let data = req.body;
  let results = []
  let currentIDIndex = 0
  while (currentIDIndex <= data.assignmentIds.length - 1) {
    const assignment = await getAssignment({AssignmentId: data.assignmentIds[currentIDIndex]}).catch((err) => ({success: false, error: err}))
    if (assignment.error) {
      results =  [...results, {
        success: false,
        message: `Assignment ${data.assignmentIds[currentIDIndex]} was not found.`,
      }]
      currentIDIndex ++
      continue
    }
    if (assignment.Assignment.AssignmentStatus != "Submitted") {
      results = [...results, {
        success: false,
        message: `Assignment ${data.assignmentIds[currentIDIndex]} was allready in status ${assignment.Assignment.status}`,
      }]
      currentIDIndex ++
      continue
    }
    let params = {
      id : data.assignmentIds[currentIDIndex],
      feedback: data.feedback,
      awardQualificationID: data.awardQualificationID ,
      workerID: assignment.Assignment.WorkerId
    }
    results.push(await approveAssignment(params).then(() => ({ success: true, message: `Assignment ${params.id} was approved`})).catch(err => ({ success: false, error: err })))
    currentIDIndex ++
  }
  if (!results.some((result) => !result.success)){
    return res.send({
      success: true,
      message: 'approved all assignments',
      data: results
    })
  } else {
    return res.send({
      success: false,
      message: 'Some assignments could not be approved',
      data: results
    });
  }
});

app.post('/rejectAssignments', async (req, res) => {
  let data = req.body;
  let results = []
  let currentIDIndex = 0
  while (currentIDIndex <= data.assignmentIds.length - 1) {
    const assignment = await getAssignment({AssignmentId: data.assignmentIds[currentIDIndex]}).catch((err) => ({success: false, error: err}))
    if (assignment.error) {
      results =  [...results, {
        success: false,
        message: `Assignment ${data.assignmentIds[currentIDIndex]} was not found.`,
      }]
      currentIDIndex ++
      continue
    }
    if (assignment.Assignment.AssignmentStatus != "Submitted") {
      results = [...results, {
        success: false,
        message: `Assignment ${data.assignmentIds[currentIDIndex]} was allready in status ${assignment.Assignment.status}`,
      }]
      currentIDIndex ++
      continue
    }
    let params = {
      id : data.assignmentIds[currentIDIndex],
      feedback: data.feedback,
      workerID: assignment.Assignment.WorkerId
    }
    results.push(rejectAssignment(params).then((data) => ({ success: true, message: `Assignment ${params.id} was rejected`})).catch(err => ({ success: false, error: err })))
    currentIDIndex ++
  }
  if (!results.some((result) => !result.success)){
    return res.send({
      success: true,
      message: 'rejected all assignments',
      data: results
    })
  } else {
    return res.send({
      success: false,
      message: 'Some assignments could not be rejected',
      data: results
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


app.post('/listAssignments', async (req, res) => {
  let data = req.body;
  let result = await listAssignments(data).catch(err => ({ error: err }));
  let groupBy = data.groupBy;
  console.log("resulted Assisgnements", result)
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

app.post('/createMessage', async (req, res) => {
  let data = req.body;
  let result = await mongo.insertData(data, "messages").catch(err => ({
    error: err
  }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'Message created',
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

app.post('/deleteMessage', async (req, res) => {
  let data = req.body;
  let result = await mongo.removeData(data, "messages").catch(err => ({
    error: err
  }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'Message deleted',
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

app.post('/getMessages', async (req, res) => {
  let type = req.body.type;
  let result = await mongo.findData(data = {}, "messages").catch(err => ({
    error: err
  }));
  result = result.filter((message) => message.type == type)
  console.log("results:", result)
  if (!result.error) {
    return res.send({
      success: true,
      message: 'Got all Messages',
      data: result
    });
  }
})

app.post('/notifyWorkers', async (req, res) => {
  let data = req.body;
  let result = await notifyWorkers(data).catch(err => ({
    error: err
  }));
  if (!result.error) {
    return res.send({
      success: true,
      message: 'Notified Workers',
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

app.post('/deleteHITFromExperiment', async (req, res) => {
  let data = req.body;
  const HITId = data.HITId 
  const experiments = await mongo.findData(data={},"experiments")
  var containingExperimentArray = experiments.filter((experiment) => {
    return experiment.hits.some((_HIT) => {
      return HITId === _HIT.HITId
    })
  })
  if (!containingExperimentArray) {
    return res.send({
      success: false,
      message: 'Could not find the containing experiment'
    })
  }
  const containingExperiment = containingExperimentArray[0]
  if (!containingExperiment || !containingExperiment.hits) {
    return res.send({
      success: false,
      message: 'Could not find the containing experiment'
    })
  }
  containingExperiment.hits = containingExperiment.hits.filter((_HIT) => {
    if(_HIT.HITId != HITId) return _HIT
  })
  const result =  mongo.updateData({ _id: ObjectId(containingExperiment._id) }, containingExperiment)
  if (result) {
    return res.send({
      success: true,
      message: 'removed HIT from Experiment',
      data: result
    });
  } else {
    return res.send({
      success: false,
      message: 'Something went wrong'
    });
  }
});

app.post('/cancelScheduledHIT', async(req, res)=> {
  const data = req.body
  clearTimeout(data.timeoutId)
})




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
    return new Promise((resolve, reject) => {
      mturk.createHIT(params, (err, data) => {
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

//// might be bugged comapare to github version
const getHIT = (params) => {
  connectToMturk();
  
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

const listHITs = (params) => {
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

const expireHIT = ({ HITId }) => {
  connectToMturk()
  return new Promise((resolve, reject) => {
    mturk.updateExpirationForHIT({HITId, ExpireAt: 0}, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    });
  });
}

const deleteHIT = (params) => {
  connectToMturk();
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

const listAssignments = (params) => {
  connectToMturk();

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

const getAssignment = (params) => {
  connectToMturk();

  return new Promise((resolve, reject) => {
    mturk.getAssignment(params, (err, data) => {
      if (err) {
        console.log(err, err.stack); // an error occurred
        reject(err);
      } else {
        console.log(data); // successful response
        resolve(data);
      }
    })
  })
}


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

const scheduleHIT = async (HIT) => {
  const result = await createHIT(HIT.HIT).catch(err => ({ error: err }));
  if (!result.error) {
    const experiments = await mongo.findData(data={},"experiments")
    var containingExperiment = experiments.filter((experiment) => {
      return experiment.hits.some((_HIT) => {
        return HIT.HITId === _HIT.HITId
      })
    })[0] 
    containingExperiment.hits = containingExperiment.hits.map((_HIT) => {
      if(_HIT.HITId == HIT.HITId) return result.HIT
      return _HIT
    })
    mongo.updateData({ _id: ObjectId(containingExperiment._id) }, containingExperiment)
  }
};

const loadScheduledHITs = async () => {
  const experiments = await mongo.findData()
  let scheduledHITs = []
  for (const experiment of experiments) {
    for (const HIT of experiment.hits) {    
        if (HIT.HITStatus == 'pending') {
          const now = Date.now()
          const target = Date.parse(HIT.scheduledDateTime) + 7200000
          if (target > now) {
          // delay to utc
          const target = Date.parse(HIT.scheduledDateTime) + 7200000 
          const delay =  target - now 
          const scheduleId = +setTimeout(() => {
            scheduleHIT({HIT: {
              AssignmentDurationInSeconds: HIT.AssignmentDurationInSeconds,
              Description: HIT.Description,
              Reward: HIT.Reward,
              Title: HIT.Title,
              AutoApprovalDelayInSeconds: HIT.AutoApprovalDelayInSeconds,
              Keywords: HIT.Keywords,
              LifetimeInSeconds: HIT.LifetimeInSeconds,
              MaxAssignments: HIT.MaxAssignments,
              Question: HIT.Question,
              QualificationRequirements: HIT.QualificationRequirements,
            },
            HITId: HIT.HITId})}, delay)
          // to be able to cancel the HIT we need to update the scheduleId in the db
          HIT.scheduleId = scheduleId
          updateHIT(HIT)
          console.log(`Scheduled HIT ${HIT.HITId} at ${HIT.scheduledDateTime}`)
        }
        else {
          HIT.HITStatus = 'failed'
          updateHIT(HIT)
          console.log(`Scheduled HIT ${HIT.HITId} scheduled for ${HIT.scheduledDateTime} was missed probably due to downtime of the server`)
        } 
      }
    }
  }
  return scheduledHITs
}
 
const updateHIT = async (HIT) => {
  const experiments = await mongo.findData(data={},"experiments")
    var containingExperiment = experiments.filter((experiment) => {
      return experiment.hits.some((_HIT) => {
        return HIT.HITId === _HIT.HITId
      })
    })[0] 
    containingExperiment.hits = containingExperiment.hits.map((_HIT) => {
      if(_HIT.HITId == HIT.HITId) return HIT
      return _HIT
    })
    mongo.updateData({ _id: ObjectId(containingExperiment._id) }, containingExperiment)
}

const parseHITForCreation = (data) => {
  const lifetimeInSeconds = data.experiment.hitExpiresAfter * 60 * 60 * 24;
  console.log(`params.experiment`, lifetimeInSeconds);

  let createHITOptions = {
    AssignmentDurationInSeconds: data.experiment.assignmentDurationInMinutes * 60,
    Description: data.experiment.description,
    Reward: data.experiment.rewardPerAssignment,
    Title: data.experiment.title,
    AutoApprovalDelayInSeconds: lifetimeInSeconds,
    Keywords: data.experiment.keywords,
    LifetimeInSeconds: lifetimeInSeconds,
    MaxAssignments: data.experiment.assignmentsPerHit,
    //////////////////////////////////////////////////////////////////////////
    // there is a major issue here, needs to be turned back and investigated//
    //////////////////////////////////////////////////////////////////////////
    Question: `<?xml version=\"1.0\"?>\n<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">\n  <HTMLContent><![CDATA[<html><head><title>HIT</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body><!-- You must include this JavaScript file -->\r\n<script src=\"https://assets.crowd.aws/crowd-html-elements.js\"></script>\r\n\r\n<!-- For the full list of available Crowd HTML Elements and their input/output documentation,\r\n      please refer to https://docs.aws.amazon.com/sagemaker/latest/dg/sms-ui-template-reference.html -->\r\n\r\n<!-- You must include crowd-form so that your task submits answers to MTurk -->\r\n<crowd-form answer-format=\"flatten-objects\">\r\n\r\n  <crowd-instructions link-text=\"View instructions\" link-type=\"button\">\r\n    <short-summary>\r\n      <p>Provide a brief instruction here</p>\r\n    </short-summary>\r\n\r\n    <detailed-instructions>\r\n      <h3>Provide more detailed instructions here</h3>\r\n      <p>Include additional information</p>\r\n    </detailed-instructions>\r\n\r\n    <positive-example>\r\n      <p>Provide an example of a good answer here</p>\r\n      <p>Explain why it's a good answer</p>\r\n    </positive-example>\r\n\r\n    <negative-example>\r\n      <p>Provide an example of a bad answer here</p>\r\n      <p>Explain why it's a bad answer</p>\r\n    </negative-example>\r\n  </crowd-instructions>\r\n\r\n  <div>\r\n    <p>What is your favorite color for a bird?</p>\r\n    <crowd-input name=\"favoriteColor\" placeholder=\"example: pink\" required></crowd-input>\r\n  </div>\r\n\r\n  <div>\r\n    <p>Check this box if you like birds</p>\r\n    <crowd-checkbox name=\"likeBirds\" checked=\"true\" required></crowd-checkbox>\r\n  </div>\r\n\r\n  <div>\r\n    <p>On a scale of 1-10, how much do you like birds?</p>\r\n    <crowd-slider name=\"howMuch\" min=\"1\" max=\"10\" step=\"1\" pin=\"true\" required></crowd-slider>\r\n  </div>\r\n\r\n  <div>\r\n    <p>Write a short essay describing your favorite bird</p>\r\n    <crowd-text-area name=\"essay\" rows=\"4\" placeholder=\"Lorem ipsum...\" required></crowd-text-area>\r\n  </div>\r\n</crowd-form></body></html>]]></HTMLContent>\n  <FrameHeight>0</FrameHeight>\n</HTMLQuestion>\n`
  };
  var requirements = {
    QualificationRequirements: []
  }
  if (data.experiment.defaultRequirements) {
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
  console.log("Guard is " + data.experiment.guardHitbyQualification);
  if (data.experiment.guardHitByQualification) {
    requirements.QualificationRequirements.push(
      {
        QualificationTypeId: data.experiment.awardQualificationId,
        Comparator: 'DoesNotExist',
      }
    );
  }
  if (data.experiment.guardHitByAdditionalQualificationids) {
    for (const id of data.experiment.guardHitByAdditionalQualificationids.split(',')) {
      requirements.QualificationRequirements.push(
        {
          QualificationTypeId: id,
          Comparator: 'Exists'
        }
      )
    }
  }
  if (data.experiment.excludeWorkersByQualificationid) {
    for (const id of data.experiment.excludeWorkersByQualificationid.split(',')) {
      requirements.QualificationRequirements.push(
        {
          QualificationTypeId: id,
          Comparator: 'DoesNotExist'
        }
      )
    }
  }
  createHITOptions = Object.assign(createHITOptions, requirements);
}

const notifyWorkers = ({ subject, message, workerIDs }) => {
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

module.exports = app ;
