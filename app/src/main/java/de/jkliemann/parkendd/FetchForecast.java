package de.jkliemann.parkendd;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkliemann on 21.05.15.
 */
public class FetchForecast extends AsyncTask<String, Void, Map<Date, Integer>> {

    private Context context;
    private RelativeLayout popup;
    private TextView textView;
    private int error = 0;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String test;


    public void init(Context context, RelativeLayout popup, TextView textView){
        this.context = context;
        this.popup = popup;
        this.textView = textView;
        popup.setVisibility(View.VISIBLE);
    }

    protected Map<Date, Integer> doInBackground(String... parm){
        String address = PreferenceManager.getDefaultSharedPreferences(context).getString("fetch_url", context.getString(R.string.default_fetch_url));
        address = address + PreferenceManager.getDefaultSharedPreferences(context).getString("city", context.getString(R.string.default_city)) + "/forecast/" + parm;
        Map <Date, Integer> forecastMap = new HashMap<Date, Integer>();
        try {
            URL url = new URL(address);
            HttpURLConnection cn = null;
            try {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ignore_cert", false)) {
                    cn = Util.getUnsecureConnection(url);
                } else {
                    cn = (HttpURLConnection) url.openConnection();
                }
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
                        test = test + line;
                        try {
                            forecastMap.put(dateFormat.parse(raw[0]), Integer.parseInt(raw[1]));
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

    protected void onPostExecute(Map<Date, Integer> forecastMap){
        popup.setVisibility(View.GONE);
        textView.setText(test);
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
