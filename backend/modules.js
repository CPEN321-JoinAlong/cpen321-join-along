const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");
const Report = require("./models/Report");

class UserAccount {
    constructor(userInfo) {
        //fields from userInfo
        this.name = userInfo.name;
        this.interests = userInfo.interests;
        this.location = userInfo.location;
        this.description = userInfo.description;
        this.profileImage = userInfo.profilePicture;

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

    findFriends() {
        return this.friends;
    }

    //return list of event objects related to the User which are have the provided Tag
    async findEventsWithTag(Tag, eventStore) {
        let eventList = await this.findAllPersonalEvents(eventStore);
        return eventList.filter((event) => event.interestTags.includes(Tag));
    }

    //returns list of unblocked events
    async filterBlockedEvents(userID, userStore, eventStore) {
        return await eventStore.findUnblockedEvents(userID, userStore);
    }

    async sendMessage(fromUserID, toUserID, userStore, chatEngine, text) {
        let userInfo = await userStore.findUserByID(toUserID);
        if (userInfo == null) return;
        await chatEngine.sendMessage(fromUserID, toUserID, text);
    }

    async sendGroupMessage(userID, eventID, eventStore, chatEngine, text) {
        let eventInfo = await eventStore.findEventById(eventID);
        if (eventID == null) return;
        await chatEngine.sendGroupMessage(userID, eventID, text);
    }

    async notifyNewMessage(otherUser) {
        //TODO: firebase
    }

    async createUserAccount(userStore) {
        // console.log(this);
        return await userStore.createUser(this);
    }

    async findEvents(EventDetails) {
        //TODO
    }
}

class UserStore {
    constructor() {}

    async findUserByEvent(eventID) {
        return await User.find({
            events: { $in: [eventID] },
        });
    }

    async findUserByID(userID) {
        console.log(userID);
        return await User.findById(userID);
    }

    async updateUserAccount(userID, userInfo) {
        return await User.findByIdAndUpdate(userID, userInfo);
    }

    async createUser(userInfo) {
        return await new User(userInfo).save();
    }

    async acceptChatInvite(userID, chatID, chatEngine) {
        let chat = await chatEngine.findChatByID(chatID);
        let user = await this.findUserByID(userID);
        if (chat && user) {
            if (chat.currCapacity < chat.numberOfPeople) {
                await User.findByIdAndUpdate(userID, {
                    $push: { chat: chatID },
                    $pull: { chatInvites: chatID },
                });
                console.log();
                await chatEngine.editChat(
                    chatID,
                    {
                        $push: { participants: userID },
                        $inc: { currCapacity: 1 },
                    },
                    this
                );
            }
        }
    }

    async rejectChatInvite(userID, chatID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { chatInvites: chatID },
        });
    }

    async acceptEventInvite(userID, eventID, eventStore, chatEngine) {
        let event = await eventStore.findEventByID(eventID);
        let user = await this.findUserByID(userID);
        let chat = await chatEngine.findChatByID(event.chat);
        if (event && user && chat) {
            if (event.currCapacity < event.numberOfPeople) {
                await User.findByIdAndUpdate(userID, {
                    $push: { events: eventID },
                    $pull: { eventInvites: eventID },
                });

                await eventStore.updateEvent(
                    eventID,
                    {
                        $push: { participants: userID },
                        $inc: { currCapacity: 1 },
                    },
                    this
                );
                await this.acceptChatInvite(userID, chat._id, chatEngine);
            }
        }
    }

    async rejectEventInvite(userID, eventID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { eventInvites: eventID },
        });
    }

    async acceptFriendRequest(userID, otherUserID) {
        let user = await this.findUserByID(userID);
        let otherUser = await this.findUserByID(otherUserID);
        if (user && otherUser) {
            await User.findByIdAndUpdate(userID, {
                $push: { friends: otherUserID },
                $pull: { friendRequests: otherUserID },
            });
            await User.findByIdAndUpdate(otherUserID, {
                $push: { friends: userID },
            });
        }
    }

    async rejectFriendRequest(userID, otherUserID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { friendRequest: otherUserID },
        });
    }

    async findUblockedUsers(userID) {
        let user = await this.findUserByID(userID);
        if (user) {
            return await User.find({
                _id: { $nin: user.blockedUsers },
            });
        }
        return null;
    }

    async addChat(chatID, chatInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $in: chatInfo.participants } },
                    { chats: { $ne: chatID } },
                ],
            },
            { $push: { chats: chatID } }
        );
    }

    async removeChat(chatID, chatInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $nin: chatInfo.participants } },
                    { chats: chatID },
                ],
            },
            { $pull: { chats: chatID } }
        );
    }

    async addEvent(eventID, eventInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $in: eventInfo.participants } },
                    { events: { $ne: eventID } },
                ],
            },
            { $push: { events: eventID } }
        );
    }

    async removeEvent(eventID, eventInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $nin: eventInfo.participants } },
                    { events: eventID },
                ],
            },
            { $pull: { events: eventID } }
        );
    }

    async deleteUser(userID) {
        return await User.findByIdAndDelete(userID);
    }

    async removeFriend(userID, otherUserID) {
        await User.updateMany(
            { _id: { $in: [userID, otherUserID] } },
            { $pull: { friends: { $in: [userID, otherUserID] } } }
        );
    }

    async leaveEvent(userID, eventID, eventStore) {
        await User.findByIdAndUpdate(userID, {
            $pull: { $events: eventID },
        });
        await eventStore.removeUser(userID, this);
    }

    async leaveChat(userID, chatID, chatEngine) {
        await User.findByIdAndUpdate(userID, {
            $pull: { $chat: chatID },
        });
        await chatEngine.removeUser(userID, this);
    }

    async findUserForLogin(Token) {
        return await User.findOne({
            token: Token,
        });
    }
}

