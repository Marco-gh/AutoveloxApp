package it.univaq.autoveloxapp.utility.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import it.univaq.autoveloxapp.utility.LocationHelper;

public class LocationService extends Service {
    public static final String KEY_ACTION = "action";

    //Meglio mettere queste variabili come statiche e pubblica
    public static final int ACTION_START = 1;
    public static final int ACTION_STOP = 0;

    public static Location user_location = null;
    public static Location getUser_location() {
        return user_location;
    }
    public static void setUser_location(Location user_location) {
        LocationService.user_location = user_location;
    }

    private LocationHandler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Per la notifica
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channelLocation", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManagerCompat.from(getApplicationContext()).createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channelLocation");
        builder.setContentTitle("My Location Service");
        startForeground(1, builder.build());

        //Nuovo thread
        HandlerThread thread = new HandlerThread("Location thread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        //Gli lego il suo handler con cui posso comunicare, ora posso fare azioni su un altro thread
        handler = new LocationHandler(thread.getLooper());

        Toast toast = new Toast(getApplicationContext());
        toast.setText("Avvio localizzazione");
        toast.show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = new Message();
        message.setData(intent.getExtras());
        handler.sendMessage(message);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Solo se distrutto il servizio, di solito viene chiuso con onStart e il valore di chiusura
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("ON DESTROY SERVICE");
    }

    /**
     * Classe privata che svolge le azioni su un altro thread
     */
    private class LocationHandler extends Handler {

        private LocationCallback callback= new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LocationService.setUser_location(locationResult.getLastLocation());
            }
        };

        //Looper -> ci permette di mantenere in vita i thread sul quale l'handler andrà a comunicare
        //Permette di collegare il thread al ciclo di vita di un altro thread e perette all'altro di
        //rimanere in ascolto di comandi
        public LocationHandler(Looper looper) {
            super(looper);
        }

        /**
         * Funzione che intercetta il messaggio e svolge le azioni
         * @param msg l'handler comunica mediante oggetti di tipo message
         */
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            if(data != null) {
                int action = data.getInt(KEY_ACTION);
                switch (action) {
                    case ACTION_STOP: {
                        LocationHelper.getInstance().stop(getApplicationContext(), callback);
                        stopSelf();
                        break;
                    }

                    case ACTION_START: {
                        LocationHelper.getInstance().start(getApplicationContext(), callback, Looper.myLooper());
                        break;
                    }
                }
            }
        }
    }
}
