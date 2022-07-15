class ChatDetails {
    constructor(chatInfo) {
        this.title = chatInfo.title;
        this.tags = chatInfo.tags;
        this.numberOfPeople = chatInfo.numberOfPeople;
        this.description = chatInfo.description;
        this.currCapacity = chatInfo.currCapacity ? chatInfo.currCapacity : 0;
        this.participants = chatInfo.participants ? chatInfo.participants : [];

        this.messages = chatInfo.messages ? chatInfo.message : [];
        this.event = chatInfo.event ? chatInfo.event : null;
    }
}

module.exports = ChatDetails;