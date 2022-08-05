class UserAccount {
    constructor(userInfo) {
        //fields from userInfo
        this.name = userInfo.name;
        this.isAdmin = userInfo.isAdmin != null ? userInfo.isAdmin : false;
        this.interests = userInfo.interests;
        this.location = userInfo.location;
        this.description = userInfo.description;
        this.profilePicture = userInfo.profilePicture;

        //fields which will be created automatically
        this.coordinates = userInfo.coordinates ? userInfo.coordinates : "0,0"
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
        this.token = userInfo.token;
    }
}

module.exports = UserAccount;
