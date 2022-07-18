package com.joinalongapp.maputils;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.joinalongapp.viewmodel.Event;

public class MapClusterItem implements ClusterItem {
    private final LatLng position;
    private final Event event;

    public MapClusterItem(double lat, double lng, Event event) {
        position = new LatLng(lat, lng);
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return event.getTitle();
    }

    @Override
    public String getSnippet() {
        return event.getDescription();
    }
}


