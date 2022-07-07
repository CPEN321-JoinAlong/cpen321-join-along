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

//login - post
app.post("/login", async (req, res) => {
    const { Token } = req.body;
    let foundUser = await userStore.findUserForLogin(Token);
    if (foundUser == null) res.status(404).send(false);
    else res.status(200).send({ user: foundUser }); //need to confirm what else to do
});


//JUST FOR TESTING
app.get("/", async (req, res) => {
    console.log(await User.deleteMany())
    res.send(await User.find({}))
})

//Adds the user object to the database and sends the id back to frontend - post
app.post("/user/create", async (req, res) => {
    let userObject = req.body;
    let userInfo = new UserAccount(userObject);
    // await User.deleteMany({});
    // console.log(id);
    res.status(200).send(await userInfo.createUserAccount(userStore));
    console.log(await User.find({}))
    // res.status(200).send("hello");
});

//Creates a chat object and sends it to frontend
app.post("/chat/create", async (req, res) => {
    let chatObject = req.body;
    let chatInfo = new ChatDetails(chatObject);
    res.status(200).send(await chatEngine.createChat(chatInfo));
});

//Creates an event object and sends it to frontend
app.post("/event/create", async (req, res) => {
    let eventObject = req.body;
    let eventInfo = new EventDetails(eventObject);
    res.status(200).send(await eventStore.createEvent(eventInfo));
    console.log()
});

//Edits User and sends it to frontend
app.put("/user/:id/edit", async (req, res) => {
    let { id } = req.params;
    await userStore.updateUserAccount(id, req.body);
    res.status(200).send(await userStore.findUserByID(id)); //do we need to send the
});

//Edits Chat and sends it to frontend
app.put("/chat/:id/edit", async (req, res) => {
    let { id } = req.params;
    await chatEngine.editChat(id, req.body);
    res.status(200).send(await chatEngine.findChatByID(id)); //do we need to send the
});

//Edits User and sends it to frontend
app.put("/event/:id/edit", async (req, res) => {
    let { id } = req.params;
    await eventStore.updateEvent(id, req.body);
    res.status(200).send(await eventStore.findEventByID(id)); //do we need to send the
});

//Sends the user object for the profile page - get
app.get("/user/:id", async (req, res) => {
    let { id } = req.params;
    let foundUser = await userStore.findUserByID(id);
    if (foundUser == null) res.status(404).send("No User Found");
    else {
        res.status(200).send({ user: foundUser });
    }
});

//Sends the user object and events the user is a part of for the home screen - get
app.get("/user/:id/home", async (req, res) => {
    let { id } = req.params;
    let foundUser = await userStore.findUserByID(id);
    if (foundUser == null) res.status(404).send("No User Found");
    else {
        let events = await new UserAccount(foundUser).findPersonalEvents(
            eventStore
        );
        res.status(200).send({ events: events }); //need to change this to event id, name and description only
    }
});

//Chat list: Sends the list of chats the user is in - get
app.get("/user/:id/chat", async (req, res) => {
    let { id } = req.params;
    let chatList = await chatEngine.findChatByUser(id);
    res.status(200).send({ chatList });
});

//Chat: Sends the chat object (which includes all the messages) - get
app.get("/chat/:id", async (req, res) => {
    let { id } = req.params;
    let chat = await chatEngine.findChatByID(id);
    res.status(200).send({ chat });
});

//Event list: Sends the list of Events the user is in - get
app.get("/user/:id/event", async (req, res) => {
    let { id } = req.params;
    let eventList = await eventStore.findEventByUser(id);
    res.status(200).send({ eventList });
});

//Event: Sends the event object (for view event details?) - get
app.get("/event:id", async (req, res) => {
    let { id } = req.params;
    let event = await eventStore.findEventByID(id);
    res.status(200).send({ event });
});
