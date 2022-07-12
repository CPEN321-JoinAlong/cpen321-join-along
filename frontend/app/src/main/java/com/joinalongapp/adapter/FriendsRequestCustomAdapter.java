package com.joinalongapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.navbar.ViewProfileFragment;
import com.joinalongapp.viewmodel.Message;
import com.joinalongapp.viewmodel.UserProfile;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;

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
        Activity activity = (Activity) holder.itemView.getContext();
        holder.getAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile otherUser = users.get(holder.getBindingAdapterPosition());
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
                    requestManager.put("user/acceptUser/" + user.getId() + "/" + otherUser.getId(), json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {

                        }

                        @Override
                        public void onError(Call call, IOException e) {

                        }
                    });
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deleteRequest(users.get(holder.getBindingAdapterPosition()).getId());
                                }
                            });
                        }
                    }, 0, 1000);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        holder.getReject().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile otherUser = users.get(holder.getBindingAdapterPosition());
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
                    requestManager.put("user/rejectUser/" + user.getId() + "/" + otherUser.getId(), json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {


                        }

                        @Override
                        public void onError(Call call, IOException e) {

                        }
                    });

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deleteRequest(users.get(holder.getBindingAdapterPosition()).getId());
                                }
                            });
                        }
                    }, 0, 1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
                Bundle info = new Bundle();
                info.putBoolean("HIDE", true);
                info.putSerializable("USER_INFO", users.get(holder.getBindingAdapterPosition()));
                viewProfileFragment.setArguments(info);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout));
                fragmentTransaction.add(R.id.frame_layout, viewProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        Picasso.get().load(((UserApplicationInfo) (holder.itemView.getContext().getApplicationContext())).getProfile().getProfilePicture()).into(holder.getProfilePicture());
    }

    @Override
    public int getItemCount() {
        if(users == null){
            return 0;
        }
        else{
            return users.size();
        }
    }

    public void changeDataset(List<UserProfile> input){
        users = input;
        notifyDataSetChanged();
    }

    private void deleteRequest(String uuid){
        for (Iterator<UserProfile> iterator = users.iterator(); iterator.hasNext(); ) {
            UserProfile value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();

    }
}
