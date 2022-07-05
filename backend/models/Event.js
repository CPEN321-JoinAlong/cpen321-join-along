const mongoose = require("mongoose");
const { Schema } = mongoose;

const EventSchema = new Schema({
    name: String,
    interestTags: [
        {
            type: String
        }
    ],
    participants: [
        {
            type: String
        }
    ],
    startDate: Date,
    endDate: Date,
    isPublic: Boolean,
    maxCapacity: String,
    currCapacity: String,
    location: String,
    eventImage: String,
    description: String,
    chat: String
})

module.exports = mongoose.model("Event", EventSchema);