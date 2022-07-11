package com.joinalongapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Message;

import java.util.List;

public class MessageListCustomAdapter extends RecyclerView.Adapter {
    private List<Message> messages;
    private int MESSAGE_SENT = 1;
    private int MESSAGE_RECEIVED = 2;

    public MessageListCustomAdapter(List<Message> messages) {
        System.out.println("here");
        this.messages = messages;
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        TextView receivedMessage;
        TextView receivedMessageName;
        TextView receivedMessageTime;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessage = itemView.findViewById(R.id.receivedMessage);
            receivedMessageName = itemView.findViewById(R.id.nameReceivedMessage);
            receivedMessageTime = itemView.findViewById(R.id.timeReceivedMessage);
            System.out.println("received");
        }

        public TextView getReceivedMessage() {
            return receivedMessage;
        }

        public TextView getReceivedMessageName() {
            return receivedMessageName;
        }

        public TextView getReceivedMessageDate() {
            return receivedMessageTime;
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder{
        TextView sentMessage;
        TextView sentMessageTime;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sentMessage);
            sentMessageTime = itemView.findViewById(R.id.timeSentMessage);
            System.out.println("sent");
        }

        public TextView getSentMessage() {
            return sentMessage;
        }

        public TextView getSentMessageTime() {
            return sentMessageTime;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messages.get(position);
        if(message.isOwner()){
            return MESSAGE_SENT;
        }
        else{
            return MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;


        if (viewType == MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message_item, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_message_item, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) messages.get(holder.getBindingAdapterPosition());

        System.out.println("onbind");

        if(holder.getItemViewType() == MESSAGE_SENT){
            ((SentMessageHolder) holder).getSentMessage().setText(message.getMessage());
            ((SentMessageHolder) holder).getSentMessageTime().setText(String.valueOf(message.getCreatedAt()));
        }
        else{
            ((ReceivedMessageHolder) holder).getReceivedMessage().setText(message.getMessage());
            ((ReceivedMessageHolder) holder).getReceivedMessageDate().setText(String.valueOf(message.getCreatedAt()));
            ((ReceivedMessageHolder) holder).getReceivedMessageName().setText(message.getName());
        }




    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
