package it.univaq.autoveloxapp.DBroom;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.univaq.autoveloxapp.model.Autovelox;

@Dao
public interface AutoveloxDAO {
    @Insert (onConflict = OnConflictStrategy.REPLACE, entity = Autovelox.class)
    void insert(Autovelox... autovelox);

    @Insert (onConflict = OnConflictStrategy.REPLACE, entity = Autovelox.class)
    void insert(List<Autovelox> autoveloxList);

    @Query("SELECT * FROM autovelox WHERE map_identifier = :map_identifier" )
    Autovelox find(int map_identifier);

    @Query("SELECT * FROM autovelox ORDER BY municipality")
    List<Autovelox> findAll();
}
