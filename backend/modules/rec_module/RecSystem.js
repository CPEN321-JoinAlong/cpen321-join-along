const categories = require("./InterestTags")
const ResponseObject = require("./../../ResponseObject")

class RecSystem {
    constructor() {
        this.categories = categories
    }

    async recommendEvents(userID, userStore, eventStore) {
        let userInfo = await userStore.findUserByID(userID)
        let eventList = await eventStore.findAllEvents();
        let freeEventList = eventList.data.filter(event => event.currCapacity < event.numberOfPeople);
        freeEventList = freeEventList.filter(event => !event.participant.includes(userID))
        freeEventList = freeEventList.sort((a, b) => this.#cosineSimilarity(userInfo.data.interests, b.tags) - this.#cosineSimilarity(userInfo.data.interests, a.tags))

        return new ResponseObject(ERROR_CODES.SUCCESS, freeEventList.subarray(0, 11))
    }

    #cosineSimilarity(userTags, eventTags) {
        let userVector = this.#getVector(userTags, eventTags)
        // console.log(userVector)
        userVector["Same"] = userVector["Same"] ? userVector["Same"] : 1
        // console.log(userVector)
        let eventVector = this.#getVector(eventTags, userTags)
        // console.log(eventVector)
        eventVector["Same"] = eventVector["Same"] ? eventVector["Same"] : 0
        // console.log(eventVector)

        var userArray = Object.keys(userVector)
            .map(function (key) {
                return userVector[key];
            });

        // console.log(userArray)

        var eventArray = Object.keys(eventVector)
            .map(function (key) {
                return eventVector[key];
            });
        // console.log(eventArray)
        return this.#dotProduct(userArray, eventArray) / (this.#magnitude(userArray) * this.#magnitude(eventArray))
    }

    #dotProduct(vecA, vecB) {
        let product = 0;
        for (let i = 0; i < vecA.length; i++) {
            product += vecA[i] * vecB[i];
        }
        return product;
    }

    #magnitude(vec) {
        let sum = 0;
        for (let i = 0; i < vec.length; i++) {
            sum += vec[i] * vec[i];
        }
        return Math.sqrt(sum);
    }

    //Returns a list of catergories the tag is under
    #getTagCategory(tag) {
        // console.log(tag)
        
        let categoryList = []
        for (let category in this.categories) {
            // console.log(category)
            if (this.#categoryCheck(tag, this.categories[category])) {
                categoryList.push(category)
            }
        }
        // console.log(categoryList)
        return categoryList
    }

    //Returns a true if tag is in the category
    #categoryCheck(tag, category) {
        // console.log(this.categories)
        return category.includes(tag)
    }

    // gets the relative numbers for the event's interest tags compared to the user's interest tags
    //[OutdoorActivity, IndoorActivity, Sports, Events, SocialActivity]


    // user: ['Frisbee', 'Health', 'Skiing']
    // event1: ['Partying', 'Anime Expo', 'Swimming', 'Skiing']
    // event2: ["Basketball", "Swimming"]

    //[0,1,0,2,1,0]
    //[0,1,0,2,1,1]
    #getVector(tags, otherTags) {

        let vector = {
            Same: 0,
            OutdoorActivity: 0,
            IndoorActivity: 0,
            Sports: 0,
            Events: 0,
            SocialActivity: 0
        }
        // console.log(otherTags)
        for (let tag of tags) {
            // console.log(tag)
            if (otherTags.includes(tag)) {
                // console.log("WHY THE FUCK")
                vector["Same"] += 2
            }

            for (let category of this.#getTagCategory(tag)) {
                vector[category]++
            }
        }
        return vector
    }
}

let userTags = ["Hiking", "Cards", "Archery"]
let eventTags = ["Movies", "Swimming", "Skiing"]
let rec = new RecSystem();
console.log(rec.cosineSimilarity(userTags, eventTags))
