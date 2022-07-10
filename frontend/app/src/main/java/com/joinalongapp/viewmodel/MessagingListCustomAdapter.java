package com.joinalongapp.viewmodel;

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

import com.joinalongapp.joinalong.R;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MessagingListCustomAdapter extends RecyclerView.Adapter<MessagingListCustomAdapter.ViewHolder>{
    private List<UserProfile> users;

    public MessagingListCustomAdapter(List<UserProfile> inputDataSet){
        users = inputDataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private Button options;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to their profile
                    Log.d("FragmentFriend", name.getText().toString());
                }
            });

            name = (TextView) itemView.findViewById(R.id.individualUserName);
            profilePicture = (ImageView) itemView.findViewById(R.id.individualProfilePicture);
            options = (Button) itemView.findViewById(R.id.friendOptions);
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
        holder.getName().setText(users.get(position).getFullName());
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
                                Log.d("FriendsAdapter", users.get(holder.getAdapterPosition()).getFullName());

                                Log.d("FriendsAdapter", "MENU1");
                                deleteFriend(users.get(holder.getAdapterPosition()).getId());
                                return true;

                            case R.id.menu2:
                                Log.d("FriendsAdapter", users.get(holder.getAdapterPosition()).getFullName());
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
        //holder.getProfilePicture().set
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void deleteFriend(UUID uuid){
        for (Iterator<UserProfile> iterator = users.iterator(); iterator.hasNext(); ) {
            UserProfile value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        // TODO: SEND BACKEND
        notifyDataSetChanged();

    }
}
