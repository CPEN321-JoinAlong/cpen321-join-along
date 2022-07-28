const User = require("./../../models/User");
const mongoose = require("mongoose");
const ERROR_CODES = require("./../../ErrorCodes.js");
const ResponseObject = require("./../../ResponseObject");

class UserStore {
    // async findUserByEvent(eventID) {
    //     return await User.find({
    //         events: { $in: [eventID] },
    //     });
    // }

    async findUserByID(userID) {
        if (!mongoose.isObjectIdOrHexString(userID)) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        // console.log(userID);
        let foundUser = await User.findById(userID);
        if (foundUser)
            return new ResponseObject(ERROR_CODES.SUCCESS, foundUser);
        else return new ResponseObject(ERROR_CODES.NOTFOUND);
    }

    async findAllUsers() {
        let userList = await User.find({});
        if (userList.length !== 0)
            return new ResponseObject(ERROR_CODES.SUCCESS, userList);
        else return new ResponseObject(ERROR_CODES.NOTFOUND, userList);
    }

    async updateUserAccount(userID, userInfo) {
        if (!mongoose.isObjectIdOrHexString(userID)) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        console.log("IN UPDATE USER ACCOUNT");
        console.log(userID);
        console.log(userInfo);
        let foundUser = await User.findByIdAndUpdate(userID, userInfo, {
            new: true,
        });
        if (foundUser)
            return new ResponseObject(ERROR_CODES.SUCCESS, foundUser);
        else return new ResponseObject(ERROR_CODES.NOTFOUND);
    }

    async createUser(userInfo) {
        let newUser = await new User(userInfo).save();
        return new ResponseObject(ERROR_CODES.SUCCESS, newUser);
    }

