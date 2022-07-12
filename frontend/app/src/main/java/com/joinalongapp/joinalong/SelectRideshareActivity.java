package com.joinalongapp.joinalong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.LyftStyle;
import com.lyft.networking.ApiConfig;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.rides.client.SessionConfiguration;

public class SelectRideshareActivity extends AppCompatActivity {
    private static final String TAG = "SelectRideshareActivity";
    RideRequestButton uberButton;
    LyftButton lyftButton;
    ImageButton close;

    //TODO: not sure if it is good practice to store this value on the frontend
    private final String UBER_CLIENT_ID = "MRK54yhjV7gLbg0aV7IKrNnmCr9VGdaE";
    private final String LYFT_CLIENT_ID = "JK5Js2s2hjn2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_rideshare);

        initUberButton();
        initLyftButton();

        close = findViewById(R.id.selectRideshareCloseButton);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change MainActivity.class to the activity that calls ManageRideshareActivity
                Intent intent = new Intent(SelectRideshareActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    private void initUberButton() {
        //TODO: currently set on sandbox so that you can't book an actual ride :)
        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId(UBER_CLIENT_ID)
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();
        UberSdk.initialize(config);

        uberButton = findViewById(R.id.rideshareUberBookingButton);
    }

    private void initLyftButton() {
        ApiConfig config = new ApiConfig.Builder()
                .setClientId(LYFT_CLIENT_ID)
                //TODO: it appears Lyft also removed client token.
                //      so just like uber, can't send ride parameter data :((
                .setClientToken("...")
                .build();

        lyftButton = (LyftButton) findViewById(R.id.lyft_button);
        lyftButton.setLyftStyle(LyftStyle.HOT_PINK);
        lyftButton.setApiConfig(config);
    }

}