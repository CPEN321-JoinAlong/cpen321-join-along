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
const ERROR_CODES = require("./ErrorCodes")

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
    let userResponse = await userStore.findUserForLogin(token);
    if (
        userResponse.status === ERROR_CODES.SUCCESS ||
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
    const Token = req.body.Token
    try {
        let response = await axios(
            `https://oauth2.googleapis.com/tokeninfo?id_token=${Token}`
        );
        if (response.status === ERROR_CODES.SUCCESS) {
            let foundUserResponse = await userStore.findUserForLogin(response.data.sub);
            if (foundUserResponse.status !== ERROR_CODES.SUCCESS)
                res.status(foundUserResponse.status).send({ token: response.data.sub });
            else res.status(foundUserResponse.status).send(foundUserResponse.data);
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
    let userResponse = await userInfo.createUserAccount(userStore);
    console.log(userResponse);
    res.status(userResponse.status).send(userResponse.data);
});

//Creates a chat object and sends it to frontend
app.post("/chat/create", async (req, res) => {
    let chatObject = req.body;
    let chatInfo = new ChatDetails(chatObject);
    let chatResponse = await chatEngine.createChat(chatInfo, userStore);
    console.log(chatResponse);
    res.status(chatResponse.status).send(chatResponse.data);
});

//Creates an event object (and a related chat object) and sends it to frontend
app.post("/event/create", async (req, res) => {
    let eventObject = req.body;
    let eventInfo = new EventDetails(eventObject);
    console.log(eventInfo);
    let eventResponse = await eventStore.createEvent(eventInfo, userStore);
    let chatResponse = await chatEngine.createChat(
        new ChatDetails({
            title: eventResponse.data.title,
            tags: eventResponse.data.tags,
            numberOfPeople: eventResponse.data.numberOfPeople,
            participants: eventResponse.data.participants,
            description: eventResponse.data.description,
            event: eventResponse.data._id,
        }),
        userStore
    );
    eventResponse.data.chat = chatResponse.data._id;
    let updatedEvent = await eventStore.updateEvent(eventResponse.data._id, eventResponse.data, userStore);
    res.status(updatedEvent.status).send(updatedEvent.data);
});

//* All the edit paths for the main modules

//Edits User and sends it to frontend
app.put("/user/:id/edit", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.updateUserAccount(id, req.body)
    res.status(userResponse.status).send(userResponse.data); 
});

//Edits Chat and sends it to frontend
app.put("/chat/:id/edit", async (req, res) => {
    let id = req.params.id;
    let chatResponse = await chatEngine.editChat(id, req.body, userStore);
    res.status(chatResponse.status).send(chatResponse.data); //update the update function to send the new object
});

//Edits User and sends it to frontend
app.put("/event/:id/edit", async (req, res) => {
    let id = req.params.id;
    let eventResponse = await eventStore.updateEvent(id, req.body, userStore);
    res.status(eventResponse.status).send(eventResponse.data) //update the update func to send the new object
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
    let id = req.params.id;
    let response = await userStore.findUserByID(id);
    res.status(response.status).send(response.data)
});

//Get user list by name
app.get("/user/name/:userName", async (req, res) => {
    let userName = req.params.userName;
    let userListReponse = await userStore.findUserByName(userName)
    res.status(userListReponse.status).send(userListReponse.data)
});

//Get event list by name
app.get("/event/title/:eventName", async (req, res) => {
    let eventName = req.params.eventName;
    let eventListResponse = await eventStore.findEventsByName(eventName)
    res.status(eventListResponse.status).send(eventListResponse.data)
});

//Sends the list of friend requests of user - get
app.get("/user/:id/friendRequest", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.findUserByID(id);
    if (userResponse.status !== ERROR_CODES.SUCCESS) res.status(userResponse.status).send([]);
    else {
        let friendListResponse = await userStore.findFriendByIDList(userResponse.data.friendRequest);
        res.status(friendListResponse.status).send(friendListResponse.data);
    }
});

//Sends the list of friends of user - get
app.get("/user/:id/friends", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.findUserByID(id);
    if (userResponse.status !== ERROR_CODES.SUCCESS) res.status(userResponse.status).send([]);
    else {
        let friendListResponse = await userStore.findFriendByIDList(userResponse.data.friends);
        res.status(friendListResponse.status).send(friendListResponse.data);
    }
});

