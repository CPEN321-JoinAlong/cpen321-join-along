const mongoose = require("mongoose");
const { Schema } = mongoose;

const ReportSchema = new Schema({
    reporter: String,
    reason: String,
    reportedID: String,
});

module.exports = mongoose.model("Report", ReportSchema);
