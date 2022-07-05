const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");
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
        this.blockedEvents = userInfo.blockedEvents ? userInfo.blockedEvents : [];
        this.token = userInfo.token ? userInfo.token : null;
    }

    findFriends(User) {
        return this.friends;
    }

    //returns list of event objects related to the User
    async findPersonalEvents(eventStore) {
        return await eventStore.findEventByIDList(this.events);
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

    async findUserByProfile(userInfo) {
        return await User.find({ name: userInfo.name });
    }

    async findUserByID(userID) {
        return await User.findById(userID);
    }

    async updateUserAccount(userID, userInfo) {
        return await User.findByIdAndUpdate(userID, userInfo);
    }

    //Puts the UserAccount into the database and return the ObjectID
    async createUser(userInfo) {
        let user = await new User(userInfo).save();
        return user._id.toString();
    }

    async deleteUser(userID) {
        return await User.findByIdAndDelete(userID);
    }

    async findUserForLogin(Token) {
        return await User.find({ token: Token });
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
                { event: "null" },
                { participants: { $all: [fromUserID, toUserID] } },
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

        chatInfo.message.push({ participantId: fromUserID, text: text });
        Chat.findByIdAndUpdate(chatInfo._id, chatInfo);
    }

    async sendGroupMessage(userID, eventID, text) {
        let chatInfo = await Chat.find({ event: eventID });
        if (chatInfo == null) return;
        chatInfo.messages.push({ participantId: userID, text: text });
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

    async findEvents(eventInfo) {
        //TODO: What exactly do you mean by 'similar events'
    }

    async notifyNewGroupMessage() {
        //TODO: firebase
    }

    async findEventsByString(searchEvent) {
        //TODO: what is string
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
        //TODO
    }
}

class EventStore {
    constructor() {}

    async findEventByDetails(location, filters) {
        return await Event.find({ location: location });
        //TODO: add filters for the events
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

    async updateEvent(eventID, eventInfo) {
        return await Event.findByIdAndUpdate(eventID, eventInfo);
    }

    async createEvent(eventInfo, chatEngine) {
        let chatInfo = await chatEngine.createChat({
            name: eventInfo.name,
            interestTags: eventInfo.interestTags,
            participants: eventInfo.participants,
            messages: [],
            maxCapacity: eventInfo.maxCapacity,
            currCapacity: eventInfo.currCapacity,
            description: eventInfo.description,
        });
        eventInfo.chat = chatInfo._id;
        let eventObject = await new Event(eventInfo).save();
        chatInfo.event = eventObject._id;
        chatEngine.editChat(chatInfo);
    }

    async deleteEvent(eventID) {
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

module.exports = {
    UserAccount,
    UserStore,
    ChatDetails,
    ChatEngine,
    EventDetails,
    EventStore,
};