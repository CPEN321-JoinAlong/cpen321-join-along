const User = require("./../../models/User");
const mongoose = require("mongoose");
const CONFLICT = 409;
const NOTFOUND = 404;
const SUCCESS = 200;
const INVALID = 422;

class UserStore {
    constructor() {}

    async findUserByEvent(eventID) {
        return await User.find({
            events: { $in: [eventID] },
        });
    }

    async findUserByID(userID) {
        console.log(userID);
        return await User.findById(userID);
    }

    async findAllUsers() {
        let userList = await User.find({});
        return userList;
    }

    async updateUserAccount(userID, userInfo) {
        console.log("IN UPDATE USER ACCOUNT");
        console.log(userID);
        console.log(userInfo);
        return await User.findByIdAndUpdate(userID, userInfo);
    }

    async createUser(userInfo) {
        return await new User(userInfo).save();
    }

    async findFriendByIDList(friendIDList) {
        return await User.find({
            _id: {
                $in: friendIDList,
            },
        });
    }

    async findChatInvites(chatReqList, chatEngine) {
        return await chatEngine.findChatByIDList(chatReqList);
    }

    async acceptChatInvite(userID, chatID, chatEngine) {
        let chat = await chatEngine.findChatByID(chatID);
        let user = await this.findUserByID(userID);
        if (chat && user) {
            if (
                chat.currCapacity < chat.numberOfPeople &&
                !user.chats.includes(chatID) &&
                !chat.participants.includes(userID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { chat: chatID },
                    $pull: { chatInvites: chatID },
                });
                console.log();
                await chatEngine.editChat(
                    chatID,
                    {
                        $push: { participants: userID },
                        $inc: { currCapacity: 1 },
                    },
                    this
                );
                return SUCCESS;
            } else {
                return CONFLICT;
            }
        } else {
            return NOTFOUND;
        }
    }

    async rejectChatInvite(userID, chatID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { chatInvites: chatID },
        });
    }

    async acceptEventInvite(userID, eventID, eventStore, chatEngine) {
        let event = await eventStore.findEventByID(eventID);
        let user = await this.findUserByID(userID);
        let chat = await chatEngine.findChatByID(event.chat);
        if (event && user && chat) {
            if (
                event.currCapacity < event.numberOfPeople &&
                !user.chats.includes(eventID) &&
                !event.participants.includes(userID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { events: eventID },
                    $pull: { eventInvites: eventID },
                });

                await eventStore.updateEvent(
                    eventID,
                    {
                        $push: { participants: userID },
                        $inc: { currCapacity: 1 },
                    },
                    this
                );
                let r = await this.acceptChatInvite(
                    userID,
                    chat._id,
                    chatEngine
                );
                return SUCCESS;
            } else {
                return CONFLICT;
            }
        } else {
            return NOTFOUND;
        }
    }

    async rejectEventInvite(userID, eventID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { eventInvites: eventID },
        });
    }

    async sendFriendRequest(userID, otherUserID) {
        console.log("IN THE SEND FRIEND REQUEST FUNCTION");
        let user = await this.findUserByID(userID);
        let otherUser = await this.findUserByID(otherUserID);
        if (user && otherUser) {
            if (
                !user.friends.includes(otherUserID) &&
                !otherUser.friends.includes(userID) &&
                !otherUser.friendRequest.includes(userID)
            ) {
                await User.findByIdAndUpdate(otherUserID, {
                    $push: { friendRequest: userID },
                });
                return SUCCESS;
            } else {
                return CONFLICT;
            }
        } else {
            return NOTFOUND;
        }
    }

    async rejectFriendRequest(userID, otherUserID) {
        await User.findByIdAndUpdate(userID, {
            $pull: { friendRequest: otherUserID },
        });
    }

    async sendChatInvite(userID, chatID, chatEngine) {
        let user = await this.findUserByID(userID);
        let chat = await chatEngine.findChatByID(chatID);
        if (user && chat) {
            if (
                !user.chats.includes(chatID) &&
                !chat.participants.includes(userID) &&
                !user.chatInvites.includes(chatID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { chatInvites: chatID },
                });
                return SUCCESS;
            } else {
                return CONFLICT;
            }
        } else {
            return NOTFOUND;
        }
    }

    async acceptFriendRequest(userID, otherUserID) {
        let user = await this.findUserByID(userID);
        let otherUser = await this.findUserByID(otherUserID);
        if (user && otherUser) {
            if (
                !user.friends.includes(otherUserID) &&
                !otherUser.friends.includes(userID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { friends: otherUserID },
                    $pull: { friendRequest: otherUserID },
                });
                await User.findByIdAndUpdate(otherUserID, {
                    $push: { friends: userID },
                });
                return SUCCESS;
            } else {
                return CONFLICT;
            }
        } else {
            return NOTFOUND;
        }
    }

    async findUnblockedUsers(userID) {
        let user = await this.findUserByID(userID);
        if (user) {
            return await User.find({
                _id: { $nin: user.blockedUsers },
            });
        }
        return null;
    }

    async addChat(chatID, chatInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $in: chatInfo.participants } },
                    { chats: { $ne: chatID } },
                ],
            },
            { $push: { chats: chatID } }
        );
    }

    async findUserByName(userName) {
        let capName = titleCase(userName);
        console.log(capName);
        return await User.find({
            name: { $regex: capName, $options: "i" },
        });
    }

    async removeChat(chatID, chatInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $nin: chatInfo.participants } },
                    { chats: chatID },
                ],
            },
            { $pull: { chats: chatID } }
        );
    }

    async addEvent(eventID, eventInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $in: eventInfo.participants } },
                    { events: { $ne: eventID } },
                ],
            },
            { $push: { events: eventID } }
        );
    }

    async removeEvent(eventID, eventInfo) {
        await User.updateMany(
            {
                $and: [
                    { _id: { $nin: eventInfo.participants } },
                    { events: eventID },
                ],
            },
            { $pull: { events: eventID } }
        );
    }

    async deleteUser(userID) {
        return await User.findByIdAndDelete(userID);
    }

    async removeFriend(userID, otherUserID) {
        await User.updateMany(
            { _id: { $in: [userID, otherUserID] } },
            { $pull: { friends: { $in: [userID, otherUserID] } } }
        );
    }

    async leaveEvent(userID, eventID, eventStore) {
        await User.findByIdAndUpdate(userID, {
            $pull: { $events: eventID },
        });
        await eventStore.removeUser(userID, this);
    }

    async leaveChat(userID, chatID, chatEngine) {
        await User.findByIdAndUpdate(userID, {
            $pull: { $chat: chatID },
        });
        await chatEngine.removeUser(userID, this);
    }

    async findUserForLogin(Token) {
        let user = await User.findOne({
            token: Token,
        });
        return user;
    }
}

function titleCase(str) {
    var splitStr = str.toLowerCase().split(" ");
    for (var i = 0; i < splitStr.length; i++) {
        splitStr[i] =
            splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
    }
    return splitStr.join(" ");
}

module.exports = UserStore;