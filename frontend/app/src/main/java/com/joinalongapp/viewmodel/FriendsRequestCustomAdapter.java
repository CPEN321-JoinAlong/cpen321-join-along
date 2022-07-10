package com.joinalongapp.viewmodel;

import android.util.Log;
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

import com.joinalongapp.joinalong.R;
import com.joinalongapp.navbar.ViewProfileFragment;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FriendsRequestCustomAdapter extends RecyclerView.Adapter<FriendsRequestCustomAdapter.ViewHolder>{
    private List<UserProfile> users;

    public FriendsRequestCustomAdapter(List<UserProfile> inputDataSet){
        users = inputDataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView profilePicture;
        private Button accept;
        private Button reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout));
                    fragmentTransaction.add(R.id.frame_layout, viewProfileFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    Log.d("FragmentFriend", name.getText().toString());
                }
            });

            name = (TextView) itemView.findViewById(R.id.individualUserName);
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
    public FriendsRequestCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent,false);
        return new FriendsRequestCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsRequestCustomAdapter.ViewHolder holder, int position) {
        holder.getName().setText(users.get(position).getFullName());
        holder.getAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(users.get(holder.getAdapterPosition()).getId());
            }
        });
        holder.getReject().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(users.get(holder.getAdapterPosition()).getId());
            }
        });
        //holder.getProfilePicture().set
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void deleteRequest(UUID uuid){
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
