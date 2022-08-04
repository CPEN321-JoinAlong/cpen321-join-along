package com.joinalongapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LocationUtils {
    private static final String TAG = "LocationUtils";

    public static Address getAddressFromString(String address, Context applicationContext) {
        Geocoder geocoder = new Geocoder(applicationContext);
        Address retVal = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                retVal = addresses.get(0);
            }
        } catch(IOException e) {
            Log.e(TAG, "Failed to set location with error: " + e.getMessage());
        }
        return retVal;
    }

    public static String standardizeAddress(Address address) {
        return address == null ? null : address.getAddressLine(0);
    }

    public static LatLng getCoordsFromAddress(Address address) {
        double lat = address.getLatitude();
        double lng = address.getLongitude();

        return new LatLng(lat, lng);
    }

    public static LatLng getLatLngFromString(String coordinates) {
        String[] latLngString = coordinates.split(",");

        double lat = Double.parseDouble(latLngString[0].trim());
        double lng = Double.parseDouble(latLngString[1].trim());

        return new LatLng(lat, lng);
    }

    public static String getLatLngAsString(LatLng coordinates) {
        String lat = String.valueOf(coordinates.latitude);
        String lng = String.valueOf(coordinates.longitude);

        return lat + "," + lng;
    }

    /**
     * Validates that a location exists. A location does not require a valid street address.
     * @param address
     * @return true if the location exists, false otherwise
     */
    public static boolean validateLocation(Address address) {
        return address != null;
    }

    /**
     * Validates that an address exists. An address must have a valid street address number and street name.
     * Eg. Vancouver, BC is not a valid address
     * Eg. 2336 Main Mall, Vancouver is a valid address
     * @param address
     * @return true if the address exists, false otherwise
     */
    public static boolean validateAddress(Address address) {
        if (!validateLocation(address)) {
            return false;
        }

        String addressNumber = address.getSubThoroughfare();
        String streetName = address.getThoroughfare();

        return addressNumber != null && streetName != null;
    }
}
