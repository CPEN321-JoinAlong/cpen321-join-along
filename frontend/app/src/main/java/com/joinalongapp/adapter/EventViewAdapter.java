package com.joinalongapp.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.joinalongapp.navbar.HomeEventListFragment;
import com.joinalongapp.navbar.HomeEventMapFragment;
import com.joinalongapp.viewmodel.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventViewAdapter extends FragmentStateAdapter {
    private static final int LIST_VIEW_TAB = 0;
    private static final int MAP_VIEW_TAB = 1;
    private static final int NUM_TABS = 2;

    //TODO change this default later
    String filter = "Recommended";

    List<Event> eventList = new ArrayList<>();

    public EventViewAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void setEventList(List<Event> eventList) {
        this.eventList.clear();
        this.eventList.addAll(eventList);
        notifyDataSetChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case MAP_VIEW_TAB:
                System.out.println("redraw map");
                Bundle bundle = new Bundle();
                bundle.putSerializable("eventsList", (Serializable) eventList);
                bundle.putString("filter", filter);
                HomeEventMapFragment homeEventMapFragment = new HomeEventMapFragment();
                homeEventMapFragment.setArguments(bundle);
                return homeEventMapFragment;
            case LIST_VIEW_TAB:
                System.out.println("redraw list");
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("eventsList", (Serializable) eventList);
                bundle2.putString("filter", filter);
                HomeEventListFragment homeEventListFragment = new HomeEventListFragment();
                homeEventListFragment.setArguments(bundle2);
                return homeEventListFragment;
            default:
                throw new IllegalStateException("Unknown tab!");
        }

    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
