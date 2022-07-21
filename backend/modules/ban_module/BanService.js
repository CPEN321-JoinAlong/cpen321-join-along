const ERROR_CODES = require("./../../ErrorCodes.js")

class BanService {
    async banUser(userID, userStore) {
        let response = await userStore.deleteUser(userID)
        return new ResponseObject(ERROR_CODES.SUCCESS, response)
    }

    async banEvent(eventID, eventStore) {
        let response = await eventStore.deleteEvent(eventID)
        return new ResponseObject(ERROR_CODES.SUCCESS, response)
    }
}

module.exports = BanService