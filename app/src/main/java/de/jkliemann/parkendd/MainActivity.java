package de.jkliemann.parkendd;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<ParkingSpot> parkingSpots = this.fetch(getString(R.string.fetch_url));
        refresh(parkingSpots);
    }


    private ArrayList<ParkingSpot> fetch(String url){
        ArrayList<ParkingSpot> parkingSpots = null;
        try{
            parkingSpots = new Fetch().execute(url).get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return parkingSpots;
    }

    private void refresh(ArrayList<ParkingSpot> spots){
        ParkingSpot[] spotarray = spots.toArray(new ParkingSpot[spots.size()]);
        SlotListAdapter adapter = new SlotListAdapter(this, spotarray);
        ListView spotView = (ListView)findViewById(R.id.spotListView);
        spotView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//       }
        if(id == R.id.action_refresh){
            ArrayList<ParkingSpot> parkingSpots = this.fetch(getString(R.string.fetch_url));
            this.refresh(parkingSpots);
        }

        return super.onOptionsItemSelected(item);
    }
}
