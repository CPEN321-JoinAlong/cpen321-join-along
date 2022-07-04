const path = require("path");
const express = require("express");
const mongoose = require("mongoose");
const User = require("./models/User");
const Event = require("./models/Event");
const Chat = require("./models/Chat");
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
    let foundUser = await User.find({token: Token})

    if(foundUser) {
        res.status(200).send({found: "true", id: `${foundUser._id}`});
    } else {
        res.status(404).send({found: "false", id: "null"});
    }
})

app.get("/user/:id", async (req, res) => {
    const { id } = req.params;
    let foundUser = await User.findById(id);
})

class UserStore {
    constructor() {
    }
        
    async findUserByProfile(userInfo) {
        return await User.find({name: userInfo.name});    
    }
        
    async findUserByID(userID) {
        return await User.findById(userID)
    }

    async updateUserAccount(userID, userInfo) {
        return await User.findByIdAndUpdate(userID, userInfo)
    } 

    async createUser(userInfo) {
        return await new User(userInfo).save()
    }

    async deleteUser(userID) {
    return await User.findByIdAndDelete(userID)
    }

    async findUserForLogin(Token) {
        return await User.find({token: Token})
    }
}

class UserAccount {

    constructor(userInfo) {
       this.name = userInfo.name;
       this.interests = userInfo.interestTags;
       this.events = userInfo.events;
       this.profileImage = userInfo.profileImage;
       this.description = userInfo.description;
       this.friends = userInfo.friends;
       this.blockedUsers = userInfo.blockedUsers;
       this.blockedEvents = userInfo.blockedEvents;
       this.token = userInfo.token;
    }

    findFriends(User) {
        return this.friends;
    }

    sendMessage(userID, message) {
        //TODO
    }

    notifyNewMessage(otherUser) {
        //TODO
    }

    createUserAccount(userInfo) {
        
        //Need to call UserStore's
    }

    findEvents(EventDetails) {

    }

}

class ChatDetails {
    constructor(chatInfo) {
       this.name = chatInfo.name;
       this.interests = chatInfo.interestTags;
       this.participants = chatInfo.participants;
       this.messages = chatInfo.messages;
       this.maxCapacity = chatInfo.maxCapacity;
       this.currCapacity = chatInfo.currCapacity;
       this.description = chatInfo.description;
    }
}

class ChatEngine {
    constructor() {}

    async sendMessage(userID, message) {
        
        //see if chat exists with userID, if not, create one
        //add message  to messages array
    }

    async sendGroupMessage(eventID, message) {
        let chatInfo = await Chat.find({event: eventID})
        if(chatInfo == null)
            return 
        chatInfo.messages.push(message)
        Chat.findByIdAndUpdate(chatInfo._id, chatInfo)
    }

    async createChat(chatInfo) {
        return await new Chat(chatInfo).save()
    }

    async editChat(chatInfo) {
        return await Chat.findByIdAndUpdate(chatInfo._id, chatInfo)
    }
}

class EventDetails {
    constructor(eventInfo) {
       this.name = chatInfo.name;
       this.interests = chatInfo.interestTags;
       this.participants = chatInfo.participants;
       this.messages = chatInfo.messages;
       this.maxCapacity = chatInfo.maxCapacity;
       this.currCapacity = chatInfo.currCapacity;
       this.description = chatInfo.description;
    }
}

class ChatEngine {
    constructor() {}

    async sendMessage(userID, message) {
        
        //see if chat exists with userID, if not, create one
        //add message  to messages array
    }

    async sendGroupMessage(eventID, message) {
        let chatInfo = await Chat.find({event: eventID})
        if(chatInfo == null)
            return 
        chatInfo.messages.push(message)
        Chat.findByIdAndUpdate(chatInfo._id, chatInfo)
    }

    async createChat(chatInfo) {
        return await new Chat(chatInfo).save()
    }

    async editChat(chatInfo) {
        return await Chat.findByIdAndUpdate(chatInfo._id, chatInfo)
    }
}