class ChatDetails {
    constructor(chatInfo) {
        this.title = chatInfo.title;
        this.tags = chatInfo.tags;
        this.numberOfPeople = chatInfo.numberOfPeople;
        this.description = chatInfo.description;
        this.currCapacity = chatInfo.currCapacity ? chatInfo.currCapacity : 0;
        this.participants = chatInfo.participants ? chatInfo.participants : [];

        this.messages = chatInfo.messages ? chatInfo.message : [];
        this.event = chatInfo.event ? chatInfo.event : null;
    }
}

class ChatEngine {
    constructor() {}

    async findChatByID(chatID) {
        return await Chat.findById(chatID);
    }

    async findChatByUser(userID) {
        return await Chat.find({
            participants: userID,
        });
    }

    //Assumes both users exist
    async sendMessage(fromUserID, toUserID, text, name, date) {
        await Chat.findOneAndUpdate(
            {
                $and: [
                    {
                        event: null,
                    },
                    {
                        participants: {
                            $all: [fromUserID, toUserID],
                        },
                    },
                ],
            },
            {
                $push: {
                    messages: {
                        participantID: fromUserID,
						participantName: name,
						timeStamp: date,
                        text: text,
                    },
                },
            }
        );
    }

    async sendGroupMessage(userID, eventID, text, name, date) {
        await Chat.findOneAndUpdate(
            {
                event: eventID,
            },
            {
                $push: {
                    messages: {
                        participantID: userID,
						participantName: name,
						timeStamp: date,
                        text: text,
                    },
                },
            }
        );
    }

    async createChat(chatInfo, userStore) {
        let chatObject = await new Chat(chatInfo).save();
        chatObject.participants.forEach(async (participant) => {
            let user = await userStore.findUserByID(participant);
            if (user) {
                user.chats.push(chatObject._id);
                await userStore.updateUserAccount(participant, user);
            }
        });
        return chatObject;
    }

    async removeUser(chatID, userID, userStore) {
        await this.editChat(
            chatID,
            {
                $pull: { participants: userID },
                $dec: { currCapacity: 1 },
            },
            userStore
        );
    }

    async editChat(chatID, chatInfo, userStore) {
        let chat = await Chat.findByIdAndUpdate(chatID, chatInfo, {
            new: true,
        });
        if (chat) {
            await userStore.addChat(chatID, chat);
            await userStore.removeChat(chatID, chat);
        }
        return await Chat.findById(chatID);
    }
}

class EventDetails {
    constructor(eventInfo) {
        this.title = eventInfo.title;
        this.eventOwnerID = eventInfo.eventOwnerID;
        this.tags = eventInfo.tags;
        this.beginningDate = eventInfo.beginningDate;
        this.endDate = eventInfo.endDate;
        this.publicVisibility = eventInfo.publicVisibility;
        this.numberOfPeople = eventInfo.numberOfPeople;
        this.location = eventInfo.location;
        this.description = eventInfo.description;

        this.participants = eventInfo.participants
            ? eventInfo.participants
            : [this.eventOwnerID];
        this.currCapacity = eventInfo.currCapacity ? eventInfo.currCapacity : 1;
        this.eventImage = eventInfo.eventImage;
        this.chat = eventInfo.chat ? eventInfo.chat : null;
    }

