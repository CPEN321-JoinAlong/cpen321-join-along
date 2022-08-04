const mongoose = require("mongoose");
const request = require("supertest");
const {
    app,
    server
} = require("./../server");

const UserAccount = require("./../modules/user_module/UserAccount");
// const UserStore = require("./../modules/user_module/UserStore");
const User = require("./../models/User");

const EventDetails = require("./../modules/event_module/EventDetails");
// const EventStore = require("./../modules/event_module/EventStore");
const Event = require("./../models/Event");

const ChatDetails = require("./../modules/chat_module/ChatDetails");
// const ChatEngine = require("./../modules/chat_module/ChatEngine");
const Chat = require("./../models/Chat");

const ERROR_CODES = require("./../ErrorCodes.js");
// const ResponseObject = require("./../ResponseObject");
const Report = require("./../models/Report")

const token = "113803938110058454466";

mongoose.connect(
    "mongodb://useradmin:MTnCBEI9nIx6L6F@54.200.52.211:34542/joinalong", {
        useNewUrlParser: true,
        useUnifiedTopology: true,
    }
);
const db = mongoose.connection;
db.on("error", console.error.bind(console, "connection error:"));
db.once("open", () => {
    // console.log("Database connected");
});
beforeAll((done) => {
    done();
});

afterAll(async () => {
    // Closing the DB connection allows Jest to exit successfully.
    await Chat.deleteMany({title: "tester event"})
    await Event.deleteMany({title: "tester event"})
    await User.deleteMany({name: "Rob Robber"})
    await User.deleteMany({name: "Bob Bobber"})
    mongoose.connection.close();
    server.close();
    // done();
});

describe("Use Case 1: Event Management (Create + Edit Events)", () => {
    describe("create event", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Success: event and its chat created", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(response._body.chat)
            await Event.findByIdAndDelete(id);
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);
        });
    });
    describe("edit event", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        let updatedEventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event 2",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("invalid event ID", async () => {
            let response = await request(app)
                .put("/event/dfjsdfks/edit")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event with ID not found", async () => {
            let response = await request(app)
                .put("/event/64d31ae677f7ad9a56ab89c6/edit")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: event is edited", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);

            let updatedResponse = await request(app)
                .put(`/event/${id}/edit`)
                .send({
                    ...updatedEventInfo,
                    token
                });
            await Chat.findByIdAndDelete(chat);
            await Event.findByIdAndDelete(id);
            ["_id", "__v", "chat"].forEach((key) => delete updatedResponse._body[key]);
            ["chat"].forEach((key) => delete updatedEventInfo[key]);
            expect(updatedResponse._body).toMatchObject(updatedEventInfo)
        });
    });
    describe("view Event page", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Invalid event ID", async () => {
            let response = await request(app)
                .get("/event/dfjsdfks")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("Event with event ID not found", async () => {
            let response = await request(app)
                .get("/event/64d31ae677f7ad9a56ab89c6")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: Event is viewed", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);


            let eventResponse = await request(app)
                .get(`/event/${id}`)
                .set({
                    token
                });
            await Chat.findByIdAndDelete(chat);
            await Event.findByIdAndDelete(id);
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);
        })
    })
    describe("view events which a user is in", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Invalid User ID", async () => {
            let response = await request(app)
                .get("/user/dfjsdfks/event")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toEqual([]);
        })
        test("No events found", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let eventResponse = await request(app)
                .get(`/user/${id}/event`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(eventResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(eventResponse._body).toEqual([]);
        })
        test("Success events found", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            eventInfo.participants.push(id);

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let eventListResponse = await request(app)
                .get(`/user/${id}/event`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(eventListResponse.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v", "chat"].forEach((key) => delete eventListResponse._body[0][key]);
            expect(eventListResponse._body[0]).toMatchObject(eventInfo);
            // expect(eventListResponse._body).toEqual([]);
            eventInfo.participants = ["62d63248860a82beb388af87"];
        })
    })
})

