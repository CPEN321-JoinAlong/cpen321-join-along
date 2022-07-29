const axios = require("axios");
const mongoose = require("mongoose");

const UserAccount = require("./../modules/user_module/UserAccount");
const UserStore = require("./../modules/user_module/UserStore");
const User = require("./../models/User");

const EventDetails = require("./../modules/event_module/EventDetails");
const EventStore = require("./../modules/event_module/EventStore");
const Event = require("./../models/Event")

const ChatDetails = require("./../modules/chat_module/ChatDetails");
const ChatEngine = require("./../modules/chat_module/ChatEngine");
const Chat = require("./../models/Chat")

const ERROR_CODES = require("./../ErrorCodes.js");
const ResponseObject = require("./../ResponseObject");

mongoose.connect("mongodb://useradmin:MTnCBEI9nIx6L6F@54.200.52.211:34542/joinalong", {
    useNewUrlParser: true,
    useUnifiedTopology: true,
});
const db = mongoose.connection;
db.on("error", console.error.bind(console, "connection error:"));
db.once("open", () => {
    // console.log("Database connected");
});
beforeAll(done => {
  done()
})

afterAll(done => {
  // Closing the DB connection allows Jest to exit successfully.
  mongoose.connection.close()
  done()
})


describe("login tests", () => {
    test("Token not valid", async () => {
        console.log(await User.find({}))
    });
});

// db.close();