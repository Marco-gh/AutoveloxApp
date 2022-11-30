package it.univaq.autoveloxapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;

public class DetailActivity extends AppCompatActivity {
    public static final String KEY_EXTRA_DETAIL = "extra_to_detail";
    private TextView textView_details_municipality, textView_details_region, textView_details_provence,
            textView_details_name, textView_details_insertion_year, textView_details_insertion_date_time,
            textView_details_identifier, textView_details_longit, textView_details_latit;
    private Autovelox autovelox = new Autovelox();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textView_details_municipality = findViewById(R.id.textView_detail_municipality);
        textView_details_region = findViewById(R.id.textView_detail_region);
        textView_details_provence = findViewById(R.id.textView_detail_provence);
        textView_details_name = findViewById(R.id.textView_detail_name);
        textView_details_insertion_year = findViewById(R.id.textView_detail_insertion_year);
        textView_details_insertion_date_time = findViewById(R.id.textView_detail_insertion_date_time);
        textView_details_identifier = findViewById(R.id.textView_detail_identifier);
        textView_details_longit = findViewById(R.id.textView_detail_longit);
        textView_details_latit = findViewById(R.id.textView_detail_latit);

        setupTextView();
    }

    public void setupTextView(){
        Intent intent = getIntent();
        System.out.println("LA SEDEEEEEEEEEEEEEE "+intent.getStringExtra(KEY_EXTRA_DETAIL));
        if(intent.getStringExtra(KEY_EXTRA_DETAIL)!=null){
            try {
                autovelox = Autovelox.parseAutovelox(intent.getStringExtra(KEY_EXTRA_DETAIL));

                textView_details_municipality.setText(getString(R.string.municipality)+": "+autovelox.getMunicipality());
                textView_details_region.setText(getString(R.string.region)+": "+autovelox.getRegion());
                textView_details_provence.setText(getString(R.string.provence)+": "+autovelox.getProvence());
                textView_details_name.setText(getString(R.string.name)+": "+autovelox.getName());
                textView_details_insertion_year.setText(getString(R.string.insertion_year)+": "+autovelox.getInsertion_year());
                textView_details_insertion_date_time.setText(getString(R.string.insertion_date_time)+": "+autovelox.getInsertion_date_time());
                textView_details_identifier.setText(getString(R.string.map_identifier)+": "+autovelox.getMap_identifier());
                textView_details_longit.setText(getString(R.string.longitude)+": "+autovelox.getLongitude());
                textView_details_latit.setText(getString(R.string.latitude)+": "+autovelox.getLatitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
