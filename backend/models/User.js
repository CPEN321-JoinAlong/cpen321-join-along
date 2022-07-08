const mongoose = require("mongoose");
const { Schema } = mongoose;

const UserSchema = new Schema({
    name: String,
    interestTags: [
        {
            type: String,
        },
    ],
    location: String,
    chats: [
        {
            type: String,
        },
    ],
    events: [
        {
            type: String,
        },
    ],
    profileImage: String,
    description: String,
    chatInvites: [
        {
            type: String,
        },
    ],
    eventInvites: [
        {
            type: String,
        },
    ],
    friendRequest: [
        {
            type: String,
        },
    ],
    friends: [
        {
            type: String,
        },
    ],
    blockedUsers: [
        {
            type: String,
        },
    ],
    blockedEvents: [
        {
            type: String,
        },
    ],
    token: String,
});

module.exports = mongoose.model("User", UserSchema);
