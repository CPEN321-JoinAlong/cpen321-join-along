const path = require("path");
const express = require("express");
const mongoose = require("mongoose");
// const admin = require("firebase-admin");
// const serviceAccount = require("/home/azureuser/serviceAccountKey.json");
const socketio = require("socket.io");
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
const ReportService = require("./modules/report_module/ReportService");
const BanService = require("./modules/ban_module/BanService");
const RecSystem = require("./modules/rec_module/RecSystem");
const distCalc = require("./DistanceCalc");

const ERROR_CODES = require("./ErrorCodes");

function logRequest(req, res, next) {
    console.log(`${new Date()}  ${req.ip} : ${req.method} ${req.path}`);
    next();
}

// mongoose.connect("mongodb://useradmin:MTnCBEI9nIx6L6F@localhost:34542/joinalong", {
// 	useNewUrlParser: true,
// 	useUnifiedTopology: true,
// });

mongoose.connect(
    "mongodb://useradmin:MTnCBEI9nIx6L6F@54.200.52.211:34542/joinalong",
    {
        useNewUrlParser: true,
        useUnifiedTopology: true,
    }
);

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
let recSystem = new RecSystem();

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

let server = app.listen(port, () => {
    console.log(
        `${new Date()}  App Started. Listening on ${host}:${port}, serving ${clientApp}`
    );
});

