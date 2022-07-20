package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.MessagingListCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.ChatDetails;
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
 * Use the {@link MessagingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingListFragment extends Fragment {

    private RecyclerView messagingListRecyclerView;
    private MessagingListCustomAdapter messagingListCustomAdapter;

    protected List<ChatDetails> dataset;


    public MessagingListFragment() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.fragment_messaging_list, container, false);

        initElements(rootView);
        initAdapter();

        return rootView;
    }

    private void initAdapter() {
        messagingListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messagingListCustomAdapter = new MessagingListCustomAdapter(dataset);
        messagingListRecyclerView.setAdapter(messagingListCustomAdapter);
    }

    private void initElements(View rootView) {
        messagingListRecyclerView = (RecyclerView) rootView.findViewById(R.id.messagingListRecyclerView);
    }

    private void initDataset() throws IOException {
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String id = user.getId();
        RequestManager requestManager = new RequestManager();

        String path = new PathBuilder()
                .addUser()
                .addNode(id)
                .addChat()
                .build();

        requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {
                System.out.println(response.toString());
                List<ChatDetails> outputChats = new ArrayList<>();
                try{
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    for(int i = 0; i < jsonArray.length(); i++){
                        ChatDetails chatDetails = new ChatDetails();
                        chatDetails.populateDetailsFromJson(jsonArray.get(i).toString());
                        outputChats.add(chatDetails);
                    }
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messagingListCustomAdapter.changeDataset(outputChats);
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