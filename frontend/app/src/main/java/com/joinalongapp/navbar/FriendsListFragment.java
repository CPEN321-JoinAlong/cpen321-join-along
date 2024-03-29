package com.joinalongapp.navbar;

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

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.adapter.FriendsListCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandlerUtils;
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
 * Use the {@link FriendsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsListFragment extends Fragment {
    private RecyclerView friendsListRecyclerView;
    private FriendsListCustomAdapter friendsListCustomAdapter;
    protected List<UserProfile> dataset;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noResults;

    public FriendsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsList.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsListFragment newInstance(String param1, String param2) {
        FriendsListFragment fragment = new FriendsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            initDataset();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("FriendsListFragmentUIComponents");
        myTrace.start();
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        initElements(rootView);
        initAdapter();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    initDataset();
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
        myTrace.stop();
        return rootView;
    }

    private void initAdapter() {
        friendsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsListCustomAdapter = new FriendsListCustomAdapter(dataset);
        friendsListRecyclerView.setAdapter(friendsListCustomAdapter);
    }

    private void initElements(View rootView) {
        friendsListRecyclerView = (RecyclerView) rootView.findViewById(R.id.friendsListRecyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.friendsSwipeRefresh);
        noResults = rootView.findViewById(R.id.friendsListNoResults);
    }

    private void initDataset() throws IOException{
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String id = user.getId();
        String operation = "Load Friends List";

        String path = new PathBuilder()
                .addUser()
                .addNode(id)
                .addNode("friends")
                .build();

        new RequestManager().get(path, userToken, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {

                if (response.isSuccessful()) {
                    List<UserProfile> outputFriends = new ArrayList<>();
                    try{
                        System.out.println(response);
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for(int i = 0; i < jsonArray.length(); i++){
                            UserProfile userProfile = new UserProfile();
                            userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                            outputFriends.add(userProfile);
                        }
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        friendsListCustomAdapter.changeDataset(outputFriends);

                                        if (outputFriends.size() == 0) {
                                            noResults.setVisibility(View.VISIBLE);
                                        } else {
                                            noResults.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                        }, 0);
                        System.out.println("efwa");

                    } catch(JSONException | IOException e){
                        FeedbackMessageBuilder.createParseError(e, operation, getActivity());
                    }
                } else if (response.code() == HttpStatusConstants.STATUS_HTTP_404) {
                    noResults.setVisibility(View.VISIBLE);
                }else {
                    ResponseErrorHandlerUtils.createErrorMessage(response, operation, "User", getActivity());
                }

            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
            }
        });

    }
}