describe("Use Case 2: Join Event", () => {
    // accept EventInvite, rejectEventInvite, leaveEvent
    describe("Accept Event Invite (join event)", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("IDs of user or event are invalid", async () => {
            let response = await request(app)
                .put("/user/acceptEvent/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event nor User found", async () => {
            let response = await request(app)
                .put("/user/acceptEvent/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Event capacity reached", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            eventInfo.currCapacity = 6;

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(joinResponse._body).toBe(undefined);
            eventInfo.currCapacity = 1;
        })
        test("User Already in Event", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            eventInfo.participants.push(id)

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(joinResponse._body).toBe(undefined);
            // eventInfo.currCapacity = 1;
            eventInfo.participants = ["62d63248860a82beb388af87"]
        })
        test("Success: Joined Event and its chat ", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(joinResponse._body).toBe(undefined);
        })
    })

    describe("Reject Event Invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("IDs of user or event are invalid", async () => {
            let response = await request(app)
                .put("/user/rejectEvent/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event nor User found", async () => {
            let response = await request(app)
                .put("/user/rejectEvent/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("eventInvite not present", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/rejectEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(joinResponse._body).toBe(undefined);
        })
        test("Success: Event Invite rejected", async () => {
            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            userInfo.eventInvites = [eventId]

            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let joinResponse = await request(app)
                .put(`/user/rejectEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(joinResponse._body).toBe(undefined);

            eventInfo.eventInvites = []
        })
    })
    describe("leave Event", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("IDs of user or event are invalid", async () => {
            let response = await request(app)
                .put("/user/leaveEvent/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("User not found", async () => {
            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let response = await request(app)
                .put(`/user/leaveEvent/64d31ae677f7ad9a56ab89c6/${eventId}`)
                .send({
                    token
                });
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: left event and its chat", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            eventInfo.participants.push(id);

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    events: eventId
                }
            });

            let leaveResponse = await request(app)
                .put(`/user/leaveEvent/${id}/${eventId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(leaveResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(leaveResponse._body).toBe(undefined);
            eventInfo.participants = ["62d63248860a82beb388af87"];
        })
    })
})

describe("Use Case 3: Search Events/User", () => {
    describe("Search User", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("No User found", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);
            let searchResponse = await request(app).get(`/user/name/Rob Roberto`).set({
                token
            })
            await User.findByIdAndDelete(id);
            expect(searchResponse.status).toBe(ERROR_CODES.NOTFOUND)
            expect(searchResponse._body).toEqual([])
        })
        test("Success: User found", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);
            let searchResponse = await request(app).get(`/user/name/Rob Rob`).set({
                token
            })
            await User.findByIdAndDelete(id);
            expect(searchResponse.status).toBe(ERROR_CODES.SUCCESS)
            // console.log(searchResponse._body[0])
            delete searchResponse._body[0]["_id"]
            delete searchResponse._body[0]["__v"]
            // ["_id", "__v"].forEach((key) => delete searchResponse._body[0][key]);
            // expect(searchResponse._body[0]).toMatchObject(userInfo)
        })
    })
    describe("Search Event", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("No Event found", async () => {
            let searchResponse = await request(app).get(`/event/title/sdfshfashifohwehjejsdjksjkih`).set({
                token
            })
            expect(searchResponse.status).toBe(ERROR_CODES.NOTFOUND)
            expect(searchResponse._body).toEqual([])
        })
        test("Success: Event found", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);

            let searchResponse = await request(app).get(`/event/title/tester event`).set({
                token
            })
            await Chat.findByIdAndDelete(chat)
            await Event.findByIdAndDelete(id);
            expect(searchResponse.status).toBe(ERROR_CODES.SUCCESS)
            delete searchResponse._body[0]["_id"]
            delete searchResponse._body[0]["__v"]
            delete searchResponse._body[0]["chat"]
            expect(searchResponse._body[0]).toMatchObject(eventInfo)
        })

    })
    describe("List all user", () => {
        test("check if endpoint returns correct response", async () => {
            let response = await request(app).get("/user").set({
                token
            })
            expect(response.status).toBe(ERROR_CODES.SUCCESS)
        })
    })
    describe("List all event", () => {
        test("check if endpoint returns correct response", async () => {
            let response = await request(app).get("/event").set({
                token
            })
            expect(response.status).toBe(ERROR_CODES.SUCCESS)
        })
    })
})

