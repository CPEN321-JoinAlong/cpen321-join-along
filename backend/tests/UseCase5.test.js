const mongoose = require("mongoose");
const request = require("supertest");
const {
    app,
    server
} = require("./../server");

const UserAccount = require("./../modules/user_module/UserAccount");
const User = require("./../models/User");

const Event = require("./../models/Event");

const ChatDetails = require("./../modules/chat_module/ChatDetails");
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
    await Chat.deleteMany({title: "tester chater"})
    await User.deleteMany({name: "Rob Robber"})
    await User.deleteMany({name: "Bob Bobber"})
    await User.updateMany({token}, {
        chats: []
    })
    mongoose.connection.close();
    server.close();
    // done();
});

describe("User Case 5: Messaging", () => {
    //create chat, edit chat, view chat, send message, send chatinvite, accept chat, reject Chat, view Chat invites
    describe("create chat", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        let chatInfo2 = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            currCapacity: 1,
        });
        test("Success: chat created without participants", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo2,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(chatInfo);
        });
        test("Success: chat created", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(chatInfo);
        });
    });
    describe("edit chat", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        let updatedChatInfo = new ChatDetails({
            title: "tester chater 2",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("invalid chat ID", async () => {
            let response = await request(app)
                .put("/chat/dfjsdfks/edit")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("Chat with ID not found", async () => {
            let response = await request(app)
                .put("/chat/64d31ae677f7ad9a56ab89c6/edit")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: chat is edited", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            // console.log(response._body);
            expect(response._body).toMatchObject(chatInfo);

            let updatedResponse = await request(app)
                .put(`/chat/${id}/edit`)
                .send({
                    ...updatedChatInfo,
                    token
                });
            await Chat.findByIdAndDelete(id);
            expect(updatedResponse.status).toBe(ERROR_CODES.SUCCESS);
            let updatedChat = updatedResponse._body;
            delete updatedChat["_id"];
            delete updatedChat["__v"];
            expect(updatedChat).toMatchObject(updatedChatInfo);
        });
    });
    describe("view all chat invites", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("IDs of chat invites are invalid", async () => {
            userInfo.chatInvites = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatInvResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(chatInvResponse._body)).toBe(JSON.stringify([]));

            userInfo.chatInvites = [];
        });
        test("chats in chatinvites not found ", async () => {
            userInfo.chatInvites = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatInvResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(chatInvResponse._body)).toBe(JSON.stringify([]));

            userInfo.chatInvites = [];
        });
        test("Success: chatInvites viewed", async () => {
            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);


            userInfo.chatInvites = [chatId]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let chatInvResponse = await request(app)
                .get(`/user/${id}/chatInvites`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            ["_id", "__v"].forEach((key) => delete chatInvResponse._body[0][key]);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body[0]).toMatchObject(chatInfo);

            userInfo.chatInvites = [];
        });


    })
    describe("send chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token: "1234567890",
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: [],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/chat/sendChatInvite/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/chat/sendChatInvite/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("user or chat not found", async () => {
            let response = await request(app)
                .put("/chat/sendChatInvite/64d50cfb436fbc75c6549dbe/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("User already in chat", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token: userInfo.token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chats: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("Chatinvite was already sent before", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token: userInfo.token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            chatInfo.participants = [id];
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chatInvites: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
        })
        test("Success: Chat Invite sent", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);

            let chatInvResponse = await request(app)
                .put(`/chat/sendChatInvite/${chatId}/${id}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
            // await Chat.deleteMany({title: "Test Initial Title"})
            // await Event.deleteMany({title: "Test Initial Title"})
        })


    })
    describe("accept chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token: "1234567890",
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("IDs of user or event are invalid", async () => {
            let response = await request(app)
                .put("/user/acceptChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/user/acceptChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/user/acceptChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("User already in chat", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token: userInfo.token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chats: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("chat capacity at max", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.currCapacity = 6;

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token: userInfo.token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            chatInfo.participants = [id];
            chatInfo.currCapacity = 1;
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.currCapacity = 1
        })
        test("Success: chat invite accepted", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);

            let chatInvResponse = await request(app)
                .put(`/user/acceptChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
        })

    })
    describe("reject chat invite", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/user/rejectChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/user/rejectChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/user/rejectChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("No chatinvite sent", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let chatInvResponse = await request(app)
                .put(`/user/rejectChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(chatInvResponse._body).toBe(undefined);
            chatInfo.participants = []
        })
        test("Success: rejected chat Invite", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);

            await User.findByIdAndUpdate(id, {
                $push: {
                    chatInvites: chatId
                }
            });

            let chatInvResponse = await request(app)
                .put(`/user/rejectChat/${id}/${chatId}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(chatInvResponse._body).toBe(undefined);
        })
    })
    describe("send message", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("IDs of user or chat are invalid", async () => {
            let response = await request(app)
                .put("/chat/sendChat/sdfsdf/64c50cfb436fbc75c654d9eb")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);

            let response2 = await request(app)
                .put("/chat/sendChat/64c50cfb436fbc75c654d9eb/sdfsdf")
                .send({
                    token
                });
            expect(response2.status).toBe(ERROR_CODES.INVALID);
            expect(response2._body).toBe(undefined);
        })
        test("Chat nor User found", async () => {
            let response = await request(app)
                .put("/chat/sendChat/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: Message send", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);

            let timeStamp = "1659172077"
            let text = "Hello"
            let chatInvResponse = await request(app)
                .put(`/chat/sendChat/${id}/${chatId}`)
                .send({
                    token,
                    timeStamp,
                    text
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatInvResponse.status).toBe(ERROR_CODES.SUCCESS);
        })
    })
    describe("get chat info", () => {
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("Invalid chat ID", async () => {
            let response = await request(app)
                .get("/chat/dfjsdfks")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("Event with event ID not found", async () => {
            let response = await request(app)
                .get("/chat/64d31ae677f7ad9a56ab89c6")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: chat info successfully sent", async () => {
            let response = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token
                });
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(chatInfo);

            let chatResponse = await request(app)
                .get(`/chat/${id}`)
                .set({
                    token
                });
            await Chat.findByIdAndDelete(id);
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);
            expect(chatResponse._body).toMatchObject(chatInfo);
        })
    })
    describe("view chats which a user is in", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token: "1234567890",
        });
        let chatInfo = new ChatDetails({
            title: "tester chater",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            beginningDate: "2022-08-08T00:00:00.000Z",
            endDate: "2022-09-01T00:00:00.000Z",
            participants: ["62eae6dc6948e5255b2d2c43"],
            currCapacity: 1,
        });
        test("Invalid User ID", async () => {
            let response = await request(app)
                .get("/user/dfjsdfks/chat")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toEqual([]);
        })
        test("No chats found", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let chatResponse = await request(app)
                .get(`/user/${id}/chat`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(chatResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(chatResponse._body).toEqual([]);
        })
        test("Success: Chat found", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            chatInfo.participants = [id];

            let chatResponse = await request(app)
                .post("/chat/create")
                .send({
                    ...chatInfo,
                    token: userInfo.token
                });
            expect(chatResponse.status).toBe(ERROR_CODES.SUCCESS);
            let chatId = chatResponse._body._id;
            await Chat.findByIdAndDelete(id);
            ["_id", "__v"].forEach((key) => delete chatResponse._body[key]);

            let chatListResponse = await request(app)
                .get(`/user/${id}/chat`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            await Chat.findByIdAndDelete(chatId);
            expect(chatListResponse.status).toBe(ERROR_CODES.SUCCESS)

        })
        
    })

})