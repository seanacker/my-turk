import { Payload, APIRes } from "../lib/types"

const SERVER_URL = 'http://localhost:3001/api/mturk/'

export default {
  login: (payload: Payload): Promise<APIRes> => {
    return sendData('login', payload)
  },

  addExperiment: (payload: Payload): Promise<APIRes> => {
    return sendData('addExperiment', payload)
  },

  saveSettings: (payload: Payload): Promise<APIRes> => {
    return sendData('saveExperiment', payload)
  },

  getExperiments: (payload: Payload): Promise<APIRes> => {
    return sendData('getExperiments', payload)
  },

  deleteExperiment: (payload: Payload): Promise<APIRes> => {
    return sendData('deleteExperiment', payload)
  },

  createHIT: (payload: Payload): Promise<APIRes> => {
    return sendData('createHIT', payload)
  },

  getHIT: (payload: Payload): Promise<APIRes> => {
    return sendData('getHIT', payload)
  },

  listHITs: (payload: Payload): Promise<APIRes> => {
    return sendData('listHITs', payload)
  },

  expireHIT: (payload: Payload): Promise<APIRes> => {
    return sendData('expireHIT', payload)
  },

  deleteHIT: (payload: Payload): Promise<APIRes> => {
    return sendData('deleteHIT', payload)
  },

  listAssignments: (payload: Payload): Promise<APIRes> => {
    return sendData('listAssignments', payload)
  },

  qualifyWorker: (payload: Payload): Promise<APIRes> => {
    return sendData('qualifyWorker', payload)
  },

  approveAssignment: (payload: Payload): Promise<APIRes> => {
    return sendData('approveAssignment', payload)
  },

  rejectAssignment: (payload: Payload): Promise<APIRes> => {
    return sendData('rejectAssignment', payload)
  },

  approveAssignments: (payload: Payload): Promise<APIRes> => {
    return sendData('approveAssignments', payload)
  },

  rejectAssignments: (payload: Payload): Promise<APIRes> => {
    return sendData('rejectAssignments', payload)
  },

  getMessages: (payload: Payload): Promise<APIRes> => {
    return sendData('getMessages', payload)
  },

  deleteMessage: (payload: Payload): Promise<APIRes> => {
    return sendData('deleteMessage', payload)
  },
    
  createMessage: (payload: Payload): Promise<APIRes> => {
    return sendData('createMessage', payload)
  },
  notifyWorkers: (payload: Payload): Promise<APIRes> => {
    return sendData('notifyWorkers', payload)
  },
  deleteHITFromExperiment: (payload: Payload): Promise<APIRes> => {
    return sendData('deleteHITFromExperiment', payload)
  },
  cancelScheduledHIT: (payload: Payload): Promise<APIRes> => {
    return sendData('cancelScheduledHIT', payload)
  }
}



/**
 * POST Request.
 * @param {String} endpoint.
 * @param {Object} payload is the body data.
 * @return {Object} The result of the request.
 */
function sendData(endpoint: string, payload: Payload): Promise<APIRes> {
  const options = {
    endpoint: SERVER_URL + endpoint,
    method: 'POST',
    payload,
  }

  return request(options)
}

/**
 * Fetch function for get and post requests.
 * All requests are synchronous right now.
 * @param {options} .
 * @return {Object} The result of the request.
 */
async function request({ endpoint, method, payload }:
  {endpoint: string, method: string, payload: Payload}): Promise<APIRes> {
  let options = {}
  if (method === 'GET') {
    options = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    }
  } else {
    options = {
      method,
      body: JSON.stringify(payload) || '',
      headers: {
        'Content-Type': 'application/json',
      },
    }
  }

  const response = await fetch(endpoint, options)
    .then((response) => {
      if (!response.ok) {
        throw new Error('Not 200 response')
      }
      return response.json()
    })
    .then((json) => {
      return json
    })
    .catch((error) => {
      console.error('Error:', error)
      return {
        success: false,
        message: error,
        endpoint
    }
    })

  return response
}
