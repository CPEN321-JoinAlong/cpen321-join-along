const categories = require("./InterestTags")
const ResponseObject = require("./../../ResponseObject")
const ERROR_CODES = require("./../../ErrorCodes")

class RecSystem {
    constructor() {
        this.categories = categories
    }

    async recommendEvents(userID, userStore, eventStore) {
        let userInfo = await userStore.findUserByID(userID)
        // console.log(userInfo)
        let eventList = await eventStore.findAllEvents();
        // console.log(eventList)
        let freeEventList = eventList.data.filter(event => event.currCapacity < event.numberOfPeople);
        // console.log(freeEventList.length)
        freeEventList = freeEventList.filter(event => !event.participants.includes(userID) && !userInfo.data.blockedEvents.includes(event._id))
        // console.log(freeEventList.length)
        // console.log(cosSim)
        freeEventList = freeEventList.sort((a, b) => this.#cosineSimilarity(userInfo.data.interests, b.tags) - this.#cosineSimilarity(userInfo.data.interests, a.tags))
        let cosSim = freeEventList.map(event => this.#cosineSimilarity(userInfo.data.interests, event.tags))
        console.log(cosSim)
        // console.log(freeEventList.slice(0, 11))

        return new ResponseObject(ERROR_CODES.SUCCESS, freeEventList.slice(0, 3))
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
                vector["Same"] += 2
            }

            for (let category of this.#getTagCategory(tag)) {
                vector[category]++
            }
        }
        return vector
    }
}

module.exports = RecSystem