package com.joinalongapp.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.navbar.ViewChatFragment;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class MessagingRequestCustomAdapter extends RecyclerView.Adapter<MessagingRequestCustomAdapter.ViewHolder> {
    private List<ChatDetails> chatDetails;

    public MessagingRequestCustomAdapter(List<ChatDetails> inputDataSet){
        chatDetails = inputDataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private Button accept;
        private Button reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.individualReportName);
            profilePicture = (ImageView) itemView.findViewById(R.id.individualProfilePicture);
            accept = (Button) itemView.findViewById(R.id.acceptButton);
            reject = (Button) itemView.findViewById(R.id.rejectButton);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getProfilePicture() {
            return profilePicture;
        }

        public Button getAccept() {
            return accept;
        }

        public Button getReject() {
            return reject;
        }
    }

    @NonNull
    @Override
    public MessagingRequestCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent,false);
        return new MessagingRequestCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagingRequestCustomAdapter.ViewHolder holder, int position) {
        holder.getName().setText(chatDetails.get(position).getTitle());
        Activity activity = (Activity) holder.itemView.getContext();
        holder.getAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatDetails otherChat = chatDetails.get(holder.getBindingAdapterPosition());
                UserProfile user = ((UserApplicationInfo) v.getContext().getApplicationContext()).getProfile();
                String userToken = ((UserApplicationInfo) v.getContext().getApplicationContext()).getUserToken();
                JSONObject json = new JSONObject();
                try {
                    json.put("token", userToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestManager requestManager = new RequestManager();
                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode("acceptChat")
                            .addNode(user.getId())
                            .addNode(otherChat.getId())
                            .build();

                    requestManager.put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            deleteRequest(chatDetails.get(holder.getBindingAdapterPosition()).getId());
                                        }
                                    });
                                }
                            }, 0);
                        }

                        @Override
                        public void onError(Call call, IOException e) {

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.getReject().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatDetails otherChat = chatDetails.get(holder.getBindingAdapterPosition());
                UserProfile user = ((UserApplicationInfo) v.getContext().getApplicationContext()).getProfile();
                String userToken = ((UserApplicationInfo) v.getContext().getApplicationContext()).getUserToken();
                JSONObject json = new JSONObject();
                try {
                    json.put("token", userToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestManager requestManager = new RequestManager();
                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode("rejectChat")
                            .addNode(user.getId())
                            .addNode(otherChat.getId())
                            .build();

                    requestManager.put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            deleteRequest(chatDetails.get(holder.getBindingAdapterPosition()).getId());
                        }

                        @Override
                        public void onError(Call call, IOException e) {

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewChatFragment viewChatFragment = new ViewChatFragment();
                Bundle info = new Bundle();
                info.putSerializable("CHAT_INFO", chatDetails.get(holder.getBindingAdapterPosition()));
                viewChatFragment.setArguments(info);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout));
                fragmentTransaction.add(R.id.frame_layout, viewChatFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        //holder.getProfilePicture().set
    }

    @Override
    public int getItemCount() {
        if (chatDetails == null) {
            return 0;
        }
        return chatDetails.size();
    }

    public void changeDataset(List<ChatDetails> input){
        chatDetails = input;
        notifyDataSetChanged();
    }

    private void deleteRequest(String uuid){
        for (Iterator<ChatDetails> iterator = chatDetails.iterator(); iterator.hasNext(); ) {
            ChatDetails value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        // TODO: SEND BACKEND
        notifyDataSetChanged();

    }
}
