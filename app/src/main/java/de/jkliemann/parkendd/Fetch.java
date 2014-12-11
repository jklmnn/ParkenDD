package de.jkliemann.parkendd;

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
    private ListView spotView = null;
    private Context context = null;
    private RelativeLayout popup = null;

    public void setUi(ListView spotView, Context context, RelativeLayout popup){
        this.spotView = spotView;
        this.context = context;
        this.popup = popup;
        popup.setVisibility(View.VISIBLE);
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
                    if(count.length() < 1){
                        count = "0";
                    }
                    if(free.length() < 1){
                        free = "0";
                    }
                    spots.add(new ParkingSpot(name, category, Integer.parseInt(count), Integer.parseInt(free)));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
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
           }finally {
               if(br != null){
                   try {
                       br.close();
                   }catch(IOException e){
                       e.printStackTrace();
                   }
               }
           }
       }catch(Exception e){
           e.printStackTrace();
       }
       return parseJSon(json);
   }

    protected void onPostExecute(ArrayList<ParkingSpot> spots){
        if(context != null && spotView != null) {
            ParkingSpot[] spotArray = spots.toArray(new ParkingSpot[spots.size()]);
            SlotListAdapter adapter = new SlotListAdapter(context, spotArray);
            spotView.setAdapter(adapter);
            spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView current = (TextView)view.findViewById(R.id.nameView);
                    String location = "Dresden " + current.getText();
                    String geoUriString = "geo:0,0?q="  + location;
                    Uri geoUri = Uri.parse(geoUriString);
                    Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                    context.startActivity(mapCall);
                }
            });
        }
        popup.setVisibility(View.GONE);
    }
}
