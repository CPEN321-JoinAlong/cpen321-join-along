const mongoose = require("mongoose");
const { Schema } = mongoose;

const ChatSchema = new Schema({
    title: String,
    tags: [
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
    numberOfPeople: Number,
    currCapacity: Number,
    description: String,
});

module.exports = mongoose.model("Chat", ChatSchema);
