const ERROR_CODES = require("./../../ErrorCodes.js")

class UserAccount {
    constructor(userInfo) {
        //fields from userInfo
        this.name = userInfo.name;
        this.interests = userInfo.interests;
        this.location = userInfo.location;
        this.description = userInfo.description;
        this.profilePicture = userInfo.profilePicture;

        //fields which will be created automatically
        this.eventInvites = userInfo.eventInvites ? userInfo.eventInvites : [];
        this.chatInvites = userInfo.chatInvites ? userInfo.chatInvites : [];
        this.friendRequest = userInfo.friendRequest
            ? userInfo.friendRequest
            : [];
        this.events = userInfo.events ? userInfo.events : [];
        this.chats = userInfo.chats ? userInfo.chats : [];
        this.friends = userInfo.friends ? userInfo.friends : [];
        this.blockedUsers = userInfo.blockedUsers ? userInfo.blockedUsers : [];
        this.blockedEvents = userInfo.blockedEvents
            ? userInfo.blockedEvents
            : [];
        this.token = userInfo.token ? userInfo.token : null;
    }
}

module.exports = UserAccount;