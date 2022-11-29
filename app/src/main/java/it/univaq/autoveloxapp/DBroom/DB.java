package it.univaq.autoveloxapp.DBroom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.univaq.autoveloxapp.model.Autovelox;

/**
 * Classe che eredita da RoomDatabase e si occupa di definire il DB tramite architettura singleton.
 * Non è un singleton puro perché manca il costruttore che dovrebbe essere private o protected ->
 * questo perché non siamo noi a istanziarci direttamente la classe.
 * Per roomDatabase serve anche un DAO per i metodi da implementare.
 */
@Database( entities = {Autovelox.class}, version = 1)
public abstract class DB extends RoomDatabase {

    public abstract AutoveloxDAO getAutoveloxDao();

    //volatile -> la variabile viene memorizzata in RAM no in cache. Quando diversi thread vi devono
    //accedere.
    private static volatile DB instance = null;

    //synchronized -> impone un semaforo sulla variabile affinché solo un thread la usi. Sincronizza
    //quindi gli accessi.
    public static synchronized DB getInstance(Context context) {

        //if solo per la prima istanziazione del DB
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
