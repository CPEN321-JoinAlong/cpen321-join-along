package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.ChatDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewChatFragment extends Fragment {

    ImageButton backButton;
    ChatDetails chatDetails;
    TextView title;
    TextView description;
    ChipGroup tags;
    ChipGroup friends;

    public ViewChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewChatFragment newInstance(String param1, String param2) {
        ViewChatFragment fragment = new ViewChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatDetails = (ChatDetails) getArguments().getSerializable("CHAT_INFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_chat, container, false);
        initDataset(view);

        title.setText(chatDetails.getTitle());
        description.setText(chatDetails.getDescription());
        //add check
        addTagsFriendsToChipGroup();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

    private void initDataset(View view){
        backButton = view.findViewById(R.id.chatBackButton);
        title = view.findViewById(R.id.viewChatTitle);
        description = view.findViewById(R.id.viewChatDescription);
        tags = view.findViewById(R.id.viewChatAddTags);
        friends = view.findViewById(R.id.viewChatAddFriends);
    }

    private void addTagsFriendsToChipGroup(){
        for(String tag : chatDetails.getStringListOfTags()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, tags, false);
            chip.setText(tag);
            tags.addView(chip);
        }

        List<String> peopleIds = chatDetails.getPeople();
        List<String> friendNames = new ArrayList<>();
        RequestManager requestManager = new RequestManager();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();

        //TODO: FIXME hacky loop of gets
        for (String friendId : peopleIds) {
            try {
                String path = new PathBuilder()
                        .addUser()
                        .addNode(friendId)
                        .build();

                requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject userJson = new JSONObject(response.body().string());
                                friendNames.add(userJson.getString("name"));
                            } catch (IOException | JSONException e) {
                                //Do nothing: Just don't load the member chip
                            }
                        }

                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        //Do nothing: Just don't load the member chip
                    }
                });
            } catch (IOException e) {
                //Do nothing: Just don't load the member chip
            }

        }

        for(String friend : friendNames){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, friends, false);
            chip.setText(friend);
            friends.addView(chip);
        }
    }
}