describe("User Case 5: Messaging", () => {
    //create chat, edit chat, view chat, send message, send chatinvite, accept chat, reject Chat, view Chat invites
    describe("create chat", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("Success: chat created", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(chatInfo);
        });
    });
    describe("edit chat", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        let updatedChatInfo = new ChatDetails({
            title: "tester chater 2",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("invalid chat ID", async () => {
            let response = await request(app)
                .put("/chat/dfjsdfks/edit")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("Chat with ID not found", async () => {
            let response = await request(app)
                .put("/chat/64d31ae677f7ad9a56ab89c6/edit")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: chat is edited", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            // console.log(response._body);
            expect(response._body).toMatchObject(chatInfo);

            let updatedResponse = await request(app)
                .put(`/chat/${id}/edit`)
                .send({
                    ...updatedChatInfo,
                    token
                });
            await Chat.findByIdAndDelete(id);
            expect(updatedResponse.status).toBe(ERROR_CODES.SUCCESS);
            let updatedChat = updatedResponse._body;
            delete updatedChat["_id"];
            delete updatedChat["__v"];
            expect(updatedChat).toMatchObject(updatedChatInfo);
        });
    });
    describe("view all chat invites", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("IDs of chat invites are invalid", async () => {
            userInfo.chatInvites = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatInvResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(chatInvResponse._body)).toBe(JSON.stringify([]));

            userInfo.chatInvites = [];
        });
        test("chats in chatinvites not found ", async () => {
            userInfo.chatInvites = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatInvResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(chatInvResponse._body)).toBe(JSON.stringify([]));

            userInfo.chatInvites = [];
        });
        test("Success: chatInvites viewed", async () => {
            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);


            userInfo.chatInvites = [chatId]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            ["_id", "__v"].forEach((key) => delete chatInvResponse._body[0][key]);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body[0]).toMatchObject(chatInfo);

            userInfo.chatInvites = [];
        });


    })
    describe("send chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/chat/sendChatInvite/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/chat/sendChatInvite/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("user or chat not found", async () => {
            let response = await request(app)
                .put("/chat/sendChatInvite/64d50cfb436fbc75c6549dbe/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("User already in chat", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chats: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("Chatinvite was already sent before", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chatInvites: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
        })
        test("Success: Chat Invite sent", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
            // await Chat.deleteMany({title: "Test Initial Title"})
            // await Event.deleteMany({title: "Test Initial Title"})
        })


    })
    describe("accept chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("IDs of user or event are invalid", async () => {
            let response = await request(app)
                .put("/user/acceptChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/user/acceptChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/user/acceptChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("User already in chat", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chats: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("chat capacity at max", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.currCapacity = 6;

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.currCapacity = 1
        })
        test("Success: chat invite accepted", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
        })

    })
    describe("reject chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/user/rejectChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/user/rejectChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/user/rejectChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("No chatinvite sent", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/user/rejectChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("Success: rejected chat Invite", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chatInvites: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/user/rejectChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
        })
    })
    describe("send message", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/chat/sendChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/chat/sendChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/chat/sendChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: Message send", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let timeStamp = "1659172077"
            let text = "Hello"
            let chatInvResponse = await request(app)
                .put(`/chat/sendChat/${id}/${chatId}`)
                .send({
                    token,
                    timeStamp,
                    text
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
        })
    })
    describe("get chat info", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("Invalid chat ID", async () => {
            let response = await request(app)
                .get("/chat/dfjsdfks")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event with event ID not found", async () => {
            let response = await request(app)
                .get("/chat/64d31ae677f7ad9a56ab89c6")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: chat info successfully sent", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(chatInfo);

            let chatResponse = await request(app)
                .get(`/chat/${id}`)
                .set({
                    token
                });
            await Chat.findByIdAndDelete(id);
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);
        })
    })
    describe("view chats which a user is in", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["64c50cfb436fbc75c654d9eb"],
            currCapacity: 1,
        });
        test("Invalid User ID", async () => {
            let response = await request(app)
                .get("/user/dfjsdfks/chat")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toEqual([]);
        })
        test("No chats found", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .get(`/user/${id}/chat`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(chatResponse._body).toEqual([]);
        })
        test("Success: Chat found", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatListResponse = await request(app)
                .get(`/user/${id}/chat`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatListResponse.status).toBe(ERROR_CODES.SUCCESS)

        })
        
    })

})

