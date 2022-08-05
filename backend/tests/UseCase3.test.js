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
    describe("List events with interest", () => {
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
        test("Success: endpoint returns list of events which include the interest tags", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({
                    ...eventInfo,
                    token
                })
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);

            let searchResponse = await request(app).get(`/event/tag/Hiking`).set({
                token
            })
            await Chat.findByIdAndDelete(chat)
            await Event.findByIdAndDelete(id);
            expect(searchResponse.status).toBe(ERROR_CODES.SUCCESS)
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
