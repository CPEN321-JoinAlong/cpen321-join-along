package com.joinalongapp.navbar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.MessagingListCustomAdapter;
import com.joinalongapp.viewmodel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessagingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView messagingListRecyclerView;
    private MessagingListFragment.LayoutManagerType layoutManagerType;
    private MessagingListCustomAdapter messagingListCustomAdapter;

    protected List<User> dataset;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MessagingListFragment() {
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
     * @return A new instance of fragment MessagingListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagingListFragment newInstance(String param1, String param2) {
        MessagingListFragment fragment = new MessagingListFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_messaging_list, container, false);

        messagingListRecyclerView = (RecyclerView) rootView.findViewById(R.id.messagingListRecyclerView);

        layoutManagerType = MessagingListFragment.LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if(savedInstanceState != null){
            layoutManagerType = (MessagingListFragment.LayoutManagerType) savedInstanceState.getSerializable("layoutManager");
        }

        messagingListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        messagingListCustomAdapter = new MessagingListCustomAdapter(dataset);
        messagingListRecyclerView.setAdapter(messagingListCustomAdapter);


        return rootView;
    }

    private void initDataset(){
        // TODO: GET LIST OF USERS
        User a = new User(UUID.randomUUID(), "Ken");
        User b = new User(UUID.randomUUID(), "Justin");
        User c = new User(UUID.randomUUID(), "Kamran");
        User d = new User(UUID.randomUUID(), "Zoeb");
        User e = new User(UUID.randomUUID(), "Ken");
        User f = new User(UUID.randomUUID(), "Justin");
        User g = new User(UUID.randomUUID(), "Kamran");
        User h = new User(UUID.randomUUID(), "Zoeb");
        User i = new User(UUID.randomUUID(), "Zoeb");

        List<User> result = new ArrayList<>();
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