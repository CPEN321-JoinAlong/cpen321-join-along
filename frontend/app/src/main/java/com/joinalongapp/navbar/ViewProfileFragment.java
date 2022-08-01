package com.joinalongapp.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.CreateReportActivity;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.UserProfile;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewProfileFragment extends Fragment {
    private ImageButton backButton;
    private UserProfile userProfile;
    private TextView profileName;
    private ImageView profilePicture;
    private ChipGroup manageTags;
    private TextView description;
    private Button report;
    private Button addFriend;
    private Button ban;
    private boolean hide;

    public ViewProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewProfileFragment newInstance(String param1, String param2) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userProfile = (UserProfile) getArguments().getSerializable("USER_INFO");
            hide = (boolean) getArguments().getBoolean("HIDE");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        initDataset(view);

        String token = ((UserApplicationInfo) (getActivity().getApplication())).getUserToken();
        UserProfile globalUserProfile = ((UserApplicationInfo) (getActivity().getApplication())).getProfile();
        String userId = globalUserProfile.getId();
        String otherUserId = userProfile.getId();

        if(userProfile.getInterests() != null){
            addTagsToChipGroup();
        }


        profileName.setText(userProfile.getFullName());
        description.setText(userProfile.getDescription());

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reportIntent = new Intent(getActivity(), CreateReportActivity.class);
                reportIntent.putExtra("REPORT_PERSON", true);
                reportIntent.putExtra("REPORTING_PERSON", userProfile);
                startActivity(reportIntent);
            }
        });


        if (hide || shouldHideAddFriendButton(globalUserProfile, otherUserId)){
            addFriend.setVisibility(View.INVISIBLE);
        }

        addFriend.setOnClickListener(new View.OnClickListener() {
            String operation = "Send Friend Request";

            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode("sendFriendRequest")
                            .addNode(userId)
                            .addNode(otherUserId)
                            .build();

                    new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            if (response.isSuccessful()) {
                                addFriend.setVisibility(View.INVISIBLE);
                            } else {
                                ResponseErrorHandler.createErrorMessage(response, operation, "User", getActivity());
                            }
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                        }
                    });
                } catch (IOException e) {
                    FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                }
            }
        });

        if(!globalUserProfile.isAdmin()){
            ban.setVisibility(View.INVISIBLE);
        }

        ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String operation = "Ban User";

                ban.setVisibility(View.INVISIBLE);

                JSONObject json = new JSONObject();
                try {
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode(otherUserId)
                            .addNode("ban")
                            .build();

                    new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            if (response.isSuccessful()) {
                                new FeedbackMessageBuilder()
                                        .setTitle("Successful Ban")
                                        .setDescription("Successfully banned event with title " + userProfile.getFullName())
                                        .withActivity(getActivity())
                                        .buildAsyncNeutralMessage();

                                Log.i("banEvent", globalUserProfile.getId() + " banned " + userProfile.getFullName() + " at " + new Date());
                                ban.setVisibility(View.INVISIBLE);
                            } else {
                                ResponseErrorHandler.createErrorMessage(response, operation, "Event", getActivity());
                            }
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                        }
                    });
                } catch (IOException e) {
                    FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                }
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Picasso.get().load(userProfile.getProfilePicture()).into(profilePicture);

        return view;
    }

    private boolean shouldHideAddFriendButton(UserProfile theGlobalUserProfile, String theOtherUserId) {
        return theGlobalUserProfile.getFriends().contains(theOtherUserId);
    }

    private void initDataset(View view){
        backButton = view.findViewById(R.id.chatBackButton);
        profileName = view.findViewById(R.id.profileName);
        profilePicture = view.findViewById(R.id.profilePicture);
        manageTags  = view.findViewById(R.id.manageChatFriends);
        description = view.findViewById(R.id.description);
        report = view.findViewById(R.id.blockUserButton);
        addFriend = view.findViewById(R.id.addFriendButton);
        ban = view.findViewById(R.id.banButton);
    }

    private void addTagsToChipGroup(){
        for(String tag : userProfile.getStringListOfTags()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, manageTags, false);
            chip.setText(tag);
            manageTags.addView(chip);
        }
    }
}