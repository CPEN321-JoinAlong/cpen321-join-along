const ERROR_CODES = require("./../../ErrorCodes.js");
const mongoose = require("mongoose");

const ResponseObject = require("./../../ResponseObject");
class BanService {
    async banUser(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let response = await userStore.deleteUser(userID);
        return response;
    }

    async banEvent(eventID, eventStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let response = await eventStore.deleteEvent(eventID);
        return response;
    }
}

module.exports = BanService;
