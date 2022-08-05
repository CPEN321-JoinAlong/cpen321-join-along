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

describe("Use Case 1: Event Management (Create + Edit Events)", () => {
    describe("create event", () => {
        let eventInfo = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            eventOwnerID: "62d63248860a82beb388af87",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            location: "2205 West Mall Toronto",
            description: "test description",
            eventImage: "image",
            numberOfPeople: 6,
            currCapacity: 1,
        });
        let eventInfo2 = new EventDetails({
            //id : 62d50cfb436fbc75c258d9eb
            title: "tester event",
            tags: ["Hiking"],
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            location: "2205 West Mall Toronto",
            description: "test description",
            numberOfPeople: 6,
        });
        test("Success: event and its chat created", async () => {
            let response = await request(app)
                .post("/event/create").send(Object.assign({token}, eventInfo))
                // .send({
                //     ...eventInfo,
                //     token
                // });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(response._body.chat)
            await Event.findByIdAndDelete(id);
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);
        });
        test("Success: event and its chat created without participants or owner", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo2,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(response._body.chat)
            await Event.findByIdAndDelete(id);
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo2[key]);
            eventInfo2.eventOwnerName = "Rob"
            delete eventInfo2.eventOwnerID
            expect(response._body).toMatchObject(eventInfo2);
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
                .put("/event/64d31ae677f7ad9a56ab89c6/edit").send(Object.assign({token}, eventInfo))
                // .send({
                //     ...eventInfo,
                //     token
                // });
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