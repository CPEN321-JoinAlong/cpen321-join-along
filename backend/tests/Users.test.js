
const UserAccount = require("./../modules/user_module/UserAccount");
const UserStore = require("./../modules/user_module/UserStore");

const EventDetails = require("./../modules/event_module/EventDetails");
const EventStore = require("./../modules/event_module/EventStore");
const Event =  require("./../models/Event")

const ChatDetails = require("./../modules/chat_module/ChatDetails");
const ChatEngine = require("./../modules/chat_module/ChatEngine");

const ERROR_CODES = require("./../ErrorCodes.js");
const ResponseObject = require("./../ResponseObject")

jest.mock("./../models/User");
jest.mock("./../modules/chat_module/ChatEngine"); //automatically creates mocks of all methods in the class
jest.mock("./../modules/user_module/UserStore");

describe("find user by ID", () => {
    test("Invalid user ID", async () => {

    })

    test("User with ID not found", async () => {

    })

    test("Success: user with given ID found", async () => {

    })
})


describe("find all users", () => {
    test("No users found", async () => {

    })

    test("Success: users found", async () => {

    })
})


describe("update user account", () => {
    test("Invalid user ID", async () => {

    })

    test("User with ID not found", async () => {

    })

    test("Success: user with given ID found and updated", async () => {

    })
})

describe("create user", () => {
    test("Success: user created", async () => {

    })
})

describe("find friends from ID list", () => {
    test("Invalid ID in list", async () => {

    })

    test("No friends with IDs from list found", async () => {

    })

    test("Success: friends with given IDs found", async () => {

    })
})

describe("find chat invites", () => {
    test("No chat invites found", async () => {

    })

    test("Success: chat invites found", async () => {

    })
})

describe("accept chat invite", () => {
    test("Invalid chat ID in list", async () => {

    })

    test("Invalid user ID in list", async () => {

    })

    test("No chat with given ID found", async () => {

    })

    test("No user with given ID found", async () => {

    })

    test("Chat is at maximum capacity", async () => {

    })

    test("Chat already includes user", async () => {

    })

    test("Success: user accepts chat invite", async () => {

    })
})