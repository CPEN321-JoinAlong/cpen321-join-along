package com.joinalongapp.joinalong;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.viewmodel.ChatDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ViewChatActivity extends AppCompatActivity {

    ImageButton backButton;
    ChatDetails chatDetails;
    TextView title;
    TextView description;
    ChipGroup tags;
    ChipGroup friends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chat);
        if (getIntent().getExtras() != null) {
            chatDetails = (ChatDetails) getIntent().getExtras().getSerializable("CHAT_INFO");
        }
        initDataset();

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
}