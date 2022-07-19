
package com.joinalongapp.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.joinalong.LoginActivity;
import com.joinalongapp.joinalong.ManageProfileActivity;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.SearchScreenActivity;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.UserProfile;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private TextView profileName;
    private ImageView profilePicture;
    private ImageButton editButton;
    private ChipGroup interestsChipGroup;
    private ImageButton logoutButton;
    private TextView profileDescription;
    private Button viewReportsButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initElements(view);
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userName = user.getFullName();
        String description = user.getDescription();
        List<String> tags = user.getStringListOfTags();

        profileName.setText(userName);
        addTagsToChipGroup(tags);
        Picasso.get().load(user.getProfilePicture()).into(profilePicture);
        profileDescription.setText(description);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfile = new Intent(getActivity(), ManageProfileActivity.class);
                editProfile.putExtra("MODE", ManageProfileActivity.ManageProfileMode.PROFILE_EDIT);
                startActivity(editProfile);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        viewReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchUsers = new Intent(getActivity(), SearchScreenActivity.class);
                searchUsers.putExtra("mode", SearchScreenActivity.SearchMode.USER_MODE);
                startActivity(searchUsers);
            }
        });

        return view;
    }

    private void addTagsToChipGroup(List<String> tags){
        for(String tag : tags){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, interestsChipGroup, false);
            chip.setText(tag);
            interestsChipGroup.addView(chip);
        }
    }

    private void initElements(View view){
        profileName = view.findViewById(R.id.profileName);
        profilePicture = view.findViewById(R.id.profilePicture);
        editButton = view.findViewById(R.id.editButton);
        interestsChipGroup = view.findViewById(R.id.interestsChipGroup);
        logoutButton = view.findViewById(R.id.logoutButton);
        profileDescription = view.findViewById(R.id.userDescription);
        viewReportsButton = view.findViewById(R.id.viewReportsButton);
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });

        Intent i = new Intent(getActivity(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}