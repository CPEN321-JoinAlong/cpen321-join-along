package com.joinalongapp.adapter;

import static com.joinalongapp.FeedbackMessageBuilder.createDefaultNeutralInvalidErrorOnHttp422;
import static com.joinalongapp.FeedbackMessageBuilder.createDefaultNeutralNotFoundErrorOnHttp404;
import static com.joinalongapp.FeedbackMessageBuilder.createServerConnectionError;
import static com.joinalongapp.FeedbackMessageBuilder.createServerInternalError;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_200;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_404;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_422;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_500;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.navbar.ViewProfileFragment;
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

public class FriendsListCustomAdapter extends RecyclerView.Adapter<FriendsListCustomAdapter.ViewHolder>{

    private List<UserProfile> users;

    public FriendsListCustomAdapter(List<UserProfile> inputDataSet){
        users = inputDataSet;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
                                try {
                                    deleteFriend(users.get(holder.getAdapterPosition()).getId(), v.getContext());
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }

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
                ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
                Bundle info = new Bundle();
                info.putBoolean("HIDE", false);
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
        Picasso.get().load(users.get(holder.getBindingAdapterPosition()).getProfilePicture()).into(holder.getProfilePicture());
    }

    @Override
    public int getItemCount() {
        if(users == null){
            return 0;
        }
        return users.size();
    }

    private void deleteFriend(String uuid, Context context) throws JSONException, IOException {
        UserProfile user = ((UserApplicationInfo) context.getApplicationContext()).getProfile();
        String token = ((UserApplicationInfo) context.getApplicationContext()).getUserToken();
        String userId = user.getId();

        JSONObject json = new JSONObject();
        json.put("token", token);

        String path = new PathBuilder()
                .addUser()
                .addNode("removeFriend")
                .addNode(userId)
                .addNode(uuid)
                .build();

        RequestManager requestManager = new RequestManager();
        new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {
                //Toast.makeText(context, "Deleted Friend!", Toast.LENGTH_SHORT).show();
                switch(response.code()) {
                    case STATUS_HTTP_200:
                        removeFriendFromViewList(uuid, (Activity) context);
                        break;
                    case STATUS_HTTP_404:
                        createDefaultNeutralNotFoundErrorOnHttp404("Remove Friend", "user", (Activity) context);
                        break;
                    case STATUS_HTTP_422:
                        createDefaultNeutralInvalidErrorOnHttp422("Remove Friend", (Activity) context);
                        break;
                    case STATUS_HTTP_500:
                    default:
                        createServerInternalError("Remove Friend", (Activity) context);
                        break;
                }
            }

            @Override
            public void onError(Call call, IOException e) {
                createServerConnectionError(e, "Remove Friend", (Activity) context);
            }
        });
    }

    private void removeFriendFromViewList(String uuid, Activity activity) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Iterator<UserProfile> iterator = users.iterator(); iterator.hasNext(); ) {
                            UserProfile value = iterator.next();
                            if (value.getId() == uuid) {
                                iterator.remove();
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        }, 0);
    }

    public void changeDataset(List<UserProfile> input){
        users = input;
        notifyDataSetChanged();
    }
}
