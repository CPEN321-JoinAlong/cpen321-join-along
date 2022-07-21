const Event = require("./../../models/Event");
const mongoose = require("mongoose");
const ERROR_CODES = require("./../../ErrorCodes.js")

class EventStore {
    async findUnblockedEvents(userID, userStore) {
        if (!mongoose.isObjectIdOrHexString(userID)) 
            return new ResponseObject(ERROR_CODES.INVALID, []);
        let user = await userStore.findUserByID(userID);
        if (user) {
            let eventList = await Event.find({
                $and: [
                    { _id: { $in: user.events } },
                    { _id: { $nin: user.blockedEvents } },
                ],
            });
            if(eventList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, eventList);
            else return new ResponseObject(ERROR_CODES.NOTFOUND, eventList)
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND, []);
        }
    }

    async removeUser(eventID, userID, userStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let eventResponse = await this.findEventByID(eventID);
        let userResponse = await userStore.findUserByID(userID);
        if (eventResponse.data && userResponse.data) {
            await this.updateEvent(
                eventID,
                {
                    $pull: { participants: userID },
                    $dec: { currCapacity: 1 },
                },
                userStore
            );
            return new ResponseObject(ERROR_CODES.SUCCESS)
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND)
        }
    }

    async findEventsByName(searchEvent) {
        let foundEventList = await Event.find({
            title: { $regex: searchEvent, $options: "i" },
        });
        if(foundEventList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, foundEventList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, foundEventList)
    }

    async findEventByUser(userID) {
        if (!mongoose.isObjectIdOrHexString(userID)) 
            return new ResponseObject(ERROR_CODES.INVALID); 
        let eventList = await Event.find({
            participants: userID,
        });
        if(eventList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, eventList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, eventList)
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
        let eventList = await Event.find({});
        if(eventList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, eventList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, eventList)
    }

    async findEventByID(eventID) {
        if (!mongoose.isObjectIdOrHexString(eventID)) 
            return new ResponseObject(ERROR_CODES.INVALID);
        console.log(eventID)
        let foundEvent = await Event.findById(eventID);
        if(foundEvent) return new ResponseObject(ERROR_CODES.SUCCESS, foundEvent)
        else return new ResponseObject(ERROR_CODES.NOTFOUND)
    }

    async findEventByIDList(eventIDList) {
        if (!eventIDList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return new ResponseObject(ERROR_CODES.INVALID, []);
        let eventList = await Event.find({
            _id: {
                $in: eventIDList,
            },
        });
        if(eventList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, eventList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, eventList)
    }

    //add the event to the database and adds it into users' event list and send event object to frontend
    async createEvent(eventInfo, userStore) {
        let eventObject = await new Event(eventInfo).save();
        eventObject.participants.forEach(async (participant) => {
            if(mongoose.isObjectIdOrHexString(participant)) {
                await userStore.updateUserAccount(participant, {
                    $push: {events: eventObject._id}
                });
            }
        });
        return new ResponseObject(ERROR_CODES.SUCCESS, eventObject);
    }

    async updateEvent(eventID, eventInfo, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) 
            return new ResponseObject(ERROR_CODES.INVALID);
        let event = await Event.findByIdAndUpdate(eventID, eventInfo, {
            new: true,
        });
        if (event) {
            await userStore.addEvent(eventID, event);
            await userStore.removeEvent(eventID, event);
            return new ResponseObject(ERROR_CODES.SUCCESS, event);
        } else {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
    }

    async deleteEvent(eventID, userStore) {
        if (!mongoose.isObjectIdOrHexString(eventID)) 
            return new ResponseObject(ERROR_CODES.INVALID);
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
            return new ResponseObject(ERROR_CODES.SUCCESS);
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
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