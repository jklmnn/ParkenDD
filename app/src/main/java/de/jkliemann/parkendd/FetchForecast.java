package de.jkliemann.parkendd;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by jkliemann on 21.05.15.
 */
public class FetchForecast extends AsyncTask<String, Void, int[]> {

    private Context context;
    private RelativeLayout popup;
    private int error = 0;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private TimePicker timePicker;
    private RatingBar ratingBar;

    public void init(Context context, RelativeLayout popup, TimePicker timePicker, RatingBar ratingBar){
        this.context = context;
        this.popup = popup;
        this.timePicker = timePicker;
        this.ratingBar = ratingBar;
        popup.setVisibility(View.VISIBLE);
    }

    private int[] fetchOldAPI(String parm){
        String address = PreferenceManager.getDefaultSharedPreferences(context).getString("fetch_url", context.getString(R.string.default_fetch_url));
        String city = "";
        try{
            city = URLEncoder.encode(PreferenceManager.getDefaultSharedPreferences(context).getString("city", context.getString(R.string.default_city)), "UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        int[] forecastMap = new int[24];
        try {
            URL url = new URL(address + city + "/forecast/" + parm);
            HttpURLConnection cn = null;
            try {
                cn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                error = 3;
                cn = null;
            }
            if (cn != null) {
                BufferedReader br = null;
                try {
                    InputStream in = cn.getInputStream();
                    br = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        String[] raw = line.split(",");
                        try {
                            forecastMap[dateFormat.parse(raw[0]).getHours()] = Integer.parseInt(raw[1]);
                        }catch (ParseException e){
                            e.printStackTrace();
                            error = 2;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            error = 4;
        }
        return forecastMap;
    }

    protected int[] doInBackground(String... parm){
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        if(gs.getAPI_V_MAJOR() == 0 && gs.getAPI_V_MINOR() == 0 ){
            return fetchOldAPI(parm[0]);
        }
        return null;
    }

    protected void onPostExecute(final int[] forecastMap){
        popup.setVisibility(View.GONE);
        timePicker.setCurrentMinute(0);
        float rating = 5 - ((float)forecastMap[timePicker.getCurrentHour()] / 20);
        ratingBar.setRating(rating);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timePicker.setCurrentMinute(0);
                float rating = 5 - ((float)forecastMap[hourOfDay] / 20);
                ratingBar.setRating(rating);
            }
        });
        switch (error){
            case 2:
                Error.showLongErrorToast(context, context.getString(R.string.invalid_error));
                break;
            case 1:
                Error.showLongErrorToast(context, context.getString(R.string.network_error));
                break;
            case 3:
                Error.showLongErrorToast(context, context.getString(R.string.connection_error));
                break;
            case 4:
                Error.showLongErrorToast(context, context.getString(R.string.url_error));
                break;
            default:
                break;
        }
    }
}
