const mongoose = require("mongoose");
const request = require("supertest");
const {
    app,
    server
} = require("./../server");

const UserAccount = require("./../modules/user_module/UserAccount");
const User = require("./../models/User");

const EventDetails = require("./../modules/event_module/EventDetails");
const Event = require("./../models/Event");

const Chat = require("./../models/Chat");

const ERROR_CODES = require("./../ErrorCodes.js");

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
