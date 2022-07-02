package com.joinalongapp.joinalong;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.viewmodel.UserProfile;

import java.io.IOException;

//TODO: make profileLocation be autocomplete
//TODO: allow upload profile pics
//      upload button directs to new activity that asks permission to access photo album
//      can then select and upload pics

/**
 * This activity accepts Extras upon starting
 * Adding a UserProfile object as an Extra will update that existing profile
 * Adding no UserProfile object will create a new profile; you may also
 * add a first and last name string for autofill in the Extras.
 */
public class ManageProfileActivity extends AppCompatActivity {
    final static String TAG ="ManageProfileActivity";
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText locationEdit;
    private ChipGroup interestsChip;
    private EditText descriptionEdit;
    private Button uploadProfilePic;
    private Button confirm;
    private ImageButton close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_profile_activity);

        initElements();

        if (!isCreatingProfile()) {
            setUpPageForEdit();
        } else {
            setUpPageForCreate();
        }

        uploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to new activity to select/upload pic from phone and come back here with the pic
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile profile = new UserProfile();
                profile.setFirstName(firstNameEdit.getText().toString());
                profile.setLastName(lastNameEdit.getText().toString());

                try {
                    profile.setLocation(getAddressFromString());
                } catch (IOException e) {
                    Log.e(TAG, "Failed to set location with error: " + e.getMessage());
                }

                interestsChip.getCheckedChipIds();
                //TODO: process interests by mapping ids to the string value

                profile.setDescription(descriptionEdit.getText().toString());

                //TODO: process picture information. This will be returned as an extra bitmap

                //TODO: post profile as json and evaluate response, upon a 200, we should continue to next intent
                //      update profile if it was a edit
                //TODO: maybe can add profile pic preview on side
            }
        });

        close.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setUpPageForCreate() {
        firstNameEdit.setHint(getIntent().getExtras().get("firstName").toString());
        lastNameEdit.setHint(getIntent().getExtras().get("lastName").toString());
    }

    private void setUpPageForEdit() {
        UserProfile existingUserProfile = (UserProfile) getIntent().getExtras().getSerializable("userProfile");
        firstNameEdit.setHint(existingUserProfile.getFirstName());
        lastNameEdit.setHint(existingUserProfile.getLastName());
        locationEdit.setHint(convertAddressToString(existingUserProfile.getLocation()));
        //TODO: implement the interest chips
        descriptionEdit.setHint(existingUserProfile.getDescription());
        //TODO: if add pic preview, need pic here

        TextView titleView = findViewById(R.id.profileTitle);
        String editTitle = "Edit Profile";
        titleView.setText(editTitle);

        String editConfirm = "Confirm Edit!";
        confirm.setText(editConfirm);
    }

    private Address getAddressFromString() throws IOException{
        Geocoder geocoder = new Geocoder(ManageProfileActivity.this);
        return geocoder.getFromLocationName(locationEdit.getText().toString(), 1).get(0);
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

    private boolean isCreatingProfile() {
        return getIntent().getExtras().getSerializable("userProfile") == null;
    }

    private String convertAddressToString(Address address) {
        return address.getAddressLine(0) +
                address.getLocality() +
                address.getAdminArea() +
                address.getCountryName();
    }
}