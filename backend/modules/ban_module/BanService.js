class BanService {
    constructor() {}

    async banUser(userID, userStore) {
        return await userStore.deleteUser(userID);
    }

    async banEvent(eventID, eventStore) {
        return await eventStore.deleteEvent(eventID);
    }
}

module.exports = BanService;