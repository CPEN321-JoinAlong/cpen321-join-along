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

        //new and empty fields
        this.events = userInfo.events ? userInfo.events : [];
        this.friends = userInfo.friends ? userInfo.friends : [];
        this.blockedUsers = userInfo.blockedUsers ? userInfo.blockedUsers : [];
        this.blockedEvents = userInfo.blockedEvents
            ? userInfo.blockedEvents
            : [];
        this.token = userInfo.token ? userInfo.token : null;
    }

    findFriends(User) {
        return this.friends;
    }

    //returns list of event objects related to the User
    async findAllPersonalEvents(eventStore) {
        return await eventStore.findEventByIDList(this.events);
    }

    //return list of event objects related to the User which are have the provided Tag
    async findEventsWithTag(Tag, eventStore) {
        let eventList = await this.findAllPersonalEvents(eventStore);
        return eventList.filter((event) => event.interestTags.includes(Tag));
    }

    //TODO chat finding method, and add chat the user is in as a field in UserAccount or something

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
        console.log(this);
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
        return await User.findById(userID);
    }

    async updateUserAccount(userID, userInfo) {
        return await User.findByIdAndUpdate(userID, userInfo);
    }

    async createUser(userInfo) {
        return await new User(userInfo).save();
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

    async findUserForLogin(Token) {
        return await User.find({
            token: Token,
        });
    }
}

class ChatDetails {
    constructor(chatInfo) {
        this.name = chatInfo.name;
        this.interests = chatInfo.interestTags;
        this.participants = chatInfo.participants;
        this.messages = chatInfo.messages;
        this.maxCapacity = chatInfo.maxCapacity;
        this.currCapacity = chatInfo.currCapacity;
        this.description = chatInfo.description;
    }
}

class ChatEngine {
    constructor() {}

    async findChatByID(chatID) {
        return await Chat.findById(ChatID);
    }

    //Assumes both users exist
    async sendMessage(fromUserID, toUserID, text) {
        let chatInfo = await Chat.find({
            $and: [
                {
                    event: "null",
                },
                {
                    participants: {
                        $all: [fromUserID, toUserID],
                    },
                },
            ],
        });
        if (chatInfo == null) return;

        //     return await this.createChat({
        //         name: "New Chat",
        //         interestTags: [],
        //         participants: [fromUserID, toUserID],
        //         messages: [{ participantId: fromUserID, text: text }],
        //         maxCapacity: 2,
        //         currCapacity: 2,
        //         description: "A new private chatroom",
        //     });

        chatInfo.message.push({
            participantId: fromUserID,
            text: text,
        });
        Chat.findByIdAndUpdate(chatInfo._id, chatInfo);
    }

    async sendGroupMessage(userID, eventID, text) {
        let chatInfo = await Chat.find({
            event: eventID,
        });
        if (chatInfo == null) return;
        chatInfo.messages.push({
            participantId: userID,
            text: text,
        });
        Chat.findByIdAndUpdate(chatInfo._id, chatInfo);
    }

    async createChat(chatInfo) {
        return await new Chat(chatInfo).save();
    }

    async editChat(chatInfo) {
        return await Chat.findByIdAndUpdate(chatInfo._id, chatInfo);
    }
}

class EventDetails {
    constructor(eventInfo) {
        this.name = eventInfo.name;
        this.interests = eventInfo.interestTags;
        this.participants = eventInfo.participants;
        this.startDate = eventInfo.startDate;
        this.endDate = eventInfo.endDate;
        this.isPublic = eventInfo.isPublic;
        this.maxCapacity = eventInfo.maxCapacity;
        this.currCapacity = eventInfo.currCapacity;
        this.location = eventInfo.location;
        this.eventImage = eventInfo.eventImage;
        this.description = eventInfo.description;
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

    async joinEventByID(eventID, userID, eventStore, chatEngine) {
        let eventInfo = await eventStore.findEventByID(eventID);
        if (
            eventInfo == null ||
            eventInfo.currCapacity == eventInfo.maxCapacity
        )
            return;
        let chatInfo = await chatEngine.findChatByID(eventInfo.chat);
        eventInfo.participants.push(userID);
        eventInfo.currCapacity++;

        return await eventStore.updateEvent(eventID, eventInfo);
    }

    async filterBlockedEvents(userID, userStore) {
        //TODO: should be a part of user searching for events (UserAccount), not a seperate function
    }

    async reportAndBlockEvent(eventID, reason) {
        //TODO: do we need this, coz the report/block button would call the ReportService directly
    }
}

class EventStore {
    constructor() {}

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
    async createEvent(eventInfo, chatEngine, userStore) {
        let eventObject = await new Event(eventInfo).save();
        eventObject.participants.forEach(async (particpant) => {
            let user = await userStore.findUserByID(particpant);
            if (user) {
                user.participants.push(eventObject._id);
                await userStore.updateUserAccount(particpant, user);
            }
        });
        return eventObject;
    }

    //might need to optimize this after mvp
    async updateEvent(eventID, eventInfo, userStore) {
        let event = await Event.findById(eventID);
        if (event) {
            await userStore.addEvent(eventID, eventInfo);
            await userStore.removeEvent(eventID, eventInfo);
        }
        return await Event.findByIdAndUpdate(eventID, eventInfo);
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
