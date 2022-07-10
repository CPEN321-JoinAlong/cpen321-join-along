package com.joinalongapp.viewmodel;

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
import com.joinalongapp.navbar.ViewChatFragment;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MessagingRequestCustomAdapter extends RecyclerView.Adapter<MessagingRequestCustomAdapter.ViewHolder> {
    private List<User> users;

    public MessagingRequestCustomAdapter(List<User> inputDataSet){
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
                    ViewChatFragment viewChatFragment = new ViewChatFragment();
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout));
                    fragmentTransaction.add(R.id.frame_layout, viewChatFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
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
    public MessagingRequestCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent,false);
        return new MessagingRequestCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagingRequestCustomAdapter.ViewHolder holder, int position) {
        holder.getName().setText(users.get(position).getName());
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
        for (Iterator<User> iterator = users.iterator(); iterator.hasNext(); ) {
            User value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        // TODO: SEND BACKEND
        notifyDataSetChanged();

    }
}
