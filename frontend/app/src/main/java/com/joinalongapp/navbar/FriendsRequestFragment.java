package com.joinalongapp.navbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.FriendsRequestCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsRequestFragment extends Fragment {

    private RecyclerView friendsRequestRecyclerView;
    private FriendsRequestCustomAdapter friendsRequestCustomAdapter;
    protected List<UserProfile> dataset;

    public FriendsRequestFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsRequest.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsRequestFragment newInstance(String param1, String param2) {
        FriendsRequestFragment fragment = new FriendsRequestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initDataset(getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_request, container, false);

        friendsRequestRecyclerView = (RecyclerView) rootView.findViewById(R.id.peopleRecyclerView);
        friendsRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsRequestCustomAdapter = new FriendsRequestCustomAdapter(dataset);
        friendsRequestRecyclerView.setAdapter(friendsRequestCustomAdapter);

        return rootView;
    }
    private void initDataset(Activity activity) throws IOException {
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String id = user.getId();
        RequestManager requestManager = new RequestManager();
        String path = new PathBuilder()
                .addUser()
                .addNode(id)
                .addNode("friendRequest")
                .build();

        requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {

                List<UserProfile> outputFriends = new ArrayList<>();
                try{
                    //System.out.println(response.body().string());
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    for(int i = 0; i < jsonArray.length(); i++){
                        UserProfile userProfile = new UserProfile();
                        userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                        outputFriends.add(userProfile);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    friendsRequestCustomAdapter.changeDataset(outputFriends);
                                }
                            });
                        }
                    }, 0);


                } catch(JSONException | IOException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onError(Call call, IOException e) {
                System.out.println(call.toString());
            }
        });

    }
}