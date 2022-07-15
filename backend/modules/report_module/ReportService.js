const Report = require("./../../models/Report");
const mongoose = require("mongoose");
const CONFLICT = 409;
const NOTFOUND = 404;
const SUCCESS = 200;
const INVALID = 422;

class ReportService {
    constructor() {}

    async reportUser(userID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return;
            reporterInfo.blockedUsers.push(userID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        return new Report({
            reporter: reporter,
            reason: reason,
            reportedID: userID,
        });
    }

    async reportEvent(eventID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return;
            reporterInfo.blockedEvents.push(eventID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        return new Report({
            reporter: reporter,
            reason: reason,
            reportedID: eventID,
        });
    }

    async viewAllReports() {
        return await Report.find({});
    }
}

module.exports = ReportService;
