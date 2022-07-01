package com.joinalongapp.joinalong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.joinalongapp.viewmodel.UserProfile;

public class MainActivity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createProfile = new Intent(MainActivity.this, ManageProfileActivity.class);
                UserProfile up = new UserProfile();
                up.setLastName("last");
                up.setFirstName("first");
                createProfile.putExtra("userProfile", up);
                startActivity(createProfile);
            }
        });
    }
}