describe("User Case 6: Profile Management", () => {
    describe("create user", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Success: user created", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);
            await User.findByIdAndDelete(id);
        });

    });

    describe("edit user", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let updatedUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Invalid user ID", async () => {
            let response = await request(app)
                .put("/user/dfjsdfks/edit")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("User with ID not found", async () => {
            let response = await request(app)
                .put("/user/64d31ae677f7ad9a56ab89c6/edit")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: user is edited", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let updatedResponse = await request(app)
                .put(`/user/${id}/edit`)
                .send(updatedUserInfo);
            await User.findByIdAndDelete(id);
            expect(updatedResponse.status).toBe(ERROR_CODES.SUCCESS);
            let updatedUser = updatedResponse._body;
            delete updatedUser["_id"];
            delete updatedUser["__v"];
            expect(updatedUser).toMatchObject(updatedUserInfo);
        });
    });

    describe("view user profile", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("Invalid user ID", async () => {
            let response = await request(app)
                .get("/user/dfjsdfks")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("User with user ID not found", async () => {
            let response = await request(app)
                .get("/user/64d31ae677f7ad9a56ab89c6")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: user is viewed", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let foundUser = await request(app)
                .get(`/user/${id}`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v"].forEach((key) => delete foundUser._body[key]);
            expect(foundUser._body).toMatchObject(userInfo);
        });
    });
    describe("view all friend requests", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("IDs of friend requests are invalid", async () => {
            userInfo.friendRequest = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendReqResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendReqResponse._body)).toBe(JSON.stringify([]));

            userInfo.friendRequest = [];
        });

        test("No friend requests for user found", async () => {
            userInfo.friendRequest = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendReqResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(friendReqResponse._body)).toBe(JSON.stringify([]));

            userInfo.friendRequest = [];
        });

        test("Success: list of friend requests of user viewed", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });

            await User.findByIdAndDelete(id)
            await User.findByIdAndDelete(otherUserID)

            let finalReq = friendReqResponse._body;

            ["_id", "__v"].forEach((key) => delete finalReq[0][key]);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(finalReq[0]).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [];
        });
    })
    describe("view all friends", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("IDs of friend are invalid", async () => {
            userInfo.friends = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));

            userInfo.friends = [];
        });

        test("No friend for user found", async () => {
            userInfo.friends = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));

            userInfo.friends = [];
        });

        test("Success: list of friend of user viewed", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friends = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });

            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);

            let final = friendResponse._body;

            ["_id", "__v"].forEach((key) => delete final[0][key]);
            expect(friendResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(final[0]).toMatchObject(otherUserInfo);

            userInfo.friends = [];
        });
    })
    describe("send friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/sendFriendRequest/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/sendFriendRequest/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Users already friends", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("friend request already sent before", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friendRequest = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friendRequest = []
        })
        test("Success: friend request send", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("accept friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/acceptUser/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/acceptUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Users already friends", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/acceptUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("Success: friend request accepted", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/acceptUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("reject friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/rejectUser/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/rejectUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("No friend request present", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            let friendReqResponse = await request(app)
                .put(`/user/rejectUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("Success: friend request rejected", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let friendReqResponse = await request(app)
                .put(`/user/rejectUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("remove friend", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/removeFriend/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/removeFriend/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: friend removed", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/removeFriend/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
    })
});

describe("Use Case 7: Report/Block User/Event", () => {
    describe("Report User", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Invalid user ID", async () => {
            let response = await request(app)
                .post("/user/sdfsdf/reportUser/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        }); 
        
        test("User reporter or reported not found", async () => {
            let response = await request(app)
                .post("/user/64d31ae677f7ad9a56ab89c6/reportUser/64d31ae677f7ad9a56ab89c6")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });

        test("Success: User is reported", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let reportResponse = await request(app)
                .post(`/user/${id}/reportUser/${otherUserID}`)
                .send({token, reason: "cuz", description: "cuz", isBlocked: false});
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(reportResponse.status).toBe(ERROR_CODES.SUCCESS);
            await Report.findByIdAndDelete(reportResponse._body._id)
        });
    });

    describe("Block User", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Invalid user ID", async () => {
            let response = await request(app)
                .post("/user/sdfsdf/reportUser/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        }); 
        
        test("User reporter or reported not found", async () => {
            let response = await request(app)
                .post("/user/64d31ae677f7ad9a56ab89c6/reportUser/64d31ae677f7ad9a56ab89c6")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("success: user is blocked", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let reportResponse = await request(app)
                .post(`/user/${id}/reportUser/${otherUserID}`)
                .send({token, reason: "cuz", description: "cuz", isBlocked: true});
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(reportResponse.status).toBe(ERROR_CODES.SUCCESS);
            await Report.findByIdAndDelete(reportResponse._body._id)
        });
    });

    describe("Report Event", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Invalid event ID", async () => {
            let response = await request(app)
                .post("/user/sdfsdf/reportEvent/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        }); 
        
        test("User reporter not found", async () => {
            let response = await request(app)
                .post("/user/64d31ae677f7ad9a56ab89c6/reportEvent/64d31ae677f7ad9a56ab89c6")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });

        test("Success: Event is reported", async () => {
            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            // console.log(eventResponse._body)
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let reportResponse = await request(app)
                .post(`/user/${id}/reportEvent/${eventId}`)
                .send({token, reason: "cuz", description: "cuz", isBlocked: false});
            await User.findByIdAndDelete(id);            
            await Chat.findByIdAndDelete(chat)
            await Event.findByIdAndDelete(eventId);
            expect(reportResponse.status).toBe(ERROR_CODES.SUCCESS);
            await Report.findByIdAndDelete(reportResponse._body._id)
        })
    });

    describe("Block Event", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let eventInfo = new EventDetails({
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Invalid event ID", async () => {
            let response = await request(app)
                .post("/user/sdfsdf/reportEvent/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        }); 
        
        test("User reporter not found", async () => {
            let response = await request(app)
                .post("/user/64d31ae677f7ad9a56ab89c6/reportEvent/64d31ae677f7ad9a56ab89c6")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });

        test("success: event is blocked", async () => {
            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            // console.log(eventResponse._body)
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let reportResponse = await request(app)
                .post(`/user/${id}/reportEvent/${eventId}`)
                .send({token, reason: "cuz", description: "cuz", isBlocked: true});
            await User.findByIdAndDelete(id);            
            await Chat.findByIdAndDelete(chat)
            await Event.findByIdAndDelete(eventId);
            expect(reportResponse.status).toBe(ERROR_CODES.SUCCESS);
            await Report.findByIdAndDelete(reportResponse._body._id)
        });
    });
    
    describe("List all reports", () => {
        test("check if endpong returns correct response", async () => {
            let response = await request(app).get("/reports").set({
                token
            })
            expect(response.status).toBe(ERROR_CODES.SUCCESS)
        })
    })

    

});

describe("User Case 8: Ban User/Event", () => {
    describe("Ban User", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Invalid user ID", async () => {
            let response = await request(app)
                .post("/user/dfjsdfks/ban")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toEqual(undefined);
        }); 

        test("User not found", async () => {
            let response = await request(app)
                .post("/user/64d31ae677f7ad9a56ab89c6/ban")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        }); 

        test("Success: User is banned", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let banResponse = await request(app).post(`/user/${id}/ban`).send({token})
            await User.findByIdAndDelete(id);
            expect(banResponse.status).toBe(ERROR_CODES.SUCCESS)            
        });
    });

    describe("Ban Event", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
            currCapacity: 1,
            participants: ["62d63248860a82beb388af87"],
        });
        test("Invalid event ID", async () => {
            let response = await request(app)
                .post("/event/dfjsdfks/ban")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toEqual(undefined);  
        }); 

        test("Event not found", async () => {
            let response = await request(app)
                .post("/event/64d31ae677f7ad9a56ab89c6/ban")
                .send({token});
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        }); 

        test("Success: Event is banned", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat
            // ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            // ["chat"].forEach((key) => delete eventInfo[key]);
            // expect(response._body).toMatchObject(eventInfo);
            
            let banResponse = await request(app).post(`/event/${id}/ban`).send({token})
            await Chat.findByIdAndDelete(chat)
            await Event.findByIdAndDelete(id);
            expect(banResponse.status).toBe(ERROR_CODES.SUCCESS)  

        });
    });

});