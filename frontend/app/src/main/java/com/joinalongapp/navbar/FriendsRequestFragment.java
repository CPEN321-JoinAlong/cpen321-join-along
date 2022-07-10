package com.joinalongapp.navbar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.FriendsRequestCustomAdapter;
import com.joinalongapp.viewmodel.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsRequestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView friendsRequestRecyclerView;
    private FriendsRequestFragment.LayoutManagerType layoutManagerType;
    private FriendsRequestCustomAdapter friendsRequestCustomAdapter;

    protected List<UserProfile> dataset;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsRequestFragment() {
        // Required empty public constructor
    }

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
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
        }
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_request, container, false);

        friendsRequestRecyclerView = (RecyclerView) rootView.findViewById(R.id.peopleRecyclerView);

        layoutManagerType = FriendsRequestFragment.LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if(savedInstanceState != null){
            layoutManagerType = (FriendsRequestFragment.LayoutManagerType) savedInstanceState.getSerializable("layoutManager");
        }

        friendsRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        friendsRequestCustomAdapter = new FriendsRequestCustomAdapter(dataset);
        friendsRequestRecyclerView.setAdapter(friendsRequestCustomAdapter);


        return rootView;
    }
    private void initDataset(){
        // TODO: GET LIST OF USERS
        UserProfile a = new UserProfile(UUID.randomUUID(), "Ken", "");
        UserProfile b = new UserProfile(UUID.randomUUID(), "Justin", "");
        UserProfile c = new UserProfile(UUID.randomUUID(), "Kamran", "");
        UserProfile d = new UserProfile(UUID.randomUUID(), "Zoeb", "");
        UserProfile e = new UserProfile(UUID.randomUUID(), "Ken", "");
        UserProfile f = new UserProfile(UUID.randomUUID(), "Justin", "");
        UserProfile g = new UserProfile(UUID.randomUUID(), "Kamran", "");
        UserProfile h = new UserProfile(UUID.randomUUID(), "Zoeb", "");
        UserProfile i = new UserProfile(UUID.randomUUID(), "Zoeb", "");

        List<UserProfile> result = new ArrayList<>();
        result.add(a);
        result.add(b);
        result.add(c);
        result.add(d);
        result.add(e);
        result.add(f);
        result.add(g);
        result.add(h);
        result.add(i);
        dataset = result;
    }
}