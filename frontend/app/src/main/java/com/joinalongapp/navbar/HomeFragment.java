package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.joinalong.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final int LIST_VIEW_TAB = 0;
    private static final int MAP_VIEW_TAB = 1;
    private static final int NUM_TABS = 2;

    private TabLayout eventViewTabs;
    private ViewPager2 eventViewPager;

    private Spinner eventFilterSpinner;
    private List<String> eventFilterList = new ArrayList<>();

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


        //TODO: refactor and implement search
        //TODO: remove the viewGroup because map moving is impossible with it there
        //      it just keeps swiping between tabs instead of moving the map!!


        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initElements(rootView);
        initSpinner();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        EventViewAdapter viewStateAdapter = new EventViewAdapter(fragmentManager, getLifecycle());
        eventViewPager.setAdapter(viewStateAdapter);
        eventViewPager.setUserInputEnabled(false);

        eventViewTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                eventViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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

                //TODO: this should be a search for events with given filter instead
                //      it should then update the event list, and recall setEventCards
                Toast.makeText(getActivity(), selectedFilter, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String selectedFilter = eventFilterList.get(0);

                //TODO: this should be a search for events with given filter instead
                //      it should then update the event list, and recall setEventCards
                Toast.makeText(getActivity(), selectedFilter, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void initElements(View view) {
        eventViewPager = view.findViewById(R.id.eventViewSelectViewPager);
        eventViewTabs = view.findViewById(R.id.homeEventDisplayTabLayout);
        eventFilterSpinner = view.findViewById(R.id.homepageEventsFilter);
    }

    private void initSpinner() {
        eventFilterList.add("Recommended");
        eventFilterList.add("My Events");

        //TODO: change this to a post request for user interests?
        //      either that or a user was passed in through bundle/arguments
        List<String> userInterests = new ArrayList<>();

        eventFilterList.addAll(userInterests);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, eventFilterList);
        eventFilterSpinner.setAdapter(adapter);
    }

    private static class EventViewAdapter extends FragmentStateAdapter {

        public EventViewAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == MAP_VIEW_TAB){
                return new HomeEventMapFragment();
            } else {
                return new HomeEventListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_TABS;
        }
    }
}