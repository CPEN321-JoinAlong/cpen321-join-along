package com.joinalongapp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.MessageActivity;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.ChatDetails;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;

public class MessagingListCustomAdapter extends RecyclerView.Adapter<MessagingListCustomAdapter.ViewHolder>{
    private List<ChatDetails> chatDetailsList;
    private Activity activity;
    private final int SINGLE_DIRECT_CHAT = 1;
    private final int GROUP_CHAT = 2;
    private final int DIRECT_CHAT_PICTURE_INDEX = 0;
    private final int FIRST_GROUP_SAMPLE_PROFILE_PICTURE_INDEX = 0;
    private final int SECOND_GROUP_SAMPLE_PROFILE_PICTURE_INDEX = 1;

    public MessagingListCustomAdapter(List<ChatDetails> inputDataSet, Activity activity){
        chatDetailsList = inputDataSet;
        this.activity = activity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private ImageView groupChatPictureFirst;
        private ImageView groupChatPictureSecond;
        private Button options;
        private CardView directChatPicture;
        private CardView groupChatFirst;
        private CardView groupChatSecond;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.individualListName);
            profilePicture = (ImageView) itemView.findViewById(R.id.individualProfilePicture);
            options = (Button) itemView.findViewById(R.id.reportOptions);
            groupChatPictureFirst = (ImageView) itemView.findViewById(R.id.groupChatProfilePicturePart1);
            groupChatPictureSecond = (ImageView) itemView.findViewById(R.id.groupChatProfilePicturePart2);
            directChatPicture = (CardView) itemView.findViewById(R.id.individualCardView);
            groupChatFirst = (CardView) itemView.findViewById(R.id.groupCardViewFirst);
            groupChatSecond = (CardView) itemView.findViewById(R.id.groupCardViewSecond);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getProfilePicture() {
            return profilePicture;
        }

        public ImageView getGroupChatPictureFirst() {
            return groupChatPictureFirst;
        }

        public ImageView getGroupChatPictureSecond() {
            return groupChatPictureSecond;
        }

        public Button getSettings(){ return options;}

        public CardView getDirectChatPicture() {
            return directChatPicture;
        }

        public CardView getGroupChatFirst() {
            return groupChatFirst;
        }

        public CardView getGroupChatSecond() {
            return groupChatSecond;
        }

    }

    @NonNull
    @Override
    public MessagingListCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent,false);
        return new MessagingListCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagingListCustomAdapter.ViewHolder holder, int position) {
        holder.getName().setText(chatDetailsList.get(position).getTitle());
        holder.getSettings().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.getSettings());
                popup.inflate(R.menu.friends_options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.menu1:
                                Log.d("FriendsAdapter", chatDetailsList.get(holder.getBindingAdapterPosition()).getTitle());
                                deleteMessage(chatDetailsList.get(holder.getAdapterPosition()).getId());
                                return true;

                            default:
                                return false;
                        }

                    }
                });
                popup.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), MessageActivity.class);
                i.putExtra("CHAT_DETAILS", chatDetailsList.get(holder.getBindingAdapterPosition()));
                activity.startActivity(i);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        ChatDetails chatDetails = chatDetailsList.get(holder.getBindingAdapterPosition());
        List<String> profilePictures = chatDetails.getProfileURLs();
        int numberOfOtherMembers = chatDetails.getProfileURLs().size();

        ImageView directChatProfilePicture = holder.getProfilePicture();
        ImageView groupChatProfilePictureFirst = holder.getGroupChatPictureFirst();
        ImageView groupChatProfilePictureSecond = holder.getGroupChatPictureSecond();
        CardView directChatPicture = holder.getDirectChatPicture();
        CardView groupChatPictureFirst = holder.getGroupChatFirst();
        CardView groupChatPictureSecond = holder.getGroupChatSecond();

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

        //Picasso.get().load(chatDetailsList.get(holder.getBindingAdapterPosition()).getProfilePicture()).into(holder.getProfilePicture());
        //holder.getProfilePicture().set
    }

    @Override
    public int getItemCount() {
        if(chatDetailsList == null){
            return 0;
        }
        else{
            return chatDetailsList.size();
        }
    }

    private void deleteMessage(String uuid){
        for (Iterator<ChatDetails> iterator = chatDetailsList.iterator(); iterator.hasNext(); ) {
            ChatDetails value = iterator.next();
            if (value.getId().toString() == uuid) {
                iterator.remove();
            }
        }
        // TODO: SEND BACKEND
        notifyDataSetChanged();

    }

    public void changeDataset(List<ChatDetails> input){
        chatDetailsList = input;
        notifyDataSetChanged();
    }
}
