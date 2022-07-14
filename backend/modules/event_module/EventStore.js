const Event = require("./../../models/Event");
const mongoose = require("mongoose");
const CONFLICT = 409;
const NOTFOUND = 404;
const SUCCESS = 200;
const INVALID = 422;

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
}

class EventStore {
    constructor() {}

    async findUnblockedEvents(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) return [];
        let user = await userStore.findUserByID(userID);
        if (user) {
            return await Event.find({
                $and: [
                    { _id: { $in: user.events } },
                    { _id: { $nin: user.blockedEvents } },
                ],
            });
        } else {
            return [];
        }
    }

    async removeUser(eventID, userID, userStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return INVALID;
        }
        let event = await this.findEventByID(eventID);
        let user = await userStore.findUserByID(userID);
        if (event && user) {
            await this.updateEvent(
                eventID,
                {
                    $pull: { participants: userID },
                    $dec: { currCapacity: 1 },
                },
                userStore
            );
            return SUCCESS;
        } else {
            return NOTFOUND;
        }
    }

    async findEventsByName(searchEvent) {
        return await Event.find({
            title: { $regex: searchEvent, $options: "i" },
        });
    }

    async findEventByUser(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) return [];
        let user = await userStore.findUserByID(userID);
        if (user) {
            return await Event.find({
                participants: userID,
            });
        } else {
            return [];
        }
    }

    // async findEventByDetails(filters) {
    //     return await Event.find({
    //         $or: [
    //             {
    //                 title: filters.title,
    //                 eventOwnerID: filters.eventOwnerID,
    //                 tags: {
    //                     $in: filters.tags,
    //                 },
    //                 location: filters.location,
    //                 description: filters.description,
    //             },
    //         ],
    //     });
    // }

    async findAllEvents() {
        return await Event.find({});
    }

    async findEventByID(eventID) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return null;
        return await Event.findById(eventID);
    }

    async findEventByIDList(eventIDList) {
        if (!eventIDList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return [];
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
                console.log("IN CREATE EVENT");
                console.log(eventObject._id);
                console.log(user.events);
                await userStore.updateUserAccount(participant, user);
            }
        });
        return eventObject;
    }

    async updateEvent(eventID, eventInfo, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return INVALID;
        let event = await Event.findByIdAndUpdate(eventID, eventInfo, {
            new: true,
        });
        if (event) {
            await userStore.addEvent(eventID, event);
            await userStore.removeEvent(eventID, event);
            return SUCCESS;
        } else {
            return NOTFOUND;
        }
    }

    async deleteEvent(eventID, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return INVALID;
        let event = await Event.findById(eventID);
        if (event) {
            let userList = event.participants.filter((id) =>
                mongoose.isObjectIdOrHexString(id)
            );
            userList.forEach(async (userID) => {
                await userStore.updateUserAccount(userID, {
                    $pull: { events: eventID },
                });
            });
            await Event.findByIdAndDelete(eventID);
            return SUCCESS;
        } else {
            return NOTFOUND;
        }
    }

    // async addUserToEvent(userID, eventID) {
    //     let eventInfo = await Event.findById(eventID);
    //     if (eventInfo == null) return;
    //     eventInfo.participants.push(userID);
    //     Event.findByIdAndUpdate(eventID, eventInfo);
    // }

    // async findEventInterest(userID) {
    //     let user = await User.findById(userID);
    //     if (user == null) return;
    //     return user.events;
    // }
}

function titleCase(str) {
    var splitStr = str.toLowerCase().split(" ");
    for (var i = 0; i < splitStr.length; i++) {
        splitStr[i] =
            splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
    }
    return splitStr.join(" ");
}

module.exports = EventStore;