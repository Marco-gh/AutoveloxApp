package it.univaq.autoveloxapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity (tableName = "autovelox")
public class Autovelox {

    //Attributi da JSON
    private String municipality;
    private String provence;
    private String region;
    private String name;
    private Integer insertion_year;
    @ColumnInfo (name = "insertion_DATE_TIME")
    private String insertion_date_time;
    @PrimaryKey
    private Integer map_identifier;
    private Double longitude = null;
    private Double latitude = null;

    public Autovelox(String municipality, String provence, String region, String name,
                     Integer insertion_year, String insertion_date_time, Integer map_identifier,
                     Double longitude, Double latitude) {
        this.municipality = municipality;
        this.provence = provence;
        this.region = region;
        this.name = name;
        this.insertion_year = insertion_year;
        this.insertion_date_time = insertion_date_time;
        this.map_identifier = map_identifier;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Autovelox(){}

    /////////////////METODI GETTER AND SETTER/////////////////////

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getProvence() {
        return provence;
    }

    public void setProvence(String provence) {
        this.provence = provence;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInsertion_year() {
        return insertion_year;
    }

    public void setInsertion_year(Integer insertion_year) {
        this.insertion_year = insertion_year;
    }

    public String getInsertion_date_time() {
        return insertion_date_time;
    }

    public void setInsertion_date_time(String insertion_date_time) {
        this.insertion_date_time = insertion_date_time;
    }

    public Integer getMap_identifier() {
        return map_identifier;
    }

    public void setMap_identifier(Integer map_identifier) {
        this.map_identifier = map_identifier;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    ///////////////METODI PER CONVERTIRE DA E IN JSON////////////////////

    /**
     *
     * @return
     */
    @Override
    public String toString(){
        try {
            JSONObject json = new JSONObject();
            json.put("ccomune", this.municipality);
            json.put("cprovincia", this.provence);
            json.put("cregione", this.region);
            json.put("cnome", this.name);
            json.put("canno_inserimento", this.insertion_year);
            json.put("cdata_e_ora_inserimento", this.insertion_date_time);
            json.put("cidentificatore_in_openstreetmap", this.map_identifier);
            json.put("clongitudine", this.longitude);
            json.put("clatitudine", this.latitude);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * From a JSON string in an Autovelox object
     * @param data JSON object in string format
     * @return an Autovelox object
     */
    public static Autovelox parseAutovelox(String data) throws JSONException {
        Autovelox autovelox = new Autovelox();
        JSONObject json = new JSONObject(data);

        if(json.getString("ccomune")!="") autovelox.setMunicipality(json.getString("ccomune"));
        else autovelox.setMunicipality("-");

        if(json.getString("cprovincia")!="") autovelox.setProvence(json.getString("cprovincia"));
        else autovelox.setProvence("-");

        if(json.getString("cregione")!="") autovelox.setRegion(json.getString("cregione"));
        else autovelox.setRegion("-");

        if(json.getString("cnome")!="") autovelox.setName(json.getString("cnome"));
        else autovelox.setName("-");

        if(json.getString("canno_inserimento")!="") autovelox.setInsertion_year(json.getInt("canno_inserimento"));
        else autovelox.setInsertion_year(0000);

        if(json.getString("cdata_e_ora_inserimento")!="") autovelox.setInsertion_date_time(json.getString("cdata_e_ora_inserimento"));
        else autovelox.setInsertion_date_time("-");

        if(json.getString("cidentificatore_in_openstreetmap")!="") autovelox.setMap_identifier(json.getInt("cidentificatore_in_openstreetmap"));
        else autovelox.setMap_identifier(0000);

        if (json.getString("clongitudine")!="") autovelox.setLongitude(json.getDouble("clongitudine"));
        else autovelox.setLongitude((double) 0000);

        if (json.getString("clatitudine")!="") autovelox.setLatitude(json.getDouble("clatitudine"));
        else autovelox.setLatitude((double) 0000);

        return autovelox;
    }
}
