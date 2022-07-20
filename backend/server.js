const path = require("path");
const express = require("express");
const mongoose = require("mongoose");
// const admin = require("firebase-admin");
// const serviceAccount = require("/home/azureuser/serviceAccountKey.json");
const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");
const Report = require("./models/Report");
const axios = require("axios");
const UserAccount = require("./modules/user_module/UserAccount");
const UserStore = require("./modules/user_module/UserStore");
const ChatDetails = require("./modules/chat_module/ChatDetails");
const ChatEngine = require("./modules/chat_module/ChatEngine");
const EventDetails = require("./modules/event_module/EventDetails");
const EventStore = require("./modules/event_module/EventStore");
const ReportService = require("./modules/report_module/ReportService")
const BanService = require("./modules/ban_module/BanService")
const ERROR_CODES = require("./../../ErrorCodes")

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

// // firebase admin SDK
// admin.initializeApp({
//     credential: admin.credential.cert(serviceAccount)
// });

// express app
let app = express();

app.use(express.json()); // to parse application/json
app.use(
    express.urlencoded({
        extended: true,
    })
); // to parse application/x-www-form-urlencoded
app.use(logRequest); // logging for debug

app.listen(port, () => {
    console.log(
        `${new Date()}  App Started. Listening on ${host}:${port}, serving ${clientApp}`
    );
});

app.use(async (req, res, next) => {
    let token;
    if (Object.keys(req.body).length !== 0) token = req.body.token;
    else token = req.headers.token;
    let user = await userStore.findUserForLogin(token);
    if (
        user != null ||
        req.path.includes("/login") || req.path.includes("/user/create") || req.path == "/test"
    ) {
        next();
    } else {
        res.status(ERROR_CODES.NOTFOUND).send("Unsuccessfull");
    }
});

//JUST FOR TESTING
app.get("/test", async (req, res) => {
    let a = {};
    //await User.deleteMany({});
    //await Event.deleteMany({});
    //await Chat.deleteMany({});
    //await User.updateMany({},{ friends: [] })
    // console.log(await User.updateMany({location: "fsdhfkjshfsdh"},{ $push: {interests: "HEH"} }))
    // await User.findByIdAndUpdate("62cc914dcb4206428b972c28", {$pull: {events: "dslkfjl"}})
    a["user"] = await User.find({});
    a["chat"] = await Chat.find({});
    a["event"] = await Event.find({});
    a["report"] = await Report.find({});
    res.send(a);
});

//login - post
app.post("/login", async (req, res) => {
    const { Token } = req.body;
    try {
        let response = await axios(
            `https://oauth2.googleapis.com/tokeninfo?id_token=${Token}`
        );
        if (response.status === ERROR_CODES.SUCCESS) {
            let foundUser = await userStore.findUserForLogin(response.data.sub);
            if (foundUser == null)
                res.status(ERROR_CODES.NOTFOUND).send({ token: response.data.sub });
            else res.status(ERROR_CODES.SUCCESS).send(foundUser);
        } else {
            res.status(ERROR_CODES.NOTACCEPTABLE).send(
                "Token not valid, try signing in again or use another account"
            );
        }
    } catch (e) {
        res.status(ERROR_CODES.NOTACCEPTABLE).send(
            "Token not valid, try signing in again or use another account"
        );
    }
});

//* All the create paths for the main modules

//Adds the user object to the database and sends the id back to frontend - post
app.post("/user/create", async (req, res) => {
    let userObject = req.body;
    let userInfo = new UserAccount(userObject);
    let user = await userInfo.createUserAccount(userStore);
    console.log(user);
    res.status(ERROR_CODES.SUCCESS).send(user);
});

//Creates a chat object and sends it to frontend
app.post("/chat/create", async (req, res) => {
    let chatObject = req.body;
    let chatInfo = new ChatDetails(chatObject);
    res.status(ERROR_CODES.SUCCESS).send(await chatEngine.createChat(chatInfo, userStore));
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
    res.status(ERROR_CODES.SUCCESS).send(event);
});

