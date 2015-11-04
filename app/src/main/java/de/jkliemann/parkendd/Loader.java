package de.jkliemann.parkendd;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jkliemann on 23.08.15.
 */
public class Loader extends AsyncTask<URL[], Void, String[]> {

    private final LoaderInterface LoaderFinished;

    private Exception lastException = null;

    public Loader(LoaderInterface li){
        LoaderFinished = li;
    }

    public static URL getMetaUrl(String address) throws MalformedURLException{
        URL meta = new URL(address);
        return meta;
    }

    public static URL getCityUrl(String address, City city) throws MalformedURLException{
        if(!address.substring(address.length() - 1).equals("/")){
            address += "/";
        }
        URL cityurl = null;
        try {
            cityurl = new URL(address + URLEncoder.encode(city.id(), "UTF-8"));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return cityurl;
    }

    public static URL getForecastUrl(String address, City city, ParkingSpot spot, Date date) throws MalformedURLException{
        DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        ISODateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(!address.substring(address.length() - 1).equals("/")){
            address += "/";
        }
        Date start = date;
        Date end = (Date)date.clone();
        end.setDate(end.getDate() + 1);
        URL url = null;
        try {
            String encodedCityId = URLEncoder.encode(city.id(), "UTF-8");
            String encodedSpotId = URLEncoder.encode(spot.id(), "UTF-8");
            String encodedStartDate = URLEncoder.encode(ISODateFormat.format(start), "UTF-8");
            String encodedEndDate = URLEncoder.encode(ISODateFormat.format(end), "UTF-8");
            url = new URL(address + encodedCityId + "/" + encodedSpotId + "/timespan?from=" + encodedStartDate + "&to=" + encodedEndDate);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return url;
    }

    public static URL getNominatimURL(Uri geouri) throws MalformedURLException{
        String host = "https://nominatim.openstreetmap.org";
        String format = "json";
        String query = geouri.getEncodedQuery();
        URL url = new URL(host + "/search?" + query + "&format=" + format + "&addressdetails=1");
        return url;
    }

    protected String[] doInBackground(URL[]... urls){
        URL[] url_list = urls[0];
        String[] data = new String[url_list.length];
        for(int i = 0; i < url_list.length; i++) {
            data[i] = "";
            URL url = url_list[i];
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "ParkenDD for Android 1.0.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    data[i] += line;
                }
                br.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                lastException = e;
            }
            updateProgress();
        }
        return data;
    }

    protected void updateProgress(){
        LoaderFinished.onProgressUpdated();
    }

    protected void onPostExecute(String[] data){
        if(lastException == null) {
            LoaderFinished.onLoaderFinished(data, this);
        }else {
            LoaderFinished.onExceptionThrown(lastException);
        }
    }
}
