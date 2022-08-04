
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

describe("User Case 9: Recommend Events", () => {
    describe("Recommend Events to User", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming","Cards","Skiing"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
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
});