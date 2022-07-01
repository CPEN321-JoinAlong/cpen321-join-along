package com.joinalongapp.joinalong;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.joinalongapp.viewmodel.UserProfile;

//TODO: make profileLocation be autocomplete
//TODO: allow upload profile pics
//      upload button directs to new activity that asks permission to access photo album
//      can then select and upload pics

public class ManageProfileActivity extends AppCompatActivity {
    final static String TAG ="ManageProfileActivity";
    EditText firstNameEdit;
    EditText lastNameEdit;
    EditText locationEdit;
    Chip interestsChip;
    EditText descriptionEdit;
    Button uploadProfilePic;
    Button confirm;
    ImageButton close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_profile_activity);

        // a putExtra should be passed from previous screen containing basic user info for an update
        // if the object is null, it must be a create

        initElements();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile profile = new UserProfile();
                profile.setFirstName(firstNameEdit.getText().toString());
                profile.setLastName(lastNameEdit.getText().toString());

                //TODO: process location string

                //TODO: process interests

                profile.setDescription(descriptionEdit.getText().toString());

                //TODO: process picture information. This will be returned as an extra bitmap
            }
        });

    }

    private void initElements() {
        firstNameEdit = findViewById(R.id.profileFirstNameEdit);
        lastNameEdit = findViewById(R.id.profileLastNameEdit);
        locationEdit = findViewById(R.id.profileLocationEdit);
        interestsChip = findViewById(R.id.profileInterestsChip);
        descriptionEdit = findViewById(R.id.profileDescription);
        uploadProfilePic = findViewById(R.id.profileUploadPictureButton);
        confirm = findViewById(R.id.profileManageConfirm);
        close = findViewById(R.id.profileCloseButton);
    }
}