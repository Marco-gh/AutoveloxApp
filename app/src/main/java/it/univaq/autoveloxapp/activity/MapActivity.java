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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.univaq.autoveloxapp.DBroom.DB;
import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;
import it.univaq.autoveloxapp.utility.LocationHelper;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    public static final String KEY_EXTRA = "extra";
    public static final String ACTION_ALL = "all";
    private TextView textView_map1, textView_map2, textView_map3;
    private Button button_activity_map;
    private Autovelox autovelox = new Autovelox();
    private GoogleMap googleMap;
    private Marker myMarker = null;
    private LocationHelper locationHelper = LocationHelper.getInstance();
    private List<Autovelox> data = new ArrayList<>();
    private boolean show_all = false;
    private Autovelox nearest_autovelox = null;
    private boolean show_nearest_autovelox = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        textView_map1 = findViewById(R.id.textView_map1);
        textView_map2 = findViewById(R.id.textView_map2);
        textView_map3 = findViewById(R.id.textView_map3);
        button_activity_map = findViewById(R.id.button_activity_map);

        Intent intent = getIntent();
        if(Objects.equals(intent.getStringExtra(MapActivity.KEY_EXTRA), ACTION_ALL)){
            show_all = true;
        }

        if(!show_all && intent.getStringExtra(MapActivity.KEY_EXTRA)!=null) {
            String autoveloxString = intent.getStringExtra(MapActivity.KEY_EXTRA);
            try {
                this.autovelox = Autovelox.parseAutovelox(autoveloxString);
                textView_map1.setText(autovelox.getMunicipality());
                textView_map2.setText(autovelox.getRegion());
                textView_map3.setText(autovelox.getInsertion_date_time());
                button_activity_map.setText(R.string.detail_autovelox);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            button_activity_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.KEY_EXTRA_DETAIL, autovelox.toString());
                    startActivity(intent);
                }
            });
        }
        else if(show_all){
            Thread thread_data = new Thread(new Runnable() {
                @Override
                public void run() {
                    data.addAll(DB.getInstance(getApplicationContext()).getAutoveloxDao().findAll());
                }
            });
            thread_data.start();
            try {
                thread_data.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            textView_map1.setText(getResources().getString(R.string.users_latitude)+": -");
            textView_map2.setText(getResources().getString(R.string.users_longitude)+": -");
            textView_map3.setText(getResources().getString(R.string.users_velocity)+": -");
            button_activity_map.setText(R.string.nearest_autovelox);
            button_activity_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(nearest_autovelox != null){
                        show_nearest_autovelox = !show_nearest_autovelox;
                        if(!show_nearest_autovelox){
                            //+-Rome
                            LatLng latLng = new LatLng(42.5, 12.5);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.5f));
                            button_activity_map.setText(R.string.nearest_autovelox);
                        }
                        else{
                            button_activity_map.setText(R.string.view_all_autovelox);
                        }
                    }
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if(!show_all){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(String.valueOf(autovelox.getMap_identifier()));
            LatLng latLng = new LatLng(autovelox.getLatitude(), autovelox.getLongitude());
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6f));
        }
        else if(show_all){
            for(int i = 0; i < data.size(); i++){
                MarkerOptions options = new MarkerOptions();
                options.title(data.get(i).getMap_identifier().toString());
                options.position(new LatLng(data.get(i).getLatitude(), data.get(i).getLongitude()));
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                myMarker = googleMap.addMarker(options);
            }
            //+-Rome
            LatLng latLng = new LatLng(42.5, 12.5);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5.5f));
        }

        int resultPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if(resultPermission == PackageManager.PERMISSION_GRANTED){
            locationHelper.start(getApplicationContext(), this);
        }
        else {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(googleMap != null) {
            MarkerOptions options = new MarkerOptions();
            options.title(getString(R.string.my_position));
            options.position(new LatLng(location.getLatitude(), location.getLongitude()));
            if(myMarker == null) {
                myMarker = googleMap.addMarker(options);
                myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
            else {
                myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                if(show_all){
                    BigDecimal lat = BigDecimal.valueOf(location.getLatitude()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal lon = BigDecimal.valueOf(location.getLongitude()).setScale(2,  RoundingMode.HALF_UP);

                    textView_map1.setText(getResources().getString(R.string.users_latitude)+": "+lat);
                    textView_map2.setText(getResources().getString(R.string.users_longitude)+": "+lon);
                    textView_map3.setText(getResources().getString(R.string.users_velocity)+": "+location.getSpeed());
                    nearest_autovelox = nearestAutovelox(location);
                    if(show_nearest_autovelox == true){
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.title(String.valueOf(nearest_autovelox.getMap_identifier()));
                        LatLng latLng = new LatLng(nearest_autovelox.getLatitude(), nearest_autovelox.getLongitude());
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.5f));
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        locationHelper.stop(getApplicationContext(), this);
        show_nearest_autovelox = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.start(getApplicationContext(), this);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_user_position), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Autovelox nearestAutovelox(@NonNull Location userLocation){
        float distance = 9999999999F;
        Autovelox returned_autovelox = new Autovelox();

        for(int i = 0; i < data.size(); i++){
            Autovelox autovelox = data.get(i);
            Double lat_autovelox = autovelox.getLatitude();
            Double lon_autovelox = autovelox.getLongitude();

            if(lat_autovelox!=null && lon_autovelox!=null){
                Location location = new Location("Posizione autovelox di DATA");
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
