const UserAccount = require("./../modules/user_module/UserAccount");
const UserStore = require("./../modules/user_module/UserStore");
const User = require("./../models/User");

const EventDetails = require("./../modules/event_module/EventDetails");
const EventStore = require("./../modules/event_module/EventStore");

const ChatDetails = require("./../modules/chat_module/ChatDetails");
const ChatEngine = require("./../modules/chat_module/ChatEngine");

const ERROR_CODES = require("./../ErrorCodes.js");
const ResponseObject = require("./../ResponseObject");

jest.mock("./../models/User");
jest.mock("./../modules/chat_module/ChatEngine"); //automatically creates mocks of all methods in the class
jest.mock("./../modules/event_module/EventStore");

afterEach(() => {
    User.findById.mockReset();
    User.find.mockReset();
});

describe("find user by ID", () => {
    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let foundUser = await userStore.findUserByID("dsfjskfsd");
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("User with ID not found", async () => {
        const userStore = new UserStore();
        User.findById.mockResolvedValue(undefined);
        let foundUser = await userStore.findUserByID(
            "62d50cfb436fbc75c258d9eb"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: user with given ID found", async () => {
        const userStore = new UserStore();
        let userInfo = new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            eventInvites: [],
            chatInvites: [],
            events: [],
            chats: [],
            friends: [],
            blockedUsers: [],
            blockedEvents: [],
            token: "324242343212",
        });
        User.findById.mockResolvedValue(userInfo);
        let foundUser = await userStore.findUserByID(
            "62d50cfb436fbc75c258d9eb"
        );
        // console.log(foundEvent)
        expect(JSON.stringify(foundUser.data)).toBe(JSON.stringify(userInfo));
        expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("find all users", () => {
    test("No users found", async () => {
        const userStore = new UserStore();
        User.find.mockResolvedValue([]);
        let foundUserList = await userStore.findAllUsers();
        // console.log(foundEvent)
        expect(JSON.stringify(foundUserList.data)).toBe(JSON.stringify([]));
        expect(foundUserList.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: users found", async () => {
        const userStore = new UserStore();
        let userList = [
            new UserAccount({
                name: "Bob Bobber",
                interests: ["Hiking"],
                location: "2423 Toronto Mall, Montreal",
                description: "Test description",
                profilePicture: "picture",
                eventInvites: [],
                chatInvites: [],
                events: [],
                chats: [],
                friends: [],
                friendRequest: [],
                blockedUsers: [],
                blockedEvents: [],
                token: "324242343212",
            }),
            new UserAccount({
                name: "Rob Robber",
                interests: ["Swimming"],
                location: "2423 Montreal Mall, Vancouver",
                description: "Test description",
                profilePicture: "picture",
            }),
        ];
        User.find.mockResolvedValue(userList);
        let foundUserList = await userStore.findAllUsers();
        // console.log(foundEvent)
        expect(JSON.stringify(foundUserList.data)).toBe(
            JSON.stringify(userList)
        );
        expect(foundUserList.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("update user account", () => {
    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let userInfo = {
            name: "Rob Robber",
        };
        let foundUser = await userStore.updateUserAccount(
            "sdfsd0cfb436fbc",
            userInfo
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("User with ID not found", async () => {
        const userStore = new UserStore();
        let userInfo = {
            name: "Rob Robber",
        };
        User.findByIdAndUpdate.mockResolvedValue(undefined);
        let foundUser = await userStore.updateUserAccount(
            "62d50cfb436fbc75c258d9eb",
            userInfo
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });
});

test("Success: user with given ID found and updated", async () => {
    const userStore = new UserStore();
    let userInfo = {
        name: "Rob Robber",
    };
    let userResult = new UserAccount({
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
    });
    User.findByIdAndUpdate.mockResolvedValue(userResult);
    let foundUser = await userStore.updateUserAccount(
        "62d50cfb436fbc75c258d9eb",
        userInfo
    );
    // console.log(foundEvent)
    expect(JSON.stringify(foundUser.data)).toBe(JSON.stringify(userResult));
    expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
});

describe("create user", () => {
    test("Success: user created", async () => {
        const userStore = new UserStore();
        let userInfo = new UserAccount({
            name: "Rob Robber",
            isAdmin: true,
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            coordinates: "90.88,76.10"
        });
        let result = new User(userInfo);
        result.save.mockResolvedValue(userInfo);
        let createdUser = await userStore.createUser(userInfo);
        expect(JSON.stringify(createdUser.data)).toBe(JSON.stringify(userInfo));
        expect(createdUser.status).toBe(ERROR_CODES.SUCCESS)
    });
});

describe("find friends from ID list", () => {
    test("Invalid ID in list", async () => {
        const userStore = new UserStore();
        let foundUser = await userStore.findFriendByIDList([
            "62d50cfb436fbc75c258d9eb",
            "dsfjskfsd",
        ]);
        // console.log(foundEvent)
        expect(JSON.stringify(foundUser.data)).toBe(JSON.stringify([]));
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("No friends with IDs from list found", async () => {
        const userStore = new UserStore();
        User.find.mockResolvedValue([]);
        let foundUser = await userStore.findFriendByIDList([
            "62d50cfb436fbc75c258d9eb",
            "62d50cfb436fbc75c258d9eb",
        ]);
        // console.log(foundEvent)
        expect(JSON.stringify(foundUser.data)).toBe(JSON.stringify([]));
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: friends with given IDs found", async () => {
        const userStore = new UserStore();
        let userResultList = [
            new UserAccount({
                name: "Bob Bobber",
                interests: ["Hiking"],
                location: "2423 Toronto Mall, Montreal",
                description: "Test description",
                profilePicture: "picture",
                eventInvites: [],
                chatInvites: [],
                events: [],
                chats: [],
                friends: [],
                friendRequest: [],
                blockedUsers: [],
                blockedEvents: [],
                token: "324242343212",
            }),
            new UserAccount({
                name: "Rob Robber",
                interests: ["Swimming"],
                location: "2423 Montreal Mall, Vancouver",
                description: "Test description",
                profilePicture: "picture",
            }),
        ];
        User.find.mockResolvedValue(userResultList);
        let foundUser = await userStore.findFriendByIDList([
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
        ]);
        // console.log(foundEvent)
        expect(JSON.stringify(foundUser.data)).toBe(
            JSON.stringify(userResultList)
        );
        expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("find chat invites", () => {
    test("Invalid ID in list", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let foundChatList = await userStore.findChatInvites(
            ["62d50cfb436fbc75c258d9eb", "dsfjskfsd"],
            chatEngine
        );
        // console.log(foundEvent)
        expect(JSON.stringify(foundChatList.data)).toBe(JSON.stringify([]));
        expect(foundChatList.status).toBe(ERROR_CODES.INVALID);
    });

    test("No chat invites found", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        // User.find.mockResolvedValue([])
        chatEngine.findChatByIDList.mockResolvedValue(
            new ResponseObject(ERROR_CODES.NOTFOUND, [])
        );
        let foundChatList = await userStore.findChatInvites(
            ["62d50cfb436fbc75c258d9eb", "62d50cfb436fbc75c258d9eb"],
            chatEngine
        );
        // console.log(foundEvent)
        expect(JSON.stringify(foundChatList.data)).toBe(JSON.stringify([]));
        expect(foundChatList.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: chat invites found", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let chatList = [
            new ChatDetails({
                title: "test chat",
                tags: ["Hiking"],
                numberOfPeople: 6,
                description: "test description",
                participants: [],
                messages: [],
                event: [],
            }),
        ];
        // User.find.mockResolvedValue([])
        chatEngine.findChatByIDList.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatList)
        );
        let foundChatList = await userStore.findChatInvites(
            ["62d50cfb436fbc75c258d9eb", "62d50cfb436fbc75c258d9eb"],
            chatEngine
        );
        // console.log(foundEvent)
        expect(JSON.stringify(foundChatList.data)).toBe(
            JSON.stringify(chatList)
        );
        expect(foundChatList.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("accept chat invite", () => {
    test("Invalid chat ID", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let foundUser = await userStore.acceptChatInvite(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let foundUser = await userStore.acceptChatInvite(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("No chat with given ID found", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
        });
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.NOTFOUND)
        );
        let foundUser = await userStore.acceptChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No user with given ID found", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let chatInfo = new ChatDetails({
            title: "test chat",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: [],
            messages: [],
            event: [],
        });
        User.findById.mockResolvedValue(undefined);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let foundUser = await userStore.acceptChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Chat is at maximum capacity", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            chats: [],
        });
        let chatInfo = new ChatDetails({
            title: "test chat",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: [],
            messages: [],
            event: [],
            currCapacity: 6,
        });
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let foundUser = await userStore.acceptChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.CONFLICT);
    });

    test("Chat already includes user", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            chats: ["64c50cfb436fbc75c648d9eb"],
        });
        let chatInfo = new ChatDetails({
            title: "test chat",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: ["62d50cfb436fbc75c258d9eb"],
            messages: [],
            event: [],
            currCapacity: 1,
        });
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let foundUser = await userStore.acceptChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.CONFLICT);
    });

    test("Success: user accepts chat invite", async () => {
        const userStore = new UserStore();
        const chatEngine = new ChatEngine();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            chats: [],
        });
        let chatInfo = new ChatDetails({
            title: "test chat",
            tags: ["Hiking"],
            numberOfPeople: 6,
            description: "test description",
            participants: [],
            messages: [],
            event: [],
            currCapacity: 1,
        });
        User.findById.mockResolvedValue(userResult);
        User.findByIdAndUpdate.mockResolvedValue(undefined);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let foundUser = await userStore.acceptChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("reject chat invite", () => {
    test("Invalid chat ID", async () => {
        const userStore = new UserStore();
        let foundUser = await userStore.rejectChatInvite(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let foundUser = await userStore.rejectChatInvite(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        const userStore = new UserStore();
        User.findById.mockResolvedValue(undefined);
        let foundUser = await userStore.rejectChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("User not invited to chat", async () => {
        const userStore = new UserStore();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            chatInvites: [],
        });
        User.findById.mockResolvedValue(userResult);
        let foundUser = await userStore.rejectChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.CONFLICT);
    });

    test("Success: user rejects chat invite", async () => {
        const userStore = new UserStore();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            chatInvites: ["64c50cfb436fbc75c648d9eb"],
        });
        User.findById.mockResolvedValue(userResult);
        let foundUser = await userStore.rejectChatInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("accept event invite", () => {
    let userResult = new UserAccount({
        //id : 62d63248010a82beb388af87
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        chatInvites: [],
    });
    let chatInfo = new ChatDetails({
        //id : 64c50cfb436fbc75c648d9eb
        title: "test chat",
        tags: ["Hiking"],
        numberOfPeople: 6,
        description: "test description",
        participants: [],
        messages: [],
        event: [],
        currCapacity: 2,
    });

    let eventInfo = new EventDetails({
        //id : 62d50cfb436fbc75c258d9eb
        title: "Event",
        eventOwnerID: "62d63248010a82beb388af87",
        tags: ["Hiking"],
        beginningDate: "2022-08-08",
        endingDate: "2022-09-01",
        publicVisibility: true,
        location: "2205 West Mall Toronto",
        description: "test description",
        numberOfPeople: 6,
        currCapacity: 2,
        chat: "64c50cfb436fbc75c648d9eb",
    });

    test("Invalid event ID", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();
        let foundUser = await userStore.acceptEventInvite(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();
        let foundUser = await userStore.acceptEventInvite(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );

        // console.log(foundEvent)
        expect(foundUser.data).toBe(null);
        expect(foundUser.status).toBe(ERROR_CODES.INVALID);
    });

    test("No event with given ID found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();

        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.NOTFOUND)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        User.findById.mockResolvedValue(userResult);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No chat associated with given event found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();

        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, eventInfo)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.NOTFOUND)
        );
        User.findById.mockResolvedValue(userResult);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No user with given ID found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();

        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, eventInfo)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        User.findById.mockResolvedValue(undefined);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Event is at maximum capacity", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();
        eventInfo.currCapacity = 6;
        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, eventInfo)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        User.findById.mockResolvedValue(userResult);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.CONFLICT);
        eventInfo.currCapacity = 2;
    });

    test("Event already includes user", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();
        eventInfo.currCapacity = 2;
        eventInfo.participants.push("62d63248010a82beb388af87");
        userResult.events.push("64c50cfb436fbc75c648d9eb");
        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, eventInfo)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        User.findById.mockResolvedValue(userResult);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.CONFLICT);
        eventInfo.participants = [];
        userResult.events = [];
    });

    test("Success: user accepts chat invite", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        const chatEngine = new ChatEngine();
        eventInfo.currCapacity = 2;
        // eventInfo.participants.push("62d63248010a82beb388af87");
        // userResult.events.push("64c50cfb436fbc75c648d9eb")
        eventStore.findEventByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, eventInfo)
        );
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        User.findById.mockResolvedValue(userResult);
        let result = await userStore.acceptEventInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            eventStore,
            chatEngine
        );
        // console.log(foundEvent)
        expect(result.data).toBe(null);
        expect(result.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("reject event invite", () => {
    test("Invalid event ID", async () => {
        const userStore = new UserStore();
        let rejectedEvent = await userStore.rejectEventInvite(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let rejectedEvent = await userStore.rejectEventInvite(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        const userStore = new UserStore();
        User.findById.mockResolvedValue(undefined);
        let rejectedEvent = await userStore.rejectEventInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("User not invited to event", async () => {
        const userStore = new UserStore();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            eventInvites: [],
        });
        User.findById.mockResolvedValue(userResult);
        let rejectedEvent = await userStore.rejectEventInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.CONFLICT);
    });

    test("Success: user rejects event invite", async () => {
        const userStore = new UserStore();
        let userResult = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            eventInvites: ["64c50cfb436fbc75c648d9eb"],
        });
        User.findById.mockResolvedValue(userResult);
        let rejectedEvent = await userStore.rejectEventInvite(
            "62d50cfb436fbc75c258d9eb",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("accept friend request", () => {
    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let sentRequest = await userStore.acceptFriendRequest(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID of friend", async () => {
        const userStore = new UserStore();
        let sentRequest = await userStore.acceptFriendRequest(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("No users with given IDs found", async () => {
        const userStore = new UserStore();
        User.findById.mockResolvedValue(undefined);
        let sentRequest = await userStore.acceptFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Users are already friends", async () => {
        let userInfo = new UserAccount({
            id: "62d63248010a82beb388af87",
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            friends: ["64c50cfb436fbc75c648d9eb"],
        });
        let friendInfo = new UserAccount({
            id: "64c50cfb436fbc75c648d9eb",
            name: "Jack Jackson",
            interests: ["Reading"],
            location: "1970 South Avenue, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            friends: ["62d63248010a82beb388af87"],
        });

        const userStore = new UserStore();
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        // User.findById.mockResolvedValue(userInfo)
        let sentRequest = await userStore.acceptFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );

        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.CONFLICT);
    });

    test("Success: user accepts friend request", async () => {
        User.mockReset();
        let userInfo = new UserAccount({
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
        });
        let friendInfo = new UserAccount({
            name: "Jack Jackson",
            interests: ["Reading"],
            location: "1970 South Avenue, Vancouver",
            description: "Test description",
            profilePicture: "picture",
        });

        const userStore = new UserStore();
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.acceptFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );

        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("reject friend request", () => {
    test("Invalid user ID", async () => {
        const userStore = new UserStore();
        let rejectedEvent = await userStore.rejectFriendRequest(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID of friend", async () => {
        const userStore = new UserStore();
        let rejectedEvent = await userStore.rejectFriendRequest(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        const userStore = new UserStore();
        User.findById.mockResolvedValue(null);
        // console.log(User.findById.mockResolvedValue)
        let rejectedEvent = await userStore.rejectFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.data).toBe(null);
        expect(rejectedEvent.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("User has not received friend request", async () => {
        const userStore = new UserStore();
        let userInfo = new UserAccount({
            //id : 62d63248010a82beb388af87
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            friendRequest: [],
            friends: [],
        });
        User.findById.mockResolvedValue(userInfo);
        let rejectedEvent = await userStore.rejectFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.status).toBe(ERROR_CODES.CONFLICT);
        expect(rejectedEvent.data).toBe(null);
    });

    test("Success: user rejects friend request", async () => {
        const userStore = new UserStore();
        let userInfo = new UserAccount({
            //id : 62d63248010a82beb388af87
            name: "Rob Robber",
            interests: ["Swimming"],
            location: "2423 Montreal Mall, Vancouver",
            description: "Test description",
            profilePicture: "picture",
            friendRequest: ["64c50cfb436fbc75c648d9eb"],
            friends: [],
        });
        User.findById.mockResolvedValue(userInfo);
        let rejectedEvent = await userStore.rejectFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(rejectedEvent.status).toBe(ERROR_CODES.SUCCESS);
        expect(rejectedEvent.data).toBe(null);
    });
});

describe("send chat invite", () => {
    const userStore = new UserStore();
    const chatEngine = new ChatEngine();
    let userResult = new UserAccount({
        //id : 62d63248010a82beb388af87
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        chatInvites: [],
        chats: [],
    });
    let chatInfo = new ChatDetails({
        //id : 64c50cfb436fbc75c648d9eb
        title: "test chat",
        tags: ["Hiking"],
        numberOfPeople: 6,
        description: "test description",
        participants: [],
        messages: [],
        event: [],
        currCapacity: 2,
    });

    test("Invalid user ID", async () => {
        let response = await userStore.sendChatInvite(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid chat ID", async () => {
        let response = await userStore.sendChatInvite(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findById.mockResolvedValue(undefined);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let response = await userStore.sendChatInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No chat with given ID found", async () => {
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.NOTFOUND)
        );
        let response = await userStore.sendChatInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("User is already a part of the chat", async () => {
        userResult.chats.push("64c50cfb436fbc75c648d9eb");
        chatInfo.participants.push("62d63248010a82beb388af87");
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let response = await userStore.sendChatInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.CONFLICT);
        userResult.chats = [];
        chatInfo.participants = [];
    });

    test("User is already invited to the chat", async () => {
        userResult.chatInvites.push("64c50cfb436fbc75c648d9eb");
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let response = await userStore.sendChatInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.CONFLICT);
        userResult.chatInvites = [];
    });

    test("Success: chat invite sent", async () => {
        User.findById.mockResolvedValue(userResult);
        chatEngine.findChatByID.mockResolvedValue(
            new ResponseObject(ERROR_CODES.SUCCESS, chatInfo)
        );
        let response = await userStore.sendChatInvite(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("send friend request", () => {
    let userStore = new UserStore();
    let userInfo = new UserAccount({
        id: "62d63248010a82beb388af87",
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        friends: [],
    });
    let friendInfo = new UserAccount({
        id: "64c50cfb436fbc75c648d9eb",
        name: "Jack Jackson",
        interests: ["Reading"],
        location: "1970 South Avenue, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        friends: [],
    });
    test("Invalid user ID", async () => {
        let sentRequest = await userStore.sendFriendRequest(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID of other user", async () => {
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "sdfsd0cfb436fbc"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findById
            .mockResolvedValueOnce(undefined)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No other user with given ID found", async () => {
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(undefined);
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Users are already friends", async () => {
        // console.log(userInfo)
        userInfo.friends.push("64c50cfb436fbc75c648d9eb");
        friendInfo.friends.push("62d63248010a82beb388af87");
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.CONFLICT);
        userInfo.friends = friendInfo.friends = [];
    });

    test("User has already sent a friend request to other user", async () => {
        // userInfo.friends.push("64c50cfb436fbc75c648d9eb")
        friendInfo.friendRequest.push("62d63248010a82beb388af87");
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.CONFLICT);
        friendInfo.friendRequest = [];
    });

    test("Success: friend request sent", async () => {
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.sendFriendRequest(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("add chat", () => {
    let chatInfo = new ChatDetails({
        title: "test chat",
        tags: ["Hiking"],
        numberOfPeople: 6,
        description: "test description",
        participants: [],
        messages: [],
        event: [],
    });
    test("Success: chat ID added to required users", async () => {
        const userStore = new UserStore();
        let foundUsers = await userStore.addChat(
            "64c50cfb436fbc75c648d9eb",
            chatInfo
        );
        expect(foundUsers.data).toBe(null);
        expect(foundUsers.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("find user by name", () => {
    test("No users with given name found", async () => {
        const userStore = new UserStore();
        User.find.mockResolvedValue([]);
        let foundUserList = await userStore.findUserByName("NoName");
        expect(JSON.stringify(foundUserList.data)).toBe(JSON.stringify([]));
        expect(foundUserList.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: users with given name returned", async () => {
        const userStore = new UserStore();
        let userList = [
            new UserAccount({
                id: "64c50cfb436fbc75c648d9eb",
                name: "Peter Parker",
                interests: ["Hiking"],
                location: "2423 Toronto Mall, Montreal",
                description: "Test description",
                profilePicture: "picture",
            }),
            new UserAccount({
                id: "64d50cfb436fbc75c648d9eb",
                name: "Lamin Lemon",
                interests: ["Swindling"],
                location: "2423 Toronto Mall, Montreal",
                description: "Test description",
                profilePicture: "picture",
            }),
        ];
        User.find.mockResolvedValue(userList);
        let foundUserList = await userStore.findUserByName("Lemon");
        expect(JSON.stringify(foundUserList.data)).toBe(
            JSON.stringify(userList)
        );
        expect(foundUserList.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("remove chat", () => {
    let chatInfo = new ChatDetails({
        title: "test chat",
        tags: ["Hiking"],
        numberOfPeople: 6,
        description: "test description",
        participants: [],
        messages: [],
        event: [],
    });
    test("Success: chat ID removed from all required users", async () => {
        const userStore = new UserStore();
        let foundUsers = await userStore.removeChat(
            "64c50cfb436fbc75c648d9eb",
            chatInfo
        );
        expect(foundUsers.data).toBe(null);
        expect(foundUsers.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("add event", () => {
    let eventInfo = new EventDetails({
        //id : 62d50cfb436fbc75c258d9eb
        title: "Event",
        eventOwnerID: "62d63248010a82beb388af87",
        tags: ["Hiking"],
        beginningDate: "2022-08-08",
        endingDate: "2022-09-01",
        publicVisibility: true,
        location: "2205 West Mall Toronto",
        description: "test description",
        numberOfPeople: 6,
        currCapacity: 2,
        chat: "64c50cfb436fbc75c648d9eb",
    });
    test("Success: event ID added to required users", async () => {
        const userStore = new UserStore();
        let foundUsers = await userStore.addEvent(
            "64c50cfb436fbc75c648d9eb",
            eventInfo
        );
        expect(foundUsers.data).toBe(null);
        expect(foundUsers.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("remove event", () => {
    let eventInfo = new EventDetails({
        //id : 62d50cfb436fbc75c258d9eb
        title: "Event",
        eventOwnerID: "62d63248010a82beb388af87",
        tags: ["Hiking"],
        beginningDate: "2022-08-08",
        endingDate: "2022-09-01",
        publicVisibility: true,
        location: "2205 West Mall Toronto",
        description: "test description",
        numberOfPeople: 6,
        currCapacity: 2,
        chat: "64c50cfb436fbc75c648d9eb",
    });
    test("Success: event ID removed from all required users", async () => {
        const userStore = new UserStore();
        let foundUsers = await userStore.removeEvent(
            "64c50cfb436fbc75c648d9eb",
            eventInfo
        );
        expect(foundUsers.data).toBe(null);
        expect(foundUsers.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("remove friend", () => {
    let userStore = new UserStore();
    let userInfo = new UserAccount({
        id: "62d63248010a82beb388af87",
        name: "Rob Robber",
        interests: ["Swimming"],
        location: "2423 Montreal Mall, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        friends: [],
    });
    let friendInfo = new UserAccount({
        id: "64c50cfb436fbc75c648d9eb",
        name: "Jack Jackson",
        interests: ["Reading"],
        location: "1970 South Avenue, Vancouver",
        description: "Test description",
        profilePicture: "picture",
        friends: [],
    });
    test("Invalid user ID", async () => {
        let sentRequest = await userStore.removeFriend(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid user ID for friend", async () => {
        let sentRequest = await userStore.removeFriend(
            "62d63248010a82beb388af87",
            "sdfsd0cfb436fbc"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findById
            .mockResolvedValueOnce(undefined)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.removeFriend(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("No user with given ID for friend found", async () => {
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(undefined);
        let sentRequest = await userStore.removeFriend(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: users have been removed from each others' friend list", async () => {
        User.findById
            .mockResolvedValueOnce(userInfo)
            .mockResolvedValueOnce(friendInfo);
        let sentRequest = await userStore.removeFriend(
            "62d63248010a82beb388af87",
            "64c50cfb436fbc75c648d9eb"
        );
        expect(sentRequest.data).toBe(null);
        expect(sentRequest.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("delete user", () => {
    const userStore = new UserStore();
    test("Invalid user ID", async () => {
        let response = await userStore.deleteUser("sdfsd0cfb436fbc");
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findById.mockResolvedValue(undefined);
        let response = await userStore.deleteUser("64c50cfb436fbc75c648d9eb");
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: user deleted", async () => {
        let userInfo = new UserAccount({
            id: "64c50cfb436fbc75c648d9eb",
            name: "Peter Parker",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            friends: ["62c50cfb436fbc75c624d9eb"],
        });
        User.findById.mockResolvedValue(userInfo);
        let response = await userStore.deleteUser("64c50cfb436fbc75c648d9eb");
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("leave event", () => {
    const userStore = new UserStore();
    const eventStore = new EventStore();
    test("Invalid user ID", async () => {
        let response = await userStore.leaveEvent(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb",
            eventStore
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid event ID", async () => {
        let response = await userStore.leaveEvent(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc",
            eventStore
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findByIdAndUpdate.mockResolvedValue(undefined);
        let response = await userStore.leaveEvent(
            "64c50cfb436fbc75c648d9eb",
            "64c50cfb436fbc75c648d9eb",
            eventStore
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: user leaves event", async () => {
        let userInfo = new UserAccount({
            id: "64c50cfb436fbc75c648d9eb",
            name: "Peter Parker",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
        });
        User.findByIdAndUpdate.mockResolvedValue(userInfo);
        let response = await userStore.leaveEvent(
            "64c50cfb436fbc75c648d9eb",
            "64c50cfb436fbc75c648d9eb",
            eventStore
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("leave chat", () => {
    const userStore = new UserStore();
    const chatEngine = new ChatEngine();
    test("Invalid user ID", async () => {
        let response = await userStore.leaveChat(
            "sdfsd0cfb436fbc",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("Invalid chat ID", async () => {
        let response = await userStore.leaveChat(
            "64c50cfb436fbc75c648d9eb",
            "sdfsd0cfb436fbc",
            chatEngine
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.INVALID);
    });

    test("No user with given ID found", async () => {
        User.findByIdAndUpdate.mockResolvedValue(undefined);
        let response = await userStore.leaveChat(
            "64c50cfb436fbc75c648d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: user leaves chat", async () => {
        let userInfo = new UserAccount({
            id: "64c50cfb436fbc75c648d9eb",
            name: "Peter Parker",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
        });
        User.findByIdAndUpdate.mockResolvedValue(userInfo);
        let response = await userStore.leaveChat(
            "64c50cfb436fbc75c648d9eb",
            "64c50cfb436fbc75c648d9eb",
            chatEngine
        );
        // console.log(foundEvent)
        expect(response.data).toBe(null);
        expect(response.status).toBe(ERROR_CODES.SUCCESS);
    });
});

describe("find user for login", () => {
    test("No user with given token found", async () => {
        const userStore = new UserStore();
        User.findOne.mockResolvedValue(undefined);
        let foundUsers = await userStore.findUserForLogin("14334345345");
        expect(foundUsers.data).toBe(null);
        expect(foundUsers.status).toBe(ERROR_CODES.NOTFOUND);
    });

    test("Success: user found", async () => {
        const userStore = new UserStore();
        let userInfo = new UserAccount({
            id: "62d63248010a82beb388af87",
            name: "Bob Bobberson",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            blockedUsers: ["64d50cfb436fbc75c648d9eb"],
            token: "14334345345",
        });
        User.findOne.mockResolvedValue(userInfo);
        let foundUsers = await userStore.findUserForLogin("14334345345");
        expect(JSON.stringify(foundUsers.data)).toBe(JSON.stringify(userInfo));
        expect(foundUsers.status).toBe(ERROR_CODES.SUCCESS);
    });
});
