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
