package de.jkliemann.parkendd;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * TODO: document your custom view class.
 */
public class SlotListAdapter extends ArrayAdapter<ParkingSpot> {
    private final Context context;
    private final ParkingSpot[] spots;
    private final GlobalSettings gs;
    private final Location currentLocation;
    private final int red = Color.argb(255, 255, 0, 0);
    private final int green = Color.argb(255, 0, 155, 0);
    private final int yellow = Color.argb(255, 185, 185, 0);
    private final int blue = Color.argb(255, 0, 0, 255);
    private static final String CLOSED = "closed";
    private static final String NODATA = "nodata";
    private static final String MANY = "many";
    private static final String FEW = "few";
    private static final String FULL ="full";


    public SlotListAdapter(Context context, ParkingSpot[] spots){
        super(context, R.layout.slot_list_adapter, spots);
        this.context = context;
        this.spots = spots;
        this.gs = GlobalSettings.getGlobalSettings();
        currentLocation = gs.getLastKnownLocation();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View slotView = inflater.inflate(R.layout.slot_list_adapter, parent, false);
        TextView countView = (TextView)slotView.findViewById(R.id.countView);
        TextView nameView = (TextView)slotView.findViewById(R.id.nameView);
        TextView distanceView = (TextView)slotView.findViewById(R.id.distanceView);
        ParkingSpot spot = spots[position];
        nameView.setText(spot.name());
        ProgressBar slotBar = (ProgressBar)slotView.findViewById(R.id.slotBar);
        slotBar.setMax(spot.count());
        slotBar.setProgress(spot.free());
        Resources res = slotView.getResources();
        if(spot.state().equals(CLOSED)) {
            countView.setText(context.getString(R.string.closed));
            nameView.setTextColor(this.red);
            slotBar.setProgressDrawable(res.getDrawable(R.drawable.dr_progress_red));
        }else if(spot.state().equals(NODATA)){
            countView.setText(context.getString(R.string.nodata) + " (" + Integer.toString(spot.count()) + ")");
            nameView.setTextColor(this.blue);
            slotBar.setVisibility(View.INVISIBLE);
            slotBar.setProgressDrawable(res.getDrawable(R.drawable.dr_progress_blue));
        }else{
            countView.setText(Integer.toString(spot.free()) + " " + context.getString(R.string.of) + " " + Integer.toString(spot.count()));
            double perc = (double)spot.free() / (double)spot.count();
            if(perc < 0.05){
                nameView.setTextColor(this.red);
                slotBar.setProgressDrawable(res.getDrawable(R.drawable.dr_progress_red));
            }else if(perc < 0.2){
                nameView.setTextColor(this.yellow);
                slotBar.setProgressDrawable(res.getDrawable(R.drawable.dr_progress_yellow));
            }else{
                nameView.setTextColor(this.green);
                slotBar.setProgressDrawable(res.getDrawable(R.drawable.dr_progress_green));
            }
        }
        if(currentLocation != null && spot.location() != null){
            distanceView.setText(Util.getViewDistance(Util.getDistance(currentLocation, spot.location())));
        }else {
            distanceView.setText("");
        }
        return slotView;
    }
}