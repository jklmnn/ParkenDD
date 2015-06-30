package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class DetailsActivity extends ActionBarActivity {

    private ParkingSpot spot;
    private DatePicker datePicker;
    private final DetailsActivity _this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent i = getIntent();
        spot = i.getParcelableExtra("spot");
        this.setTitle(spot.name() + " - " + getString(R.string.title_activity_details));
        fillForm();
    }

    private void fillForm(){
        TextView available = (TextView)findViewById(R.id.available);
        TextView availableval = (TextView)findViewById(R.id.availval);
        TextView count = (TextView)findViewById(R.id.count);
        TextView countval = (TextView)findViewById(R.id.countval);
        TextView distance = (TextView)findViewById(R.id.distance);
        TextView distanceval = (TextView)findViewById(R.id.distanceval);
        Button mapbutton = (Button)findViewById(R.id.mapbutton);
        mapbutton.setText(getString(R.string.map));
        mapbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        available.setText(getString(R.string.available) + ":");
        count.setText(getString(R.string.count) + ":");
        distance.setText(getString(R.string.distance) + ":");
        availableval.setText(Integer.toString(spot.free()));
        countval.setText(Integer.toString(spot.count()));
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        Location currentLocation = gs.getLastKnownLocation();
        try {
            distanceval.setText(Util.getViewDistance(Util.getDistance(currentLocation, spot.location())));
        }catch (NullPointerException e){
            distanceval.setText(getString(R.string.nodata));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
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
//        }

        return super.onOptionsItemSelected(item);
    }

    private void openMap(){
        Uri geoUri = spot.geoUri();
        try{
            Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
            startActivity(mapCall);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Error.showLongErrorToast(this, getString(R.string.intent_error));
        }
    }

}
