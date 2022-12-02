package it.univaq.autoveloxapp.DBroom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.univaq.autoveloxapp.model.Autovelox;

@Database( entities = {Autovelox.class}, version = 1)
public abstract class DB extends RoomDatabase {

    public abstract AutoveloxDAO getAutoveloxDao();

    private static volatile DB instance = null;

    public static synchronized DB getInstance(Context context) {
        if(instance == null) {
            synchronized (DB.class) {
                if(instance == null) {
                    instance = Room.databaseBuilder(context, DB.class,"myRoomDatabase.db").build();
                }
            }
        }
        return instance;
    }

}
