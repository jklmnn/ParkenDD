package de.jkliemann.parkendd;

import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        City city  = GlobalSettings.getGlobalSettings().getCityByName(PreferenceManager.getDefaultSharedPreferences(this).getString("city", getString(R.string.default_city)));
        Location self = GlobalSettings.getGlobalSettings().getLastKnownLocation();
        setTitle(city.name());
        MapView map = (MapView)findViewById(R.id.osmap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapctl = map.getController();
        mapctl.setZoom(13);
        mapctl.setCenter(new GeoPoint(self.getLatitude(), self.getLongitude()));
        ItemizedIconOverlay<OverlayItem> spotOverlay = new ItemizedIconOverlay<OverlayItem>(this, createItemList(city.spots()),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }
        );
        map.getOverlays().add(spotOverlay);
        map.invalidate();
    }

    private ArrayList<OverlayItem> createItemList(ArrayList<ParkingSpot> spotlist){
        ArrayList<OverlayItem> itemList = new ArrayList<>();
        for(ParkingSpot spot : spotlist){
            String desc = "";
            switch (spot.state()){
                case "closed":
                    desc = getString(R.string.closed);
                    break;
                case "nodata":
                    desc = getString(R.string.nodata);
                    break;
                default:
                    desc = String.valueOf(spot.free()) + " " + getString(R.string.of)  + " " + String.valueOf(spot.count());
                    break;
            }
            try {
                itemList.add(new OverlayItem(spot.name(), desc, new GeoPoint(spot.location().getLatitude(), spot.location().getLongitude())));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return itemList;
    }

}
