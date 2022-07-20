const Event = require("./../../models/Event");
const mongoose = require("mongoose");
const ERROR_CODES = require("./../../ErrorCodes.js")

class EventStore {
    async findUnblockedEvents(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) return { status: ERROR_CODES.INVALID, data: [] };
        let user = await userStore.findUserByID(userID);
        if (user) {
            let r = await Event.find({
                $and: [
                    { _id: { $in: user.events } },
                    { _id: { $nin: user.blockedEvents } },
                ],
            });
            return { status: ERROR_CODES.SUCCESS, data: r };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: [] };
        }
    }

    async removeUser(eventID, userID, userStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return { status: ERROR_CODES.INVALID, data: null };
        }
        let event = await this.findEventByID(eventID);
        let user = await userStore.findUserByID(userID);
        if (event && user) {
            let r = await this.updateEvent(
                eventID,
                {
                    $pull: { participants: userID },
                    $dec: { currCapacity: 1 },
                },
                userStore
            );
            return { status: ERROR_CODES.SUCCESS, data: r };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: null };
        }
    }

    async findEventsByName(searchEvent) {
        let r = await Event.find({
            title: { $regex: searchEvent, $options: "i" },
        });
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async findEventByUser(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) return { status: ERROR_CODES.INVALID, data: [] }; 
        let user = await userStore.findUserByID(userID);
        if (user) {
            let r = await Event.find({
                participants: userID,
            });
            return { status: ERROR_CODES.SUCCESS, data: r };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: [] }; 
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
        let r = await Event.find({});
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async findEventByID(eventID) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return { status: ERROR_CODES.INVALID, data: null };
        let r = await Event.findById(eventID);
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async findEventByIDList(eventIDList) {
        if (!eventIDList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return { status: ERROR_CODES.INVALID, data: [] };
        let r = await Event.find({
            _id: {
                $in: eventIDList,
            },
        });
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    //add the event to the database and adds it into users' event list and send event object to frontend
    async createEvent(eventInfo, userStore) {
        let eventObject = await new Event(eventInfo).save();
        eventObject.participants.forEach(async (participant) => {
            // console.log(participant);
            let user = await userStore.findUserByID(participant);
            if (user) {
                user.events.push(eventObject._id);
                // console.log("IN CREATE EVENT");
                // console.log(eventObject._id);
                // console.log(user.events);
                await userStore.updateUserAccount(participant, user);
            }
        });
        return { status: ERROR_CODES.SUCCESS, data: eventObject };
    }

    async updateEvent(eventID, eventInfo, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return { status: ERROR_CODES.INVALID, data: null };
        let event = await Event.findByIdAndUpdate(eventID, eventInfo, {
            new: true,
        });
        if (event) {
            await userStore.addEvent(eventID, event);
            await userStore.removeEvent(eventID, event);
            return { status: ERROR_CODES.SUCCESS, data: null };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: null };
        }
    }

    async deleteEvent(eventID, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) return { status: ERROR_CODES.INVALID, data: null };
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
            return { status: ERROR_CODES.SUCCESS, data: null };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: null };
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

module.exports = EventStore;