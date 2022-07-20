const Chat = require("./../../models/Chat");
const ERROR_CODES = require("./../../ErrorCodes.js")//isn't error codes in modules? so ./../ErrorCodes.js?

class ChatEngine {
    async findChatByID(chatID) {
        console.log(chatID);
        let r = await Chat.findById(chatID);
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async findChatByIDList(chatIDList) {
        let r = await Chat.find({
            _id: { $in: chatIDList },
        });
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async findChatByUser(userID) {
        let r = await Chat.find({
            participants: userID,
        });
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    //send Message to a chat object
    async sendChatMessage(userID, chatID, text, name, date, userStore) {
        let chat = await Chat.findById(chatID);
        let user = await userStore.findUserByID(userID);
        if (user && chat) {
            let r = await Chat.findByIdAndUpdate(
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
            return { status: ERROR_CODES.SUCCESS, data: r };
        } else {
            return { status: ERROR_CODES.NOTFOUND, data: null };
        }
    }

    //Assumes both users exist
    async sendMessage(fromUserID, toUserID, text, name, date) {
        let r = await Chat.findOneAndUpdate(
            {
                $and: [
                    {
                        event: null,
                    },
                    {
                        participants: {
                            $all: [fromUserID, toUserID],
                        },
                    },
                ],
            },
            {
                $push: {
                    messages: {
                        participantID: fromUserID,
                        participantName: name,
                        timeStamp: date,
                        text,
                    },
                },
            }
        );
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async sendGroupMessage(userID, eventID, text, name, date) {
        let r = await Chat.findOneAndUpdate(
            {
                event: eventID,
            },
            {
                $push: {
                    messages: {
                        participantID: userID,
                        participantName: name,
                        timeStamp: date,
                        text,
                    },
                },
            }
        );
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async createChat(chatInfo, userStore) {
        let chatObject = await new Chat(chatInfo).save();
        chatObject.participants.forEach(async (participant) => {
            let user = await userStore.findUserByID(participant);
            if (user) {
                user.chats.push(chatObject._id);
                await userStore.updateUserAccount(participant, user);
            }
        });
        return { status: ERROR_CODES.SUCCESS, data: chatObject };
    }

    async removeUser(chatID, userID, userStore) {
        let r = await this.editChat(
            chatID,
            {
                $pull: { participants: userID },
                $dec: { currCapacity: 1 },
            },
            userStore
        );
        return { status: ERROR_CODES.SUCCESS, data: r };
    }

    async editChat(chatID, chatInfo, userStore) {
        let chat = await Chat.findByIdAndUpdate(chatID, chatInfo, {
            new: true,
        });
        if (chat) {
            await userStore.addChat(chatID, chat);
            await userStore.removeChat(chatID, chat);
        }
        let r = await Chat.findById(chatID);
        return { status: ERROR_CODES.SUCCESS, data: r };
    }
}

module.exports = ChatEngine;