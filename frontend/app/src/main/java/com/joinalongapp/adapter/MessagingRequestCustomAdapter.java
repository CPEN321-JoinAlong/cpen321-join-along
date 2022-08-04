package com.joinalongapp.adapter;

import static com.joinalongapp.FeedbackMessageBuilder.createServerConnectionError;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.joinalong.ViewChatActivity;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.viewmodel.UserProfile;
import com.squareup.picasso.Picasso;

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
    private List<ChatDetails> chatDetailsList;
    private final int SINGLE_DIRECT_CHAT = 1;
    private final int GROUP_CHAT = 2;
    private final int DIRECT_CHAT_PICTURE_INDEX = 0;
    private final int FIRST_GROUP_SAMPLE_PROFILE_PICTURE_INDEX = 0;
    private final int SECOND_GROUP_SAMPLE_PROFILE_PICTURE_INDEX = 1;

    public MessagingRequestCustomAdapter(List<ChatDetails> inputDataSet){
        chatDetailsList = inputDataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private ImageView groupChatImageFirst;
        private ImageView groupChatImageSecond;
        private CardView directChatCardView;
        private CardView groupChatCardViewFirst;
        private CardView groupChatCardViewSecond;
        private Button accept;
        private Button reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.individualRequestName);
            profilePicture = (ImageView) itemView.findViewById(R.id.requestIndividualProfilePicture);
            accept = (Button) itemView.findViewById(R.id.acceptButton);
            reject = (Button) itemView.findViewById(R.id.rejectButton);
            groupChatImageFirst = (ImageView) itemView.findViewById(R.id.groupChatRequestProfilePictureFirst);
            groupChatImageSecond = (ImageView) itemView.findViewById(R.id.groupChatRequestProfilePictureSecond);
            directChatCardView = (CardView) itemView.findViewById(R.id.requestIndividualCardView);
            groupChatCardViewFirst = (CardView)  itemView.findViewById(R.id.groupRequestCardViewFirst);
            groupChatCardViewSecond = (CardView)  itemView.findViewById(R.id.groupRequestCardViewSecond);
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

        public ImageView getGroupChatImageFirst() {
            return groupChatImageFirst;
        }

        public ImageView getGroupChatImageSecond() {
            return groupChatImageSecond;
        }

        public CardView getGroupChatCardViewFirst() {
            return groupChatCardViewFirst;
        }

        public CardView getGroupChatCardViewSecond() {
            return groupChatCardViewSecond;
        }

        public CardView getDirectChatCardView() {
            return directChatCardView;
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
        holder.getName().setText(chatDetailsList.get(position).getTitle());
        Activity activity = (Activity) holder.itemView.getContext();
        holder.getAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatDetails otherChat = chatDetailsList.get(holder.getBindingAdapterPosition());
                UserProfile user = ((UserApplicationInfo) v.getContext().getApplicationContext()).getProfile();
                String userToken = ((UserApplicationInfo) v.getContext().getApplicationContext()).getUserToken();
                String operation = "Accept Chat";

                JSONObject json = new JSONObject();
                try {
                    json.put("token", userToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode("acceptChat")
                            .addNode(user.getId())
                            .addNode(otherChat.getId())
                            .build();

                    new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {

                            if (response.isSuccessful()) {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                deleteRequest(chatDetailsList.get(holder.getBindingAdapterPosition()).getId());
                                            }
                                        });
                                    }
                                }, 0);
                            } else {
                                ResponseErrorHandler.createErrorMessage(response, operation, "chat", activity);
                            }

                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            createServerConnectionError(e, operation, activity);
                        }
                    });
                } catch (IOException e) {
                    createServerConnectionError(e, operation, activity);
                }
            }
        });

        holder.getReject().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatDetails otherChat = chatDetailsList.get(holder.getBindingAdapterPosition());
                UserProfile user = ((UserApplicationInfo) v.getContext().getApplicationContext()).getProfile();
                String userToken = ((UserApplicationInfo) v.getContext().getApplicationContext()).getUserToken();
                String operation = "Reject Chat Request";

                JSONObject json = new JSONObject();
                try {
                    json.put("token", userToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode("rejectChat")
                            .addNode(user.getId())
                            .addNode(otherChat.getId())
                            .build();

                    new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            if (response.isSuccessful()) {
                                deleteRequest(chatDetailsList.get(holder.getBindingAdapterPosition()).getId());
                            } else {
                                ResponseErrorHandler.createErrorMessage(response, operation, "chat", activity);
                            }
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            createServerConnectionError(e, operation, activity);
                        }
                    });
                } catch (IOException e) {
                    createServerConnectionError(e, operation, activity);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ViewChatActivity.class);
                i.putExtra("CHAT_INFO", chatDetailsList.get(holder.getBindingAdapterPosition()));
                v.getContext().startActivity(i);
            }
        });

        ChatDetails chatDetails = chatDetailsList.get(holder.getBindingAdapterPosition());
        int numberOfOtherMembers = chatDetails.getProfileURLs().size();
        List<String> profilePictures = chatDetails.getProfileURLs();

        ImageView directChatProfilePicture = holder.getProfilePicture();
        ImageView groupChatProfilePictureFirst = holder.getGroupChatImageFirst();
        ImageView groupChatProfilePictureSecond = holder.getGroupChatImageSecond();
        CardView directChatPicture = holder.getDirectChatCardView();
        CardView groupChatPictureFirst = holder.getGroupChatCardViewFirst();
        CardView groupChatPictureSecond = holder.getGroupChatCardViewSecond();

        if(numberOfOtherMembers == SINGLE_DIRECT_CHAT){
            Picasso.get().load(profilePictures.get(DIRECT_CHAT_PICTURE_INDEX)).into(directChatProfilePicture);
            groupChatPictureFirst.setVisibility(View.INVISIBLE);
            groupChatPictureSecond.setVisibility(View.INVISIBLE);
        } else if(numberOfOtherMembers >= GROUP_CHAT){
            directChatPicture.setVisibility(View.INVISIBLE);
            Picasso.get().load(profilePictures.get(FIRST_GROUP_SAMPLE_PROFILE_PICTURE_INDEX)).into(groupChatProfilePictureFirst);
            Picasso.get().load(profilePictures.get(SECOND_GROUP_SAMPLE_PROFILE_PICTURE_INDEX)).into(groupChatProfilePictureSecond);
        } else{
            groupChatPictureFirst.setVisibility(View.INVISIBLE);
            groupChatPictureSecond.setVisibility(View.INVISIBLE);
        }
        //holder.getProfilePicture().set
    }

    @Override
    public int getItemCount() {
        if (chatDetailsList == null) {
            return 0;
        }
        return chatDetailsList.size();
    }

    public void changeDataset(List<ChatDetails> input){
        chatDetailsList = input;
        notifyDataSetChanged();
    }

    private void deleteRequest(String uuid){
        for (Iterator<ChatDetails> iterator = chatDetailsList.iterator(); iterator.hasNext(); ) {
            ChatDetails value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        // TODO: SEND BACKEND
        notifyDataSetChanged();

    }
}
