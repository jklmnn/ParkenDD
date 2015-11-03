package de.jkliemann.parkendd;

import android.location.Location;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by jkliemann on 23.08.15.
 */
public class Parser {

    private static final String CITIES = "cities";
    private static final String VERSION = "api_version";
    private static final String NAME = "name";
    private static final String COORDS = "coords";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String DATA_SOURCE = "source";
    private static final String DATA_URL = "url";


    public static ArrayList<City> meta(String data) throws JSONException{
        ArrayList<City> cities = new ArrayList<>();
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        JSONObject global = new JSONObject(data);
        String version = global.getString(VERSION);
        String[] vs = version.split("\\.");
        gs.setAPI(Integer.parseInt(vs[0]), Integer.parseInt(vs[1]));
        if(gs.getAPI_V_MAJOR() == 1){
            JSONObject cityobjects = global.getJSONObject(CITIES);
            Iterator<String> city_ids = cityobjects.keys();
            while(city_ids.hasNext()){
                String id = city_ids.next();
                JSONObject co = cityobjects.getJSONObject(id);
                String name = co.getString(NAME);
                Location location;
                try {
                    JSONObject coord = co.getJSONObject(COORDS);
                    double lat = coord.getDouble(LAT);
                    double lon = coord.getDouble(LNG);
                    location = new Location("gps");
                    location.setLatitude(lat);
                    location.setLongitude(lon);
                } catch (JSONException e) {
                    location = null;
                }
                boolean active = false;
                try{
                    active = co.getBoolean("active_support");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                String source = co.getString(DATA_SOURCE);
                String url = co.getString(DATA_URL);
                City c = new City(id, name, location);
                c.setActive(active);
                c.setData_source(source);
                c.setData_url(url);
                cities.add(c);
            }
        }
        return cities;
    }

    private static final String DATA = "data";

    public static Map<Date, Integer> forecast(String data) throws JSONException, ParseException{
        String VERSION = "version";
        DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        ISODateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        JSONObject global = new JSONObject(data);
        String version = global.getString(VERSION);
        if(version.equals("1.0")){
            JSONObject jsondata = global.getJSONObject(DATA);
            Iterator<String> dates = jsondata.keys();
            Map<Date, Integer> map = new HashMap<>();
            while (dates.hasNext()) {
                String datestring = dates.next();
                Date date = ISODateFormat.parse(datestring);
                Integer num = jsondata.getInt(datestring);
                map.put(date, num);
            }
            return map;
        }else{
            return new HashMap<>();
        }
    }

    private static final String LOTS = "lots";
    private static final String TOTAL = "total";
    private static final String FREE = "free";
    private static final String STATE = "state";
    private static final String FORECAST = "forecast";
    private static final String LOT_TYPE = "lot_type";
    private static final String LAST_DOWNLOADED = "last_downloaded";
    private static final String LAST_UPDATED = "last_updated";
    private static final String ID = "id";
    private static final String ADDRESS = "address";
    private static final String REGION = "region";

    public static City city(String data, City CITY) throws JSONException, NullPointerException{
        ArrayList<ParkingSpot> spotlist = new ArrayList<>();
        JSONObject global = new JSONObject(data);
        String last_downloaded = global.getString(LAST_DOWNLOADED);
        String last_updated = global.getString(LAST_UPDATED);
        JSONArray spotarray = global.getJSONArray(LOTS);
        for(int i = 0; i < spotarray.length(); i++){
            JSONObject lot = spotarray.getJSONObject(i);
            String name = lot.getString(NAME);
            String state = lot.getString(STATE);
            String city = CITY.name();
            String id = lot.getString(ID);
            String type = "";
            String adr = "";
            String region = "";
            try{
                type = lot.getString(LOT_TYPE);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                adr = lot.getString(ADDRESS);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try {
                region = lot.getString(REGION);
            }catch (JSONException e){
                e.printStackTrace();
            }
            int total = lot.getInt(TOTAL);
            int free = lot.getInt(FREE);
            double lat, lon;
            try {
                JSONObject coord = lot.getJSONObject(COORDS);
                lat = coord.getDouble(LAT);
                lon = coord.getDouble(LNG);
            } catch (JSONException e) {
                lat = 0;
                lon = 0;
            }
            Boolean forecast = lot.getBoolean(FORECAST);
            ParkingSpot spot = new ParkingSpot(name, state, city, id, total, free, lat, lon, forecast);
            spot.setType(type);
            spot.setAddress(adr);
            spot.setCategory(region);
            spotlist.add(spot);
        }
        CITY.setSpots(spotlist);
        CITY.setLast_downloaded(last_downloaded);
        CITY.setLast_updated(last_updated);
        return CITY;
    }

    public static Location nominatim(String data) throws JSONException{
        JSONArray osm = new JSONArray(data);
        Location loc = null;
        if(osm.length() > 0){
            JSONObject jsondata = osm.getJSONObject(0);
            double lat = jsondata.getDouble("lat");
            double lon = jsondata.getDouble("lon");
            loc = new Location("gps");
            loc.setLatitude(lat);
            loc.setLongitude(lon);
            JSONObject address = jsondata.getJSONObject("address");
            String name = "";
            try{
                name += address.getString("road") + " ";
                try{
                    name += address.getString("house_number");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                name += "\n";
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                name += address.getString("postcode") + " ";
            }catch (JSONException e){
                e.printStackTrace();
            }
            try {
                name += address.getString("city");
            }catch (JSONException e){
                e.printStackTrace();
                try {
                    name += address.getString("town");
                }catch (JSONException e2){
                    e2.printStackTrace();
                }
            }
            Bundle extra = new Bundle();
            extra.putString("detail", name);
            loc.setExtras(extra);
        }
        return loc;
    }
}
