package de.jkliemann.parkendd;

import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

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
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapctl = map.getController();
        mapctl.setZoom(13);
        ItemizedIconOverlay<OverlayItem> spotOverlay = new ItemizedIconOverlay<>(this, createItemList(city.spots()),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        mapctl.setCenter(item.getPoint());
                        SpotIconBitmap icon = (SpotIconBitmap) item.getDrawable();
                        ParkingSpot spot = icon.getSpot();
                        TextView name = (TextView) findViewById(R.id.detailNameView);
                        name.setText(spot.name());
                        ((TextView) findViewById(R.id.detailCountValue)).setText(String.valueOf(spot.count()));
                        ((TextView) findViewById(R.id.detailFreeValue)).setText(String.valueOf(spot.free()));
                        String state;
                        switch (spot.state()) {
                            case "closed":
                                state = getString(R.string.closed);
                                break;
                            case "nodata":
                                state = getString(R.string.nodata);
                                break;
                            default:
                                state = getString(R.string.open);
                                break;
                        }
                        String type;
                        switch (spot.type()) {
                            case "Tiefgarage":
                                type = getString(R.string.Tiefgarage);
                                break;
                            case "Parkplatz":
                                type = getString(R.string.Parkplatz);
                                break;
                            case "Parkhaus":
                                type = getString(R.string.Parkhaus);
                                break;
                            default:
                                type = getString(R.string.nodata);
                                break;
                        }
                        ((TextView) findViewById(R.id.detailStateValue)).setText(state);
                        ((TextView) findViewById(R.id.detailTypeValue)).setText(type);
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        SpotIconBitmap icon = (SpotIconBitmap)item.getDrawable();
                        ParkingSpot spot = icon.getSpot();
                        return false;
                    }
                }
        );
        map.getOverlays().add(spotOverlay);
        try {
            GeoPoint selfLoc = new GeoPoint(self.getLatitude(), self.getLongitude());
            mapctl.setCenter(selfLoc);
            OverlayItem selfItem = new OverlayItem("Location", "", selfLoc);
            ArrayList<OverlayItem> selfList = new ArrayList<>();
            selfList.add(selfItem);
            ItemizedIconOverlay<OverlayItem> selfOverlay = new ItemizedIconOverlay<>(this, selfList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int index, OverlayItem item) {
                    mapctl.setCenter(item.getPoint());
                    return false;
                }

                @Override
                public boolean onItemLongPress(int index, OverlayItem item) {
                    return false;
                }
            });
            map.getOverlays().add(selfOverlay);
        }catch (NullPointerException e){
            e.printStackTrace();
            mapctl.setCenter(new GeoPoint(city.location().getLatitude(), city.location().getLongitude()));
        }
        map.invalidate();
    }

    private ArrayList<OverlayItem> createItemList(ArrayList<ParkingSpot> spotlist){
        ArrayList<OverlayItem> itemList = new ArrayList<>();
        for(ParkingSpot spot : spotlist){
            String desc = "";
            SpotIcon marker = new SpotIcon(spot);
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
                OverlayItem olItem = new OverlayItem(spot.name(), desc, new GeoPoint(spot.location().getLatitude(), spot.location().getLongitude()));
                olItem.setMarker(marker.getBitmapDrawable(this));
                olItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
                itemList.add(olItem);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return itemList;
    }

}