    async findFriendByIDList(friendIDList) {
        if (!friendIDList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return new ResponseObject(ERROR_CODES.INVALID, []);
        let friendList = await User.find({
            _id: {
                $in: friendIDList,
            },
        });
        if (friendList.length !== 0)
            return new ResponseObject(ERROR_CODES.SUCCESS, friendList);
        else return new ResponseObject(ERROR_CODES.NOTFOUND, friendList);
    }

    async findChatInvites(chatReqList, chatEngine) {
        if (!chatReqList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return new ResponseObject(ERROR_CODES.INVALID, []);
        let response = await chatEngine.findChatByIDList(chatReqList);
        return response;
    }

    async acceptChatInvite(userID, chatID, chatEngine) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID, null);
        }
        let chat = await chatEngine.findChatByID(chatID);
        let user = await this.findUserByID(userID);
        console.log(chat);
        console.log(user);
        if (chat.data && user.data) {
            if (
                chat.data.currCapacity < chat.data.numberOfPeople &&
                !user.data.chats.includes(chatID) &&
                !chat.data.participants.includes(userID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { chat: chatID },
                    $pull: { chatInvites: chatID },
                });
                await chatEngine.editChat(
                    chatID,
                    {
                        $push: { participants: userID },
                        $inc: { currCapacity: 1 },
                    },
                    this
                );
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async rejectChatInvite(userID, chatID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let user = await this.findUserByID(userID);
        if (user.data) {
            if (user.data.chatInvites.includes(chatID)) {
                await User.findByIdAndUpdate(userID, {
                    $pull: { chatInvites: chatID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async acceptEventInvite(userID, eventID, eventStore, chatEngine) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let event = await eventStore.findEventByID(eventID);
        // console.log(event)
        let user = await this.findUserByID(userID);
        let chat = event.data ? await chatEngine.findChatByID(event.data.chat) : null;
        if (event.data && user.data && chat.data) {
            if (
                event.data.currCapacity < event.data.numberOfPeople &&
                !user.data.events.includes(eventID) &&
                !event.data.participants.includes(userID)
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

                // console.log("GOING INTO JOINING CHAT");

                let acceptedChatInvite = await this.acceptChatInvite(
                    userID,
                    chat.data._id,
                    chatEngine
                );
                // console.log(acceptedChatInvite);

                return new ResponseObject(
                    ERROR_CODES.SUCCESS,
                    acceptedChatInvite.data
                );
                
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            // console.log("NOT FOUND");
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async rejectEventInvite(userID, eventID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let userResponse = await this.findUserByID(userID);
        if (userResponse.data) {
            if (userResponse.data.eventInvites.includes(eventID)) {
                await User.findByIdAndUpdate(userID, {
                    $pull: { eventInvites: eventID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async sendFriendRequest(userID, otherUserID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(otherUserID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        // console.log("IN THE SEND FRIEND REQUEST FUNCTION");
        let user = await this.findUserByID(userID);
        let otherUser = await this.findUserByID(otherUserID);
        console.log(user)
        console.log(otherUser)
        if (user.data && otherUser.data) {
            if (
                !user.data.friends.includes(otherUserID) &&
                !otherUser.data.friends.includes(userID) &&
                !otherUser.data.friendRequest.includes(userID)
            ) {
                await User.findByIdAndUpdate(otherUserID, {
                    $push: { friendRequest: userID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async rejectFriendRequest(userID, otherUserID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(otherUserID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let userResponse = await this.findUserByID(userID);
        // console.log(userResponse)
        if (userResponse.data) {
            if (userResponse.data.friendRequest.includes(otherUserID)) {
                await User.findByIdAndUpdate(userID, {
                    $pull: { friendRequest: otherUserID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async sendChatInvite(userID, chatID, chatEngine) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let user = await this.findUserByID(userID);
        let chat = await chatEngine.findChatByID(chatID);
        if (user.data && chat.data) {
            if (
                !user.data.chats.includes(chatID) &&
                !chat.data.participants.includes(userID) &&
                !user.data.chatInvites.includes(chatID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { chatInvites: chatID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async acceptFriendRequest(userID, otherUserID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(otherUserID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let user = await this.findUserByID(userID);
        let otherUser = await this.findUserByID(otherUserID);
        if (user.data && otherUser.data) {
            if (
                !user.data.friends.includes(otherUserID) &&
                !otherUser.data.friends.includes(userID)
            ) {
                await User.findByIdAndUpdate(userID, {
                    $push: { friends: otherUserID },
                    $pull: { friendRequest: otherUserID },
                });
                await User.findByIdAndUpdate(otherUserID, {
                    $push: { friends: userID },
                });
                return new ResponseObject(ERROR_CODES.SUCCESS);
            } else {
                return new ResponseObject(ERROR_CODES.CONFLICT);
            }
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async findUnblockedUsers(userID) {
        if (!mongoose.isObjectIdOrHexString(userID)) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let user = await this.findUserByID(userID);
        if (user.data) {
            let unblockedUsers = await User.find({
                _id: { $nin: user.blockedUsers },
            });
            if (unblockedUsers.length !== 0)
                return new ResponseObject(ERROR_CODES.SUCCESS, unblockedUsers);
            else
                return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
        return new ResponseObject(ERROR_CODES.NOTFOUND);
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

        return new ResponseObject(ERROR_CODES.SUCCESS);
    }

    async findUserByName(userName) {
        let capName = titleCase(userName);
        console.log(capName);
        let foundUserList = await User.find({
            name: { $regex: capName, $options: "i" },
        });
        if (foundUserList.length !== 0)
            return new ResponseObject(ERROR_CODES.SUCCESS, foundUserList);
        else return new ResponseObject(ERROR_CODES.NOTFOUND, foundUserList);
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

        return new ResponseObject(ERROR_CODES.SUCCESS);
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

        return new ResponseObject(ERROR_CODES.SUCCESS);
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

        return new ResponseObject(ERROR_CODES.SUCCESS);
    }

    async deleteUser(userID) {
        if (!mongoose.isObjectIdOrHexString(userID))
            return new ResponseObject(ERROR_CODES.INVALID);
        let user = await User.findById(userID);
        if (user) {
            let friendList = user.friends.filter((id) =>
                mongoose.isObjectIdOrHexString(id)
            );
            friendList.forEach(async (friendID) => {
                await this.updateUserAccount(friendID, {
                    $pull: { friends: friendID },
                });
            });
            await User.findByIdAndDelete(userID);
            return new ResponseObject(ERROR_CODES.SUCCESS);
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async removeFriend(userID, otherUserID) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(otherUserID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let userResponse = await this.findUserByID(userID);
        let otherUserResponse = await this.findUserByID(otherUserID);
        if (userResponse.data && otherUserResponse.data) {
            await User.updateMany(
                { _id: { $in: [userID, otherUserID] } },
                { $pull: { friends: { $in: [userID, otherUserID] } } }
            );
            return new ResponseObject(ERROR_CODES.SUCCESS);
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async leaveEvent(userID, eventID, eventStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(eventID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        // console.log("IN LEAVE EVENT");
        let user = await User.findByIdAndUpdate(userID, {
            $pull: { $events: eventID },
        });
        if (user) {
            // console.log("IN LEAVE EVENT");
            let response = await eventStore.removeUser(eventID, userID, this);
            // console.log(response);
            return new ResponseObject(ERROR_CODES.SUCCESS);
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async leaveChat(userID, chatID, chatEngine) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID);
        }
        let user = await User.findByIdAndUpdate(userID, {
            $pull: { $chat: chatID },
        });
        // console.log(user)
        if (user) {
            let response = await chatEngine.removeUser(chatID, userID, this);
            // console.log("IN LEAVE CHAT");
            // console.log(response);
            return new ResponseObject(ERROR_CODES.SUCCESS);
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND);
        }
    }

    async findUserForLogin(Token) {
        let user = await User.findOne({
            token: Token,
        });
        if (user) return new ResponseObject(ERROR_CODES.SUCCESS, user);
        else return new ResponseObject(ERROR_CODES.NOTFOUND);
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
