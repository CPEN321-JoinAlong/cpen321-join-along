const mongoose = require("mongoose");
const { Schema } = mongoose;

const EventSchema = new Schema({
    title: String,
    eventOwnerID: String,
    eventOwnerName: String,
    tags: [
        {
            type: String
        }
    ],
    participants: [
        {
            type: String
        }
    ],
    beginningDate: Date,
    endDate: Date,
    publicVisibility: Boolean,
    numberOfPeople: Number,
    currCapacity: Number,
    location: String,
    eventImage: String,
    description: String,
    chat: String
})

module.exports = mongoose.model("Event", EventSchema);