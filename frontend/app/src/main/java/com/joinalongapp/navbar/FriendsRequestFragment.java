package com.joinalongapp.navbar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.adapter.FriendsRequestCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noResults;

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

        initElements(rootView);
        initAdapter();

        Activity activity = this.getActivity();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    initDataset(activity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000); //TODO: FIXME: a delay seems kinda hacky here
            }
        });

        return rootView;
    }

    private void initAdapter() {
        friendsRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsRequestCustomAdapter = new FriendsRequestCustomAdapter(dataset);
        friendsRequestRecyclerView.setAdapter(friendsRequestCustomAdapter);
    }

    private void initElements(View rootView) {
        friendsRequestRecyclerView = (RecyclerView) rootView.findViewById(R.id.peopleRecyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.friendsFragmentSwipeRefresh);
        noResults = rootView.findViewById(R.id.friendsRequestNoResults);
    }

    private void initDataset(Activity activity) throws IOException {
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String id = user.getId();
        String operation = "Get Friend Requests";

        String path = new PathBuilder()
                .addUser()
                .addNode(id)
                .addNode("friendRequest")
                .build();

        new RequestManager().get(path, userToken, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {

                if (response.isSuccessful()) {
                    List<UserProfile> outputFriends = new ArrayList<>();
                    try {
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

                                        if (outputFriends.size() == 0) {
                                            noResults.setVisibility(View.VISIBLE);
                                        } else {
                                            noResults.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                        }, 0);

                    } catch(JSONException | IOException e){
                        FeedbackMessageBuilder.createParseError(e, operation, activity);
                    }
                } else if (response.code() == HttpStatusConstants.STATUS_HTTP_404) {
                    noResults.setVisibility(View.VISIBLE);
                } else {
                    ResponseErrorHandler.createErrorMessage(response, operation, "User", activity);
                }
            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
            }
        });

    }
}