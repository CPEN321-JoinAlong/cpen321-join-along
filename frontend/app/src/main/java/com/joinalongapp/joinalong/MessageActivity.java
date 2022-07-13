package com.joinalongapp.joinalong;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.MessageListCustomAdapter;
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



        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean group = false;
                if(chatDetails.getNumPeople() == 2){
                    group = true;
                }
                Date date = new Date();
                Message message = new Message();
                message.setMessage(messageField.getText().toString());
                messageField.setText("");
                message.setOwner(true);
                message.setCreatedAt(date.getTime());
                String path = "sendSingle";
                //String otherId = chatDetails.get;
                if(group){
                    path = "sendGroup";

                }
                JSONObject json = null;
                try {
                    json = message.toJson();
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("chat/" + path + "/" + user.getId() + "/" + chatDetails.getId());

                RequestManager requestManager = new RequestManager();
                try {
                    requestManager.put("chat/sendChat/" + user.getId() + "/" + chatDetails.getId(), json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            System.out.println(response);
                            if(response.isSuccessful()){
                                List<Message> messageList = messageAdapter.getMessages();
                                messageList.add(message);
                                messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
                            }
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            System.out.println("");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
    }

    private void initMessages(String id, String token, Activity activity) throws IOException, JSONException {
        RequestManager requestManager = new RequestManager();
        JSONObject json = new JSONObject();
        json.put("token", token);
        System.out.println(id);

        requestManager.get("chat/" + id, token, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = (JSONArray) jsonObject.get("messages");
                    List<Message> outputMessages = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++){
                        Message message = new Message();
                        message.populateDetailsFromJson(jsonArray.get(i).toString());
                        outputMessages.add(message);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageAdapter.changeDataset(outputMessages);
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

            }
        });
    }
}