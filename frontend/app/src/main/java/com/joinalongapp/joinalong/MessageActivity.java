package com.joinalongapp.joinalong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.adapter.MessageListCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandlerUtils;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.viewmodel.Message;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import okhttp3.Call;
import okhttp3.Response;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView messageRecycler;
    private MessageListCustomAdapter messageAdapter;
    private List<Message> messages;
    private ImageButton sendMessageButton;
    private EditText messageField;
    private TextView chatTitle;
    private ImageButton backButton;
    private ImageButton chatInfoButton;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("MessageActivityUIComponents");
        myTrace.start();
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

                String messageContent;
                if (messageField.getText().toString().isEmpty()) {
                    messageField.setError("Empty message");
                    return;
                } else {
                    messageContent = messageField.getText().toString();
                }

                String userId = user.getId();
                String chatId = chatDetails.getId();
                long timestamp = new Date().getTime();

                socket.emit("messageDetection", userId, chatId, timestamp, messageContent);
                messageField.setText("");
            }
        });

        chatInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MessageActivity.this, ViewChatActivity.class);
                i.putExtra("CHAT_INFO", chatDetails);
                startActivity(i);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(messageAdapter);

        try {
            socket = IO.socket(RequestManager.getBaseUrl());

            socket.connect();
            System.out.println("successful socket connect");
            socket.emit("join", user.getFullName());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("socket io error");
        }

        //UPDATE ON RECEIVE MESSAGE
        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];

                            Message incomingMessage = new Message();
                            incomingMessage.populateDetailsFromJson(json.toString());
                            incomingMessage.setOwner(incomingMessage.getId().equals(user.getId()));

                            messages.add(incomingMessage);

                            messageAdapter.notifyDataSetChanged();

                            messageRecycler.scrollToPosition(messages.size() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        // FOR DEBUG
        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Exception e = (Exception) args[0];
                        Log.e("socketio", "Transport error " + e);
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
            }
        });

        // FOR DEBUG
        socket.io().on(Manager.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("socketio", "connectError" + args[0]);
            }
        });
        myTrace.stop();
    }


    private void initElements(){
        messageRecycler = (RecyclerView) findViewById(R.id.chatRecycler);
        messageAdapter = new MessageListCustomAdapter(messages);
        sendMessageButton = findViewById(R.id.sendChatButton);
        messageField = findViewById(R.id.editTextChatMessage);
        chatTitle = findViewById(R.id.chatTitleName);
        backButton = findViewById(R.id.chatBackButton);
        chatInfoButton = findViewById(R.id.chatInfo);
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
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = (JSONArray) jsonObject.get("messages");

                        List<Message> outputMessages = new ArrayList<>();
                        String userId = ((UserApplicationInfo) getApplication()).getProfile().getId();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Message message = new Message();
                            message.populateDetailsFromJson(jsonArray.get(i).toString());
                            message.setOwner(message.getId().equals(userId));
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
                    } catch (IOException | JSONException e) {
                        FeedbackMessageBuilder.createParseError(e, "Load Messages", MessageActivity.this);
                    }
                } else {
                    ResponseErrorHandlerUtils.createErrorMessage(response, "Load Messages", "Message", MessageActivity.this);
                }

            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, "Load Messages", MessageActivity.this);
            }
        });
    }
}