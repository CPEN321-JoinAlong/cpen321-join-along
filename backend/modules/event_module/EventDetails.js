class EventDetails {
    constructor(eventInfo) {
        this.title = eventInfo.title;
        this.eventOwnerID = eventInfo.eventOwnerID;
        this.eventOwnerName = eventInfo.eventOwnerName ? eventInfo.eventOwnerName : "Rob";
        this.tags = eventInfo.tags;
        this.beginningDate = eventInfo.beginningDate;
        this.endDate = eventInfo.endDate;
        this.publicVisibility = eventInfo.publicVisibility != null ? eventInfo.publicVisibility : true;
        this.numberOfPeople = eventInfo.numberOfPeople;
        this.location = eventInfo.location;
        this.description = eventInfo.description;

        this.coordinates = eventInfo.coordinates ? eventInfo.coordinates : "Not available"; 
        this.participants = eventInfo.participants
            ? eventInfo.participants
            : (this.eventOwnerID ? [this.eventOwnerID] : []);
        this.currCapacity = eventInfo.currCapacity ? eventInfo.currCapacity : 1;
        this.eventImage = eventInfo.eventImage ? eventInfo.eventImage : null;
        this.chat = eventInfo.chat ? eventInfo.chat : null;
    }
}

module.exports = EventDetails

