package com.joinalongapp.joinalong;

import android.content.Intent;
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
import java.util.List;

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
        setContentView(R.layout.activity_manage_profile);

        initElements();

        if (getIntent().getExtras() != null) {
            if (!isCreatingProfile()) {
                setUpPageForEdit();
            } else {
                setUpPageForCreate();
            }
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
                profile.setLocation(locationEdit.getText().toString());


                interestsChip.getCheckedChipIds();
                //TODO: process interests by mapping ids to the string value

                profile.setDescription(descriptionEdit.getText().toString());

                //TODO: process picture information. This will be returned as an extra bitmap
                //TODO: maybe can add profile pic preview on side


                if (validateElements(profile)) {
                    //TODO: post profile as json and evaluate response, upon a 200, we should continue to next intent
                    //      update profile if it was a edit
                }

                startMainActivity();

            }
        });

    }

    private void startMainActivity() {
        Intent i = new Intent(ManageProfileActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void setUpPageForCreate() {
        if (getIntent().getExtras().get("firstName") != null) {
            firstNameEdit.setText(getIntent().getExtras().get("firstName").toString());
        }
        if (getIntent().getExtras().get("lastName") != null) {
            lastNameEdit.setText(getIntent().getExtras().get("lastName").toString());
        }
    }

    private void setUpPageForEdit() {
        UserProfile existingUserProfile = (UserProfile) getIntent().getExtras().getSerializable("userProfile");
        firstNameEdit.setHint(existingUserProfile.getFirstName());
        lastNameEdit.setHint(existingUserProfile.getLastName());
        locationEdit.setHint(existingUserProfile.getLocation());
        //TODO: implement the interest chips
        descriptionEdit.setHint(existingUserProfile.getDescription());
        //TODO: if add pic preview, need pic here

        TextView titleView = findViewById(R.id.profileTitle);
        String editTitle = "Edit Profile";
        titleView.setText(editTitle);

        String editConfirm = "Confirm Edit!";
        confirm.setText(editConfirm);
    }

    private Address getAddressFromString(String address) {
        Geocoder geocoder = new Geocoder(ManageProfileActivity.this);
        Address retVal = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                retVal = addresses.get(0);
            }
        } catch(IOException e) {
            Log.e(TAG, "Failed to set location with error: " + e.getMessage());
        }
        return retVal;
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
        close.setVisibility(View.GONE);
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

    private boolean validateElements(UserProfile profile) {
        boolean isValid = true;

        if (profile.getFirstName().isEmpty()) {
            firstNameEdit.setError("First name must not be blank.");
            isValid = false;
        }

        if (profile.getLastName().isEmpty()) {
            lastNameEdit.setError("Last name must not be blank.");
            isValid = false;
        }

        if (getAddressFromString(profile.getLocation()) == null) {
            locationEdit.setError("Unable to find address.");
            isValid = false;
        }

        if (interestsChip.getCheckedChipIds().isEmpty()) {
            // TODO
        }

        return isValid;
    }
}