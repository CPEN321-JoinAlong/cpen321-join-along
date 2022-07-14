class EventDetails {
    constructor(eventInfo) {
        this.title = eventInfo.title;
        this.eventOwnerID = eventInfo.eventOwnerID;
        this.tags = eventInfo.tags;
        this.beginningDate = eventInfo.beginningDate;
        this.endDate = eventInfo.endDate;
        this.publicVisibility = eventInfo.publicVisibility;
        this.numberOfPeople = eventInfo.numberOfPeople;
        this.location = eventInfo.location;
        this.description = eventInfo.description;

        this.participants = eventInfo.participants
            ? eventInfo.participants
            : [this.eventOwnerID];
        this.currCapacity = eventInfo.currCapacity ? eventInfo.currCapacity : 1;
        this.eventImage = eventInfo.eventImage;
        this.chat = eventInfo.chat ? eventInfo.chat : null;
    }

    async findEvents(eventInfo, eventStore) {
        return await eventStore.findEventByDetails(eventInfo);
    }
}

module.exports = EventDetails

