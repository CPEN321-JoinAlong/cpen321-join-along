const axios = require("axios");
const mongoose = require("mongoose");
const request = require("supertest");
const { app, server } = require("./../server");

const UserAccount = require("./../modules/user_module/UserAccount");
const UserStore = require("./../modules/user_module/UserStore");
const User = require("./../models/User");

const EventDetails = require("./../modules/event_module/EventDetails");
const EventStore = require("./../modules/event_module/EventStore");
const Event = require("./../models/Event");

const ChatDetails = require("./../modules/chat_module/ChatDetails");
const ChatEngine = require("./../modules/chat_module/ChatEngine");
const Chat = require("./../models/Chat");

const ERROR_CODES = require("./../ErrorCodes.js");
const ResponseObject = require("./../ResponseObject");

const token = "104872861014317782334";

mongoose.connect(
    "mongodb://useradmin:MTnCBEI9nIx6L6F@54.200.52.211:34542/joinalong",
    {
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

afterAll((done) => {
    // Closing the DB connection allows Jest to exit successfully.
    mongoose.connection.close();
    server.close();
    done();
});

describe("create user", () => {
    let userInfo = new UserAccount({
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        token,
    });
    test("user created", async () => {
        let response = await request(app).post("/user/create").send(userInfo);
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
        let id = response._body._id;
        ["_id", "__v"].forEach((key) => delete response._body[key]);
        expect(response._body).toMatchObject(userInfo);
        await User.findByIdAndDelete(id);
        // await User.findByIdAndDelete("62e357dcc8357de7d4335256");
        // await User.findByIdAndDelete("62e358c01bbf828d5524b1b7");
    });
});

describe("create chat", () => {
    let chatInfo = new ChatDetails({
        title: "tester chater",
        tags: ["Hiking"],
        numberOfPeople: 6,
        description: "test description",
        participants: ["64c50cfb436fbc75c654d9eb"],
        currCapacity: 1,
    });
    test("chat created", async () => {
        let response = await request(app).post("/chat/create").send({...chatInfo, token});
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
        let id = response._body._id;
        await Chat.findByIdAndDelete(id);
        ["_id", "__v"].forEach((key) => delete response._body[key]);
        console.log(response._body)
        expect(response._body).toMatchObject(chatInfo);
    });
});

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
        participants: ["62d63248860a82beb388af87"]
    });
    test("event and its chat created", async () => {
        let response = await request(app).post("/event/create").send({...eventInfo, token});
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
        let id = response._body._id;
        await Event.findByIdAndDelete(id);
        ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
        ["chat"].forEach((key) => delete eventInfo[key]);
        console.log(response._body)
        console.log(eventInfo)
        expect(response._body).toMatchObject(eventInfo);
    });
});

describe("edit user", () => {
    test("user is edited", async () => {});
});

describe("edit chat", () => {
    test("chat is edited", async () => {});
});

describe("edit event", () => {
    test("event is edited", async () => {});
});

describe("view user", () => {
    test("user is viewed", async () => {});
    test("list of users with given name viewed", async () => {});
    test("list of friend requests of user viewed", async () => {});
    test("list of friends of user viewed", async () => {});
});

describe("view event", () => {
    test("list of events with given named viewed", async () => {});
    test("list of events a user is in viewed", async () => {});
});

describe("view chat", () => {
    test("chat is viewed", async () => {});
    test("list of chat with given name viewed", async () => {});
    test("list of chat requests of user viewed", async () => {});
});

describe("send chat", () => {
    test("chat is sent to single user", async () => {});
    test("chat is sent to chat group", async () => {});
});
