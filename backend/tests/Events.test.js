const UserStore = require("./../modules/user_module/UserStore");
const EventStore = require("./../modules/event_module/EventStore");
const Event = require("./../models/Event");
const EventDetails = require("./../modules/event_module/EventDetails");
jest.mock("./../modules/event_module/EventStore"); //automatically creates mocks of all methods in the class

const mongoose = require("mongoose");
const { db } = require("../models/User");

const CONFLICT = 409;
const NOTFOUND = 404;
const SUCCESS = 200;
const INVALID = 422;

let userStore = new UserStore();

// beforeAll(() => {
//     mongoose.connect("mongodb://localhost:27017/joinalong", {
//         useNewUrlParser: true,
//         useUnifiedTopology: true,
//     });
//     const db = mongoose.connection;
//     db.on("error", console.error.bind(console, "connection error:"));
//     db.once("open", () => {
//         // console.log("Database connected");
//     });
// });

// afterAll(() => {
//     db.close();
// });

beforeEach(() => {
    EventStore.mockClear();
});

test("EventStore is called once", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
});

test("create event", async () => {
    const eventStore = new EventStore();
    const eventInfo = new EventDetails({
        title: "test event",
        eventOwnerID: "sdffsdlkfslkj",
        tags: ["Hiking"],
        location: "test location",
        description: "This is test event",
    });
    let result = {
        _id: "62cccd5a5bb7051aea562f69",
        title: "test event",
        eventOwnerID: "sdffsdlkfslkj",
        tags: ["Hiking"],
        location: "test location",
        description: "This is test event",
    };
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockCreateEvent = mockEventStoreInstance.createEvent;
    mockCreateEvent.mockReturnValue(result);
    expect(await mockCreateEvent(eventInfo, userStore)).toBe(result);
});

test("update event", async () => {
    const eventStore = new EventStore();
    const eventInfo = new EventDetails({
        _id: "62cccd5a5bb7051aea562f79",
        title: "test event",
        eventOwnerID: "sdffsdlkfslkj",
        tags: ["Hiking", "BasketBall"],
        location: "test location",
        description: "This is test event",
    });
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockUpdateEvent = mockEventStoreInstance.updateEvent;
    mockUpdateEvent.mockReturnValue(SUCCESS);
    expect(
        await mockUpdateEvent("62cccd5a5bb7051aea562f79", eventInfo, userStore)
    ).toBe(INVALID);
});

test("delete event", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockDeleteEvent = mockEventStoreInstance.deleteEvent;
    mockDeleteEvent.mockReturnValue(SUCCESS);
    expect(await mockDeleteEvent("62cccd5a5bb7051aea562f69", userStore)).toBe(
        SUCCESS
    );
});

test("remove User", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockRemoveUser = mockEventStoreInstance.removeUser;
    mockRemoveUser.mockReturnValue(INVALID);
    expect(
        await mockRemoveUser("slkdfjklsdfj", "slkfjsdklfsd", userStore)
    ).toBe(INVALID);
});

test("find all events", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockFindAllEvents = mockEventStoreInstance.findAllEvents;
    let result = [
        {
            _id: "62cccd5a5bb7051aea562f69",
            title: "test event",
            eventOwnerID: "sdffsdlkfslkj",
            tags: ["Hiking"],
            location: "test location",
            description: "This is test event",
        },
        {
            _id: "62cccd7a5bd7051aea232f69",
            title: "test event 2",
            eventOwnerID: "sdffsdlkdfdfslkj",
            tags: ["Swimming"],
            location: "test location",
            description: "This is test event",
        },
    ];
    mockFindAllEvents.mockReturnValue(result);
    expect(await mockFindAllEvents()).toBe(result);
});

test("find event by id", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockFindEventByID = mockEventStoreInstance.findEventByID;
    let result = {
        _id: "62cccd5a5bb7051aea562f69",
        title: "test event",
        eventOwnerID: "sdffsdlkfslkj",
        tags: ["Hiking"],
        location: "test location",
        description: "This is test event",
    };
    mockFindEventByID.mockReturnValue(result);
    expect(await mockFindEventByID("62cccd5a5bb7051aea562f69")).toBe(result);
});

test("find events by idlist", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockFindEventByIDList = mockEventStoreInstance.findEventByIDList;
    let result = [
        {
            _id: "62cccd5a5bb7051aea562f69",
            title: "test event",
            eventOwnerID: "sdffsdlkfslkj",
            tags: ["Hiking"],
            location: "test location",
            description: "This is test event",
        },
        {
            _id: "62cccd7a5bd7051aea232f69",
            title: "test event 2",
            eventOwnerID: "sdffsdlkdfdfslkj",
            tags: ["Swimming"],
            location: "test location",
            description: "This is test event",
        },
    ];
    mockFindEventByIDList.mockReturnValue(result);
    expect(
        await mockFindEventByIDList([
            "62cccd5a5bb7051aea562f69",
            "62cccd7a5bd7051aea232f69",
        ])
    ).toBe(result);
});

test("find events by name", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockFindEventsByName = mockEventStoreInstance.findEventsByName;
    let result = [
        {
            _id: "62cccd5a5bb7051aea562f69",
            title: "test event",
            eventOwnerID: "sdffsdlkfslkj",
            tags: ["Hiking"],
            location: "test location",
            description: "This is test event",
        },
    ];
    mockFindEventsByName.mockReturnValue(result);
    expect(await mockFindEventsByName("test event")).toBe(result);
});

test("find unblocked events", async () => {
    let eventStore = new EventStore();
    expect(EventStore).toHaveBeenCalledTimes(1);
    const mockEventStoreInstance = EventStore.mock.instances[0];
    const mockFindUnblockedEvents = mockEventStoreInstance.findUnblockedEvents;
    let result = [
        {
            _id: "62cccd5a5bb7051aea562f69",
            title: "test event",
            eventOwnerID: "sdffsdlkfslkj",
            tags: ["Hiking"],
            location: "test location",
            description: "This is test event",
        },
    ];
    mockFindUnblockedEvents.mockReturnValue(result);
    expect(await mockFindUnblockedEvents(["67cdy85a5bb7051aea5sd4dg"])).toBe(
        result
    );
});

// afterEach(async () => {
//     await Event.deleteMany({
//         title: "test event",
//     });
// });