//* Chat info

//Chat list: Sends the list of chats the user is in - get
app.get("/user/:id/chat", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.findUserByID(id);
    if (userResponse.status !== ERROR_CODES.SUCCESS) res.status(userResponse.status).send([]);
    else {
        let chatListResponse = await chatEngine.findChatByUser(id);
        res.status(chatListResponse.status).send(chatListResponse.data);
    }
});

//Sends the list of chatInvites - get
app.get("/user/:id/chatInvites", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.findUserByID(id);
    if (userResponse.status !== ERROR_CODES.SUCCESS) res.status(userResponse.status).send([]);
    else {
        let chatInvResponse = await userStore.findChatInvites(foundUser.chatInvites, chatEngine);
        res.status(chatInvResponse.status).send(chatInvResponse.data);
    }
});

//Chat: send message to a single user
app.put("/chat/sendChat/:userID/:chatID", async (req, res) => {
    console.log("hello");
    let userID = req.params.userID
    let chatID = req.params.chatID
    let timeStamp = req.body.timeStamp
    let text = req.body.text

    let userResponse = await userStore.findUserByID(userID);
    if (userResponse.status !== ERROR_CODES.SUCCESS) return res.status(userResponse.status).send("Unsuccesfull")
    let fromUserName = userResponse.data.name;
    let updatedChatResponse = await chatEngine.sendChatMessage(
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
    
    res.status(updatedChatResponse.status).send(updatedChatResponse.data)
    
});



//Chat: Sends the chat object (which includes all the messages) - get
app.get("/chat/:id", async (req, res) => {
    let id = req.params.id;
    console.log("IN CHAT END POINT")
    console.log(id)
    let chatResponse = await chatEngine.findChatByID(id);
    res.status(chatResponse.status).send(chatResponse.data);
});

//* Event info

//Event list: Sends the list of Events the user is in - get
app.get("/user/:id/event", async (req, res) => {
    let id = req.params.id;
    let userResponse = await userStore.findUserByID(id);
    if (userResponse.status !== ERROR_CODES.SUCCESS) res.status(userResponse.status).send([]);
    else {
        let eventListResponse = await eventStore.findEventByUser(id)
        res.status(eventListResponse.status).send(eventListResponse.data)
    }
});

//Event list: Send list of Event
// app.post("/event/filter", async (req, res) => {
//     let eventList = await eventStore.findEventByDetails(req.body);
//     res.status(ERROR_CODES.SUCCESS).send(eventList);
// });

//Event: Sends the event object (for view event details?) - get
app.get("/event/:id", async (req, res) => {
    let id = req.params.id
    let eventResponse = await eventStore.findEventByID(id)
    res.status(eventResponse.status).send(eventResponse.data)
});

//Event: Sends all events in the database
app.get("/event", async (req, res) => {
    let eventListResponse = await eventStore.findAllEvents();
    res.status(eventListResponse.status).send(eventListResponse.data);
});

app.put("/chat/sendChatInvite/:chatID/:userID", async (req, res) => {
    let userID = req.params.userID
    let chatID = req.params.chatID
    let chatInviteResponse = await userStore.sendChatInvite(userID, chatID, chatEngine);
    if(chatInviteResponse.status === ERROR_CODES.SUCCESS) res.status(chatInviteResponse.status).send("Successful");
    else res.status(chatInviteResponse.status).send("Unsuccessful");

});

app.put("/user/sendFriendRequest/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID
    let otherUserID = req.params.otherUserID
    let friendReqResponse = await userStore.sendFriendRequest(userID, otherUserID);
    if(friendReqResponse == ERROR_CODES.SUCCESS) res.status(friendReqResponse.status).send("Successful");
    else res.status(friendReqResponse.status).send("Unsuccessful");
});

//* Accept or Reject requests (can be used for join as well) and invites
app.put("/user/acceptChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID
    let chatID = req.params.chatID
    let chatInviteResponse = await userStore.acceptChatInvite(userID, chatID, chatEngine);
    if(chatInviteResponse.status === ERROR_CODES.SUCCESS) res.status(chatInviteResponse.status).send("Successful");
    else res.status(chatInviteResponse.status).send("Unsuccessful");
});

