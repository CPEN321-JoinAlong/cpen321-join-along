const path = require("path");
const express = require("express");
const mongoose = require("mongoose");
const admin = require("firebase-admin");
const serviceAccount = require("/home/azureuser/serviceAccountKey.json");
const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");

const {
    UserAccount,
    UserStore,
    ChatDetails,
    ChatEngine,
    EventDetails,
    EventStore,
    ReportService,
    BanService,
} = require("./modules");

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
let reportService = new ReportService();
let banService = new BanService();

// firebase admin SDK
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

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

app.use(async(req, res, next) => {
    const { Token } = req.body;
    let user = await userStore.findUserForLogin(Token)	
	console.log(user)
    if( user != null || req.path.includes("/user/create")) {
        next();
    } else {
        res.status(404).send("Unsuccessfull")
    }
})

//JUST FOR TESTING
app.get("/", async (req, res) => {
    let a = {}
    a["user"] = await User.find({});
    a["chat"] = await Chat.find({});
    a["event"] = await Event.find({});
    res.send(a);
});

//login - post
app.post("/login", async (req, res) => {
    const { Token } = req.body;
    let foundUser = await userStore.findUserForLogin(Token);
    if (foundUser == null) res.status(404).send("Unsuccessfull");
    else res.status(200).send({ user: foundUser });
});

//* All the create paths for the main modules

//Adds the user object to the database and sends the id back to frontend - post
app.post("/user/create", async (req, res) => {
    let userObject = req.body;
    let userInfo = new UserAccount(userObject);
    res.status(200).send(await userInfo.createUserAccount(userStore));
});

//Creates a chat object and sends it to frontend
app.post("/chat/create", async (req, res) => {
    let chatObject = req.body;
    let chatInfo = new ChatDetails(chatObject);
    res.status(200).send(await chatEngine.createChat(chatInfo, userStore));
});

//Creates an event object (and a related chat object) and sends it to frontend
app.post("/event/create", async (req, res) => {
    let eventObject = req.body;
    let eventInfo = new EventDetails(eventObject);
    console.log(eventInfo);
    let event = await eventStore.createEvent(eventInfo, userStore);
    let chat = await chatEngine.createChat(
        new ChatDetails({
            title: event.title,
            tags: event.tags,
            numberOfPeople: event.numberOfPeople,
            participants: event.participants,
            description: event.description,
            event: event._id,
        }),
        userStore
    );
    event.chat = chat._id;
    await eventStore.updateEvent(event._id, event, userStore);
    res.status(200).send({ event });
});

//* All the edit paths for the main modules

//Edits User and sends it to frontend
app.put("/user/:id/edit", async (req, res) => {
    let { id } = req.params;
    await userStore.updateUserAccount(id, req.body);
    res.status(200).send(await userStore.findUserByID(id)); //do we need to send the
});

//Edits Chat and sends it to frontend
app.put("/chat/:id/edit", async (req, res) => {
    let { id } = req.params;
    await chatEngine.editChat(id, req.body, userStore);
    res.status(200).send(await chatEngine.findChatByID(id)); //do we need to send the
});

//Edits User and sends it to frontend
app.put("/event/:id/edit", async (req, res) => {
    let { id } = req.params;
    await eventStore.updateEvent(id, req.body, userStore);
    res.status(200).send(await eventStore.findEventByID(id)); //do we need to send the
});

//* Home Screen

