package com.joinalongapp.navbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandlerUtils;
import com.joinalongapp.joinalong.CreateReportActivity;
import com.joinalongapp.joinalong.ManageEventActivity;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.UserProfile;
import com.lyft.deeplink.RideTypeEnum;
import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.LyftStyle;
import com.lyft.lyftbutton.RideParams;
import com.lyft.networking.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewEventFragment extends Fragment {
    private TextView title;
    private ChipGroup tags;
    private TextView description;
    private TextView location;
    private TextView beginDate;
    private TextView endDate;
    private ChipGroup organizer;
    private ChipGroup members;
    private TextView numPeople;
    private ImageButton backButton;
    private Button joinButton;
    private LyftButton lyftButton;
    private Event event;
    private ImageButton options;
    private PopupMenu menu;
    private Chip userChip;
    private Button ban;
    private String token;
    private String userId;

    public ViewEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewEvent.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewEventFragment newInstance(String param1, String param2) {
        ViewEventFragment fragment = new ViewEventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("ViewEventFragmentUIComponents");
        myTrace.start();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_event, container, false);

        initElements(view);

        Bundle bundle = getArguments();
        token = ((UserApplicationInfo) (getActivity().getApplication())).getUserToken();
        userId = ((UserApplicationInfo) (getActivity().getApplication())).getProfile().getId();
        if (bundle != null) {
            event = (Event) bundle.getSerializable("event");
            initEventDetails(event);
        }

        if(token == null){
            token = (String) bundle.getString("testingToken");
        }

        if(userId == null){
            userId = (String) bundle.getString("testingId");
        }

        initButtonVisibility();

        initBackButton();
        initJoinButton();
        initLyftButton();
        initEventMenu();
        initBanButton();
        myTrace.stop();
        return view;
    }

    private void initLyftButton() {
        ApiConfig config = new ApiConfig.Builder()
                .setClientId("...")
                .setClientToken("...")
                .build();

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            lyftButton.setLyftStyle(LyftStyle.MULBERRY_DARK);
        } else {
            lyftButton.setLyftStyle(LyftStyle.MULTI_COLOR);
        }

        lyftButton.setApiConfig(config);

        RideParams.Builder rideParamsBuilder = new RideParams.Builder();
        rideParamsBuilder.setRideTypeEnum(RideTypeEnum.STANDARD);

        lyftButton.setRideParams(rideParamsBuilder.build());
        lyftButton.load();
    }

    private void initBanButton(){

        UserProfile globalUserProfile = ((UserApplicationInfo) (getActivity().getApplication())).getProfile();

        if(!globalUserProfile.isAdmin()){
            ban.setVisibility(View.INVISIBLE);
        }

        ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ban.setVisibility(View.INVISIBLE);
                banOrDeleteEvent(globalUserProfile, "banned");
            }
        });
    }

    private void banOrDeleteEvent(UserProfile globalUserProfile, String operation) {
        String operationForErrorMsg;
        if (operation.equals("deleted")) {
            operationForErrorMsg = "Delete Event";
        } else {
            operationForErrorMsg = "Ban Event";
        }

        RequestManager requestManager = new RequestManager();
        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String path = new PathBuilder()
                    .addEvent()
                    .addNode(event.getEventId())
                    .addNode("ban")
                    .build();

            requestManager.post(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    if (response.isSuccessful()) {
                        new FeedbackMessageBuilder()
                                .setTitle("Successfully " + operation)
                                .setDescription("Successfully "+ operation +" event with title " + event.getTitle())
                                .withActivity(getActivity())
                                .buildAsyncNeutralMessage();

                        Log.i("deleteEvent", globalUserProfile.getId() + operation + event.getTitle() + " at " + new Date());
                        ban.setVisibility(View.INVISIBLE);
                        shouldAllowMenuItem(R.id.eventDelete, false);
                    } else {
                        ResponseErrorHandlerUtils.createErrorMessage(response, operationForErrorMsg, "Event", getActivity());
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    FeedbackMessageBuilder.createServerConnectionError(e, operationForErrorMsg, getActivity());
                }
            });
        } catch (IOException e) {
            FeedbackMessageBuilder.createServerConnectionError(e, operationForErrorMsg, getActivity());
        }
    }

    private void initEventMenu() {
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.eventOptions));
                menu.inflate(R.menu.events_options_menu);
                initMenuOptionsVisibility();
                FragmentActivity activity = getActivity();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.eventLeave:
                                UserApplicationInfo userApplicationInfo = ((UserApplicationInfo) getActivity().getApplication());
                                //String userId = userApplicationInfo.getProfile().getId();
                                String eventId = event.getEventId();
                                String operation = "Leave Event";

                                try {
                                    String path = new PathBuilder()
                                            .addUser()
                                            .addNode("leaveEvent")
                                            .addNode(userId)
                                            .addNode(eventId)
                                            .build();

                                    JSONObject json = new JSONObject();
                                    json.put("token", token);

                                    new RequestManager().put(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                                        @Override
                                        public void onSuccess(Call call, Response response) {

                                            if (response.isSuccessful()) {
                                                new Timer().schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        activity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                //TODO: can remove the next few lines of code since it's done in modify on leave?
                                                                //      have to check...
                                                                joinButton.setVisibility(View.VISIBLE);
                                                                lyftButton.setVisibility(View.GONE);
                                                                menu.getMenu().findItem(R.id.eventLeave).setVisible(false);

                                                                new AlertDialog.Builder(activity)
                                                                        .setTitle("Successfully Left Event")
                                                                        .setMessage("You have now left " + event.getTitle())
                                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        })
                                                                        .create()
                                                                        .show();

                                                                String userName = userApplicationInfo.getProfile().getFullName();
                                                                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, members, false);
                                                                chip.setText(userName);
                                                                members.removeView(chip);

                                                                modifyViewOnLeaveEvent(userApplicationInfo);
                                                            }
                                                        });



                                                    }
                                                }, 0);
                                            } else {
                                                ResponseErrorHandlerUtils.createErrorMessage(response, operation, "Event", getActivity());
                                            }
                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                                        }
                                    });
                                } catch (IOException e) {
                                    FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                                } catch (JSONException e) {
                                    FeedbackMessageBuilder.createParseError(e, operation, getActivity());
                                }

                                return true;

                            case R.id.eventReport:
                                Intent report = new Intent(getActivity(), CreateReportActivity.class);
                                report.putExtra("REPORT_PERSON", false);
                                report.putExtra("REPORTING_EVENT", event);
                                startActivity(report);
                                return true;

                            case R.id.eventEdit:
                                Intent editEvent = new Intent(getActivity(), ManageEventActivity.class);
                                editEvent.putExtra("EVENT", event);
                                startActivity(editEvent);
                                return true;

                            case R.id.eventDelete:
                                UserProfile globalUserProfile = ((UserApplicationInfo) (getActivity().getApplication())).getProfile();
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new AlertDialog.Builder(activity)
                                                        .setTitle("Delete Event Confirmation")
                                                        .setMessage("Are you sure you want to permanently delete this event?\nDeleting is not reversible.")
                                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                                banOrDeleteEvent(globalUserProfile, "deleted");
                                                            }
                                                        })
                                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .create()
                                                        .show();
                                            }
                                        });
                                    }
                                }, 0);

                                return true;

                            default:
                                return false;

                        }
                    }
                });
                menu.show();
            }
        });
    }

    private void modifyViewOnLeaveEvent(UserApplicationInfo userApplicationInfo) {
        joinButton.setVisibility(View.VISIBLE);
        lyftButton.setVisibility(View.GONE);
        members.removeView(userChip);
        userChip = null;
        event.setCurrentNumPeopleRegistered(event.getCurrentNumPeopleRegistered() - 1);
        String numPeopleInEventString = " (" + event.getCurrentNumPeopleRegistered() + "/" + event.getNumberOfPeopleAllowed() + ")";
        numPeople.setText(numPeopleInEventString);
        event.removeMemberFromList(userApplicationInfo.getProfile().getId());

    }

    private void initJoinButton() {
        FragmentActivity activity = getActivity();
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RequestManager requestManager = new RequestManager();
                UserApplicationInfo userApplicationInfo = ((UserApplicationInfo) getActivity().getApplication());
                //String userId = userApplicationInfo.getProfile().getId();
                String eventId = event.getEventId();
                String operation = "Join Event";

                String path = new PathBuilder()
                        .addUser()
                        .addNode("acceptEvent")
                        .addNode(userId)
                        .addNode(eventId)
                        .build();

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("token", token);
                    String userToken = jsonObject.toString();
                    requestManager.put(path, userToken, new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            if (response.isSuccessful()) {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                createJoinEventSuccessMessage(activity);
                                                modifyViewOnJoinEvent(userApplicationInfo);
                                            }
                                        });
                                    }
                                }, 0);
                            } else if (response.code() == HttpStatusConstants.STATUS_HTTP_409 ) {
                                new FeedbackMessageBuilder()
                                        .setTitle("Event Full!")
                                        .setDescription("The event is now full.\nUnable to join event.")
                                        .withActivity(activity)
                                        .buildAsyncNeutralMessage();
                            } else {
                                ResponseErrorHandlerUtils.createErrorMessage(response, operation, "Event", activity);
                            }
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
                        }
                    });
                } catch (JSONException e) {
                    FeedbackMessageBuilder.createParseError(e, operation, getActivity());
                } catch (IOException e) {
                    FeedbackMessageBuilder.createServerConnectionError(e, operation, getActivity());
                }

            }
        });
    }

    private void modifyViewOnJoinEvent(UserApplicationInfo userApplicationInfo) {
        joinButton.setVisibility(View.GONE);
        lyftButton.setVisibility(View.VISIBLE);
        String userName = userApplicationInfo.getProfile().getFullName();
        userChip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, members, false);
        userChip.setText(userName);
        members.addView(userChip);
        event.setCurrentNumPeopleRegistered(event.getCurrentNumPeopleRegistered() + 1);
        String numPeopleInEventString = " (" + event.getCurrentNumPeopleRegistered() + "/" + event.getNumberOfPeopleAllowed() + ")";
        numPeople.setText(numPeopleInEventString);
        event.addMemberToList(userApplicationInfo.getProfile().getId());
    }

    private void createJoinEventSuccessMessage(FragmentActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Event Successfully Joined!")
                .setMessage("Congratulations, you are now a part of " + event.getTitle())
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void initBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: this needs fixing to use the native back button
                if (getArguments().getString("theFrom").equals("search")) {
                    getActivity().onBackPressed();
                } else if (getArguments().getString("theFrom").equals("map")) {
                    //TODO go back to map
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.frame_layout, new HomeFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.frame_layout, new HomeFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });
    }

    private void initMenuOptionsVisibility() {

        if (isEventOwner(userId)) {
            shouldAllowMenuItem(R.id.eventLeave, false);
            shouldAllowMenuItem(R.id.eventEdit, true);
            shouldAllowMenuItem(R.id.eventReport, false);
            shouldAllowMenuItem(R.id.eventDelete, true);

        } else if (isPartOfEvent(userId)){
            shouldAllowMenuItem(R.id.eventLeave, true);
            shouldAllowMenuItem(R.id.eventEdit, false);
            shouldAllowMenuItem(R.id.eventReport, true);
            shouldAllowMenuItem(R.id.eventDelete, false);

        } else {
            shouldAllowMenuItem(R.id.eventLeave, false);
            shouldAllowMenuItem(R.id.eventEdit, false);
            shouldAllowMenuItem(R.id.eventReport, true);
            shouldAllowMenuItem(R.id.eventDelete, false);
        }
    }

    private void shouldAllowMenuItem(int menuItemId, boolean shouldAllow) {
        menu.getMenu().findItem(menuItemId).setEnabled(shouldAllow);
        menu.getMenu().findItem(menuItemId).setVisible(shouldAllow);
    }

    private void initButtonVisibility() {
        if (isPartOfEvent(userId)) {
            joinButton.setVisibility(View.GONE);
            lyftButton.setVisibility(View.VISIBLE);
        } else if (event.getCurrentNumPeopleRegistered() == event.getNumberOfPeopleAllowed()) {
            joinButton.setEnabled(false);
            joinButton.setText("Event full!");
            lyftButton.setVisibility(View.GONE);
        } else {
            joinButton.setVisibility(View.VISIBLE);
            lyftButton.setVisibility(View.GONE);
        }
    }

    private boolean isPartOfEvent(String userId) {
        return event.getMembers().contains(userId);
    }

    private void initElements(View view) {
        title = view.findViewById(R.id.viewEventTitle);
        tags = view.findViewById(R.id.viewEventAddTags);
        description = view.findViewById(R.id.eventViewDescription);
        location = view.findViewById(R.id.eventViewLocation);
        beginDate = view.findViewById(R.id.eventViewBeginDate);
        endDate = view.findViewById(R.id.eventViewEndDate);
        organizer = view.findViewById(R.id.viewEventOrganizers);
        members = view.findViewById(R.id.viewEventMembers);
        numPeople = view.findViewById(R.id.eventViewNumPeople);
        backButton = view.findViewById(R.id.viewEventBackButton);
        joinButton = view.findViewById(R.id.joinEventButton);
        lyftButton = view.findViewById(R.id.lyft_button);
        options = view.findViewById(R.id.eventOptions);
        ban = view.findViewById(R.id.eventBanButton);
    }

    private void initEventDetails(Event event) {
        title.setText(event.getTitle());
        description.setText(event.getDescription());

        for (String tag : event.getStringListOfTags()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, tags, false);
            chip.setText(tag);
            tags.addView(chip);
        }

        location.setText(event.getLocation());

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a");

        beginDate.setText(sdf.format(event.getBeginningDate()));
        endDate.setText(sdf.format(event.getEndDate()));

        Chip ownerChip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, organizer, false);

        FragmentActivity fragmentActivity = getActivity();
        String path = "user/" + event.getEventOwnerId();
        String userToken = token; //((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        RequestManager organizerRequestManager = new RequestManager();
        try {
            organizerRequestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String organizerName = json.getString("name");
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    fragmentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ownerChip.setText(organizerName);
                                        }
                                    });
                                }
                            }, 0);

                        } catch (IOException | JSONException e) {
                            // Do nothing: Just don't load the owner chip
                        }
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    // Do nothing:  don't load the owner chip
                }
            });
        } catch (IOException e) {
            // Do nothing: Just don't load the owner chip
        }

        organizer.addView(ownerChip);

        //todo: consolidate this into one backend call

        for (String memberId : event.getMembers()) {
            RequestManager membersRequestManager = new RequestManager();
            Chip memberChip = (Chip) getLayoutInflater().inflate(R.layout.individual_choice_chip, members, false);
            String memberPath = "user/" + memberId;

            try {
                membersRequestManager.get(memberPath, userToken, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                String memberName = json.getString("name");
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        fragmentActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                memberChip.setText(memberName);
                                                members.addView(memberChip);
                                            }
                                        });
                                    }
                                }, 0);

                            } catch (IOException | JSONException e) {
                                // Do nothing: Just don't load the member chip
                            }
                        }
                        // ELSE: Do nothing: Just don't load the member chip
                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        // Do nothing: Just don't load the member chip
                    }
                });
            } catch (IOException e) {
                // Do nothing: Just don't load the member chip
            }

        }

        String numberOfPeopleAllowed;
        if (event.getNumberOfPeopleAllowed() == Integer.MAX_VALUE) {
            numberOfPeopleAllowed = "unlimited";
        } else {
            numberOfPeopleAllowed = String.valueOf(event.getNumberOfPeopleAllowed());
        }
        String numPeopleInEventString = " (" + event.getCurrentNumPeopleRegistered() + "/" + numberOfPeopleAllowed + ")";
        numPeople.setText(numPeopleInEventString);


    }

    public boolean isEventOwner(String userId){
        String eventOwnerId = event.getEventOwnerId();
        return eventOwnerId.equals(userId);
    }
}