package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.joinalongapp.adapter.MessageListCustomAdapter;
import com.joinalongapp.viewmodel.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    private MessageListCustomAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        List<Message> lm = new ArrayList<>();
        Message a = new Message("hello", "Ken", UUID.randomUUID(), 0);
        Message b = new Message("abc", "yes", UUID.randomUUID(), 1);
        Message c = new Message("test", "yes", UUID.randomUUID(), 3);
        lm.add(a);
        lm.add(b);
        c.setOwner(true);
        lm.add(c);

        mMessageRecycler = (RecyclerView) findViewById(R.id.chatRecycler);
        mMessageAdapter = new MessageListCustomAdapter(lm);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
    }
}