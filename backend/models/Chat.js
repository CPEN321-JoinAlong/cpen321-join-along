const mongoose = require("mongoose");
const { Schema } = mongoose;

const ChatSchema = new Schema({
    name: String,
    interestTags: [
        {
            type: String,
        },
    ],
    participants: [
        {
            type: String,
        },
    ],
    messages: [
        {
            participantID: String,
            text: String,
        },
    ],
    event: String,
    maxCapacity: String,
    currCapacity: String,
    description: String,
});

module.exports = mongoose.model("Chat", ChatSchema);
