const Report = require("./../../models/Report");
const ERROR_CODES = require("./../../ErrorCodes.js")

class ReportService {
    async report(name, reporterID, reportedID, reason, description, isEvent, isBlocked, userStore) {
        if (!mongoose.isObjectIdOrHexString(reporterID) || !mongoose.isObjectIdOrHexString(reportedID)) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }

        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporterID);
            if (reporterInfo == null) return new ResponseObject(ERROR_CODES.NOTFOUND);
            if(isEvent)
                reporterInfo.blockedEvents.push(reportedID);
            else
                reporterInfo.blockedUsers.push(reportedID)
            userStore.updateUserAccount(reporterID, reporterInfo);
        }

        let report = new Report({
            name,
            reporterID,
            reportedID,
            reason,
            description,
            isEvent,
            isBlocked
        });

        return new ResponseObject(ERROR_CODES.SUCCESS, report);
    }

    async viewAllReports() {
        let reports = await Report.find({});
        return new ResponseObject(ERROR_CODES.SUCCESS, reports);
    }
}

module.exports = ReportService;
