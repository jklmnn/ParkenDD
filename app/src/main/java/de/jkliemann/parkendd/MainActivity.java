package de.jkliemann.parkendd;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<ParkingSpot> parkingSpots = null;
        try {
            parkingSpots = new Fetch().execute("http://jkliemann.de/offenesdresden.de/json.php").get();
        }catch(Exception e){
            e.printStackTrace();
        }
        refresh(parkingSpots);
    }


    private void refresh(ArrayList<ParkingSpot> spots){
        String showspot = "";
        for(ParkingSpot spot : spots){
            showspot = showspot + "Name: " + spot.name() + "\n";
            showspot = showspot + "Count: " + Integer.toString(spot.count()) + "\n";
            showspot = showspot + "Free: " + Integer.toString(spot.free()) + "\n";
            showspot = showspot + "Category: " + spot.category() + "\n";
        }
        TextView tv = (TextView)findViewById(R.id.textView);
        tv.setText(showspot);
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

        return super.onOptionsItemSelected(item);
    }
}
