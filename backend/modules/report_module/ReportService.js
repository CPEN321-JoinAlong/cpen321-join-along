const Report = require("./../../models/Report");
const ERROR_CODES = require("./../../ErrorCodes.js")

class ReportService {
    async reportUser(userID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return { status: ERROR_CODES.NOTFOUND, data: null };
            reporterInfo.blockedUsers.push(userID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        let r = new Report({
            reporter,
            reason,
            reportedID: userID,
        });

        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async reportEvent(eventID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return { status: ERROR_CODES.NOTFOUND, data: null };
            reporterInfo.blockedEvents.push(eventID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        let r = new Report({
            reporter,
            reason,
            reportedID: eventID,
        });

        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async viewAllReports() {
        let r = await Report.find({});
        return { status: ERROR_CODES.SUCCESS, data: r };
    }
}

module.exports = ReportService;
