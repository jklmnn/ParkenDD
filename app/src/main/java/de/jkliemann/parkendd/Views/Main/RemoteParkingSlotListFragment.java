package de.jkliemann.parkendd.Views.Main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.Model.ParkingSpot;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Utilities.Error;
import de.jkliemann.parkendd.Views.SlotListAdapter;
import de.jkliemann.parkendd.Web.Loader;
import de.jkliemann.parkendd.Web.LoaderInterface;
import de.jkliemann.parkendd.Web.Parser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RemoteParkingSlotListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RemoteParkingSlotListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteParkingSlotListFragment extends Fragment implements LoaderInterface {

    SharedPreferences preferences;
    private Loader meta;
    private Loader cityLoader;
    private City city;
    private HashMap<Integer, Location> addressMap;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String query;
    private String initialRequest;

    private OnFragmentInteractionListener mListener;

    public RemoteParkingSlotListFragment() {
        // Required empty public constructor
    }


    public static RemoteParkingSlotListFragment newInstance(String mQuery) {
        RemoteParkingSlotListFragment fragment = new RemoteParkingSlotListFragment();
        fragment.initialRequest = mQuery;
        fragment.query = mQuery;

        return fragment;
    }

    // Swipe to refresh
    private void setUpRefreshLayout(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_spots);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        doSearch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking_slot_list, container, false);
        setUpRefreshLayout(view);
        resetProgressBar();
        onProgressUpdated();
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onExceptionThrown(Exception e) {
        if(e instanceof FileNotFoundException) {
            Error.displaySnackBarMessage(swipeRefreshLayout, getString(R.string.server_error));
        }else if(e instanceof UnknownHostException){
            Error.displaySnackBarMessage(swipeRefreshLayout, getString(R.string.connection_error));
        }
        ((MainActivity) getActivity()).progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderFinished(String[] data, Loader loader) {
        if(loader.equals(meta)){
            ArrayList<City> citylist;
            try{
                citylist = Parser.meta(data[0]);
                ((ParkenDD)getActivity().getApplication()).updateCities(citylist);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                Location[] loc = Parser.nominatim(data[1]);
                for(int i = 0; i < loc.length; i++) {
                    try {
                        addressMap.put(i, loc[i]);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }

                if(loc.length > 0) {
                    try {
                        ((ParkenDD) getActivity().getApplication()).setLocation(addressMap.get(0));
                        refresh();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }else{
                    // TODO : handle error
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        if(loader.equals(cityLoader)){
            try{
                city = Parser.city(data[0], city);
                setList(city);
                ((ParkenDD) getActivity().getApplication()).getTracker().trackScreenView("/" + city.id(), city.name());
                TimeZone tz = Calendar.getInstance().getTimeZone();
                DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity());
                dateFormat.setTimeZone(tz);
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
                timeFormat.setTimeZone(tz);
                String locDate = dateFormat.format(city.last_updated());
                String locTime = timeFormat.format(city.last_updated());
                Error.displaySnackBarMessage(swipeRefreshLayout, getString(R.string.last_update) + ": " + locDate + " " + locTime);
                onProgressUpdated();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onProgressUpdated() {
        ((MainActivity) getActivity()).progressBar.setProgress(((MainActivity) getActivity()).progressBar.getProgress() + 1);
    }

    private void refresh(){
        URL[] cityurl = new URL[1];
        try{
            city = ((ParkenDD) getActivity().getApplication()).currentCity();
            getActivity().setTitle(city.name() + ": " + initialRequest);
            cityurl[0] = Loader.getCityUrl(getString(R.string.serveraddress), city);
            cityLoader = new Loader(this);
            cityLoader.execute(cityurl);
            onProgressUpdated();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    private void setList(City CITY){
        ExpandableListView spotView = (ExpandableListView)getView().findViewById(R.id.spotListView);
        String sortOptions[] = getResources().getStringArray(R.array.setting_sort_options);
        String sortPreference = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("sorting", sortOptions[0]);
        Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_closed", true);
        Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_nodata", false);
        Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_full", true);
        final ParkingSpot[] spotArray;
        ParkingSpot[] preArray;
        ArrayList<ParkingSpot> spots = CITY.spots();
        ArrayList<ParkingSpot> cachelist = new ArrayList<>();
        for(ParkingSpot spot : spots){
            if(hide_closed && spot.state().equals("closed")){
                cachelist.add(spot);
            }
            if(hide_nodata && spot.state().equals("nodata")){
                cachelist.add(spot);
            }
            if(hide_full && spot.free() == 0 && !spot.state().equals("nodata") && !spot.state().equals("closed")){
                cachelist.add(spot);
            }
        }
        for (ParkingSpot spot : cachelist){
            spots.remove(spot);
        }
        if(sortPreference.equals(sortOptions[0])){
            try{
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byEUKLID.INSTANCE);
            }catch (NullPointerException e){
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[1])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byNAME.INSTANCE);
            }catch (NullPointerException e){
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[2])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byDISTANCE.INSTANCE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[3])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byFREE.INSTANCE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else{
            preArray = spots.toArray(new ParkingSpot[spots.size()]);
        }
        spotArray = preArray;
        SlotListAdapter adapter = new SlotListAdapter(getActivity(), spotArray);
        spotView.setAdapter(adapter);
        onProgressUpdated();
        ((MainActivity) getActivity()).progressBar.setVisibility(View.INVISIBLE);
    }

    private void doSearch() {
        Uri data = null;
        try {
            query = "geo:0,0?q=" + URLEncoder.encode(query, "UTF-8").replace("+", "%20");
            data = Uri.parse(query);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        URL[] serverurl = new URL[2];
        try {
            serverurl[1] = Loader.getNominatimURL(data);
            serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
            meta = new Loader(this);
            meta.execute(serverurl);
            onProgressUpdated();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        addressMap = new HashMap<>();
    }

    private void resetProgressBar() {
        ((MainActivity) getActivity()).progressBar.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).progressBar.setProgress(0);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
