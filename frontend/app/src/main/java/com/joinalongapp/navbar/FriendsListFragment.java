package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.FriendsListCustomAdapter;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Tag;
import com.joinalongapp.viewmodel.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView friendsListRecyclerView;
    private LayoutManagerType layoutManagerType;
    private FriendsListCustomAdapter friendsListCustomAdapter;

    protected List<UserProfile> dataset;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsListFragment() {
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
     * @return A new instance of fragment FriendsList.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsListFragment newInstance(String param1, String param2) {
        FriendsListFragment fragment = new FriendsListFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        friendsListRecyclerView = (RecyclerView) rootView.findViewById(R.id.friendsListRecyclerView);

        layoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if(savedInstanceState != null){
            layoutManagerType = (LayoutManagerType) savedInstanceState.getSerializable("layoutManager");
        }

        friendsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        friendsListCustomAdapter = new FriendsListCustomAdapter(dataset);
        friendsListRecyclerView.setAdapter(friendsListCustomAdapter);


        return rootView;
    }

    private void initDataset(){
        // TODO: GET LIST OF USERS
        UserProfile a = new UserProfile(UUID.randomUUID().toString(), "Ken", "L");
        Tag t = new Tag("Hiking");
        Tag ta = new Tag("swimming");
        List<Tag> lt = new ArrayList<>();
        lt.add(t);
        lt.add(ta);
        a.setInterests(lt);
        a.setDescription("PLEASE WORK");
        UserProfile b = new UserProfile(UUID.randomUUID().toString(), "Justin", "D");
        UserProfile c = new UserProfile(UUID.randomUUID().toString(), "Kamran", "A");
        UserProfile d = new UserProfile(UUID.randomUUID().toString(), "Zoeb", "G");
        UserProfile e = new UserProfile(UUID.randomUUID().toString(), "Ken", "");
        UserProfile f = new UserProfile(UUID.randomUUID().toString(), "Justin", "");
        UserProfile g = new UserProfile(UUID.randomUUID().toString(), "Kamran", "");
        UserProfile h = new UserProfile(UUID.randomUUID().toString(), "Zoeb", "");
        UserProfile i = new UserProfile(UUID.randomUUID().toString(), "Zoeb", "");

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