//Sends the user object and events the user is a part of for the home screen - get
app.get("/user/:id/home", async (req, res) => {
    let { id } = req.params;
    let foundUser = await userStore.findUserByID(id);
    if (foundUser == null) res.status(404).send("No User Found");
    else {
        let events = await eventStore.findEventByIDList(foundUser.events);
        res.status(200).send({ events }); //need to change this to event id, name and description only
    }
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

//* Chat info

//Chat list: Sends the list of chats the user is in - get
app.get("/user/:id/chat", async (req, res) => {
    let { id } = req.params;
    let chatList = await chatEngine.findChatByUser(id);
    res.status(200).send({ chatList });
});

//Chat: send message to a single user
app.put("/chat/sendSingle/:fromUserID/:toUserID", async (req, res) => {
    console.log("hello");
    let { fromUserID, toUserID } = req.params;
    let { timeStamp, text } = req.body;
    fromUserName = await userStore.findUserByID(fromUserID).name;
    await chatEngine.sendMessage(fromUserID, toUserID, text, fromUserName, timeStamp);

    getMessaging().send({
        data: {
            name: fromUserName,
            text: text
        },
        topic: fromUserID + "_" + toUserID
    }).then(((response) => console.log("Message sent: ", response))).catch((err) => console.log("Error: ", err))

    res.status(200).send("Successful");
});

//Chat: send message to a group
app.put("/chat/sendGroup/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    let { timeStamp, text } = req.body;

    fromUserName = await userStore.findUserByID(userID).name;
    eventName = await eventStore.findEventByID(eventID).title;
    await chatEngine.sendGroupMessage(userID, eventID, text, fromUserName, timeStamp);

    getMessaging().send({
        data: {
            name: fromUserName,
            text: text
        },
        topic: eventName
    }).then(((response) => console.log("Message sent: ", response))).catch((err) => console.log("Error: ", err))

    res.status(200).send("Successful");
});

//Chat: Sends the chat object (which includes all the messages) - get
app.get("/chat/:id", async (req, res) => {
    let { id } = req.params;
    let chat = await chatEngine.findChatByID(id);
    res.status(200).send({ chat });
});

//* Event info

//Event list: Sends the list of Events the user is in - get
app.get("/user/:id/event", async (req, res) => {
    let { id } = req.params;
    let eventList = await eventStore.findEventByUser(id);
    res.status(200).send({ eventList });
});

//Event list: Send list of Event
app.post("/event/filter", async (req, res) => {
    let eventList = await eventStore.findEventByDetails(req.body);
    res.status(200).send({ eventList });
});

//Event: Sends the event object (for view event details?) - get
app.get("/event/:id", async (req, res) => {
    let { id } = req.params;
    let event = await eventStore.findEventByID(id);
    res.status(200).send({ event });
});

//* Accept or Reject requests (can be used for join as well) and invites

app.put("/user/acceptChat/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    await userStore.acceptChatInvite(userID, chatID, chatEngine);
    res.status(200).send("Sucessful");
});

app.put("/user/acceptEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    await userStore.acceptEventInvite(userID, eventID, eventStore, chatEngine);
    res.status(200).send("Sucessful");
});

app.put("/user/acceptUser/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    await userStore.acceptFriendRequest(userID, otherUserID);
    res.status(200).send("Sucessful");
});

app.put("/user/rejectUser/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    await userStore.rejectChatInvite(userID, chatID);
    res.status(200).send("Sucessful");
});

app.put("/user/rejectEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    await userStore.rejectEventInvite(userID, eventID);
    res.status(200).send("Sucessful");
});

app.put("/user/rejectChat/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    await userStore.rejectFriendRequest(userID, otherUserID);
    res.status(200).send("Sucessful");
});

//* Remove friend or leave event and chat

app.put("/user/removeFriend/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    await userStore.removeFriend(userID, otherUserID);
    res.status(200).send("Sucessful");
});

app.put("/user/leaveEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    let event = eventStore.findEventByID(eventID);
    await userStore.leaveEvent(userID, eventID);
    await userStore.leaveChat(userID, event.chat);
    res.status(200).send("Sucessful");
});

app.put("/user/leaveChat/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    await userStore.leaveChat(userID, chatID);
    res.status(200).send("Sucessful");
});

//* Report and Ban paths

app.post("/user/:reporterID/reportUser/:reportedID", async (req, res) => {
    let { reporterID, reportedID } = req.params;
    let { reason, isBlocked } = req.body;
    await reportService.reportUser(
        reportedID,
        reporterID,
        reason,
        isBlocked,
        userStore
    );
    res.status(200).send("Successful");
});

app.post("/user/:reporterID/reportEvent/:reportedID", async (req, res) => {
    let { reporterID, reportedID } = req.params;
    let { reason, isBlocked } = req.body;
    await reportService.reportEvent(
        reportedID,
        reporterID,
        reason,
        isBlocked,
        userStore
    );
    res.status(200).send("Successful");
});

app.get("/reports", async (req, res) => {
    let reports = await reportService.viewAllReports();
    res.status(200).send({ reports });
});

app.post("/user/:id/ban", async (req, res) => {
    let { id } = req.params;
    await banService.banUser(id, userStore);
    res.status(200).send("Successful");
});

app.post("/event/:id/ban", async (req, res) => {
    let { id } = req.params;
    await banService.banEvent(id, eventStore);
    res.status(200).send("Successful");
});
