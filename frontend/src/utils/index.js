export default {

    resolveErrorMessage: async errorResponse => {
        let errorMessage = "Error: "
        console.log(errorResponse);
        if (errorResponse.statusText) {
            errorMessage += errorResponse.statusText;
        }
        if (typeof errorResponse.json === 'function') {
            let json = await errorResponse.json();
            errorMessage = ''
            if (json.hasOwnProperty("message")) {
                errorMessage += json.message
                errorMessage += " "
            }
            if (json.hasOwnProperty("errors") && Array.isArray(json.errors)) {
                for (const currentError of json.errors) {
                    errorMessage += currentError.defaultMessage
                    errorMessage += " "
                }
            }
        }
        if (errorResponse.hasOwnProperty("message")) {
            errorMessage += errorResponse.message;
        }
        return errorMessage;
    }
}