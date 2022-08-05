const mongoose = require("mongoose");
const request = require("supertest");
const axios = require("axios")
const {
    app,
    server
} = require("./../server");

const User = require("./../models/User");
const Event = require("./../models/Event");
const Chat = require("./../models/Chat");
const ERROR_CODES = require("./../ErrorCodes.js");

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
    await Chat.deleteMany({ title: "tester event" })
    await Event.deleteMany({ title: "tester event" })
    await User.deleteMany({ name: "Rob Robber" })
    await User.deleteMany({ name: "Bob Bobber" })
    mongoose.connection.close();
    server.close();
    // done();
});

describe("Use Case 4: Login", () => {
    test("Token not valid", async () => {
        let response = await request(app).post("/login").send({ Token: "hdfshfksjdfh" })
        expect(response.status).toBe(ERROR_CODES.NOTACCEPTABLE)
    })

    test("User not not signed up", async () => {
        let tokenResponse = await axios.post("https://oauth2.googleapis.com/token", {
            "refresh_token": "1//04VYavoqMHpqSCgYIARAAGAQSNwF-L9Ir5tAqDJ8IPkxIJeoENO4F2_6YHKoeFDdA-S1pPJbCuEnXxHkbWB5rKxsjMAsnyT6-mtw",
            "client_id": "226767731113-9b0igsuolr8r02dt7926l2ispe0qf01q.apps.googleusercontent.com",
            "client_secret": "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",
            "scope": "",
            "grant_type": "refresh_token"
        })
        console.log(tokenResponse)
        if(tokenResponse.status === ERROR_CODES.SUCCESS) {
            let response = await request(app).post("/login").send({ Token: tokenResponse.data.id_token })
            expect(response.status).toBe(ERROR_CODES.NOTFOUND)
            expect(response._body).toEqual(expect.objectContaining({
                token: expect.any(String)
            }))
        }
    })

    test("User found", async () => {
        let tokenResponse = await axios.post("https://oauth2.googleapis.com/token", {
            "refresh_token": "1//04MhGUB7mNdDcCgYIARAAGAQSNwF-L9IrpKBaQY0yMHnqkcGJLo5sIEcY9vwNJjBXz5Ntfk50e9cFL0hXzdCT8jMID6e5qYU_4G4",
            "client_id": "226767731113-9b0igsuolr8r02dt7926l2ispe0qf01q.apps.googleusercontent.com",
            "client_secret": "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",
            "scope": "",
            "grant_type": "refresh_token"
        })
        console.log(tokenResponse)
        if(tokenResponse.status === ERROR_CODES.SUCCESS) {
            let response = await request(app).post("/login").send({ Token: tokenResponse.data.id_token })
            expect(response.status).toBe(ERROR_CODES.SUCCESS)
            expect(response._body).toMatchObject((await User.findById("62eae6dc6948e5255b2d2c43"))._doc)
        }
    })
})