package com.joinalongapp.joinalong;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.viewmodel.Tag;
import com.joinalongapp.viewmodel.UserProfile;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

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
    private final static String TAG ="ManageProfileActivity";
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText locationEdit;
    private ChipGroup interestsChip;
    private EditText descriptionEdit;
    private MaterialButtonToggleGroup useProfilePicToggle;
    private Button useProfilePic;
    private Button dontUseProfilePic;
    private Button confirm;
    private ImageButton close;
    private AutoCompleteTextView autoCompleteInterests;
    private ImageView profilePicPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profile);

        UserProfile profile = new UserProfile();

        initElements();
        String[] sampleTags = getResources().getStringArray(R.array.sample_tags);
        initAutoCompleteChipGroup(autoCompleteInterests, interestsChip, sampleTags);
        try {
            initUseGoogleProfilePicToggle(profile);
        } catch (IOException e) {
            Log.e(TAG, "Failed to set profile pic: " + e.getMessage());
        }


        if (getIntent().getExtras() != null) {
            if (!isCreatingProfile()) {
                setUpPageForEdit();
            } else {
                setUpPageForCreate();
            }
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setFirstName(firstNameEdit.getText().toString());
                profile.setLastName(lastNameEdit.getText().toString());
                profile.setLocation(locationEdit.getText().toString());


                List<Tag> tags = getTagsFromChipGroup();
                for (Tag tag : tags) {
                    profile.addTagToInterests(tag);
                }

                profile.setDescription(descriptionEdit.getText().toString());

                UserApplicationInfo userInput = new UserApplicationInfo();

                if (isCreatingProfile()) {
                    userInput.setUserToken(getIntent().getExtras().getString("userToken"));
                } else {
                    userInput.setUserToken(((UserApplicationInfo) getApplication()).getUserToken());
                }

                if (validateElements(profile)) {
                    userInput.setProfile(profile);

                    if (isCreatingProfile()) {
                        try {
                            String jsonBody = userInput.toJsonString();
                            RequestManager requestManager = new RequestManager();
                            requestManager.post("user/create", jsonBody, new RequestManager.OnRequestCompleteListener() {
                                @Override
                                public void onSuccess(Call call, Response response) {
                                    UserApplicationInfo newUserInfo = new UserApplicationInfo();
                                    try {
                                        newUserInfo.populateUserInfoFromJson(response.body().string());
                                        ((UserApplicationInfo) getApplication()).updateApplicaitonInfo(newUserInfo);
                                        startMainActivity();

                                    } catch (IOException | JSONException e) {
                                        Log.e(TAG, "Unable to load user profile");
                                    }
                                }

                                @Override
                                public void onError(Call call, IOException e) {
                                    Log.e(TAG, "Unable to create profile");
                                    Toast.makeText(ManageProfileActivity.this, "Unable to create profile. Please try again later.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (JSONException | IOException e) {
                            Log.e(TAG, "Failed to create user profile");
                            Toast.makeText(ManageProfileActivity.this, "Unable to create profile. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        try {
                            String jsonBody = userInput.toJsonString();
                            RequestManager requestManager = new RequestManager();

                            String userId = ((UserApplicationInfo) getApplication()).getProfile().getId();
                            String path = "user/" + userId + "/edit";

                            requestManager.put(path, jsonBody, new RequestManager.OnRequestCompleteListener() {
                                @Override
                                public void onSuccess(Call call, Response response) {
                                    System.out.println(response.code());
                                    UserApplicationInfo newUserInfo = new UserApplicationInfo();
                                    try {
                                        //TODO: change this to call profile fragment
                                        //      currently there is no body in response, so can't call update
                                        newUserInfo.populateUserInfoFromJson(response.body().string());
                                        ((UserApplicationInfo) getApplication()).updateApplicaitonInfo(newUserInfo);

                                        startMainActivity();

                                    } catch (IOException | JSONException e) {
                                        Log.e(TAG, "Unable to update user profile");
                                    }
                                }

                                @Override
                                public void onError(Call call, IOException e) {
                                    Log.e(TAG, "Unable to update profile");
                                    Toast.makeText(ManageProfileActivity.this, "Unable to update profile. Please try again later.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (JSONException | IOException e) {
                            Log.e(TAG, "Failed to update user profile");
                            Toast.makeText(ManageProfileActivity.this, "Unable to update profile. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private List<Tag> getTagsFromChipGroup(){
        List<Tag> result = new ArrayList<>();
        for(int i = 0; i < interestsChip.getChildCount(); i++){
            Chip chip = (Chip) interestsChip.getChildAt(i);
            result.add(new Tag(chip.getText().toString()));
        }
        return result;
    }

    private void initAutoCompleteChipGroup(AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup, String[] fillArray){
        ArrayAdapter<String> arrayAdapterTags = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, fillArray);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapterTags);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoCompleteTextView.setText("");

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);
                chip.setText((String) parent.getItemAtPosition(position));
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chipGroup.removeView(chip);
                    }
                });
                chipGroup.addView(chip);
            }
        });
    }

    private void initUseGoogleProfilePicToggle(UserProfile userProfile) throws IOException {
        useProfilePicToggle.setVisibility(View.GONE);
        useProfilePic.setVisibility(View.GONE);
        dontUseProfilePic.setVisibility(View.GONE);
        findViewById(R.id.useGoogleProfilePicTitle).setVisibility(View.GONE);
        //TODO: this might be future functionality
        //      for now, always uses the user google account profile pic

        userProfile.setProfilePicture(getProfilePicUrl());
        Picasso.get().load(getProfilePicUrl()).into(profilePicPreview);

//        setProfilePicToggleColors(R.color.orange_light, R.color.orange_prim);
//        profilePicPreview.setVisibility(View.GONE);
//        userProfile.setProfilePicture("");
//
//        useProfilePicToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
//            @Override
//            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
//                if (isChecked) {
//                    if (checkedId == R.id.useGoogleProfilePic) {
//                        userProfile.setProfilePicture(getProfilePicUrl());
//                        setProfilePicToggleColors(R.color.orange_prim, R.color.orange_light);
//                        profilePicPreview.setVisibility(View.VISIBLE);
//                        Picasso.get().load(getProfilePicUrl()).into(profilePicPreview);
//                    } else {
//                        if (checkedId == R.id.dontUseGoogleProfilePic) {
//                            userProfile.setProfilePicture("");
//                            setProfilePicToggleColors(R.color.orange_light, R.color.orange_prim);
//                            profilePicPreview.setVisibility(View.GONE);
//                        }
//                    }
//                } else {
//                    // This is the default case, which is to share the cost
//                    if (group.getCheckedButtonId() == View.NO_ID) {
//                        userProfile.setProfilePicture("");
//                        setProfilePicToggleColors(R.color.orange_light, R.color.orange_prim);
//                        profilePicPreview.setVisibility(View.GONE);
//                    }
//                }
//            }
//        });
    }

    private void setProfilePicToggleColors(int usePicColor, int dontUsePicColor) {
        useProfilePic.setBackgroundColor(usePicColor);
        dontUseProfilePic.setBackgroundColor(dontUsePicColor);
    }


    private void startMainActivity() {
        Intent i = new Intent(ManageProfileActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void setUpPageForCreate() {
        close.setVisibility(View.GONE);
        if (getIntent().getExtras().get("firstName") != null) {
            firstNameEdit.setText(getIntent().getExtras().getString("firstName"));
        }
        if (getIntent().getExtras().get("lastName") != null) {
            lastNameEdit.setText(getIntent().getExtras().getString("lastName"));
        }
    }

    private String getProfilePicUrl() {
        return getIntent().getExtras().getString("profilePic");
    }

    private void setUpPageForEdit() {
        UserProfile existingUserProfile = ((UserApplicationInfo) getApplication()).getProfile();
        firstNameEdit.setText(existingUserProfile.getFirstName());
        lastNameEdit.setText(existingUserProfile.getLastName());
        locationEdit.setText(existingUserProfile.getLocation());

        List<String> existingInterests = existingUserProfile.getStringListOfTags();
        for (String interest : existingInterests) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, interestsChip, false);
            chip.setText(interest);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interestsChip.removeView(chip);
                }
            });
            interestsChip.addView(chip);
        }

        descriptionEdit.setText(existingUserProfile.getDescription());
        Picasso.get().load(existingUserProfile.getProfilePicture()).into(profilePicPreview);

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
        interestsChip = findViewById(R.id.profileManagementChipGroup);
        autoCompleteInterests = findViewById(R.id.profileManagementAutoComplete);
        descriptionEdit = findViewById(R.id.profileDescription);
        useProfilePicToggle = findViewById(R.id.useGoogleProfilePicToggle);
        useProfilePic = findViewById(R.id.useGoogleProfilePic);
        dontUseProfilePic = findViewById(R.id.dontUseGoogleProfilePic);
        confirm = findViewById(R.id.profileManageConfirm);
        close = findViewById(R.id.profileCloseButton);
        profilePicPreview = findViewById(R.id.profilePicPreview);
    }

    private boolean isCreatingProfile() {
        return getIntent().getExtras().get("MODE") == ManageProfileMode.PROFILE_CREATE;
    }

    public enum ManageProfileMode {
        PROFILE_EDIT,
        PROFILE_CREATE
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

        if (interestsChip.getChildCount() == 0) {
            autoCompleteInterests.setError("Please add at least one interest");
            isValid = false;
        }

        return isValid;
    }
}