app.put("/user/acceptEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID
    let eventID = req.params.eventID
    let eventInvResponse = await userStore.acceptEventInvite(userID, eventID, eventStore, chatEngine);
    if(eventInvResponse.status === ERROR_CODES.SUCCESS) res.status(eventInvResponse.status).send("Successful");
    else res.status(eventInvResponse.status).send("Unsuccessful");
});

app.put("/user/acceptUser/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID
    let otherUserID = req.params.otherUserID
    let friendReqResponse = await userStore.acceptFriendRequest(userID, otherUserID);
    if(friendReqResponse.status === ERROR_CODES.SUCCESS) res.status(friendReqResponse.status).send("Successful");
    else res.status(friendReqResponse.status).send("Unsuccessful");
});

app.put("/user/rejectChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID
    let chatID = req.params.chatID
    let chatInvResponse = await userStore.rejectChatInvite(userID, chatID)
    if(chatInvResponse.status === ERROR_CODES.SUCCESS) res.status(chatInvResponse.status).send("Successful")
    else res.status(chatInvResponse.status).send("Unsuccessful")
});

app.put("/user/rejectEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID
    let eventID = req.params.eventID
    let eventInvResponse = await userStore.rejectEventInvite(userID, eventID);
    if(eventInvResponse.status === ERROR_CODES.SUCCESS) res.status(eventInvResponse.status).send("Successful");
    else res.status(eventInvResponse.status).send("Unsuccessful");
});

app.put("/user/rejectUser/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID
    let otherUserID = req.params.otherUserID
    let friendReqResponse = await userStore.rejectFriendRequest(userID, otherUserID);
    if(friendReqResponse.status === ERROR_CODES.SUCCESS) res.status(friendReqResponse.status).send("Successful");
    else res.status(friendReqResponse.status).send("Unsuccessful");
});

//* Remove friend or leave event and chat

app.put("/user/removeFriend/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID
    let otherUserID = req.params.otherUserID
    let friendResponse = await userStore.removeFriend(userID, otherUserID);
    if(friendResponse.status === ERROR_CODES.SUCCESS) res.status(friendResponse.status).send("Successful");
    else res.status(friendResponse.status).send("Unsuccessful");
});

app.put("/user/leaveEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID
    let eventID = req.params.eventID
    let event = eventStore.findEventByID(eventID);
    if(event.status !== ERROR_CODES.SUCCESS) return res.status(event.status).send("Event not found")
    let eventResponse = await userStore.leaveEvent(userID, eventID, eventStore);
    await userStore.leaveChat(userID, event.chat, chatEngine);
    if(eventResponse.status === ERROR_CODES.SUCCESS) res.status(eventResponse.status).send("Successful");
    else res.status(eventResponse.status).send("Unsuccessful");
});

app.put("/user/leaveChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID
    let chatResponse = await userStore.leaveChat(userID, chatID, chatEngine);
    if(chatResponse.status === ERROR_CODES.SUCCESS) res.status(chatResponse.status).send("Successful");
    else res.status(chatResponse.status).send("Unsuccessful");
});

//* Report and Ban paths

app.post("/user/:reporterID/reportUser/:reportedID", async (req, res) => {
    let reporterID = req.params.reporterID
    let reportedID = req.params.reportedID
    let reason = req.body.reason
    let isBlocked = req.body.isBlocked
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
    let reporterID = req.params.reporterID
    let reportedID = req.params.reportedID
    let reason = req.body.reason
    let isBlocked = req.body.isBlocked
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
    let id = req.params.id;
    await banService.banUser(id, userStore);
    res.status(ERROR_CODES.SUCCESS).send("Successful");
});

app.get("/user", async(req, res) => {
    let userListResponse = await userStore.findAllUsers();
    res.status(userListResponse.status).send(userListResponse.data)
})

app.post("/event/:id/ban", async (req, res) => {
    let id = req.params.id;
    await banService.banEvent(id, eventStore);
    res.status(ERROR_CODES.SUCCESS).send("Successful");
});
