package com.joinalongapp.joinalong;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.MessageListCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.viewmodel.Message;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView messageRecycler;
    private MessageListCustomAdapter messageAdapter;
    private List<Message> messages;
    private ImageButton sendMessageButton;
    private EditText messageField;
    private TextView chatTitle;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        String token = ((UserApplicationInfo) getApplication()).getUserToken();
        UserProfile user = ((UserApplicationInfo) getApplication()).getProfile();
        Bundle info = getIntent().getExtras();
        ChatDetails chatDetails = (ChatDetails) info.getSerializable("CHAT_DETAILS");
        try {
            initMessages(chatDetails.getId(), token, this);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        initElements();

        chatTitle.setText(chatDetails.getTitle());
        Activity activity = this;

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                Message message = new Message();
                message.setMessage(messageField.getText().toString());
                messageField.setText("");
                message.setOwner(true);
                message.setCreatedAt(date.getTime());

                JSONObject json = null;
                try {
                    json = message.toJson();
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(json.toString());

                RequestManager requestManager = new RequestManager();
                try {
                    String path = new PathBuilder()
                            .addChat()
                            .addNode("sendChat")
                            .addNode(user.getId())
                            .addNode(chatDetails.getId())
                            .build();

                    requestManager.put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            // TODO: add messages
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            System.out.println("");
                        }
                    });

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    List<Message> messageList = messageAdapter.getMessages();
                                    if(messageList == null){
                                        messageList = new ArrayList<>();
                                    }
                                    messageList.add(message);
                                    messageAdapter.notifyDataSetChanged();
                                    messageRecycler.scrollToPosition(messages.size() - 1);
                                }
                            });
                        }
                    }, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });

        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(messageAdapter);
    }


    private void initElements(){
        messageRecycler = (RecyclerView) findViewById(R.id.chatRecycler);
        messageAdapter = new MessageListCustomAdapter(messages);
        sendMessageButton = findViewById(R.id.sendChatButton);
        messageField = findViewById(R.id.editTextChatMessage);
        chatTitle = findViewById(R.id.chatTitleName);
        backButton = findViewById(R.id.chatBackButton);
    }

    private void initMessages(String id, String token, Activity activity) throws IOException, JSONException {
        RequestManager requestManager = new RequestManager();
        JSONObject json = new JSONObject();
        json.put("token", token);

        String path = new PathBuilder()
                .addChat()
                .addNode(id)
                .build();

        requestManager.get(path, token, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = (JSONArray) jsonObject.get("messages");
                    List<Message> outputMessages = new ArrayList<>();
                    String userId = ((UserApplicationInfo) getApplication()).getProfile().getId();
                    for(int i = 0; i < jsonArray.length(); i++){
                        Message message = new Message();
                        message.populateDetailsFromJson(jsonArray.get(i).toString());
                        message.setOwner(message.getId().equals(userId));
                        System.out.println(message.getMessage());
                        outputMessages.add(message);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messages = outputMessages;
                                    messageAdapter.changeDataset(outputMessages);
                                    messageRecycler.scrollToPosition(messages.size() - 1);
                                }
                            });
                        }
                    }, 0);
                } catch(IOException | JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onError(Call call, IOException e) {
                // TODO: add error messages.
            }
        });
    }
}