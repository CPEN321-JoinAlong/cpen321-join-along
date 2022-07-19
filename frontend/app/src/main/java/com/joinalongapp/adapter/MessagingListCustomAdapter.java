package com.joinalongapp.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.MessageActivity;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.ChatDetails;

import java.util.Iterator;
import java.util.List;

public class MessagingListCustomAdapter extends RecyclerView.Adapter<MessagingListCustomAdapter.ViewHolder>{
    private List<ChatDetails> chatDetailsList;

    public MessagingListCustomAdapter(List<ChatDetails> inputDataSet){
        chatDetailsList = inputDataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private Button options;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);



            name = (TextView) itemView.findViewById(R.id.individualReportName);
            profilePicture = (ImageView) itemView.findViewById(R.id.individualProfilePicture);
            options = (Button) itemView.findViewById(R.id.reportOptions);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getProfilePicture() {
            return profilePicture;
        }

        public Button getSettings(){ return options;}

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

                                Log.d("FriendsAdapter", "MENU1");
                                //deleteFriend(chatDetailsList.get(holder.getAdapterPosition()).getId());
                                return true;

                            case R.id.menu2:
                                Log.d("FriendsAdapter", chatDetailsList.get(holder.getBindingAdapterPosition()).getTitle());
                                Log.d("FriendsAdapter", "MENU2");
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
                v.getContext().startActivity(i);
            }
        });
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

    private void deleteFriend(String uuid){
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
