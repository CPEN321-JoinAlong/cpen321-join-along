package com.joinalongapp.maputils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.joinalongapp.joinalong.R;


//https://medium.com/@tonyshkurenko/work-with-clustermanager-bdf3d70fb0fd
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final LayoutInflater inflater;

    public MapInfoWindowAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        final View popup = inflater.inflate(R.layout.map_info_window_layout, null);

        ((TextView) popup.findViewById(R.id.mapInfoWindowEventName)).setText(marker.getTitle());
        ((TextView) popup.findViewById(R.id.mapInfoWindowDescription)).setText(marker.getSnippet());

        return popup;
    }

    @Override
    public View getInfoContents(Marker marker) {
        final View popup = inflater.inflate(R.layout.map_info_window_layout, null);

        ((TextView) popup.findViewById(R.id.mapInfoWindowEventName)).setText(marker.getTitle());
        ((TextView) popup.findViewById(R.id.mapInfoWindowDescription)).setText(marker.getSnippet());

        return popup;
    }

}