//* All the edit paths for the main modules

//Edits User and sends it to frontend
app.put("/user/:id/edit", async (req, res) => {
    let { id } = req.params;
    console.log(await userStore.updateUserAccount(id, req.body));
    res.status(ERROR_CODES.SUCCESS).send(await userStore.findUserByID(id)); //do we need to send the
});

//Edits Chat and sends it to frontend
app.put("/chat/:id/edit", async (req, res) => {
    let { id } = req.params;
    await chatEngine.editChat(id, req.body, userStore);
    res.status(ERROR_CODES.SUCCESS).send(await chatEngine.findChatByID(id)); //do we need to send the
});

//Edits User and sends it to frontend
app.put("/event/:id/edit", async (req, res) => {
    let { id } = req.params;
    let result = await eventStore.updateEvent(id, req.body, userStore);
    if (result == ERROR_CODES.SUCCESS){
        res.status(ERROR_CODES.SUCCESS).send(await eventStore.findEventByID(id)); //do we need to send the
    } else {
        res.status(result).send(await eventStore.findEventByID(id));
    }
});

//* Home Screen

// //Sends the user object and events the user is a part of for the home screen - get
// app.get("/user/:id/home", async (req, res) => {
//     let { id } = req.params;
//     let foundUser = await userStore.findUserByID(id);
//     if (foundUser == null) res.status(ERROR_CODES.NOTFOUND).send("No User Found");
//     else {
//         let events = await eventStore.findEventByIDList(foundUser.events);
//         res.status(ERROR_CODES.SUCCESS).send(events); //need to change this to event id, name and description only
//     }
// });

//Sends the user object for the profile page - get
app.get("/user/:id", async (req, res) => {
    let { id } = req.params;
    let response = await userStore.findUserByID(id);
    res.status(response.status).send(response.data)
});

//Get user list by name
app.get("/user/name/:userName", async (req, res) => {
    let { userName } = req.params;
    let foundUserList = await userStore.findUserByName(userName);
    if (foundUserList.length !== 0) {
        res.status(ERROR_CODES.SUCCESS).send(foundUserList);
    } else {
        res.status(ERROR_CODES.NOTFOUND).send(foundUserList);
    }
});

//Get event list by name
app.get("/event/title/:eventName", async (req, res) => {
    let { eventName } = req.params;
    let foundEventList = await eventStore.findEventsByName(eventName);
    if (foundEventList.length !== 0) {
        res.status(ERROR_CODES.SUCCESS).send(foundEventList);
    } else {
        res.status(ERROR_CODES.NOTFOUND).send(foundEventList);
    }
});

//Sends the list of friend requests of user - get
app.get("/user/:id/friendRequest", async (req, res) => {
    let { id } = req.params;
    let response = await userStore.findUserByID(id);
    if (response.status !== ERROR_CODES.SUCCESS) res.status(response.status).send([]);
    else {
        let friendsReqList = await userStore.findFriendByIDList(
            foundUser.friendRequest
        );
        res.status(ERROR_CODES.SUCCESS).send(friendsReqList);
    }
});

//Sends the list of friends of user - get
app.get("/user/:id/friends", async (req, res) => {
    let { id } = req.params;
    let response = await userStore.findUserByID(id);
    if (response.status !== ERROR_CODES.SUCCESS) res.status(response.status).send([]);
    else {
        let friendsList = await userStore.findFriendByIDList(foundUser.friends);
        res.status(ERROR_CODES.SUCCESS).send(friendsList);
    }
});

//* Chat info

//Chat list: Sends the list of chats the user is in - get
app.get("/user/:id/chat", async (req, res) => {
    let { id } = req.params;
    let chatList = await chatEngine.findChatByUser(id);
    res.status(ERROR_CODES.SUCCESS).send(chatList);
});

