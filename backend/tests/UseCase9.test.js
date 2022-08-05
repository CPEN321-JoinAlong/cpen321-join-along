const mongoose = require("mongoose");
const request = require("supertest");
const {
    app,
    server
} = require("./../server");

const UserAccount = require("./../modules/user_module/UserAccount");
const User = require("./../models/User");

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
    await Event.deleteMany({title: "Test Event 1"})
    await Event.deleteMany({title: "Test Event 2"})
    await Event.deleteMany({title: "Test Event 3"})
    await Chat.deleteMany({title: "Test Event 1"})
    await Chat.deleteMany({title: "Test Event 2"})
    await Chat.deleteMany({title: "Test Event 3"})

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
            interests: ["Hiking", "Camping", "Rock Climbing", "Darts", "Escape Rooms", "Archery", "Spike Ball", "Frisbee", "Cards"],
            location: "2205 Lower Mall Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        let eventList = [
            {
                "title": "Test Event 1",
                "tags": ["Hiking", "Camping", "Rock Climbing", "Darts", "Escape Rooms", "Archery", "Spike Ball", "Frisbee", "Cards"],
                "participants": [
                    "62d50cfb436fbc75c258d9eb"
                ],
                "beginningDate": "2022-08-03T05:57:00.000Z",
                "endDate": "2022-08-03T05:57:00.000Z",
                "publicVisibility": true,
                "numberOfPeople": 123,
                "currCapacity": 1,
                "location": "2336 Main Mall, Vancouver, BC V6T 1Z4, Canada",
                "coordinates": "49.2622063,-123.2497726",
                "eventImage": null,
                "description": "Help pls",
                "distance": -1
            },
            {
                "title": "Test Event 2",
                "tags": ["Camping", "Rock Climbing", "Darts", "Escape Rooms", "Archery", "Frisbee", "Cards"],
                "participants": [
                    "62d50cfb436fbc75c258d9eb"
                ],
                "beginningDate": "2022-08-04T07:00:00.000Z",
                "endDate": "2022-08-04T07:00:00.000Z",
                "publicVisibility": true,
                "numberOfPeople": 5,
                "currCapacity": 2,
                "location": "12345 Main St, Vancouver, BC V5X 4N6, Canada",
                "coordinates": "49.2065541,-123.1024594",
                "eventImage": null,
                "description": "fun times",
                "distance": -1
            },
            {
                "title": "Test Event 3",
                "tags": ["Camping", "Rock Climbing", "Darts", "Frisbee", "Cards"],
                "participants": [
                    "62d50cfb436fbc75c258d9eb"
                ],
                "beginningDate": "2022-08-04T15:27:00.000Z",
                "endDate": "2022-08-04T15:27:00.000Z",
                "publicVisibility": true,
                "numberOfPeople": 7,
                "currCapacity": 1,
                "location": "2336 Main Mall, Vancouver, BC V6T 1Z4, Canada",
                "coordinates": "49.262206299999995,-123.2497726",
                "eventImage": null,
                "description": "IDK how I can do this myself but I will try",
                "distance": -1
            }
        ]

        test("Success: User is recommended events", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);
            eventList.forEach(event => {
                event.eventOwnerID = id,
                event.eventOwnerName = userInfo.name
            })

            console.log(eventList)

            let eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventList[0],
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventList[1],
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);
            eventResponse = await request(app)
                .post("/event/create")
                .send({
                    ...eventList[2],
                    token
                });
            expect(eventResponse.status).toBe(ERROR_CODES.SUCCESS);

            let recResponse = await request(app).get(`/user/${id}/recommendedEvents`).send({token})
            expect(recResponse.status).toBe(ERROR_CODES.SUCCESS)      
            console.log(recResponse._body)     
            recResponse._body.forEach(event => {
                ["_id", "__v", "chat"].forEach((key) => delete event[key]);
            })
            for(let i = 0; i < 3; i++){
                expect(recResponse._body[i]).toMatchObject(eventList[i])
            }
        });
    });
});