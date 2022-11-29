package it.univaq.autoveloxapp.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

public class LocationHelper {

    public void start(Context context, LocationListener listener) {
        //Il risultato del controllo del permesso è un intero
        int resultPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if(resultPermission == PackageManager.PERMISSION_GRANTED) {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }
    }

    public void stop(Context context, LocationListener listener) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        manager.removeUpdates(listener);
    }
}


