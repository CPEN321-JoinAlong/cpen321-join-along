package com.joinalongapp.joinalong;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
import com.joinalongapp.viewmodel.ChatDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class ViewChatActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ChatDetails chatDetails;
    private TextView title;
    private TextView description;
    private ChipGroup tags;
    private ChipGroup friends;
    private ImageButton chatMenu;
    private PopupMenu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chat);
        if (getIntent().getExtras() != null) {
            chatDetails = (ChatDetails) getIntent().getExtras().getSerializable("CHAT_INFO");
        }
        initDataset();
        initChatMenu();

        title.setText(chatDetails.getTitle());
        description.setText(chatDetails.getDescription());
        //add check
        addTagsFriendsToChipGroup();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewChatActivity.this.onBackPressed();
            }
        });
    }

    private void initDataset(){
        backButton = findViewById(R.id.chatBackButton);
        title = findViewById(R.id.viewChatTitle);
        description = findViewById(R.id.viewChatDescription);
        tags = findViewById(R.id.viewChatAddTags);
        friends = findViewById(R.id.viewChatAddFriends);
        chatMenu = findViewById(R.id.chatOptions);
    }

    private void addTagsFriendsToChipGroup(){
        for(String tag : chatDetails.getStringListOfTags()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, tags, false);
            chip.setText(tag);
            tags.addView(chip);
        }

        List<String> peopleIds = chatDetails.getPeople();
        List<String> friendNames = new ArrayList<>();
        RequestManager requestManager = new RequestManager();
        String userToken = ((UserApplicationInfo) getApplication()).getUserToken();

        //TODO: FIXME hacky loop of gets
        for (String friendId : peopleIds) {
            try {
                String path = new PathBuilder()
                        .addUser()
                        .addNode(friendId)
                        .build();

                requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject userJson = new JSONObject(response.body().string());

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        ViewChatActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, friends, false);
                                                try {
                                                    chip.setText(userJson.getString("name"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                friends.addView(chip);
                                            }
                                        });
                                    }
                                }, 0);
                                //todo remove
                                friendNames.add(userJson.getString("name"));
                            } catch (IOException | JSONException e) {
                                //Do nothing: Just don't load the member chip
                            }
                        }

                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        //Do nothing: Just don't load the member chip
                    }
                });
            } catch (IOException e) {
                //Do nothing: Just don't load the member chip
            }

        }

        for(String friend : friendNames){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, friends, false);
            chip.setText(friend);
            friends.addView(chip);
        }
    }

    private void initChatMenu() {
        chatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu = new PopupMenu(ViewChatActivity.this, v.findViewById(R.id.chatOptions));
                menu.inflate(R.menu.chat_options_menu);
                initMenuOptionsVisibility();
                Activity activity = ViewChatActivity.this;

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.chatLeave:
                                UserApplicationInfo userApplicationInfo = ((UserApplicationInfo) getApplication());
                                String userId = userApplicationInfo.getProfile().getId();
                                String token = userApplicationInfo.getUserToken();
                                String chatId = chatDetails.getId();
                                String operation = "Leave Chat";

                                try {
                                    String path = new PathBuilder()
                                            .addUser()
                                            .addNode("leaveChat")
                                            .addNode(userId)
                                            .addNode(chatId)
                                            .build();

                                    JSONObject json = new JSONObject();
                                    json.put("token", token);

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
                                                                menu.getMenu().findItem(R.id.chatLeave).setVisible(false);

                                                                new AlertDialog.Builder(activity)
                                                                        .setTitle("Successfully Left Chat")
                                                                        .setMessage("You have now left " + chatDetails.getTitle())
                                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        })
                                                                        .create()
                                                                        .show();

                                                                String userName = userApplicationInfo.getProfile().getFullName();
                                                                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, friends, false);
                                                                chip.setText(userName);
                                                                friends.removeView(chip);

                                                            }
                                                        });



                                                    }
                                                }, 0);
                                            } else {
                                                ResponseErrorHandler.createErrorMessage(response, operation, "Event", activity);
                                            }
                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
                                        }
                                    });
                                } catch (IOException e) {
                                    FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
                                } catch (JSONException e) {
                                    FeedbackMessageBuilder.createParseError(e, operation, activity);
                                }

                                return true;

                            case R.id.chatEdit:
                                Intent manageChat = new Intent(activity, ManageChatActivity.class);
                                manageChat.putExtra("CHAT_DETAILS", chatDetails);
                                manageChat.putExtra("EDIT_OPTION", true);
                                startActivity(manageChat);
                                return true;

                            default:
                                return false;

                        }
                    }
                });
                menu.show();
            }
        });
    }

    private void initMenuOptionsVisibility() {
        UserApplicationInfo userApplicationInfo = ((UserApplicationInfo) getApplication());
        //String userId = userApplicationInfo.getProfile().getId();


    }
}