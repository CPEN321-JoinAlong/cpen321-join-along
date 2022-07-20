const ERROR_CODES = require("./../../ErrorCodes.js")

class BanService {
    async banUser(userID, userStore) {
        let r = await userStore.deleteUser(userID);
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async banEvent(eventID, eventStore) {
        let r = await eventStore.deleteEvent(eventID);
        return { status: ERROR_CODES.SUCCESS, data: r };
    }
}

module.exports = BanService;