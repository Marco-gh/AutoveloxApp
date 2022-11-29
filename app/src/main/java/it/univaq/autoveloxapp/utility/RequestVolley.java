package it.univaq.autoveloxapp.utility;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class RequestVolley {

    //Coda dove mettere le richieste
    private RequestQueue queue;

    //Istanza della classe astratta (in singleton)
    private volatile static RequestVolley instance = null;

    //Unico metodo di accesso all'istanza, nel caso chiama il costruttore privato
    public synchronized static RequestVolley getInstance(Context context) {
        if(instance == null) {
            synchronized (RequestVolley.class) {
                if(instance == null) instance = new RequestVolley(context);
            }
        }
        return instance;
    }

    private RequestVolley(Context context){
        //è il tipo di coda più semplice
        queue = Volley.newRequestQueue(context);
    }

    public void doGetRequest(String urlAddress, OnCompleteCallback callback) {
        StringRequest request = new StringRequest(urlAddress, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callback != null){
                    callback.onCompleted(response);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(callback != null){
                    callback.onCompleted(null);
                }
            }
        });
        queue.add(request);
    }

    public interface OnCompleteCallback {
        void onCompleted(String response);
    }
}
