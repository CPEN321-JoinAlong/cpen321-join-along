const UserAccount = require("./../modules/user_module/UserAccount");
const UserStore = require("./../modules/user_module/UserStore");

const EventDetails = require("./../modules/event_module/EventDetails");
const EventStore = require("./../modules/event_module/EventStore");
const Event =  require("./../models/Event")

const ChatDetails = require("./../modules/chat_module/ChatDetails");
const ChatEngine = require("./../modules/chat_module/ChatEngine");

const ERROR_CODES = require("./../ErrorCodes.js");
const ResponseObject = require("./../ResponseObject")

jest.mock("./../models/Event");
jest.mock("./../modules/chat_module/ChatEngine"); //automatically creates mocks of all methods in the class
jest.mock("./../modules/user_module/UserStore");
// jest.mock("./../models/User");


beforeEach(() => {
    // UserStore.mockClear();
    // ChatEngine.mockClear();
    // Event.mockClear();
});

afterEach(() => {
    // UserStore.mockClear();
    // ChatEngine.mockClear();
    // Event.mockClear();
});

describe("find event by id", () => {
    test("invalid Event ID", async () => {
        const eventStore = new EventStore();
        let foundEvent = await eventStore.findEventByID("dsfjskfsd");
        // console.log(foundEvent)
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    test("event not found", async () => {
        const eventStore = new EventStore();
        Event.findById.mockResolvedValue(undefined)
        let foundEvent = await eventStore.findEventByID("62d50cfb436fbc75c258d9eb");
        // console.log(foundEvent)
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })
    
    test("Success: event found", async () => {
        const eventStore = new EventStore();
        let eventInfo = new EventDetails({
            title: "Event",
            eventOwnerID: "64d50cfb433efc75c258d9sd",
            tags: ["hiking"],
            beginningDate: "2022-08-08",
            endingDate: "2022-09-01",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description"
        })
        Event.findById.mockResolvedValue(eventInfo)
        let foundEvent = await eventStore.findEventByID("62d50cfb436fbc75c258d9eb");
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify(eventInfo));
        expect(foundEvent.status).toBe(ERROR_CODES.SUCCESS);
    })
})

describe("find events through a list of eventIDs", () => {
    test("invalid eventID in list", async () => {
        const eventStore = new EventStore();
        let foundEvent = await eventStore.findEventByIDList(["62d50cfb436fbc75c258d9eb", "dsfjskfsd"]);
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    test("event in list not found", async () => {
        const eventStore = new EventStore();
        Event.find.mockResolvedValue([])
        let foundEvent = await eventStore.findEventByIDList(["62d50cfb436fbc75c258d9eb"]);
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })
    
    test("Success: Events found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [
            new EventDetails({
                title: "Event",
                eventOwnerID: "62d50cfb436fbc75c258d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            }),
            new EventDetails({
                title: "Event 2",
                eventOwnerID: "64c50cfb436fbc75c648d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            })
        ]
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findEventByIDList(["62d50cfb436fbc75c258d9eb", "64c50cfb436fbc75c648d9eb"])
        // console.log(foundEventList)
        expect(foundEventList.status).toBe(ERROR_CODES.SUCCESS);
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
    })
})

describe("find event by name", () => {
    test("No Event found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [];
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findEventsByName("Even")
        // console.log(foundEventList)
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
        expect(foundEventList.status).toBe(ERROR_CODES.NOTFOUND);
    })

    test("Success: Events found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [
            new EventDetails({
                title: "Event",
                eventOwnerID: "64d50cfb433efc75c258d9sd",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            }),
            new EventDetails({
                title: "Event 2",
                eventOwnerID: "68hd0cfh485efc786h8jd85",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            })
        ]
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findEventsByName("Even")
        // console.log(foundEventList)
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
        expect(foundEventList.status).toBe(ERROR_CODES.SUCCESS);
    })
})

describe("find events through a userID", () => {
    test("invalid userID", async () => {
        const eventStore = new EventStore();
        let foundEvent = await eventStore.findEventByUser("62d50cfb436fbc");
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    test("No events found", async () => {
        const eventStore = new EventStore();
        Event.find.mockResolvedValue([])
        let foundEvent = await eventStore.findEventByUser("62d50cfb436fbc75c258d9eb");
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })
    
    test("Success: Events found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [
            new EventDetails({
                title: "Event",
                eventOwnerID: "62d50cfb436fbc75c258d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description",
                participants: ["62d50cfb436fbc75c258d9eb"]
            }),
            new EventDetails({
                title: "Event 2",
                eventOwnerID: "64c50cfb436fbc75c648d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description",
                participants: ["62d50cfb436fbc75c258d9eb", "64c50cfb436fbc75c648d9eb"]
            })
        ]
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findEventByUser("62d50cfb436fbc75c258d9eb")
        // console.log(foundEventList)
        expect(foundEventList.status).toBe(ERROR_CODES.SUCCESS);
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
    })
})

describe("find all events", () => {
    test("No Event found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [];
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findAllEvents()
        // console.log(foundEventList)
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
        expect(foundEventList.status).toBe(ERROR_CODES.NOTFOUND);
    })

    test("Success: Events found", async () => {
        const eventStore = new EventStore();
        let eventResultList = [
            new EventDetails({
                title: "Event",
                eventOwnerID: "64d50cfb433efc75c258d9sd",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            }),
            new EventDetails({
                title: "Event 2",
                eventOwnerID: "68hd0cfh485efc786h8jd85",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description"
            })
        ]
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findAllEvents()
        // console.log(foundEventList)
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
        expect(foundEventList.status).toBe(ERROR_CODES.SUCCESS);
    })
})

describe("find all UnblockedEvents", () => {
    test("invalid userID", async () => {
        const userStore = new UserStore();    
        const eventStore = new EventStore();
        let foundEvent = await eventStore.findUnblockedEvents("62d50cfb436fbc", userStore);
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    test("user not found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.NOTFOUND))
        let foundEvent = await eventStore.findUnblockedEvents("62d50cfb436fbc75c258d9eb", userStore);
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })

    test("No events found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.NOTFOUND, new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture"
        })))
        Event.find.mockResolvedValue([])
        let foundEvent = await eventStore.findUnblockedEvents("62d50cfb436fbc75c258d9eb", userStore);
        // console.log(foundEvent)
        expect(JSON.stringify(foundEvent.data)).toBe(JSON.stringify([]));
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })
    
    test("Success: Events found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.NOTFOUND, new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            blockedEvents: ["6ksj0cfb436fbc75c25hd93h"]
        })))
        let eventResultList = [
            new EventDetails({
                title: "Event",
                eventOwnerID: "62d50cfb436fbc75c258d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description",
                participants: ["62d50cfb436fbc75c258d9eb"],
                currCapacity: 1,
                numberOfPeople: 6,
                chat: "68ndhfb436fbc83jjj4rh4" 
            }),
            new EventDetails({
                title: "Event 2",
                eventOwnerID: "64c50cfb436fbc75c648d9eb",
                tags: ["hiking"],
                beginningDate: "2022-08-08",
                endingDate: "2022-09-01",
                publicVisibility: true,
                location: "2205 West Mall Toronto",
                description: "test description",
                participants: ["62d50cfb436fbc75c258d9eb", "64c50cfb436fbc75c648d9eb"],
                numberOfPeople: 3,
                currCapacity: 2
            })
        ]
        Event.find.mockResolvedValue(eventResultList)
        let foundEventList = await eventStore.findUnblockedEvents("62d50cfb436fbc75c258d9eb", userStore)
        // console.log(foundEventList)
        expect(foundEventList.status).toBe(ERROR_CODES.SUCCESS);
        expect(JSON.stringify(foundEventList.data)).toBe(JSON.stringify(eventResultList));
    })
})

