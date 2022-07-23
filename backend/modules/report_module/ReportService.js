const Report = require("./../../models/Report");
const ERROR_CODES = require("./../../ErrorCodes.js")
const mongoose = require("mongoose")
const ResponseObject = require("./../../ResponseObject")

class ReportService {
    async report(reporterID, reportedID, reason, description, isEvent, isBlocked, userStore) {
        if (!mongoose.isObjectIdOrHexString(reporterID) || !mongoose.isObjectIdOrHexString(reportedID)) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }

        let reporterInfo = await userStore.findUserByID(reporterID);
        if (reporterInfo.data == null) return new ResponseObject(ERROR_CODES.NOTFOUND);
        
	if (isBlocked) {
            if(isEvent)
                reporterInfo.blockedEvents.push(reportedID);
            else
                reporterInfo.blockedUsers.push(reportedID)
            userStore.updateUserAccount(reporterID, reporterInfo);
        }
	let name = reporterInfo.data.name

        let report = await new Report({
            name,
            reporterID,
            reportedID,
            reason,
            description,
            isEvent,
            isBlocked
        }).save();

        return new ResponseObject(ERROR_CODES.SUCCESS, report);
    }

    async viewAllReports() {
        let reports = await Report.find({});
        return new ResponseObject(ERROR_CODES.SUCCESS, reports);
    }
}

module.exports = ReportService;