app.use(async (req, res, next) => {
    let token;
    if (Object.keys(req.body).length !== 0) token = req.body.token;
    else token = req.headers.token;
    try {
        let userResponse = await userStore.findUserForLogin(token);
        if (
            userResponse.status === ERROR_CODES.SUCCESS ||
            req.path.includes("/login") ||
            req.path.includes("/user/create") ||
            req.path == "/test"
        ) {
            next();
        } else {
            res.status(ERROR_CODES.NOTFOUND).send("Unsuccessful");
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

// //JUST FOR TESTING
// app.get("/test", async (req, res) => {
//     let a = {};
//     try {
//         // let impEvents = ["62e317ac77f7ad9a56ab886b", "62e48622b43f633a5a0e3860", "62e363a8e21c7698113dc974", "62db9dd9337ad5e08e29b562", "62dba10ae96413b41863b7c5", "62dba1bce96413b41863b7f2", "62d7ae2d010a82beb388b2ff", "62e8bd3d7080c5316bc7e32f", "62e8bd7d7080c5316bc7e33f", "62e98d597080c5316bc7e73e"]
//         // let chatIds = ["62e317ad77f7ad9a56ab886d", "62e48622b43f633a5a0e3862", "62e363a8e21c7698113dc976", "62db9dd9337ad5e08e29b564", "62dba10ae96413b41863b7c7", "62dba1bce96413b41863b7f4", "62d7ae2d010a82beb388b301", "62e8bd3d7080c5316bc7e331", "62e8bd7d7080c5316bc7e341", "62e98d597080c5316bc7e740"]
//         // // await Chat.deleteMany({title: "test"})
//         // // await Event.deleteMany({_id: {$nin: impEvents}})
//         // await User.updateMany({}, {$pull: {events: {$nin: impEvents}}})
//         // // await Chat.deleteMany({_id: {$nin: chatIds}})
//         // await User.updateMany({}, {$pull: {chats: {$nin: chatIds}}})

//         a["user"] = await User.find({});
//         a["chat"] = await Chat.find({});
//         a["event"] = await Event.find({});
//         a["report"] = await Report.find({});

//         // a["user"].forEach(async (user) => {
//         //     let coordinates = await distCalc(user.location);
//         //     await User.findByIdAndUpdate(user._id, { coordinates });
//         // });

//         // a["event"].forEach(async (event) => {
//         //     let coordinates = await distCalc(event.location);
//         //     await Event.findByIdAndUpdate(event._id, { coordinates });
//         // });

//         // a["user"] = await User.find({});
//         // a["event"] = await Event.find({});

//         res.status(ERROR_CODES.SUCCESS).send(a);
//     } catch (e) {
//         console.log(e);
//         res.status(ERROR_CODES.DBERROR).send(null);
//     }
// });

//login - post
app.post("/login", async (req, res) => {
    const Token = req.body.Token;
    try {
        let response = await axios(
            `https://oauth2.googleapis.com/tokeninfo?id_token=${Token}`
        );
        if (response.status === ERROR_CODES.SUCCESS) {
            let foundUserResponse = await userStore.findUserForLogin(
                response.data.sub
            );
            if (foundUserResponse.status !== ERROR_CODES.SUCCESS) {
                res.status(foundUserResponse.status).send({
                    token: response.data.sub,
                });
            } else
                res.status(foundUserResponse.status).send(
                    foundUserResponse.data
                );
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
    try {
        let userResponse = await userStore.createUser(userInfo);
        // console.log(userResponse);
        res.status(userResponse.status).send(userResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Creates a chat object and sends it to frontend
app.post("/chat/create", async (req, res) => {
    let chatObject = req.body;
    let chatInfo = new ChatDetails(chatObject);
    try {
        let user = await userStore.findUserForLogin(req.body.token);
        let newList = chatInfo.participants.filter(userId => userId != user.data._id)
        chatInfo.participants = [user.data._id]
        chatInfo.currCapacity = 1
        let chatResponse = await chatEngine.createChat(chatInfo, userStore);
        for (let userId of newList) {
            await userStore.sendChatInvite(userId, chatResponse.data._id, chatEngine)
        }
        res.status(chatResponse.status).send(chatResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Creates an event object (and a related chat object) and sends it to frontend
app.post("/event/create", async (req, res) => {
    let eventObject = req.body;
    let eventInfo = new EventDetails(eventObject);
    // console.log(eventInfo);
    try {
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
        let updatedEvent = await eventStore.updateEvent(
            eventResponse.data._id,
            eventResponse.data,
            userStore
        );
        let user = await userStore.findUserForLogin(req.headers.token);
        if (user.data) {
            updatedEvent.data = { ...(updatedEvent.data.toJSON()), distance: distCalc(user.data.coordinates, updatedEvent.data.coordinates) }
            res.status(updatedEvent.status).send(updatedEvent.data);
        } else {
            updatedEvent.data = { ...(updatedEvent.data.toJSON()), distance: -1 }
            res.status(updatedEvent.status).send(updatedEvent.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* All the edit paths for the main modules

//Edits User and sends it to frontend
app.put("/user/:id/edit", async (req, res) => {
    let id = req.params.id;
    try {
        let userResponse = await userStore.updateUserAccount(id, req.body);
        res.status(userResponse.status).send(userResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Edits Chat and sends it to frontend
app.put("/chat/:id/edit", async (req, res) => {
    let id = req.params.id;
    try {
        let chat = await chatEngine.findChatByID(id);
        if (chat.data == null) return res.status(chat.status).send(null)
        let newParticipants = req.body.participants.filter(userId => !chat.data.participants.includes(userId))
        req.body.participants = req.body.participants.filter(userId => chat.data.participants.includes(userId))
        let chatResponse = await chatEngine.editChat(id, req.body, userStore);
        for (let userId of newParticipants) {
            await userStore.sendChatInvite(userId, chatResponse.data._id, chatEngine)
        }
        res.status(chatResponse.status).send(chatResponse.data); //update the update function to send the new object
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Edits User and sends it to frontend
app.put("/event/:id/edit", async (req, res) => {
    let id = req.params.id;
    try {
        let eventResponse = await eventStore.updateEvent(
            id,
            req.body,
            userStore
        );
        console.log(eventResponse)
        console.log(req.body.token)
        let user = await userStore.findUserForLogin(req.body.token);
        console.log(user)
        if (user.data) {
            if (eventResponse.data) {
                eventResponse.data = { ...(eventResponse.data.toJSON()), distance: distCalc(user.data.coordinates, eventResponse.data.coordinates) }
            }
            res.status(eventResponse.status).send(eventResponse.data);
        } else {
            if (eventResponse.data) {
                eventResponse.data = { ...(eventResponse.data.toJSON()), distance: -1 }
            }
            res.status(eventResponse.status).send(eventResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Sends the user object for the profile page - get
app.get("/user/:id", async (req, res) => {
    let id = req.params.id;
    try {
        let response = await userStore.findUserByID(id);
        res.status(response.status).send(response.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Get user list by name
app.get("/user/name/:userName", async (req, res) => {
    let userName = req.params.userName;
    try {
        let userListReponse = await userStore.findUserByName(userName);
        res.status(userListReponse.status).send(userListReponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Get event list by name
app.get("/event/title/:eventName", async (req, res) => {
    let eventName = req.params.eventName;
    try {
        let eventListResponse = await eventStore.findEventsByName(eventName);
        let user = await userStore.findUserForLogin(req.headers.token);
        if (user.data) {
            eventListResponse.data = eventListResponse.data.map((event) => ({
                ...(event.toJSON()),
                distance: distCalc(user.data.coordinates, event.coordinates),
            }));
            res.status(eventListResponse.status).send(eventListResponse.data);
        } else {
            eventListResponse.data = eventListResponse.data.map((event) => ({
                ...(event.toJSON()),
                distance: -1,
            }));
            res.status(eventListResponse.status).send(eventListResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.get("/event/tag/:eventTag", async (req, res) => {
    let eventTag = req.params.eventTag;
    try {
        let eventListResponse = await eventStore.findAllEvents();
        eventListResponse.data = eventListResponse.data.filter(event => event.tags.includes(eventTag))
        let user = await userStore.findUserForLogin(req.headers.token);
        if (user.data) {
            eventListResponse.data = eventListResponse.data.map((event) => ({
                ...(event.toJSON()),
                distance: distCalc(user.data.coordinates, event.coordinates),
            }));
            res.status(eventListResponse.status).send(eventListResponse.data);
        } else {
            eventListResponse.data = eventListResponse.data.map((event) => ({
                ...(event.toJSON()),
                distance: -1,
            }));
            res.status(eventListResponse.status).send(eventListResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Sends the list of friend requests of user - get
app.get("/user/:id/friendRequest", async (req, res) => {
    let id = req.params.id;
    try {
        let userResponse = await userStore.findUserByID(id);
        if (userResponse.status !== ERROR_CODES.SUCCESS)
            res.status(userResponse.status).send([]);
        else {
            let friendListResponse = await userStore.findFriendByIDList(
                userResponse.data.friendRequest
            );
            res.status(friendListResponse.status).send(friendListResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Sends the list of friends of user - get
app.get("/user/:id/friends", async (req, res) => {
    let id = req.params.id;
    try {
        let userResponse = await userStore.findUserByID(id);
        if (userResponse.status !== ERROR_CODES.SUCCESS)
            res.status(userResponse.status).send([]);
        else {
            let friendListResponse = await userStore.findFriendByIDList(
                userResponse.data.friends
            );
            res.status(friendListResponse.status).send(friendListResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* Chat info

//Chat list: Sends the list of chats the user is in - get
app.get("/user/:id/chat", async (req, res) => {
    let id = req.params.id;
    try {
        // let userResponse = await userStore.findUserByID(id);
        // if (userResponse.status !== ERROR_CODES.SUCCESS)
        //     res.status(userResponse.status).send([]);
        // else {
        let chatListResponse = await chatEngine.findChatByUser(id);
        if (chatListResponse.status !== ERROR_CODES.SUCCESS)
            return res.status(chatListResponse.status).send([])
        for (let chat of chatListResponse.data) {
            let participants = []
            if (chat.participants.length > 1) {
                participants = chat.participants.filter(userId => userId != id);
            } else {
                participants = chat.participants
            }
            let userList = await userStore.findFriendByIDList(participants);
            if (userList.data) {
                chat._doc.images = userList.data.map(user => user.profilePicture)
            }
        }
        res.status(chatListResponse.status).send(chatListResponse.data);
        // }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Sends the list of chatInvites - get
app.get("/user/:id/chatInvites", async (req, res) => {
    let id = req.params.id;
    try {
        let userResponse = await userStore.findUserByID(id);
        if (userResponse.status !== ERROR_CODES.SUCCESS)
            res.status(userResponse.status).send([]);
        else {
            let chatInvResponse = await userStore.findChatInvites(
                userResponse.data.chatInvites,
                chatEngine
            );
            for (let chat of chatInvResponse.data) {
                let participants = []
                if (chat.participants.length > 1) {
                    participants = chat.participants.filter(userId => userId != id);
                } else {
                    participants = chat.participants
                }
                let userList = await userStore.findFriendByIDList(participants);
                if (userList.data) {
                    chat._doc.images = userList.data.map(user => user.profilePicture)
                }
            }
            res.status(chatInvResponse.status).send(chatInvResponse.data);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Chat: send message to a single user
app.put("/chat/sendChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID;
    let timeStamp = req.body.timeStamp;
    let text = req.body.text;

    try {
        let updatedChatResponse = await chatEngine.sendChatMessage(
            userID,
            chatID,
            text,
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

        res.status(updatedChatResponse.status).send(updatedChatResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Chat: Sends the chat object (which includes all the messages) - get
app.get("/chat/:id", async (req, res) => {
    let id = req.params.id;
    // console.log("IN CHAT END POINT");
    // console.log(id);
    try {
        let chatResponse = await chatEngine.findChatByID(id);
        res.status(chatResponse.status).send(chatResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* Event info

//Event list: Sends the list of Events the user is in - get
app.get("/user/:id/event", async (req, res) => {
    let id = req.params.id;
    try {
        let userResponse = await userStore.findUserByID(id);
        if (userResponse.status !== ERROR_CODES.SUCCESS)
            res.status(userResponse.status).send([]);
        else {
            let eventListResponse = await eventStore.findEventByUser(id);
            let sortedList = eventListResponse.data.sort(
                (a, b) =>
                    Date.parse(a.beginningDate) - Date.parse(b.beginningDate)
            );
            sortedList = sortedList.map((event) => ({
                ...(event.toJSON()),
                distance: distCalc(
                    userResponse.data.coordinates,
                    event.coordinates
                ),
            }));
            res.status(eventListResponse.status).send(sortedList);
        }
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Event: Sends the event object (for view event details?) - get
app.get("/event/:id", async (req, res) => {
    let id = req.params.id;
    try {
        let eventResponse = await eventStore.findEventByID(id);
        let user = await userStore.findUserForLogin(req.headers.token);
        if (eventResponse.data) {
            eventResponse.data = { ...(eventResponse.data.toJSON()), distance: distCalc(user.data.coordinates, eventResponse.data.coordinates) }
        }
        res.status(eventResponse.status).send(eventResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//Event: Sends all events in the database
app.get("/event", async (req, res) => {
    let token = req.headers.token
    try {
        let eventListResponse = await eventStore.findAllEvents();
        let sortedList = eventListResponse.data
            .sort((a, b) => Date.parse(a.beginningDate) - Date.parse(b.beginningDate))
            .filter(a => a.currCapacity !== 0);
        // console.log(sortedList)
        let user = await userStore.findUserForLogin(token);
        sortedList = sortedList.map((event) => ({
            ...(event.toJSON()),
            distance: distCalc(user.data.coordinates, event.coordinates),
        }));
        res.status(eventListResponse.status).send(sortedList);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/chat/sendChatInvite/:chatID/:userID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID;
    try {
        let chatInviteResponse = await userStore.sendChatInvite(
            userID,
            chatID,
            chatEngine
        );
        if (chatInviteResponse.status === ERROR_CODES.SUCCESS)
            res.status(chatInviteResponse.status).send("Successful");
        else res.status(chatInviteResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/sendFriendRequest/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID;
    let otherUserID = req.params.otherUserID;
    try {
        let friendReqResponse = await userStore.sendFriendRequest(
            userID,
            otherUserID
        );
        // if (friendReqResponse === ERROR_CODES.SUCCESS)
        res.status(friendReqResponse.status).send(friendReqResponse.data);
        // console.log(friendReqResponse);
        // else res.status(friendReqResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* Accept or Reject requests (can be used for join as well) and invites
app.put("/user/acceptChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID;
    try {
        let chatInviteResponse = await userStore.acceptChatInvite(
            userID,
            chatID,
            chatEngine
        );
        if (chatInviteResponse.status === ERROR_CODES.SUCCESS)
            res.status(chatInviteResponse.status).send("Successful");
        else res.status(chatInviteResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/acceptEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID;
    let eventID = req.params.eventID;
    try {
        let eventInvResponse = await userStore.acceptEventInvite(
            userID,
            eventID,
            eventStore,
            chatEngine
        );
        if (eventInvResponse.status === ERROR_CODES.SUCCESS)
            res.status(eventInvResponse.status).send("Successful");
        else res.status(eventInvResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/acceptUser/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID;
    let otherUserID = req.params.otherUserID;
    try {
        let friendReqResponse = await userStore.acceptFriendRequest(
            userID,
            otherUserID
        );
        if (friendReqResponse.status === ERROR_CODES.SUCCESS)
            res.status(friendReqResponse.status).send("Successful");
        else res.status(friendReqResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/rejectChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID;
    try {
        let chatInvResponse = await userStore.rejectChatInvite(userID, chatID);
        if (chatInvResponse.status === ERROR_CODES.SUCCESS)
            res.status(chatInvResponse.status).send("Successful");
        else res.status(chatInvResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/rejectEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID;
    let eventID = req.params.eventID;
    try {
        let eventInvResponse = await userStore.rejectEventInvite(
            userID,
            eventID
        );
        if (eventInvResponse.status === ERROR_CODES.SUCCESS)
            res.status(eventInvResponse.status).send("Successful");
        else res.status(eventInvResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/rejectUser/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID;
    let otherUserID = req.params.otherUserID;
    try {
        let friendReqResponse = await userStore.rejectFriendRequest(
            userID,
            otherUserID
        );
        if (friendReqResponse.status === ERROR_CODES.SUCCESS)
            res.status(friendReqResponse.status).send("Successful");
        else res.status(friendReqResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* Remove friend or leave event and chat

app.put("/user/removeFriend/:userID/:otherUserID", async (req, res) => {
    let userID = req.params.userID;
    let otherUserID = req.params.otherUserID;
    try {
        let friendResponse = await userStore.removeFriend(userID, otherUserID);
        if (friendResponse.status === ERROR_CODES.SUCCESS)
            res.status(friendResponse.status).send("Successful");
        else res.status(friendResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/leaveEvent/:userID/:eventID", async (req, res) => {
    let userID = req.params.userID;
    let eventID = req.params.eventID;
    let event = await eventStore.findEventByID(eventID);
    // console.log("IN THE ENDPOINT");
    // console.log(event);
    if (event.status !== ERROR_CODES.SUCCESS)
        return res.status(event.status).send("Event not found");
    // console.log("EVENT FOUND");
    try {
        let eventResponse = await userStore.leaveEvent(
            userID,
            eventID,
            eventStore
        );
        // console.log("THIS IS EVENT RESPONSE");
        // console.log(eventResponse);
        await userStore.leaveChat(userID, event.data.chat, chatEngine);
        if (eventResponse.status === ERROR_CODES.SUCCESS)
            res.status(eventResponse.status).send("Successful");
        else res.status(eventResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.put("/user/leaveChat/:userID/:chatID", async (req, res) => {
    let userID = req.params.userID;
    let chatID = req.params.chatID;
    try {
        let chatResponse = await userStore.leaveChat(
            userID,
            chatID,
            chatEngine
        );
        if (chatResponse.status === ERROR_CODES.SUCCESS)
            res.status(chatResponse.status).send("Successful");
        else res.status(chatResponse.status).send("Unsuccessful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

//* Report and Ban paths

app.post("/user/:reporterID/reportUser/:reportedID", async (req, res) => {
    let reporterID = req.params.reporterID;
    let reportedID = req.params.reportedID;
    let reason = req.body.reason;
    let description = req.body.description;
    let isEvent = 0;
    let isBlocked = req.body.isBlocked;
    try {
        let response = await reportService.report(
            reporterID,
            reportedID,
            reason,
            description,
            isEvent,
            isBlocked,
            userStore,
            eventStore
        );
        res.status(response.status).send(response.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.post("/user/:reporterID/reportEvent/:reportedID", async (req, res) => {
    let reporterID = req.params.reporterID;
    let reportedID = req.params.reportedID;
    let reason = req.body.reason;
    let description = req.body.description;
    let isEvent = 1;
    let isBlocked = req.body.isBlocked;
    try {
        let response = await reportService.report(
            reporterID,
            reportedID,
            reason,
            description,
            isEvent,
            isBlocked,
            userStore,
            eventStore
        );
        res.status(response.status).send(response.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.get("/reports", async (req, res) => {
    try {
        let reports = await reportService.viewAllReports();
        res.status(ERROR_CODES.SUCCESS).send(reports.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.post("/user/:id/ban", async (req, res) => {
    let id = req.params.id;
    try {
        let user = await userStore.findUserByID(id)
        if (user.status !== ERROR_CODES.SUCCESS)
            return res.status(user.status).send("User not found")

        for (let eventID of user.data.events)
            await userStore.leaveEvent(id, eventID, eventStore)

        for (let chatID of user.data.chats)
            await userStore.leaveChat(id, chatID, chatEngine)

        let response = await banService.banUser(id, userStore);
        res.status(response.status).send("Successful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.get("/user", async (req, res) => {
    try {
        let userListResponse = await userStore.findAllUsers();
        res.status(userListResponse.status).send(userListResponse.data);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.post("/event/:id/ban", async (req, res) => {
    let id = req.params.id;
    try {
        let response = await banService.banEvent(id, eventStore, userStore);
        res.status(response.status).send("Successful");
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

app.get("/chat", async (req, res) => {
    res.send(await Chat.find({}));
});

//*  Socket.io connection
const io = new socketio.Server(server);
//let io = socketio.listen(server)
io.on("connection", (socket) => {
    socket.on("join", (userName) => {
        console.log("Joined" + userName);
        socket.broadcast.emit("user", userName + "joined the chat");
    });

    //	socket.on('messageDetection', (userName, message) => {
    //		console.log("message" + userName + message)
    //		io.emit('message', { participantName: userName, text: message})
    //	})

    socket.on("messageDetection", async (userID, chatID, timeStamp, text) => {
        console.log(userID + " sent message: " + text);
        try {
            let updatedChatResponse = await chatEngine.sendChatMessage(
                userID,
                chatID,
                text,
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

            // res.status(updatedChatResponse.status).send(updatedChatResponse.data);
            io.emit(
                "message",
                updatedChatResponse.data.messages[
                updatedChatResponse.data.messages.length - 1
                ]
            );
        } catch (e) {
            console.log(e);
            // res.status(ERROR_CODES.DBERROR).send(null);
            io.emit("message", null);
        }
    });

    socket.on("disconnect", () => {
        socket.broadcast.emit("disconnected", "socket disconnected");
    });
});

app.get("/user/:id/recommendedEvents", async (req, res) => {
    let id = req.params.id;
    try {
        let response = await recSystem.recommendEvents(
            id,
            userStore,
            eventStore
        );
        let eventList = response.data;

        let sortedList = eventList.sort(
            (a, b) => Date.parse(a.beginningDate) - Date.parse(b.beginningDate)
        );

        let user = await userStore.findUserByID(id);
        if (!user.data) {
            return res.status(response.status).send([]);
        }

        sortedList = sortedList.map((event) => ({
            ...(event.toJSON()),
            distance: distCalc(user.data.coordinates, event.coordinates),
        }));

        res.status(response.status).send(sortedList);
    } catch (e) {
        console.log(e);
        res.status(ERROR_CODES.DBERROR).send(null);
    }
});

module.exports = { app, server };
