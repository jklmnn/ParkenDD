package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * TODO: document your custom view class.
 */
public class SlotListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final ParkingSpot[] spots;
    private final GlobalSettings gs;
    private final Location currentLocation;
    private final int red = Color.argb(0xaa, 0xef, 0x53, 0x50);
    private final int green = Color.argb(0xaa, 0x66, 0xbb, 0x6a);
    private final int yellow = Color.argb(0xaa, 0xff, 0xee, 0x58);
    private final int blue = Color.argb(0xaa, 0x42, 0xa5, 0xf5);
    private static final String CLOSED = "closed";
    private static final String NODATA = "nodata";

    public SlotListAdapter(Context context, ParkingSpot[] spots){
        this.context = context;
        this.spots = spots;
        this.gs = GlobalSettings.getGlobalSettings();
        currentLocation = gs.getLastKnownLocation();
    }

    @Override
    public int getGroupCount() {
        return spots.length;
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View slotView = inflater.inflate(R.layout.slot_list_adapter, parent, false);
        TextView countView = (TextView)slotView.findViewById(R.id.countView);
        TextView nameView = (TextView)slotView.findViewById(R.id.nameView);
        TextView distanceView = (TextView)slotView.findViewById(R.id.distanceView);
        ParkingSpot spot = spots[position];
        nameView.setText(spot.name());
        if(spot.state().equals(CLOSED)) {
            countView.setText(context.getString(R.string.closed));
            slotView.setBackgroundColor(red);
        }else if(spot.state().equals(NODATA)){
            countView.setText(context.getString(R.string.nodata) + " (" + Integer.toString(spot.count()) + ")");
            slotView.setBackgroundColor(blue);
        }else{
            countView.setText(Integer.toString(spot.free()) + " " + context.getString(R.string.of) + " " + Integer.toString(spot.count()));
            double perc = (double)spot.free() / (double)spot.count();
            if(perc < 0.05){
                slotView.setBackgroundColor(red);
            }else if(perc < 0.2){
                slotView.setBackgroundColor(yellow);
            }else{
                slotView.setBackgroundColor(green);
            }
        }
        if(currentLocation != null && spot.location() != null){
            distanceView.setText(Util.getViewDistance(Util.getDistance(currentLocation, spot.location())));
        }else {
            distanceView.setText("");
        }
        return slotView;
    }

    @Override
    public View getChildView(int groupId, int childId, boolean isLastChild, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View detailView = inflater.inflate(R.layout.list_detail, null);
        ParkingSpot child = spots[groupId];
        TextView type = (TextView)detailView.findViewById(R.id.type);
        type.setText(context.getString(R.string.type) + ":");
        TextView typeval = (TextView)detailView.findViewById(R.id.typeVal);
        typeval.setText(child.type());
        TextView address = (TextView)detailView.findViewById(R.id.address);
        address.setText(context.getString(R.string.address) + ":");
        TextView addressval = (TextView)detailView.findViewById(R.id.addressval);
        addressval.setText(child.address() + "\n" + child.category());
        Log.i("CATEGORY", child.category());
        return detailView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void onGroupExpanded(int position){
        super.onGroupExpanded(position);
    }
}