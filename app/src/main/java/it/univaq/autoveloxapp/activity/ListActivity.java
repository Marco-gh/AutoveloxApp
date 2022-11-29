package it.univaq.autoveloxapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.autoveloxapp.DBroom.DB;
import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;
import it.univaq.autoveloxapp.utility.AdapterMain;
import it.univaq.autoveloxapp.utility.RequestVolley;
import it.univaq.autoveloxapp.utility.services.LocationService;

public class ListActivity extends AppCompatActivity {
    private List<Autovelox> data = new ArrayList<>();
    private RecyclerView recyclerView;
    private AdapterMain autoveloxAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Button button_start = findViewById(R.id.button_nearest_autovelox);
        this.autoveloxAdapter = new AdapterMain(this.data);
        this.recyclerView = findViewById(R.id.recycler_view);
        this.recyclerView.setAdapter(this.autoveloxAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if(isNetworkAvailable()){
            RequestVolley.getInstance(getApplicationContext()).doGetRequest("http://www.datiopen.it/export/json/Mappa-degli-autovelox-in-italia.json", new RequestVolley.OnCompleteCallback() {
                @Override
                public void onCompleted(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //get lancia l'eccezione mentre opt dà errore
                            JSONObject jsonObject = jsonArray.optJSONObject(i);
                            if (jsonObject != null) {
                                Autovelox autovelox = null;
                                autovelox = Autovelox.parseAutovelox(jsonObject.toString());
                                data.add(autovelox);
                            }
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DB.getInstance(getApplicationContext()).getAutoveloxDao().insert(data);
                            }
                        }).start();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                autoveloxAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }else{
            loadData();
        }

        autoveloxAdapter.setOnAutoveloxAdapterListener(new AdapterMain.OnAutoveloxAdapterListener() {
            @Override
            public void OnOpenAutovelox(Autovelox autovelox) {
                if(LocationService.getUser_location()!=null) {
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    intent.putExtra(MapActivity.KEY_EXTRA, autovelox.toString());
                    startActivity(intent);
                }
                else{
                    startLocalization();
                }
            }
        });

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LocationService.getUser_location()!=null){
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    intent.putExtra(MapActivity.KEY_EXTRA, nearestAutovelox().toString());
                    startActivity(intent);
                    //System.out.println("TI STAMPO IL DB"+DB.getInstance(getApplicationContext()).getAutoveloxDao().findAll().toString());
                }
                else{
                    startLocalization();
                }
            }
        });
    }

    private void startLocalization() {
        int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.putExtra(LocationService.KEY_ACTION, LocationService.ACTION_START);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }
    }

    private void stopLocalization() {
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        intent.putExtra(LocationService.KEY_ACTION, LocationService.ACTION_STOP);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Permessi per prendere l'app in foreground e background
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocalization();
            } else {
                //da gestire la richiesta di permessi negata
            }
        }
    }

    private void loadData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                data.addAll(DB.getInstance(getApplicationContext()).getAutoveloxDao().findAll());
                //se continua a essere vuoto il DB
                if(data.size()==0){
                    Toast toast = new Toast(getApplicationContext());
                    toast.setText(R.string.Toast_no_data);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                }
                autoveloxAdapter.notifyDataSetChanged();
            }
        }).start();
    }

    public boolean isNetworkAvailable() {
        Context context = getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Giusto usare questo metodo?
        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public Autovelox nearestAutovelox(){
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/
        float distance = 9999999999F;
        Autovelox returned_autovelox = new Autovelox();
        for(int i = 0; i < this.data.size(); i++){
            Autovelox autovelox = data.get(i);
            Double lat_autovelox = autovelox.getLatitude();
            Double lon_autovelox = autovelox.getLongitude();

            if(lat_autovelox!=null && lon_autovelox!=null){
                Location location = new Location("Posizione autovelox");
                location.setLatitude(lat_autovelox);
                location.setLongitude(lon_autovelox);
                float distanceTo = LocationService.getUser_location().distanceTo(location);
                if(distanceTo < distance){
                    distance = distanceTo;
                    returned_autovelox = autovelox;
                }
            }
        }
        return returned_autovelox;
    }
}
