const Chat = require("./../../models/Chat");
const mongoose = require("mongoose");
const CONFLICT = 409;
const NOTFOUND = 404;
const SUCCESS = 200;
const INVALID = 422;

class ChatEngine {
    async findChatByID(chatID) {
        console.log(chatID);
        return await Chat.findById(chatID);
    }

    async findChatByIDList(chatIDList) {
        return await Chat.find({
            _id: { $in: chatIDList },
        });
    }

    async findChatByUser(userID) {
        return await Chat.find({
            participants: userID,
        });
    }

    //send Message to a chat object
    async sendChatMessage(userID, chatID, text, name, date, userStore) {
        let chat = await Chat.findById(chatID);
        let user = await userStore.findUserByID(userID);
        if (user && chat) {
            return await Chat.findByIdAndUpdate(
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
        } else {
            return null;
        }
    }

    //Assumes both users exist
    async sendMessage(fromUserID, toUserID, text, name, date) {
        await Chat.findOneAndUpdate(
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
    }

    async sendGroupMessage(userID, eventID, text, name, date) {
        await Chat.findOneAndUpdate(
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
        return chatObject;
    }

    async removeUser(chatID, userID, userStore) {
        await this.editChat(
            chatID,
            {
                $pull: { participants: userID },
                $dec: { currCapacity: 1 },
            },
            userStore
        );
    }

    async editChat(chatID, chatInfo, userStore) {
        let chat = await Chat.findByIdAndUpdate(chatID, chatInfo, {
            new: true,
        });
        if (chat) {
            await userStore.addChat(chatID, chat);
            await userStore.removeChat(chatID, chat);
        }
        return await Chat.findById(chatID);
    }
}

module.exports = ChatEngine;