package it.univaq.autoveloxapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.univaq.autoveloxapp.DBroom.DB;
import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;
import it.univaq.autoveloxapp.utility.LocationHelper;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    public static final String KEY_EXTRA = "extra";
    /**
     * @Value 1 se si vuole visualizzare uno specifico autovelox
     * @Value 2 se si cerca l'autovelox più vicino alla posizione
     */
    public static final String KEY_ACTION = "ACTION";
    private TextView textView_map_municipality, textView_map_region, textView_map_date_time;
    private Button button_to_detail;
    private Autovelox autovelox = new Autovelox();
    private GoogleMap googleMap;
    private Marker myMarker = null;
    private LocationHelper locationHelper = new LocationHelper();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        int resultPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if(resultPermission == PackageManager.PERMISSION_GRANTED){
            locationHelper.start(getApplicationContext(), this);
        }
        else {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }

        textView_map_municipality = findViewById(R.id.textView_map_municipality);
        textView_map_region = findViewById(R.id.textView_map_region);
        textView_map_date_time = findViewById(R.id.textView_map_date_time);
        button_to_detail = findViewById(R.id.button_to_details);

        Intent intent = getIntent();
        if (intent.getStringExtra(MapActivity.KEY_EXTRA) != null) {
            String autoveloxString = intent.getStringExtra(MapActivity.KEY_EXTRA);
            try {
                this.autovelox = Autovelox.parseAutovelox(autoveloxString);
                textView_map_municipality.setText(autovelox.getMunicipality());
                textView_map_region.setText(autovelox.getRegion());
                textView_map_date_time.setText(autovelox.getInsertion_date_time());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        button_to_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //PER ANDARE ALLA SCHERMATA DETTAGLI
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Intent intent = getIntent();
        if(intent.getIntExtra(MapActivity.KEY_ACTION, 1) == 1){
            this.googleMap = googleMap;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(String.valueOf(autovelox.getMap_identifier()));
            LatLng latLng = new LatLng(autovelox.getLatitude(), autovelox.getLongitude());
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Intent intent = getIntent();
        if(googleMap != null) {
            if(myMarker == null) {
                MarkerOptions options = new MarkerOptions();
                options.title("My Location");
                options.position(new LatLng(location.getLatitude(), location.getLongitude()));
                myMarker = googleMap.addMarker(options);
            }
            else {
                myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            //Se si cerca l'autovelox piu vicino
            if(intent.getIntExtra(MapActivity.KEY_ACTION, 1) == 2){
                Autovelox nearestAutovelox = nearestAutovelox(location);
                this.googleMap = googleMap;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(String.valueOf(nearestAutovelox.getMap_identifier()));
                LatLng latLng = new LatLng(nearestAutovelox.getLatitude(), nearestAutovelox.getLongitude());
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        locationHelper.stop(getApplicationContext(), this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.start(getApplicationContext(), this);
            } else {
                Toast.makeText(getApplicationContext(), "No user location enabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Autovelox nearestAutovelox(@NonNull Location userLocation){
        List<Autovelox> data = DB.getInstance(getApplicationContext()).getAutoveloxDao().findAll();
        float distance = 9999999999F;
        Autovelox returned_autovelox = new Autovelox();

        for(int i = 0; i < data.size(); i++){
            Autovelox autovelox = data.get(i);
            Double lat_autovelox = autovelox.getLatitude();
            Double lon_autovelox = autovelox.getLongitude();

            if(lat_autovelox!=null && lon_autovelox!=null){
                Location location = new Location("Posizione autovelox di data");
                location.setLatitude(lat_autovelox);
                location.setLongitude(lon_autovelox);
                float distanceTo = userLocation.distanceTo(location);
                if(distanceTo < distance){
                    distance = distanceTo;
                    returned_autovelox = autovelox;
                }
            }
        }
        return returned_autovelox;
    }
}
