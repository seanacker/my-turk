const SERVER_URL = 'http://localhost:9010/api/v1/'

export default {
    login: payload => {
        return sendData('login', payload)
    },

    addExperiment: payload => {
        return sendData('experiments/', payload)
    },

    saveSettings: payload => {
        return sendData('experiments/', payload)
    },

    getExperiments: payload => {
        const options = {
            endpoint: SERVER_URL + "experiments/",
            method: 'GET',
            payload: payload,
        }
        return request(options)
    },

    getExperiment: id => {
        const options = {
            endpoint: SERVER_URL + "experiments/" + id + "/",
            method: 'GET',
        }
        return request(options)
    },

    //TODO overwrite
    deleteExperiment: payload => {
        return sendData('deleteExperiment', payload)
    },

    createHIT: experimentId => {
        const options = {
            endpoint: SERVER_URL + "experiments/" + experimentId + "/hits/",
            method: 'POST',
        }
        return request(options)
    },

    getHIT: hitId => {
        const options = {
            endpoint: SERVER_URL + "HITs/" + hitId + "/",
            method: 'GET',
        }
        return request(options)
    },

    //TODO overwrite
    deleteHIT: payload => {
        return sendData('deleteHIT', payload)
    },

    //TODO overwrite
    listAssignments: payload => {
        return sendData('listAssignments', payload)
    },

    //TODO overwrite
    approveAssignment: payload => {
        return sendData('approveAssignment', payload)
    },

    //TODO overwrite
    rejectAssignment: payload => {
        return sendData('rejectAssignment', payload)
    },
}

/**
 * POST Request.
 * @param {String} endpoint.
 * @param {Object} payload is the body data.
 * @return {Object} The result of the request.
 */
function sendData(endpoint, payload) {
    let options = {
        endpoint: SERVER_URL + endpoint,
        method: 'POST',
        payload: payload,
    }

    return request(options)
}

/**
 * Fetch function for get and post requests.
 * All requests are synchronous right now.
 * @param {options} .
 * @return {Object} The result of the request.
 */
async function request({endpoint, method, payload}) {
    let options = {}
    if (method === 'GET') {
        options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
        }
    } else {
        options = {
            method: method,
            body: JSON.stringify(payload) || '',
            headers: {
                'Content-Type': 'application/json',
            },
        }
    }

    let response = fetch(endpoint, options)
        .then(response => {
            if (!response.ok) {
                throw new Error('Not 200 response')
            }
            return response.json()
        })
        .then(data => {
            return {
                success: true,
                data: data
            }
        })
        .catch(error => {
            console.error('Error:', error)
            return false
        })

    return response
}
