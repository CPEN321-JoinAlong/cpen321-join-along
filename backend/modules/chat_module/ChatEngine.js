const Chat = require("./../../models/Chat");
const ERROR_CODES = require("./../../ErrorCodes.js")//isn't error codes in modules? so ./../ErrorCodes.js?
const ResponseObject = require("./../../ResponseObject")

class ChatEngine {
    async findChatByID(chatID) {
        if (!mongoose.isObjectIdOrHexString(chatID)) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }
        console.log(chatID);
        let foundChat = await Chat.findById(chatID);
        if(foundChat) return new ResponseObject(ERROR_CODES.SUCCESS, foundChat)
        else return new ResponseObject(ERROR_CODES.NOTFOUND)
    }

    async findChatByIDList(chatIDList) {
        if(!chatIDList.every((id) => mongoose.isObjectIdOrHexString(id)))
            return new ResponseObject(ERROR_CODES.INVALID, [])
        let chatList = await Chat.find({
            _id: { $in: chatIDList },
        });
        if(chatList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, chatList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, chatList)
    }

    async findChatByUser(userID) {
        if (!mongoose.isObjectIdOrHexString(userID)) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }
        let chatList = await Chat.find({
            participants: userID,
        });
        if(chatList.length !== 0) return new ResponseObject(ERROR_CODES.SUCCESS, chatList)
        else return new ResponseObject(ERROR_CODES.NOTFOUND, chatList)
    }

    //send Message to a chat object
    async sendChatMessage(userID, chatID, text, name, date, userStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }
        let chat = await Chat.findById(chatID);
        let userResponse = await userStore.findUserByID(userID)
        if (userResponse.data && chat) {
            let response = await Chat.findByIdAndUpdate(
                chatID,
                {
                    $push: {
                        messages: {
                            participantID: userID,
                            participantName: name,
                            timeStamp: date,
                            text,
                        },
                    },
                },
                {
                    new: true,
                }
            );
            return new ResponseObject(ERROR_CODES.SUCCESS, response)
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND)
        }
    }

    async createChat(chatInfo, userStore) {
        let chatObject = await new Chat(chatInfo).save();
        chatObject.participants.forEach(async (participant) => {
            if(mongoose.isObjectIdOrHexString(participant)) {
                await userStore.updateUserAccount(participant, {
                    $push: {chats: chatObject._id}
                });
            }
        });
        return new ResponseObject(ERROR_CODES.SUCCESS, chatObject);
    }

    async removeUser(chatID, userID, userStore) {
        if (
            !mongoose.isObjectIdOrHexString(userID) ||
            !mongoose.isObjectIdOrHexString(chatID)
        ) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }
        let chatResponse = await this.findChatByID(chatID);
        let userResponse = await userStore.findUserByID(userID);
        if(userResponse.data && chatResponse.data) {
            await this.editChat(
                chatID,
                {
                    $pull: { participants: userID },
                    $dec: { currCapacity: 1 },
                },
                userStore
            );
            return new ResponseObject(ERROR_CODES.SUCCESS)
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND)
        }
    }

    async editChat(chatID, chatInfo, userStore) {
        if (!mongoose.isObjectIdOrHexString(chatID)) {
            return new ResponseObject(ERROR_CODES.INVALID)
        }
        let chat = await Chat.findByIdAndUpdate(chatID, chatInfo, {
            new: true,
        });
        if (chat) {
            await userStore.addChat(chatID, chat);
            await userStore.removeChat(chatID, chat);
            return new ResponseObject(ERROR_CODES.SUCCESS, chat)
        } else {
            return new ResponseObject(ERROR_CODES.NOTFOUND)
        }
        
    }
}

module.exports = ChatEngine;