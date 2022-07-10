package com.joinalongapp;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.joinalongapp.joinalong.R;


//https://medium.com/@tonyshkurenko/work-with-clustermanager-bdf3d70fb0fd
public class CustomInfoViewAdapter implements GoogleMap.InfoWindowAdapter {
    private final LayoutInflater inflater;

    public CustomInfoViewAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        final View popup = inflater.inflate(R.layout.map_info_window_layout, null);
        TextView tv = popup.findViewById(R.id.map_info_window);
        tv.setText(marker.getSnippet());
        return popup;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        final View popup = inflater.inflate(R.layout.map_info_window_layout, null);
        TextView tv = popup.findViewById(R.id.map_info_window);
        tv.setText(marker.getSnippet());
        return popup;
    }
}