describe("remove user from event", () => {
    test("invalid userID", async () => {
        const userStore = new UserStore();    
        const eventStore = new EventStore();
        let foundEvent = await eventStore.removeUser("62d50cfb436fbc75c258d9eb", "62d50cfb436fbc", userStore);
        // console.log(foundEvent)
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    test("invalid event", async () => {
        const userStore = new UserStore();    
        const eventStore = new EventStore();
        let foundEvent = await eventStore.removeUser("62d50cfb436fbc", "62d50cfb436fbc75c258d9eb",  userStore);
        // console.log(foundEvent)
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.INVALID);
    })

    
    test("user not found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        Event.findById.mockResolvedValue(new EventDetails({
            title: "Event",
            eventOwnerID: "62d50cfb436fbc75c258d9eb",
            tags: ["hiking"],
            beginningDate: "2022-08-08",
            endingDate: "2022-09-01",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            participants: ["62d50cfb436fbc75c258d9eb"],
            currCapacity: 1,
            numberOfPeople: 6,
            chat: "68ndhfb436fbc83jjj4rh4" 
        }))
        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.NOTFOUND))
        let foundEvent = await eventStore.removeUser("62d50cfb436fbc75c258d9eb", "62d50cfb436fbc75c258d9eb", userStore);
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })
    
    test("event not found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        Event.findById.mockResolvedValue(undefined)
        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.SUCCESS, new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            blockedEvents: ["6ksj0cfb436fbc75c25hd93h"]
        })))
        let foundEvent = await eventStore.removeUser("62d50cfb436fbc75c258d9eb", "62d50cfb436fbc75c258d9eb", userStore);
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.NOTFOUND);
    })

    test("user not found", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        let eventInfo = new EventDetails({
            title: "Event",
            eventOwnerID: "62d50cfb436fbc75c258d9eb",
            tags: ["hiking"],
            beginningDate: "2022-08-08",
            endingDate: "2022-09-01",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            participants: ["62d50cfb436fbc75c258d9eb"],
            currCapacity: 1,
            numberOfPeople: 6,
            chat: "68ndhfb436fbc83jjj4rh4" 
        })
        Event.findById.mockResolvedValue(eventInfo)
        Event.findByIdAndUpdate.mockResolvedValue(eventInfo)

        userStore.findUserByID.mockResolvedValue(new ResponseObject(ERROR_CODES.SUCCESS, new UserAccount({
            name: "Bob Bobber",
            interests: ["Hiking"],
            location: "2423 Toronto Mall, Montreal",
            description: "Test description",
            profilePicture: "picture",
            blockedEvents: ["6ksj0cfb436fbc75c25hd93h"]
        })))
        let foundEvent = await eventStore.removeUser("62d50cfb436fbc75c258d9eb", "62d50cfb436fbc75c258d9eb", userStore);
        expect(foundEvent.data).toBe(null);
        expect(foundEvent.status).toBe(ERROR_CODES.SUCCESS);
    })
})

describe("create event", () => {
    test("Success: event created", async () => {
        const userStore = new UserStore();
        const eventStore = new EventStore();
        let eventInfo = new EventDetails({
            title: "Event",
            eventOwnerID: "62d50cfb436fbc75c258d9eb",
            tags: ["hiking"],
            beginningDate: "2022-08-08",
            endingDate: "2022-09-01",
            publicVisibility: true,
            location: "2205 West Mall Toronto",
            description: "test description",
            participants: ["62d50cfb436fbc75c258d9eb"],
            currCapacity: 1,
            numberOfPeople: 6,
            chat: "68ndhfb436fbc83jjj4rh4" 
        })
        let result = Event(eventInfo);
        console.log(result)
        // result.$__save.mockResolvedValue(eventInfo)
        userStore.updateUserAccount.mockResolvedValue(new ResponseObject(ERROR_CODES.SUCCESS))
        let createdEvent = await eventStore.createEvent(eventInfo, userStore);
        expect(JSON.stringify(createdEvent.data)).toBe(JSON.stringify(eventInfo))
        expect(createdEvent.status).toBe(ERROR_CODES.SUCCESS)
    })
})