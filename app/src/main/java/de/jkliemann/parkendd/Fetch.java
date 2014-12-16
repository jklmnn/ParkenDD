package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jkliemann on 10.12.14.
 */
public class Fetch extends AsyncTask<String, Void, ArrayList<ParkingSpot>> {

    private static final String NAME = "name";
    private static final String LOTS = "lots";
    private static final String COUNT = "count";
    private static final String FREE = "free";
    private static final String STATE = "state";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private ListView spotView = null;
    private Context context = null;
    private RelativeLayout popup = null;
    private Boolean error = false;

    public void setUi(ListView spotView, Context context, RelativeLayout popup){
        this.spotView = spotView;
        this.context = context;
        this.popup = popup;
        popup.setVisibility(View.VISIBLE);
    }

    private static Uri geoUriFromCoord(String lat, String lon, String label){
        String location;
        if(!lat.equals("null") && !lon.equals("null")){
            location = "geo:0,0?q="+lat+","+lon+"("+label+")";
        }else{
            location = "geo:0,0?q=Dresden " + label;
        }
        System.out.println(location);
        return Uri.parse(location);
    }

    private ArrayList<ParkingSpot> parseJSon(String json){
        ArrayList<ParkingSpot> spots = new ArrayList<ParkingSpot>();
        try{
            JSONArray global = new JSONArray(json);
            for(int i = 0; i < global.length(); i++){
                JSONObject catg = global.getJSONObject(i);
                String category = catg.getString(NAME);
                JSONArray lots = catg.getJSONArray(LOTS);
                for(int j = 0; j < lots.length(); j++){
                    JSONObject lot = lots.getJSONObject(j);
                    String name = lot.getString(NAME);
                    String count = lot.getString(COUNT);
                    String free = lot.getString(FREE);
                    String state = lot.getString(STATE);
                    String lat = lot.getString(LAT);
                    String lon = lot.getString(LON);
                    if(count.length() < 1 || count.equals("null")){
                        count = "0";
                    }
                    if(free.length() < 1 || free.equals("null")){
                        free = "0";
                    }
                    spots.add(new ParkingSpot(name, category, state, Integer.parseInt(count), Integer.parseInt(free), geoUriFromCoord(lat, lon, name)));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
            error = true;
        }
        return spots;
    }

   protected ArrayList<ParkingSpot> doInBackground(String... address){
       String json = "";
       try {
           URL url = new URL(address[0]);
           HttpURLConnection cn = (HttpURLConnection) url.openConnection();
           BufferedReader br = null;
           InputStream in = cn.getInputStream();
           try{
               br = new BufferedReader(new InputStreamReader(in));
               String line = "";
               while((line = br.readLine()) != null){
                   json = json + line + "\n";
               }
           }catch(IOException e){
               e.printStackTrace();
               error = true;
           }finally {
               if(br != null){
                   try {
                       br.close();
                   }catch(IOException e){
                       e.printStackTrace();
                       error = true;
                   }
               }
           }
       }catch(Exception e){
           e.printStackTrace();
           error = true;
       }
       return parseJSon(json);
   }

    protected void onPostExecute(ArrayList<ParkingSpot> spots){
        if(context != null && spotView != null) {
            final ParkingSpot[] spotArray = spots.toArray(new ParkingSpot[spots.size()]);
            SlotListAdapter adapter = new SlotListAdapter(context, spotArray);
            spotView.setAdapter(adapter);
            spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri geoUri = spotArray[position].geoUri();
                    try {
                        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                        context.startActivity(mapCall);
                    }catch(ActivityNotFoundException e){
                        e.printStackTrace();
                        Error.showLongErrorToast(context, context.getString(R.string.intent_error));
                    }
                }
            });
        }
        popup.setVisibility(View.GONE);
        if(error) {
            Error.showLongErrorToast(context, context.getString(R.string.network_error));
        }
    }
}
