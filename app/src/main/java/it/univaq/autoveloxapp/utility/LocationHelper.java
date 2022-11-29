package it.univaq.autoveloxapp.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    private static volatile LocationHelper instance = null;

    private LocationHelper(){}

    public static synchronized LocationHelper getInstance() {
        if(instance == null) {
            synchronized (LocationHelper.class) {
                if(instance == null) instance = new LocationHelper();
            }
        }
        return instance;
    }

    public void start(Context context, LocationCallback callback, Looper looper) {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(0L);

            //The main entry point for interacting with the fused location provider
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            client.requestLocationUpdates(request, callback, looper);
        }
    }

    public void stop(Context context, LocationCallback callback) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        client.removeLocationUpdates(callback);
    }
}


