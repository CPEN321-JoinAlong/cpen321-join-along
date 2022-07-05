const path = require("path");
const express = require("express");
const mongoose = require("mongoose");
const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");

// import {UserStore} from './modules.js'
const {
    UserAccount,
    UserStore,
    ChatDetails,
    ChatEngine,
    EventDetails,
    EventStore,
} = require("./modules");
// const methodOverride = require('method-override');

function logRequest(req, res, next) {
    console.log(`${new Date()}  ${req.ip} : ${req.method} ${req.path}`);
    next();
}

mongoose.connect("mongodb://localhost:27017/joinalong", {
    useNewUrlParser: true,
    useUnifiedTopology: true,
});

const db = mongoose.connection;
db.on("error", console.error.bind(console, "connection error:"));
db.once("open", () => {
    console.log("Database connected");
});

const host = "localhost";
const port = 3000;
const clientApp = path.join(__dirname, "public");

let userStore = new UserStore();
let eventStore = new EventStore();
let chatEngine = new ChatEngine();

// express app
let app = express();

app.set("views", path.join(__dirname, "views"));
app.use(express.json()); // to parse application/json
app.use(
    express.urlencoded({
        extended: true,
    })
); // to parse application/x-www-form-urlencoded
app.use(logRequest); // logging for debug

app.use(express.static(__dirname + "/public"));

app.listen(port, () => {
    console.log(
        `${new Date()}  App Started. Listening on ${host}:${port}, serving ${clientApp}`
    );
});

app.post("/login", async (req, res) => {
    const { Token } = req.body;
    let foundUser = await userStore.findUserForLogin(Token);
    if (foundUser == null) res.status(404).send(false);
    else res.status(200).send(true); //need to confirm what else to do
});

//Adds the user object to the database and sends the id back to frontend
app.post("/user/create", async (req, res) => {
    let userObject = req.body;
    let userInfo = new UserAccount(userObject);
    await User.deleteMany({});
    // console.log(await User.find({}))
    let id = await userInfo.createUserAccount(userStore);
    console.log(id);
    res.status(200).send({ id: id });
});

//Send the user and events for the home screen
app.get("/user/:id", async (req, res) => {
    let { id } = req.params;
    let foundUser = await userStore.findUserByID(id);
    if (foundUser == null) res.status(404).send("No User Found");
    else {
        let events = await new UserAccount(foundUser).findPersonalEvents(
            eventStore
        );
        res.status(200).send({ user: foundUser, events: events });
    }
});
