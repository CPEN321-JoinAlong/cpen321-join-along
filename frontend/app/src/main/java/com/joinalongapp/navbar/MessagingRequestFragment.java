package com.joinalongapp.navbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.adapter.MessagingRequestCustomAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
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
 * Use the {@link MessagingRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingRequestFragment extends Fragment {
    private MessagingRequestCustomAdapter messagingRequestCustomAdapter;

    protected List<ChatDetails> dataset;

    public MessagingRequestFragment() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.fragment_messaging_list, container, false);

        RecyclerView messagingRequestRecyclerView = (RecyclerView) rootView.findViewById(R.id.messagingListRecyclerView);

        messagingRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        messagingRequestCustomAdapter = new MessagingRequestCustomAdapter(dataset);
        messagingRequestRecyclerView.setAdapter(messagingRequestCustomAdapter);


        return rootView;
        }

    private void initDataset(Activity activity) throws IOException {
        UserProfile user = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String id = user.getId();
        String operation = "Get Chat Invites";
        RequestManager requestManager = new RequestManager();

        String path = new PathBuilder()
                .addUser()
                .addNode(id)
                .addNode("chatInvites")
                .build();

        requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {

                if (response.isSuccessful()) {
                    List<ChatDetails> outputChats = new ArrayList<>();
                    try{
                        //System.out.println(response.body().string());
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for(int i = 0; i < jsonArray.length(); i++){
                            ChatDetails chatDetails = new ChatDetails();
                            chatDetails.populateDetailsFromJson(jsonArray.get(i).toString());
                            outputChats.add(chatDetails);
                        }

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messagingRequestCustomAdapter.changeDataset(outputChats);
                                    }
                                });
                            }
                        }, 0);

                    } catch(JSONException | IOException e){
                        FeedbackMessageBuilder.createParseError(e, operation, activity);
                    }
                } else {
                    ResponseErrorHandler.createErrorMessage(response, operation, "User or Chat", activity);
                }
            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
            }
        });
    }
}