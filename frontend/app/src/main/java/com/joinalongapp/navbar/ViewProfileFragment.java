package com.joinalongapp.navbar;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.ReportActivity;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.ReportDetails;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageButton backButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private UserProfile userProfile;
    private TextView profileName;
    private ImageView profilePicture;
    private ChipGroup manageTags;
    private TextView description;
    private Button report;
    private Button addFriend;
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
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
                Intent reportIntent = new Intent(getActivity(), ReportActivity.class);
                reportIntent.putExtra("REPORT_PERSON", true);
                startActivity(reportIntent);
            }
        });
        if(hide){
            addFriend.setVisibility(View.INVISIBLE);
        }
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend.setVisibility(View.INVISIBLE);
                RequestManager requestManager = new RequestManager();
                JSONObject json = new JSONObject();
                try {
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    requestManager.put("user/acceptUser/" + userId + "/" + otherUserId, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            System.out.println(response.toString());
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            System.out.println(call.toString());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void initDataset(View view){
        backButton = view.findViewById(R.id.backButton);
        profileName = view.findViewById(R.id.profileName);
        profilePicture = view.findViewById(R.id.profilePicture);
        manageTags  = view.findViewById(R.id.manageTags);
        description = view.findViewById(R.id.description);
        report = view.findViewById(R.id.blockUserButton);
        addFriend = view.findViewById(R.id.addFriendButton);
    }

    private void addTagsToChipGroup(){
        for(String tag : userProfile.getStringListOfTags()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, manageTags, false);
            chip.setText(tag);
            manageTags.addView(chip);
        }
    }
}