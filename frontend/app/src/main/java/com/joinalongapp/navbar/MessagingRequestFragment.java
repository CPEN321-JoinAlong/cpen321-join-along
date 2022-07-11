package com.joinalongapp.navbar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.MessagingRequestCustomAdapter;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.ChatDetails;
import com.joinalongapp.adapter.MessagingRequestCustomAdapter;
import com.joinalongapp.viewmodel.Tag;
import com.joinalongapp.viewmodel.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessagingRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingRequestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView messagingRequestRecyclerView;
    private MessagingRequestFragment.LayoutManagerType layoutManagerType;
    private MessagingRequestCustomAdapter messagingRequestCustomAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    protected List<ChatDetails> dataset;

    public MessagingRequestFragment() {
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
     * @return A new instance of fragment MessagingRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagingRequestFragment newInstance(String param1, String param2) {
        MessagingRequestFragment fragment = new MessagingRequestFragment();
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

        messagingRequestRecyclerView = (RecyclerView) rootView.findViewById(R.id.messagingListRecyclerView);

        layoutManagerType = MessagingRequestFragment.LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if(savedInstanceState != null){
            layoutManagerType = (MessagingRequestFragment.LayoutManagerType) savedInstanceState.getSerializable("layoutManager");
        }

        messagingRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        messagingRequestCustomAdapter = new MessagingRequestCustomAdapter(dataset);
        messagingRequestRecyclerView.setAdapter(messagingRequestCustomAdapter);


        return rootView;
        }

    private void initDataset(){
        // TODO: GET LIST OF USERS
        ChatDetails a = new ChatDetails();
        a.setId(UUID.randomUUID());
        a.setTitle("CHAT 1");
        a.setDescription("SAMPLE DESCRIPTION");
        Tag t = new Tag("hike");
        UserProfile u = new UserProfile(UUID.randomUUID(), "Ken", "Liang");
        UserProfile ub = new UserProfile(UUID.randomUUID(), "Justin", "D");
        List<Tag> lt = new ArrayList<>();
        List<UserProfile> lu = new ArrayList<>();
        List<UserProfile> lb = new ArrayList<>();
        lt.add(t);
        lu.add(u);

        a.setTags(lt);
        a.setPeople(lu);
        lb.add(ub);
        ChatDetails b = new ChatDetails();
        b.setId(UUID.randomUUID());
        b.setTitle("yes");
        b.setPeople(lb);
        ChatDetails c = new ChatDetails();
        ChatDetails d = new ChatDetails();


        List<ChatDetails> result = new ArrayList<>();
        result.add(a);
        result.add(b);
        result.add(c);
        result.add(d);

        dataset = result;
    }
}