    async findEvents(eventInfo, eventStore) {
        return await eventStore.findEventByDetails(eventInfo);
    }

    async notifyNewGroupMessage() {
        //TODO: firebase
    }

    async findEventsByName(searchEvent) {
        return await Event.find({
            name: searchEvent,
        });
    }
}

class EventStore {
    constructor() {}

    async findUnblockedEvents(userID, userStore) {
        let user = await userStore.findUserByID(userID);
        if (user) {
            return await Event.find({
                $and: [
                    { _id: { $in: user.events } },
                    { _id: { $nin: user.blockedEvents } },
                ],
            });
        }
        return null;
    }

    async removeUser(eventID, userID, userStore) {
        await this.updateEvent(eventID, {
            $pull: { participants: userID },
            $dec: { currCapacity: 1 },
        }, userStore);
    }

    async findEventByUser(userID) {
        return await Event.find({
            participants: userID,
        });
    }

    async findEventByDetails(filters) {
        return await Event.find({
            $or: [
                {
                    name: filters.name,
                    eventOwnerID: filters.eventOwnerID,
                    interestTags: {
                        $in: filters.interestTags,
                    },
                    location: filters.location,
                    description: filters.description,
                },
            ],
        });
    }

    async findEventByID(eventID) {
        return await Event.findById(eventID);
    }

    async findEventByIDList(eventIDList) {
        return await Event.find({
            _id: {
                $in: eventIDList,
            },
        });
    }

    //add the event to the database and adds it into users' event list and send event object to frontend
    async createEvent(eventInfo, userStore) {
        let eventObject = await new Event(eventInfo).save();
        eventObject.participants.forEach(async (participant) => {
            console.log(participant);
            let user = await userStore.findUserByID(participant);
            if (user) {
                user.events.push(eventObject._id);
                await userStore.updateUserAccount(participant, user);
            }
        });
        return eventObject;
    }

    async updateEvent(eventID, eventInfo, userStore) {
        console.log(eventInfo);
        let event = await Event.findByIdAndUpdate(eventID, eventInfo, {
            new: true,
        });
        if (event) {
            await userStore.addEvent(eventID, event);
            await userStore.removeEvent(eventID, event);
        }
        return await Event.findById(eventID);
    }

    async deleteEvent(eventID) {
        let event = await Event.findById(eventID);
        if (event) {
            event.participants.forEach(async(Par));
        }
        return await Event.findByIdAndDelete(eventID);
    }

    async addUserToEvent(userID, eventID) {
        let eventInfo = await Event.findById(eventID);
        if (eventInfo == null) return;
        eventInfo.participants.push(userID);
        Event.findByIdAndUpdate(eventID, eventInfo);
    }

    async findEventInterest(userID) {
        let user = await User.findById(userID);
        if (user == null) return;
        return user.events;
    }
}

class ReportService {
    constructor() {}

    async reportUser(userID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return;
            reporterInfo.blockedUsers.push(userID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        return new Report({
            reporter: reporter,
            reason: reason,
            reportedID: userID,
        });
    }

    async reportEvent(eventID, reporter, reason, isBlocked, userStore) {
        if (isBlocked) {
            let reporterInfo = userStore.findUserByID(reporter);
            if (reporterInfo == null) return;
            reporterInfo.blockedEvents.push(eventID);
            userStore.updateUserAccount(reporter, reporterInfo);
        }

        return new Report({
            reporter: reporter,
            reason: reason,
            reportedID: eventID,
        });
    }

    async viewAllReports() {
        return await Report.find({});
    }
}

class BanService {
    constructor() {}

    async banUser(userID, userStore) {
        return await userStore.deleteUser(userID);
    }

    async banEvent(eventID, eventStore) {
        return await eventStore.deleteEvent(eventID);
    }
}

function removeItemOnce(arr, value) {
    var index = arr.indexOf(value);
    if (index > -1) {
        arr.splice(index, 1);
    }
    return arr;
}

module.exports = {
    UserAccount,
    UserStore,
    ChatDetails,
    ChatEngine,
    EventDetails,
    EventStore,
    ReportService,
    BanService,
};