//Sends the list of chatInvites - get
app.get("/user/:id/chatInvites", async (req, res) => {
    let { id } = req.params;
    let response = await userStore.findUserByID(id);
    if (response.status !== ERROR_CODES.SUCCESS) res.status(response.status).send([]);
    else {
        let chatInviteList = await userStore.findChatInvites(
            foundUser.chatInvites,
            chatEngine
        );
        res.status(ERROR_CODES.SUCCESS).send(chatInviteList);
    }
});

//Chat: send message to a single user
app.put("/chat/sendChat/:userID/:chatID", async (req, res) => {
    console.log("hello");
    let { userID, chatID } = req.params;
    let { timeStamp, text } = req.body;
    let response = await userStore.findUserByID(userID);
    if(response.status !== ERROR_CODES.SUCCESS) return res.status(response.status).send("Unsuccesfull")
    let fromUserName = response.data.name;
    let updatedChat = await chatEngine.sendChatMessage(
        userID,
        chatID,
        text,
        fromUserName,
        timeStamp,
        userStore
    );


    // getMessaging().send({
    //     data: {
    //         name: fromUserName,
    //         text: text
    //     },
    //     topic: fromUserID + "_" + toUserID
    // }).then(((response) => console.log("Message sent: ", response))).catch((err) => console.log("Error: ", err))
    
    if(updatedChat)
        res.status(ERROR_CODES.SUCCESS).send("Successful");
    else
        res.status(ERROR_CODES.NOTFOUND).send("Unsuccessfull");
});

//Chat: send message to a single user
app.put("/chat/sendSingle/:fromUserID/:toUserID", async (req, res) => {
    console.log("hello");
    let { fromUserID, toUserID } = req.params;
    let { timeStamp, text } = req.body;
    fromUserName = await userStore.findUserByID(fromUserID).name;
    await chatEngine.sendMessage(
        fromUserID,
        toUserID,
        text,
        fromUserName,
        timeStamp
    );

    // getMessaging().send({
    //     data: {
    //         name: fromUserName,
    //         text: text
    //     },
    //     topic: fromUserID + "_" + toUserID
    // }).then(((response) => console.log("Message sent: ", response))).catch((err) => console.log("Error: ", err))

    res.status(ERROR_CODES.SUCCESS).send("Successful");
});

//Chat: send message to a group
app.put("/chat/sendGroup/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    let { timeStamp, text } = req.body;

    fromUserName = await userStore.findUserByID(userID).name;
    eventName = await eventStore.findEventByID(eventID).title;
    await chatEngine.sendGroupMessage(
        userID,
        eventID,
        text,
        fromUserName,
        timeStamp
    );

    // getMessaging().send({
    //     data: {
    //         name: fromUserName,
    //         text: text
    //     },
    //     topic: eventName
    // }).then(((response) => console.log("Message sent: ", response))).catch((err) => console.log("Error: ", err))

    res.status(ERROR_CODES.SUCCESS).send("Successful");
});

//Chat: Sends the chat object (which includes all the messages) - get
app.get("/chat/:id", async (req, res) => {
    let { id } = req.params;
    console.log("IN CHAT END POINT")
    console.log(id)
    let chat = await chatEngine.findChatByID(id);
    res.status(ERROR_CODES.SUCCESS).send(chat);
});

//* Event info

//Event list: Sends the list of Events the user is in - get
app.get("/user/:id/event", async (req, res) => {
    let { id } = req.params;
    let eventList = await eventStore.findEventByUser(id, userStore);
    res.status(ERROR_CODES.SUCCESS).send(eventList);
});

//Event list: Send list of Event
app.post("/event/filter", async (req, res) => {
    let eventList = await eventStore.findEventByDetails(req.body);
    res.status(ERROR_CODES.SUCCESS).send(eventList);
});

//Event: Sends the event object (for view event details?) - get
app.get("/event/:id", async (req, res) => {
    let { id } = req.params;
    let event = await eventStore.findEventByID(id);
    res.status(ERROR_CODES.SUCCESS).send(event);
});

