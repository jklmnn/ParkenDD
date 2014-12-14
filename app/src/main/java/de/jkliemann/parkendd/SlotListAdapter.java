package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * TODO: document your custom view class.
 */
public class SlotListAdapter extends ArrayAdapter<ParkingSpot> {
    private final Context context;
    private final ParkingSpot[] spots;
    private final int red = Color.argb(255, 255, 0, 0);
    private final int green = Color.argb(255, 0, 255, 0);
    private final int yellow = Color.argb(255, 255, 255, 0);
    private final int blue = Color.argb(255, 0, 0, 255);


    public SlotListAdapter(Context context, ParkingSpot[] spots){
        super(context, R.layout.slot_list_adapter, spots);
        this.context = context;
        this.spots = spots;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View slotView = inflater.inflate(R.layout.slot_list_adapter, parent, false);
        TextView countView = (TextView)slotView.findViewById(R.id.countView);
        TextView nameView = (TextView)slotView.findViewById(R.id.nameView);
        ParkingSpot spot = spots[position];
        nameView.setText(spot.name() + " (" + Integer.toString(spot.count()) + ")");
        if(spot.state().equals("closed")) {
            countView.setText(context.getString(R.string.closed));
            countView.setTextColor(this.red);
        }else if(spot.state().equals("nodata")){
            countView.setText(context.getString(R.string.nodata));
            countView.setTextColor(this.blue);
        }else{
            countView.setText(Integer.toString(spot.free()));
            if(spot.state().equals("many")){
                countView.setTextColor(this.green);
            }else if(spot.state().equals("few")){
                countView.setTextColor(this.yellow);
            }else if(spot.state().equals("full")){
                countView.setTextColor(this.red);
            }
        }
        return slotView;
    }
}