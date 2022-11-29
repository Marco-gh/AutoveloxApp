package it.univaq.autoveloxapp.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;
import it.univaq.autoveloxapp.utility.services.LocationService;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    public static final String KEY_EXTRA = "extra";
    private TextView textView_map_municipality, textView_map_region, textView_map_date_time;
    private Button button_to_detail;
    private Autovelox autovelox = new Autovelox();
    private GoogleMap googleMap;
    private Marker myMarker = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        button_to_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textView_map_municipality.setText(LocationService.getUser_location().toString());
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(String.valueOf(autovelox.getMap_identifier()));
        LatLng latLng = new LatLng(autovelox.getLatitude(), autovelox.getLongitude());
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
        if(LocationService.getUser_location()!=null){
            userMarkerOnMap(LocationService.getUser_location());
        }
    }

    public void userMarkerOnMap(@NonNull Location location){
        if(googleMap != null) {
            //A seconda che ho già un marker o meno
            if(myMarker == null && LocationService.getUser_location()!=null) {
                MarkerOptions options = new MarkerOptions();
                options.title(String.valueOf(R.string.my_position));
                LatLng latLng = new LatLng(LocationService.getUser_location().getLatitude(),LocationService.getUser_location().getLongitude());
                options.position(latLng);
                myMarker = googleMap.addMarker(options);
            }
            else {
                if (myMarker != null) {
                    myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println("LA SEDE: POSIZIONE CAMBIATA"+location.toString());
        userMarkerOnMap(location);
    }
}