//Event: Sends all events in the database
app.get("/event", async (req, res) => {
    let eventList = await eventStore.findAllEvents();
    res.status(ERROR_CODES.SUCCESS).send(eventList);
});

app.put("/chat/sendChatInvite/:chatID/:userID", async (req, res) => {
    let { userID, chatID } = req.params;
    let result = await userStore.sendChatInvite(userID, chatID, chatEngine);
    if(result == SUCCESS)
        res.status(ERROR_CODES.SUCCESS).send("Sucessful");
    else {
        console.log(result);
        res.status(result).send("Unsuccessfull");
    }
});

app.put("/user/sendFriendRequest/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    let result = await userStore.sendFriendRequest(userID, otherUserID);
    if(result == SUCCESS)
        res.status(ERROR_CODES.SUCCESS).send("Sucessful");
    else {
        console.log(result);
        res.status(result).send("Unsuccessfull");
    }
});

//* Accept or Reject requests (can be used for join as well) and invites
app.put("/user/acceptChat/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    let result = await userStore.acceptChatInvite(userID, chatID, chatEngine);
    if(result == SUCCESS)
        res.status(ERROR_CODES.SUCCESS).send("Sucessful");
    else res.status(result).send("Unsuccessfull");
});

app.put("/user/acceptEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    let result = await userStore.acceptEventInvite(userID, eventID, eventStore, chatEngine);
    if(result == SUCCESS)
        res.status(ERROR_CODES.SUCCESS).send("Sucessful");
    else res.status(result).send("Unsuccessfull");
});

app.put("/user/acceptUser/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    let result = await userStore.acceptFriendRequest(userID, otherUserID);
    if(result == SUCCESS)
        res.status(ERROR_CODES.SUCCESS).send("Sucessful");
    else res.status(result).send("Unsuccessfull");
});

app.put("/user/rejectChat/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    await userStore.rejectChatInvite(userID, chatID);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
});

app.put("/user/rejectEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    await userStore.rejectEventInvite(userID, eventID);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
});

app.put("/user/rejectUser/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    await userStore.rejectFriendRequest(userID, otherUserID);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
});

//* Remove friend or leave event and chat

app.put("/user/removeFriend/:userID/:otherUserID", async (req, res) => {
    let { userID, otherUserID } = req.params;
    await userStore.removeFriend(userID, otherUserID);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
});

app.put("/user/leaveEvent/:userID/:eventID", async (req, res) => {
    let { userID, eventID } = req.params;
    let event = eventStore.findEventByID(eventID);
    await userStore.leaveEvent(userID, eventID, eventStore);
    await userStore.leaveChat(userID, event.chat, chatEngine);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
});

app.put("/user/leaveChat/:userID/:chatID", async (req, res) => {
    let { userID, chatID } = req.params;
    await userStore.leaveChat(userID, chatID, chatEngine);
    res.status(ERROR_CODES.SUCCESS).send("Sucessful");
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
    res.status(ERROR_CODES.SUCCESS).send("Successful");
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
    res.status(ERROR_CODES.SUCCESS).send("Successful");
});

app.get("/reports", async (req, res) => {
    let reports = await reportService.viewAllReports();
    res.status(ERROR_CODES.SUCCESS).send(reports);
});

app.post("/user/:id/ban", async (req, res) => {
    let { id } = req.params;
    await banService.banUser(id, userStore);
    res.status(ERROR_CODES.SUCCESS).send("Successful");
});

app.get("/user", async(req, res) => {
    let userList = await userStore.findAllUsers();
    res.status(ERROR_CODES.SUCCESS).send(userList)
})

app.post("/event/:id/ban", async (req, res) => {
    let { id } = req.params;
    await banService.banEvent(id, eventStore);
    res.status(ERROR_CODES.SUCCESS).send("Successful");
});
