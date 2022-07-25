const mongoose = require("mongoose");
const { Schema } = mongoose;

const ReportSchema = new Schema({
    reporterName: String,
    reporterID: String,
    reportedName: String,
    reportedID: String,
    reason: String,
    description: String,
    isEvent: Boolean,
    isBlocked: Boolean,
});

module.exports = mongoose.model("Report", ReportSchema);
