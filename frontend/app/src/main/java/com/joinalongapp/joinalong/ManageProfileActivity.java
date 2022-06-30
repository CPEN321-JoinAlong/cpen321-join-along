package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ManageProfileActivity extends AppCompatActivity {
    final static String TAG ="ManageProfileActivity";
    EditText firstNameEdit;
    EditText locationEdit;
    Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_profile_activity);

        // a putExtra should be passed from previous screen containing basic user info for an update
        // if the object is null, it must be a create


        confirm = findViewById(R.id.profile_manage_confirm);
        firstNameEdit = findViewById(R.id.profileFirstNameEdit);

        //TODO: make this an autocomplete
        locationEdit = findViewById(R.id.profileLocationEdit);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, firstNameEdit.getText().toString());
                Log.d(TAG, locationEdit.getText().toString());

            }
        });


    }
}