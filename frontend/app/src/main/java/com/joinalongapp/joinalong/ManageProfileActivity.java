package com.joinalongapp.joinalong;

import static com.joinalongapp.LocationUtils.getAddressFromString;
import static com.joinalongapp.LocationUtils.standardizeAddress;
import static com.joinalongapp.LocationUtils.validateLocation;
import static com.joinalongapp.TextInputUtils.isValidNameTitle;

import android.content.Intent;
import android.location.Address;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.controller.PathBuilder;
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

        UserProfile originalProfile = ((UserApplicationInfo) getApplication()).getProfile();

        initElements();

        String[] sampleTags = getResources().getStringArray(R.array.sample_tags);
        initAutoCompleteChipGroup(autoCompleteInterests, interestsChip, sampleTags);

        try {
            initUseGoogleProfilePicToggle(originalProfile);
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
                UserApplicationInfo userInput = createUserObject();

                if (validateElements()) {
                    modifyOriginalProfile(originalProfile);
                    userInput.setProfile(originalProfile);

                    if (isCreatingProfile()) {
                        createProfile(userInput);
                    } else {
                        editProfile(userInput);
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

    private void editProfile(UserApplicationInfo userInput) {
        try {
            String jsonBody = userInput.toJsonString();
            RequestManager requestManager = new RequestManager();

            String userId = ((UserApplicationInfo) getApplication()).getProfile().getId();
            String path = new PathBuilder()
                    .addUser()
                    .addNode(userId)
                    .addEdit()
                    .build();

            requestManager.put(path, jsonBody, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {

                    if (response.code() == HttpStatusConstants.STATUS_HTTP_200) {
                        ((UserApplicationInfo) getApplication()).updateApplicationInfo(userInput);
                    } else {
                        Log.e(TAG, "Unable to update user profile");
                    }

                    //todo: should go to profile?
                    //      success message
                    startMainActivity();
                }

                @Override
                public void onError(Call call, IOException e) {
                    Log.e(TAG, "Unable to update profile" + e.getMessage());
                    Toast.makeText(ManageProfileActivity.this, "Unable to update profile. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Failed to update user profile");
            Toast.makeText(ManageProfileActivity.this, "Unable to update profile. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    private void createProfile(UserApplicationInfo userInput) {
        try {
            String jsonBody = userInput.toJsonString();
            RequestManager requestManager = new RequestManager();

            String path = new PathBuilder()
                    .addUser()
                    .addCreate()
                    .build();

            requestManager.post(path, jsonBody, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    UserApplicationInfo newUserInfo = new UserApplicationInfo();
                    try {
                        newUserInfo.populateDetailsFromJson(response.body().string());
                        ((UserApplicationInfo) getApplication()).updateApplicationInfo(newUserInfo);
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
    }

    @NonNull
    private UserApplicationInfo createUserObject() {
        UserApplicationInfo userInput = (UserApplicationInfo) getApplication();

        if (isCreatingProfile()) {
            userInput.setUserToken(getIntent().getExtras().getString("userToken"));
        } else {
            userInput.setUserToken(((UserApplicationInfo) getApplication()).getUserToken());
        }
        return userInput;
    }

    private void modifyOriginalProfile(UserProfile originalProfile) {
        originalProfile.setFirstName(firstNameEdit.getText().toString());
        originalProfile.setLastName(lastNameEdit.getText().toString());
        originalProfile.setLocation(standardizeAddress(locationEdit.getText().toString(), getApplicationContext()));

        List<Tag> tags = getTagsFromChipGroup();
        originalProfile.setTags(tags);

        originalProfile.setDescription(descriptionEdit.getText().toString());
    }

    private List<Tag> getTagsFromChipGroup(){
        List<Tag> result = new ArrayList<>();
        for(int i = 0; i < interestsChip.getChildCount(); i++){
            Chip chip = (Chip) interestsChip.getChildAt(i);
            result.add(new Tag(chip.getText().toString()));
        }
        return result;
    }

    public void initChipsForChipGroup(ChipGroup chipGroup, String chipText) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);
        chip.setText(chipText);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
            }
        });
        chipGroup.addView(chip);
    }

    private void initAutoCompleteChipGroup(AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup, String[] fillArray){
        ArrayAdapter<String> arrayAdapterTags = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, fillArray);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapterTags);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoCompleteTextView.setText("");

                initChipsForChipGroup(chipGroup, (String) parent.getItemAtPosition(position));
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

        if (isCreatingProfile()) {
            userProfile.setProfilePicture(getProfilePicUrl());
            Picasso.get().load(getProfilePicUrl()).into(profilePicPreview);
        } else {
            String profilePicUrl = ((UserApplicationInfo) getApplication()).getProfile().getProfilePicture();
            Picasso.get().load(profilePicUrl).into(profilePicPreview);
        }


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
            initChipsForChipGroup(interestsChip, interest);
        }

        descriptionEdit.setText(existingUserProfile.getDescription());
        Picasso.get().load(existingUserProfile.getProfilePicture()).into(profilePicPreview);

        TextView titleView = findViewById(R.id.profileTitle);
        String editTitle = "Edit Profile";
        titleView.setText(editTitle);

        String editConfirm = "Confirm Edit!";
        confirm.setText(editConfirm);
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

    private boolean validateElements() {
        boolean isValid = true;

        if (firstNameEdit.getText().toString().isEmpty()) {
            firstNameEdit.setError("First name must not be blank.");
            isValid = false;
        }

        if (!isValidNameTitle(firstNameEdit.getText().toString())) {
            firstNameEdit.setError("First name contains invalid character(s).");
            isValid = false;
        }

        if (lastNameEdit.getText().toString().isEmpty()) {
            lastNameEdit.setError("Last name must not be blank.");
            isValid = false;
        }

        if (!isValidNameTitle(lastNameEdit.getText().toString())) {
            lastNameEdit.setError("Last name contains invalid character(s).");
            isValid = false;
        }

        Address address = getAddressFromString(locationEdit.getText().toString(), getApplicationContext());

        if (!validateLocation(address)) {
            locationEdit.setError("Unable to find location.");
            isValid = false;
        }

        if (interestsChip.getChildCount() == 0) {
            autoCompleteInterests.setError("Please add at least one interest");
            isValid = false;
        }

        return isValid;
    }
}