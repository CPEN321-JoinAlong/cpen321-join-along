package com.joinalongapp.joinalong;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.viewmodel.Tag;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


public class ManageChatActivity extends AppCompatActivity {

    private TextView title;
    private EditText chatTitle;
    private AutoCompleteTextView tagAutoComplete;
    private ChipGroup tagChipGroup;
    private EditText chatDescription;
    private AutoCompleteTextView friendAutoComplete;
    private ChipGroup friendChipGroup;
    private ImageButton cancelButton;
    private Button submitButton;
    private List<UserProfile> trackFriends;
    private Map<String, String> chipIdToUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_chat);

        String token = ((UserApplicationInfo) getApplication()).getUserToken();
        UserProfile user = ((UserApplicationInfo) getApplication()).getProfile();

        initElement();

        Bundle info = getIntent().getExtras();
        Boolean manageOption = info.getBoolean("EDIT_OPTION");
        //UserProfile user = (UserProfile) info.getSerializable("USER");

        if(manageOption){
            ChatDetails chatDetails = (ChatDetails) info.getSerializable("CHAT_DETAILS");
            title.setText("Edit Chat");
            autofillChatDetails(chatDetails);
        }

        String[] tags = getResources().getStringArray(R.array.sample_tags);

        //TODO: this is a temp solution, no clue if it actually works or not
        //      ken, please check

        List<String> friendUserIds = user.getFriends();
        String userToken = ((UserApplicationInfo) getApplication()).getUserToken();
        RequestManager requestManager = new RequestManager();
        List<String> friendNames = new ArrayList<>();
        Map<String, String> idToName = new HashMap<>();

        for (String friendId : friendUserIds) {
            try {
                requestManager.get("user/" + friendId, userToken, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {
                        try {
                            JSONObject userJson = new JSONObject(response.body().string());
                            friendNames.add(userJson.getString("name"));
                            idToName.put(friendId, userJson.getString("name"));
                        } catch (IOException | JSONException e) {
                            //todo
                        }
                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        //todo
                    }
                });
            } catch (IOException e) {
                //todo
            }

        }

        String[] friends = friendNames.toArray(new String[friendNames.size()]);

        initAutoCompleteChipGroup(tagAutoComplete, tagChipGroup, tags);
        initAutoCompleteChipGroup(friendAutoComplete, friendChipGroup, friends);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInvalidFields()){
                    ChatDetails resultChat = new ChatDetails();

                    resultChat.setDescription(chatDescription.getText().toString());
                    resultChat.setTags(chipGroupToList(tagChipGroup));
                    resultChat.setTitle(chatTitle.getText().toString());
                    resultChat.setPeople(chipFriendsGroupToList(friendChipGroup, idToName));

                    RequestManager submitManager = new RequestManager();
                    try{
                        JSONObject json = resultChat.toJson();
                        json.put("token", token);
                        submitManager.post("chat/create", json.toString(), new RequestManager.OnRequestCompleteListener() {
                            @Override
                            public void onSuccess(Call call, Response response) {
                                System.out.println(response.body());
                            }

                            @Override
                            public void onError(Call call, IOException e) {
                                System.out.println(call.toString());
                            }
                        });
                    } catch(IOException | JSONException e){

                    }


                }
            }
        });
    }


    private void initElement(){
        title = findViewById(R.id.manageChatTitle);
        chatTitle = findViewById(R.id.manageChatEditTextTitle);
        tagAutoComplete = findViewById(R.id.autoCompleteTagText);
        tagChipGroup = findViewById(R.id.manageChatTags);
        chatDescription = findViewById(R.id.manageChatEditTextDescription);
        friendAutoComplete = findViewById(R.id.autoCompleteFriendText);
        friendChipGroup = findViewById(R.id.manageTags);
        cancelButton = findViewById(R.id.manageChatCancelButton);
        submitButton = findViewById(R.id.submitManageChatButton);
        chipIdToUserId = new HashMap<>();
    }

    private void autofillChatDetails(ChatDetails chatDetails){
        chatTitle.setText(chatDetails.getTitle());
        chatDescription.setText(chatDetails.getDescription());

        List<String> tags = chatDetails.getStringListOfTags();

        List<String> peopleIds = chatDetails.getPeople();
        List<String> people = new ArrayList<>();
        RequestManager requestManager = new RequestManager();
        String userToken = ((UserApplicationInfo) getApplication()).getUserToken();
        for (String friendId : peopleIds) {
            try {
                requestManager.get("user/" + friendId, userToken, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {
                        try {
                            JSONObject userJson = new JSONObject(response.body().string());
                            people.add(userJson.getString("name"));
                        } catch (IOException | JSONException e) {
                            //todo
                        }
                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        //todo
                    }
                });
            } catch (IOException e) {
                //todo
            }

        }

        for(String tag : tags){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, tagChipGroup, false);
            chip.setText(tag);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagChipGroup.removeView(chip);
                }
            });
            tagChipGroup.addView(chip);
        }

        for(String person : people){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, friendChipGroup, false);
            chip.setText(person);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendChipGroup.removeView(chip);
                }
            });
            friendChipGroup.addView(chip);
        }
    }

    private List<Tag> chipGroupToList(ChipGroup chipGroup){
        List<Tag> result = new ArrayList<>();
        List<Integer> ids = chipGroup.getCheckedChipIds();
        for(Integer id : ids){
            Chip chip = chipGroup.findViewById(id);
            result.add(new Tag(chip.getText().toString()));
        }
        return result;
    }

    private List<String> chipFriendsGroupToList(ChipGroup chipGroup, Map idToName){
        List<String> result = new ArrayList<>();
        List<Integer> ids = chipGroup.getCheckedChipIds();
        for(Integer id : ids){
            Chip chip = chipGroup.findViewById(id);
            String name = chip.getText().toString();
            for(Object s : idToName.values()){
                String individualName = (String) s;
                if(individualName.equals(name)){
                    result.add((String)idToName.get(individualName));
                }
            }
        }
        return result;
    }

    private Boolean checkInvalidFields(){
        Boolean flag = true;

        if(editTextEmpty(chatTitle)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Title field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(tagChipGroup.getChildCount() == 0){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Tag field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(editTextEmpty(chatDescription)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Description field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(friendChipGroup.getChildCount() == 0){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Friends field", Toast.LENGTH_SHORT);
            toast.show();
        }

        return flag;
    }

    private Boolean editTextEmpty(EditText input){
        if(input.getText().toString().trim().length() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    private void initAutoCompleteChipGroup(AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup, String[] fillArray){
        ArrayAdapter<String> arrayAdapterTags = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, fillArray);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapterTags);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoCompleteTextView.setText("");

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);

                chip.setText((String) parent.getItemAtPosition(position));
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chipGroup.removeView(chip);
                    }
                });
                chipGroup.addView(chip);
            }
        });
    }


}