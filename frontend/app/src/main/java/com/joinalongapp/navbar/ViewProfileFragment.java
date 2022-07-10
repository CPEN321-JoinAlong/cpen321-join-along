package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.UserProfile;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        initDataset(view);
        if(userProfile.getInterests() != null){
            addTagsToChipGroup();
        }


        profileName.setText(userProfile.getFullName());
        description.setText(userProfile.getDescription());


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
    }

    private void addTagsToChipGroup(){
        for(String tag : userProfile.getStringListOfTags()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, manageTags, false);
            chip.setText(tag);
            manageTags.addView(chip);
        }
    }


}