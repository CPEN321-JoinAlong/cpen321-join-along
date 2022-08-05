const Report = require("./../../models/Report");
const ERROR_CODES = require("./../../ErrorCodes.js");
const mongoose = require("mongoose");
const ResponseObject = require("./../../ResponseObject");

class ReportService {
    async report(
        reporterID,
        reportedID,
        reason,
        description,
        isEvent,
        isBlocked,
        userStore,
        eventStore
    ) {
        if (
            !mongoose.isObjectIdOrHexString(reporterID) ||
            !mongoose.isObjectIdOrHexString(reportedID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }

        let reporterInfo = await userStore.findUserByID(reporterID);
        if (reporterInfo.data == null)
            return new ResponseObject(ERROR_CODES.NOTFOUND);

        let reportedInfo = isEvent
            ? await eventStore.findEventByID(reportedID)
            : await userStore.findUserByID(reportedID);

        if (isBlocked) {
            if (isEvent) reporterInfo.data.blockedEvents.push(reportedID);
            else reporterInfo.data.blockedUsers.push(reportedID);
            userStore.updateUserAccount(reporterID, reporterInfo);
        }

        let reporterName = reporterInfo.data.name;
        let reportedName = isEvent
            ? reportedInfo.data.title
            : reportedInfo.data.name;

        let report = await new Report({
            reporterName,
            reporterID,
            reportedName,
            reportedID,
            reason,
            description,
            isEvent,
            isBlocked,
        }).save();

        return new ResponseObject(ERROR_CODES.SUCCESS, report);
    }

    async viewAllReports() {
        let reports = await Report.find({});
        return new ResponseObject(ERROR_CODES.SUCCESS, reports);
    }
}

module.exports = ReportService;
