package com.joinalongapp.navbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.adapter.EventViewAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandlerUtils;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.SearchScreenActivity;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.Event;
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
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private TabLayout eventViewTabs;
    private ViewPager2 eventViewPager;

    private Spinner eventFilterSpinner;
    private List<String> eventFilterList = new ArrayList<>();
    private static EventViewAdapter viewStateAdapter;
    private SharedPreferences prefForLastFilter;
    private SharedPreferences.Editor editor;
    private SharedPreferences.Editor editorForLastFilter;

    private ImageButton homepageSearchBar;
    private ImageButton homepageDarkButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment2.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.dark_mode_prefs), Context.MODE_PRIVATE);
        prefForLastFilter = getActivity().getSharedPreferences(getString(R.string.selected_filter_on_home), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editorForLastFilter = prefForLastFilter.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("HomeFragmentUIComponents");
        myTrace.start();
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initElements(rootView);
        initSpinner();
        initDarkMode();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        viewStateAdapter = new EventViewAdapter(fragmentManager, getLifecycle());
        eventViewPager.setAdapter(viewStateAdapter);

        //without this, moving map is seen as swipe between tabs
        eventViewPager.setUserInputEnabled(false);

        initViewTabs();
        initFilters();
        initSearchbar();
        myTrace.stop();
        return rootView;
    }

    private void initSearchbar() {
        homepageSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchScreenActivity.class);
                i.putExtra("mode", SearchScreenActivity.SearchMode.EVENT_MODE);
                startActivity(i);
            }
        });
    }

    private void initFilters() {
        String lastPref = prefForLastFilter.getString(getString(R.string.selected_filter_on_home), null);

        if (lastPref != null) {
            eventFilterSpinner.setSelection(eventFilterList.indexOf(lastPref));
        }

        eventFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = eventFilterList.get(position);

                int curr = eventViewPager.getCurrentItem();
                getEventsWithFilter(selectedFilter, curr);
                viewStateAdapter.setFilter(selectedFilter);

                editorForLastFilter.putString(getString(R.string.selected_filter_on_home), selectedFilter).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // default is already taken care of
                // it always takes the first item in the list
            }
        });
    }

    private void initViewTabs() {
        eventViewTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                eventViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //DO NOTHING
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //DO NOTHING
            }
        });

        eventViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                eventViewTabs.selectTab(eventViewTabs.getTabAt(position));
            }
        });
    }

    private void initElements(View view) {
        eventViewPager = view.findViewById(R.id.eventViewSelectViewPager);
        eventViewTabs = view.findViewById(R.id.homeEventDisplayTabLayout);
        eventFilterSpinner = view.findViewById(R.id.homepageEventsFilter);
        homepageSearchBar = view.findViewById(R.id.homeSearchButton);
        homepageDarkButton = view.findViewById(R.id.darkModeButton);
    }

    private void initSpinner() {
        eventFilterList.add("All Events");
        eventFilterList.add("Recommended");
        eventFilterList.add("Joined Events");

        //TODO: change this to get user interests from global
        UserProfile userProfile = ((UserApplicationInfo) getActivity().getApplication()).getProfile();
        List<String> userInterests = userProfile.getStringListOfTags();

        eventFilterList.addAll(userInterests);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, eventFilterList);
        eventFilterSpinner.setAdapter(adapter);
    }

    private void getEventsWithFilter(String filter, int curr) {
        String userId = ((UserApplicationInfo) getActivity().getApplication()).getProfile().getId();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String path;

        switch (filter) {
            case "Recommended":
                path = new PathBuilder().addUser().addNode(userId).addNode("recommendedEvents").build();
                break;
            case "Joined Events":
                path = new PathBuilder().addUser().addNode(userId).addEvent().build();
                break;
            case "All Events":
                path = new PathBuilder().addEvent().build();
                break;
            default:
                path = new PathBuilder().addEvent().addNode("tag").addNode(filter).build();
                break;
        }

        FragmentActivity fragmentActivity = getActivity();

        RequestManager requestManager = new RequestManager();
        String operation = "Load Events";

        try {
            requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {

                    if (response.isSuccessful()) {
                        try {
                            List<Event> eventList = getEventListFromResponse(response);
                            updateEventLists(eventList, fragmentActivity, curr);
                        } catch (IOException | JSONException e) {
                            FeedbackMessageBuilder.createParseError(e, operation, fragmentActivity);
                            //TODO: add no events found message
                        }
                    } else if (response.code() == HttpStatusConstants.STATUS_HTTP_404) {
                        List<Event> eventList = new ArrayList<>();
                        updateEventLists(eventList, fragmentActivity, curr);
                    } else {
                        ResponseErrorHandlerUtils.createErrorMessage(response, operation, "Event", fragmentActivity);
                        //TODO: add no events found message
                    }

                }
                @Override
                public void onError(Call call, IOException e) {
                    FeedbackMessageBuilder.createServerConnectionError(e, operation, fragmentActivity);
                    //TODO: add no events found message
                }
            });

        } catch (IOException e) {
            FeedbackMessageBuilder.createServerConnectionError(e, operation, fragmentActivity);
            //TODO: add no events found message
        }
    }

    @NonNull
    private List<Event> getEventListFromResponse(Response response) throws JSONException, IOException {
        List<Event> eventList = new ArrayList<>();
        if (response.code() == HttpStatusConstants.STATUS_HTTP_200) {
            JSONArray jsonEvents = new JSONArray(response.body().string());
            for (int i = 0; i < jsonEvents.length(); i++) {
                Event event = new Event();
                event.populateDetailsFromJson(jsonEvents.getString(i));
                eventList.add(event);
            }
        }
        return eventList;
    }

    private void updateEventLists(List<Event> eventList, FragmentActivity fragmentActivity, int curr) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                fragmentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewStateAdapter.setEventList(eventList);
                        eventViewPager.setAdapter(viewStateAdapter);
                        eventViewTabs.selectTab(eventViewTabs.getTabAt(curr));
                        eventViewPager.setCurrentItem(curr);
                    }
                });
            }
        }, 0);
    }

    private void initDarkMode(){
        boolean darkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;

        homepageDarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(darkMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean(getString(R.string.dark_mode_prefs), false);

                }
                else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean(getString(R.string.dark_mode_prefs), true);

                }

                editor.apply();
            }
        });

    }

}