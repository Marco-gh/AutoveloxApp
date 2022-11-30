package it.univaq.autoveloxapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.univaq.autoveloxapp.DBroom.DB;
import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;
import it.univaq.autoveloxapp.utility.AdapterMain;
import it.univaq.autoveloxapp.utility.RequestVolley;

public class ListActivity extends AppCompatActivity {
    private List<Autovelox> data = new ArrayList<>();
    private RecyclerView recyclerView;
    private AdapterMain autoveloxAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Button button_view_all_autovelox = findViewById(R.id.button_view_all_autovelox);
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
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra(MapActivity.KEY_EXTRA, autovelox.toString());
                startActivity(intent);
            }
        });

        button_view_all_autovelox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra(MapActivity.KEY_EXTRA, MapActivity.ACTION_ALL);

                startActivity(intent);
            }
        });
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
}
