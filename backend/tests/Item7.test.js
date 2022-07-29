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

describe("Use Case 1: Event Management (Create + Edit Events", () => {    
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
                .send({ ...eventInfo, token });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(response._body.chat)
            await Event.findByIdAndDelete(id);
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            console.log(response._body);
            console.log(eventInfo);
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
                .send({ ...eventInfo, token });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event with ID not found", async () => {
            let response = await request(app)
                .put("/event/64d31ae677f7ad9a56ab89c6/edit")
                .send({ ...eventInfo, token });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: event is edited", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({ ...eventInfo, token });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);

            let updatedResponse = await request(app)
                .put(`/event/${id}/edit`)
                .send({ ...updatedEventInfo, token });
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
                .set({ token });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("Event with event ID not found", async () => {
            let response = await request(app)
                .get("/event/64d31ae677f7ad9a56ab89c6")
                .set({ token });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: Event is viewed", async () => {
            let response = await request(app)
                .post("/event/create")
                .send({ ...eventInfo, token });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            let chat = response._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete response._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(response._body).toMatchObject(eventInfo);


            let eventResponse = await request(app)
                .get(`/event/${id}`)
                .set({ token });
            await Chat.findByIdAndDelete(chat);
            await Event.findByIdAndDelete(id);
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);
        })
    })
    describe("view events which a user is In", () => {
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
                .set({ token });
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
                .set({ token });
            await User.findByIdAndDelete(id);
            expect(eventResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(eventResponse._body).toEqual([]);
        })
        test("No events found", async () => {
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
                .send({ ...eventInfo, token });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);


            let eventListResponse = await request(app)
                .get(`/user/${id}/event`)
                .set({ token });
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
    //accept EventInvite, rejectEventInvite, leaveEvent
    describe("Accept EventInvite (join event)", () => {
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event nor User found", async () => {
            let response = await request(app)
                .put("/user/acceptEvent/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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
                .send({ ...eventInfo, token });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({token});
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
                .send({ ...eventInfo, token });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({token});
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
                .send({ ...eventInfo, token });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/acceptEvent/${id}/${eventId}`)
                .send({token});
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(joinResponse._body).toBe(undefined);
        })
    })
})


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
            .send({ ...chatInfo, token });
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
        let id = response._body._id;
        await Chat.findByIdAndDelete(id);
        ["_id", "__v"].forEach((key) => delete response._body[key]);
        console.log(response._body);
        expect(response._body).toMatchObject(chatInfo);
    });
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event nor User found", async () => {
            let response = await request(app)
                .put("/user/rejectEvent/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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
                .send({ ...eventInfo, token });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            let eventId = eventResponse._body._id;
            let chat = eventResponse._body.chat;
            ["_id", "__v", "chat"].forEach((key) => delete eventResponse._body[key]);
            ["chat"].forEach((key) => delete eventInfo[key]);
            expect(eventResponse._body).toMatchObject(eventInfo);

            let joinResponse = await request(app)
                .put(`/user/rejectEvent/${id}/${eventId}`)
                .send({token});
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(joinResponse._body).toBe(undefined);
        })
        test("Success: Event Invite rejected", async () => {
            
            
            let eventResponse = await request(app)
            .post("/event/create")
            .send({ ...eventInfo, token });
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
                .send({token});
            await User.findByIdAndDelete(id);
            await Event.findByIdAndDelete(eventId);
            await Chat.findByIdAndDelete(chat);
            expect(joinResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(joinResponse._body).toBe(undefined);

            eventInfo.eventInvites = []
        })
    })
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
            .send({...chatInfo, token});
        expect(response.status).toBe(ERROR_CODES.INVALID);
        expect(response._body).toBe(undefined);
    });
    test("Chat with ID not found", async () => {
        let response = await request(app)
            .put("/chat/64d31ae677f7ad9a56ab89c6/edit")
            .send({...chatInfo, token});
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
        expect(response._body).toBe(undefined);
    })
    test("Success: chat is edited", async () => {
        let response = await request(app)
            .post("/chat/create")
            .send({ ...chatInfo, token });
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
        let id = response._body._id;
        ["_id", "__v"].forEach((key) => delete response._body[key]);
        console.log(response._body);
        expect(response._body).toMatchObject(chatInfo);
        
        let updatedResponse = await request(app)
            .put(`/chat/${id}/edit`)
            .send({...updatedChatInfo, token});
        await Chat.findByIdAndDelete(id);
        expect(updatedResponse.status).toBe(ERROR_CODES.SUCCESS)
        let updatedChat = updatedResponse._body
        delete updatedChat["_id"]
        delete updatedChat["__v"]
        expect(updatedChat).toMatchObject(updatedChatInfo);
    })
});


describe("Profile Management Use Case", () => {
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
                .set({ token });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("User with user ID not found", async () => {
            let response = await request(app)
                .get("/user/64d31ae677f7ad9a56ab89c6")
                .set({ token });
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
                .set({ token });
            await User.findByIdAndDelete(id);
            expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v"].forEach((key) => delete foundUser._body[key]);
            expect(foundUser._body).toMatchObject(userInfo);
        });
    });

    //need see/send/accept/remove friendRequests, see/accept/remove friends, 

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
            .set({ token });
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
            .set({ token });
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
            .set({ token });
            
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            
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
            .set({ token });
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
            .set({ token });
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
            .set({ token });
            
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/sendFriendRequest/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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

            await User.findByIdAndUpdate(id, {$push: {friends: otherUserID}});

            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({token});
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
                .send({token});
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
                .send({token});
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/acceptUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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

            await User.findByIdAndUpdate(id, {$push: {friends: otherUserID}});

            let friendReqResponse = await request(app)
                .put(`/user/acceptUser/${id}/${otherUserID}`)
                .send({token});
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
                .send({token});
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/rejectUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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
                .send({token});
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
                .send({token});
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
                .send({token});
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/removeFriend/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({token});
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

            await User.findByIdAndUpdate(id, {$push: {friends: otherUserID}});

            let friendReqResponse = await request(app)
                .put(`/user/removeFriend/${id}/${otherUserID}`)
                .send({token});
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
    })
});

describe("view event", () => {
    test("No events found", async () => {});
    test("Success: view all events", async () => {});
    
    test("Event id is invalid", async () => {});
    test("No event found", async () => {});
    test("Success: event is viewed", async () => {});

    test("No event found", async () => {});
    test("Success: list of events with given named viewed", async () => {});

    test("No event found", async () => {});
    test("Success: list of events a user is in viewed", async () => {});
});

describe("view chat", () => {
    test("No chat found", async () => {});
    test("Invalid chat ID", async () => {});
    test("Success: chat is viewed", async () => {});

    test("No chat found", async () => {});
    test("Invalid chat ID", async () => {});
    test("Success: list of chat with given name viewed", async () => {});

    test("No chat found", async () => {});
    test("Invalid user ID", async () => {});
    test("Success: list of chat requests of user viewed", async () => {});
});

describe("send chat", () => {
    test("Invalid user ID", async () => {});
    test("Invalid chat ID", async () => {});
    test("No user found", async () => {});
    test("No chat found", async () => {});
    test("Success: chat is sent to single user", async () => {});
    
    
    test("Invalid user ID", async () => {});
    test("Invalid chat ID", async () => {});
    test("No user found", async () => {});
    test("No chat found", async () => {});
    test("Success: chat is sent to chat group", async () => {});
});

describe("send chat invite", () => {
    test("Success: chat invite is sent", async () => {});
});

describe("send friend request", () => {
    test("Success: friend request is sent", async () => {});
});

describe("accept chat invite", () => {
    test("Success: chat invite accepted", async () => {});
});

describe("accept event invite", () => {
    test("Success: event invite accepted", async () => {});
});

describe("accept friend request", () => {
    test("Success: friend request accepted", async () => {});
});

describe("reject chat invite", () => {
    test("Success: chat invite rejected", async () => {});
});

describe("reject event invite", () => {
    test("Success: event invite rejected", async () => {});
});

describe("reject friend request", () => {
    test("Success: friend request rejected", async () => {});
});

describe("remove friend", () => {
    test("Success: friend removed", async () => {});
});

describe("leave event", () => {
    test("Success: event left", async () => {});
});

describe("leave chat", () => {
    test("Success: chat left", async () => {});
});

describe("report user", () => {
    test("Success: user reported", async () => {});
});

describe("report event", () => {
    test("Success: event reported", async () => {});
});

describe("view reports", () => {
    test("Success: all reports viewed", async () => {});
});

describe("ban user", () => {
    test("Success: user is banned", async () => {});
});

describe("ban event", () => {
    test("Success: event is banned", async () => {});
});