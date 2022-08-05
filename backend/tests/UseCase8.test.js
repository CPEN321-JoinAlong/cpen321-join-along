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
                .post("/event/create").send(Object.assign({token, eventInfo}))
                // .send({
                //     ...eventInfo,
                //     token
                // });
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