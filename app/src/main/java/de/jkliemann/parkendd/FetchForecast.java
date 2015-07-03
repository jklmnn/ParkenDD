package de.jkliemann.parkendd;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jkliemann on 03.07.15.
 */
public class FetchForecast extends AsyncTask<Object, Void, Map<ParkingSpot, Map<Date, Integer>>> {

    private enum API {zero, one};
    private static final DateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-DD");
    private static final DateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
    private static final DateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
    private static final String DATA = "data";

    private final FetchForecastInterface forecastInterface;

    public static final int PROGRESS = 2;

    public FetchForecast(FetchForecastInterface forecastInterface){
        this.forecastInterface = forecastInterface;
    }

    private Map<Date, Integer> parseAPI_1_0(String data) throws JSONException, ParseException{
        JSONObject global = new JSONObject(data);
        JSONObject jsondata = global.getJSONObject(DATA);
        Iterator<String> dates = jsondata.keys();
        Map<Date, Integer> map = new HashMap<>();
        while(dates.hasNext()){
            String datestring = dates.next();
            Date date = newDateFormat.parse(datestring);
            Integer num = jsondata.getInt(datestring);
            map.put(date, num);
        }
        return map;
    }

    private Map<Date, Integer> parseAPI_0_0(String data) throws ParseException{
        String[] lines = data.split(System.getProperty("line.separator"));
        Map<Date, Integer> map = new HashMap<>();
        for(int i = 0; i < lines.length; i++){
            String[] line = lines[i].split(",");
            Date date = oldDateFormat.parse(line[0]);
            Integer num = Integer.parseInt(line[1]);
            map.put(date, num);
        }
        return map;
    }

    private String fetch(URL url) throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String data = "";
        String line;
        while ((line = br.readLine()) != null) {
            data += line + "\n";
        }
        br.close();
        connection.disconnect();
        return data;
    }

    protected void onProgressUpdate(Void... v){
        forecastInterface.updateProgress();
    }

    protected Map<ParkingSpot, Map<Date, Integer>> doInBackground(Object... parm){
        String fetch_url = null;
        City city = null;
        API api = null;
        String date = null;
        Map<ParkingSpot, Map<Date, Integer>> forecastMap = new HashMap<>();
        if(GlobalSettings.getGlobalSettings().getAPI_V_MAJOR() == 0 && GlobalSettings.getGlobalSettings().getAPI_V_MINOR() == 0){
            api = API.zero;
        }else if(GlobalSettings.getGlobalSettings().getAPI_V_MAJOR() == 1 && GlobalSettings.getGlobalSettings().getAPI_V_MINOR() == 0){
            api = API.one;
        }
        try {
            if (parm[0] instanceof String) {
                fetch_url = (String) parm[0];
            }
            if (parm[1] instanceof City) {
                city = (City) parm[1];
            }
            if (parm[2] instanceof Date){
                date = queryDateFormat.format(parm[2]);
            }
            ArrayList<ParkingSpot> spotList = city.spots();
            publishProgress();
            for(ParkingSpot spot : spotList){
                if(spot.forecast()){
                    URL forecasturl = null;
                    try {
                        String encCityName = URLEncoder.encode(city.name(), "UTF-8");
                        String encCityId = URLEncoder.encode(city.id(), "UTF-8");
                        String encSpotName = URLEncoder.encode(spot.name(), "UTF-8");
                        String encSpotId = URLEncoder.encode(spot.id(), "UTF-8");
                        String encDate = URLEncoder.encode(date, "UTF-8");
                        if (api == API.zero) {
                            forecasturl = new URL(fetch_url + encCityName + "/forecast/?spot=" + encSpotName + "&date=" + encDate);
                        }else if(api == API.one){
                            forecasturl = new URL(fetch_url + encCityId + "/" + encSpotId + "/timespan?from=" + encDate + "&to=" + encDate);
                        }
                        try{
                            String data = fetch(forecasturl);
                            try{
                                Map<Date, Integer> spotmap = null;
                                if(api == API.zero){
                                    spotmap = parseAPI_0_0(data);
                                }else if(api == API.one){
                                    spotmap = parseAPI_1_0(data);
                                }
                                forecastMap.put(spot, spotmap);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }catch (JSONException e){
                                e.printStackTrace();
                            }catch (ParseException e){
                                e.printStackTrace();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                    }catch (MalformedURLException e){
                        e.printStackTrace();
                    }
                }
            }
            publishProgress();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return forecastMap;
    }

    protected void onPostExecute(Map<ParkingSpot, Map<Date, Integer>> forecastMap){
        forecastInterface.onForecastFinished(forecastMap);
    }
}
