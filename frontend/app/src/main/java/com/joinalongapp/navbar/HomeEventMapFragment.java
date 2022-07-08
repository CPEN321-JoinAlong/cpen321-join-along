package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joinalongapp.joinalong.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeEventMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeEventMapFragment extends Fragment {
    private MapView mapView;
    private GoogleMap googleMaps;

    public HomeEventMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeEventMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeEventMapFragment newInstance(String param1, String param2) {
        HomeEventMapFragment fragment = new HomeEventMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_event_map, container, false);

        mapView = view.findViewById(R.id.eventMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                //TODO: implement map search
                //TODO: might need map API key

                googleMaps = googleMap;

                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                googleMaps.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Marker in Sydney"));
                googleMaps.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        });

        sendViewToBack(view);

        return view;
    }

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }
}