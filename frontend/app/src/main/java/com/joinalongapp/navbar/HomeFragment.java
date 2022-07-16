package com.joinalongapp.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.Constants;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.SearchScreenActivity;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.EventList;

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
    private static final int LIST_VIEW_TAB = 0;
    private static final int MAP_VIEW_TAB = 1;
    private static final int NUM_TABS = 2;
    private static final String TAG = "HomeFragment";

    private TabLayout eventViewTabs;
    private ViewPager2 eventViewPager;

    private Spinner eventFilterSpinner;
    private List<String> eventFilterList = new ArrayList<>();
    private static EventViewAdapter viewStateAdapter;

    private ImageButton homepageSearchBar;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //TODO: refactor


        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initElements(rootView);
        initSpinner();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        viewStateAdapter = new EventViewAdapter(fragmentManager, getLifecycle());
        eventViewPager.setAdapter(viewStateAdapter);

        //without this, moving map is seen as swipe between tabs
        eventViewPager.setUserInputEnabled(false);

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

        eventFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = eventFilterList.get(position);

                int curr = eventViewPager.getCurrentItem();
                getEventsForFilter(selectedFilter, curr);
                viewStateAdapter.setFilter(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //default is already taken care of
            }
        });

        homepageSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchScreenActivity.class);
                i.putExtra("mode", SearchScreenActivity.SearchMode.EVENT_MODE);
                startActivity(i);
            }
        });

        return rootView;
    }

    private void initElements(View view) {
        eventViewPager = view.findViewById(R.id.eventViewSelectViewPager);
        eventViewTabs = view.findViewById(R.id.homeEventDisplayTabLayout);
        eventFilterSpinner = view.findViewById(R.id.homepageEventsFilter);
        homepageSearchBar = view.findViewById(R.id.homeSearchButton);
    }

    private void initSpinner() {
        eventFilterList.add("Recommended");
        eventFilterList.add("My Events");

        //TODO: change this to get user interests from global
        List<String> userInterests = new ArrayList<>();

        eventFilterList.addAll(userInterests);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, eventFilterList);
        eventFilterSpinner.setAdapter(adapter);
    }

    private void getEventsForFilter(String filter, int curr) {
        String userId = ((UserApplicationInfo) getActivity().getApplication()).getProfile().getId();
        String userToken = ((UserApplicationInfo) getActivity().getApplication()).getUserToken();
        String path;

        switch (filter) {
            case "My Events":
                path = "user/" + userId + "/event";
                break;
            case "Recommended":
            default:
                path = "event";
                break;
        }

        FragmentActivity fragmentActivity = getActivity();

        RequestManager requestManager = new RequestManager();

        try {
            requestManager.get(path, userToken, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    try {
                        List<Event> eventList = new ArrayList<>();
                        if (response.code() == Constants.STATUS_HTTP_200) {
                            JSONArray jsonEvents = new JSONArray(response.body().string());
                            for (int i = 0; i < jsonEvents.length(); i++) {
                                Event event = new Event();
                                event.populateDetailsFromJson(jsonEvents.getString(i));
                                eventList.add(event);
                            }
                        }

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

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Unable to parse events from server.");
                    }
                }
                @Override
                public void onError(Call call, IOException e) {
                    Log.e(TAG, "Unable to get events from server.");
                }

            });

        } catch (IOException e) {
            Log.e(TAG, "Unable to get events from server.");
        }
    }

    private static class EventViewAdapter extends FragmentStateAdapter {
        //TODO change this default later
        String filter = "Recommended";

        EventList eventList = new EventList();

        public EventViewAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        public void setEventList(List<Event> eventList) {
            this.eventList.eventList.clear();
            this.eventList.eventList.addAll(eventList);
            notifyDataSetChanged();
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == MAP_VIEW_TAB){
                System.out.println("redraw map");
                Bundle bundle = new Bundle();
                bundle.putSerializable("eventsList", eventList);
                bundle.putString("filter", filter);
                HomeEventMapFragment fragment = new HomeEventMapFragment();
                fragment.setArguments(bundle);
                return fragment;
            } else {
                //TODO add event list udpate
                System.out.println("redraw list");
                Bundle bundle = new Bundle();
                bundle.putSerializable("eventsList", eventList);
                bundle.putString("filter", filter);
                HomeEventListFragment fragment = new HomeEventListFragment();
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_TABS;
        }
    }
}