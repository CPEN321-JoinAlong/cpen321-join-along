package com.joinalongapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

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

    public static String standardizeAddress(String addressString, Context applicationContext) {
        Address address = getAddressFromString(addressString, applicationContext);
        return address == null ? null : address.getAddressLine(0);
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
