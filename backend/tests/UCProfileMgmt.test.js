const mongoose = require("mongoose");
const request = require("supertest");
const {
    app,
    server
} = require("../server");

const UserAccount = require("../modules/user_module/UserAccount");
const User = require("../models/User");

const Event = require("../models/Event");

const Chat = require("../models/Chat");

const ERROR_CODES = require("../ErrorCodes.js");

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
    await User.deleteMany({name: "Rob Robber"})
    await User.deleteMany({name: "Bob Bobber"})
    mongoose.connection.close();
    server.close();
    // done();
});

describe("User Case 6: Profile Management", () => {
    describe("create user", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Success: user created", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);
            await User.findByIdAndDelete(id);
        });

    });

    describe("edit user", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let updatedUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("Invalid user ID", async () => {
            let response = await request(app)
                .put("/user/dfjsdfks/edit")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("User with ID not found", async () => {
            let response = await request(app)
                .put("/user/64d31ae677f7ad9a56ab89c6/edit")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: user is edited", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let updatedResponse = await request(app)
                .put(`/user/${id}/edit`)
                .send(updatedUserInfo);
            await User.findByIdAndDelete(id);
            expect(updatedResponse.status).toBe(ERROR_CODES.SUCCESS);
            let updatedUser = updatedResponse._body;
            delete updatedUser["_id"];
            delete updatedUser["__v"];
            expect(updatedUser).toMatchObject(updatedUserInfo);
        });
    });

    describe("view user profile", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("Invalid user ID", async () => {
            let response = await request(app)
                .get("/user/dfjsdfks")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        });
        test("User with user ID not found", async () => {
            let response = await request(app)
                .get("/user/64d31ae677f7ad9a56ab89c6")
                .set({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        });
        test("Success: user is viewed", async () => {
            let response = await request(app)
                .post("/user/create")
                .send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let foundUser = await request(app)
                .get(`/user/${id}`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
            ["_id", "__v"].forEach((key) => delete foundUser._body[key]);
            expect(foundUser._body).toMatchObject(userInfo);
        });
    });
    describe("view all friend requests", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("IDs of user is invalid", async () => {
            let friendResponse = await request(app)
                .get(`/user/fssx/friendRequest`)
                .set({
                    token
                });
            expect(friendResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));
        });

        test("IDs of friend requests are invalid", async () => {
            userInfo.friendRequest = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendReqResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendReqResponse._body)).toBe(JSON.stringify([]));

            userInfo.friendRequest = [];
        });

        test("No friend requests for user found", async () => {
            userInfo.friendRequest = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendReqResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(friendReqResponse._body)).toBe(JSON.stringify([]));

            userInfo.friendRequest = [];
        });

        test("Success: list of friend requests of user viewed", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendReqResponse = await request(app)
                .get(`/user/${id}/friendRequest`)
                .set({
                    token
                });

            await User.findByIdAndDelete(id)
            await User.findByIdAndDelete(otherUserID)

            let finalReq = friendReqResponse._body;

            ["_id", "__v"].forEach((key) => delete finalReq[0][key]);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(finalReq[0]).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [];
        });
    })
    describe("view all friends", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });

        test("IDs of user is invalid", async () => {
            let friendResponse = await request(app)
                .get(`/user/fssx/friends`)
                .set({
                    token
                });
            expect(friendResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));
        });

        test("IDs of friend are invalid", async () => {
            userInfo.friends = ["sdflkjsdf"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendResponse.status).toBe(ERROR_CODES.INVALID);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));

            userInfo.friends = [];
        });

        test("No friend for user found", async () => {
            userInfo.friends = ["64d31ae677f7ad9a56ab89c6"]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });
            await User.findByIdAndDelete(id);
            expect(friendResponse.status).toBe(ERROR_CODES.NOTFOUND);
            expect(JSON.stringify(friendResponse._body)).toBe(JSON.stringify([]));

            userInfo.friends = [];
        });

        test("Success: list of friend of user viewed", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friends = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);


            let friendResponse = await request(app)
                .get(`/user/${id}/friends`)
                .set({
                    token
                });

            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);

            let final = friendResponse._body;

            ["_id", "__v"].forEach((key) => delete final[0][key]);
            expect(friendResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(final[0]).toMatchObject(otherUserInfo);

            userInfo.friends = [];
        });
    })
    describe("send friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/sendFriendRequest/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/sendFriendRequest/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Users already friends", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("friend request already sent before", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friendRequest = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friendRequest = []
        })
        test("Success: friend request send", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/sendFriendRequest/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("accept friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/acceptUser/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/acceptUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Users already friends", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/acceptUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("Success: friend request accepted", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);


            let friendReqResponse = await request(app)
                .put(`/user/acceptUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("reject friend request", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/rejectUser/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/rejectUser/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("No friend request present", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            let friendReqResponse = await request(app)
                .put(`/user/rejectUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.CONFLICT);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
        test("Success: friend request rejected", async () => {
            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            userInfo.friendRequest = [otherUserID]

            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            let friendReqResponse = await request(app)
                .put(`/user/rejectUser/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
        })
    })
    describe("remove friend", () => {
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        let otherUserInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "4223 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            token,
        });
        test("IDs of user or otherUser are invalid", async () => {
            let response = await request(app)
                .put("/user/removeFriend/sdfsdf/sdfdf")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.INVALID);
            expect(response._body).toBe(undefined);
        })
        test("No users found", async () => {
            let response = await request(app)
                .put("/user/removeFriend/64d31ae677f7ad9a56ab89c6/62d31ae677f7bc9a56ab49c6")
                .send({
                    token
                });
            expect(response.status).toBe(ERROR_CODES.NOTFOUND);
            expect(response._body).toBe(undefined);
        })
        test("Success: friend removed", async () => {
            let response = await request(app).post("/user/create").send(userInfo);
            expect(response.status).toBe(ERROR_CODES.SUCCESS);
            let id = response._body._id;
            ["_id", "__v"].forEach((key) => delete response._body[key]);
            expect(response._body).toMatchObject(userInfo);

            otherUserInfo.friends = [id];

            let otherResponse = await request(app).post("/user/create").send(otherUserInfo);
            expect(otherResponse.status).toBe(ERROR_CODES.SUCCESS);
            let otherUserID = otherResponse._body._id;
            ["_id", "__v"].forEach((key) => delete otherResponse._body[key]);
            expect(otherResponse._body).toMatchObject(otherUserInfo);

            await User.findByIdAndUpdate(id, {
                $push: {
                    friends: otherUserID
                }
            });

            let friendReqResponse = await request(app)
                .put(`/user/removeFriend/${id}/${otherUserID}`)
                .send({
                    token
                });
            await User.findByIdAndDelete(id);
            await User.findByIdAndDelete(otherUserID);
            expect(friendReqResponse.status).toBe(ERROR_CODES.SUCCESS);
            expect(friendReqResponse._body).toBe(undefined);
            otherUserInfo.friends = []
        })
    })
});
