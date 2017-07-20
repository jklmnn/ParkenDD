package de.jkliemann.parkendd.Views.Main;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
 * {@link ForecastFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForecastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForecastFragment extends Fragment implements LoaderInterface, View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private RelativeLayout relativeLayout;
    private RelativeLayout datePickerLayout;
    private TimePicker timePicker;
    private View mView;
    private static final int dateOffset = 1900;
    private Map<ParkingSpot, Map<Date, Integer>> spotmap;
    private Date date;
    ProgressBar pg;
    ParkingSpot[] spotList;
    Loader forecastLoader;
    DateFormat dateFormat;
    City city;

    public ForecastFragment() {
        // Required empty public constructor
    }

    public static ForecastFragment newInstance() {
        ForecastFragment fragment = new ForecastFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.RelativeLayout);
        datePickerLayout = (RelativeLayout) view.findViewById(R.id.datePickerLayout);
        datePickerLayout.setVisibility(View.INVISIBLE);

        mView = view;

        setupProgressBar();
        setupOkButton();
        setupCancelButton();
        setupTimePicker();
        ((ParkenDD) getActivity().getApplication()).getTracker().trackScreenView("/forecast/" + city.id(), "Vorhersage-" + city.name());
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
            Error.displaySnackBarMessage(mView, getString(R.string.server_error));
        }else if(e instanceof UnknownHostException){
            Error.displaySnackBarMessage(mView, getString(R.string.connection_error));
        }
        pg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderFinished(String[] data, Loader instance) {
        spotmap = new HashMap<>();
        Map<Date, Integer> dateMap;
        for (int i = 0; i < data.length; i++) {
            try {
                dateMap = Parser.forecast(data[i]);
            } catch (JSONException e) {
                e.printStackTrace();
                dateMap = new HashMap<>();
            } catch (ParseException e) {
                e.printStackTrace();
                dateMap = new HashMap<>();
            }
            spotmap.put(spotList[i], dateMap);
        }

        updateList(timePicker.getCurrentHour(), mView);
        onProgressUpdated();
    }

    @Override
    public void onProgressUpdated() {
        if(pg != null)
            pg.setProgress(pg.getProgress() + 1);
    }

    private void setupProgressBar() {
        pg = ((MainActivity)getActivity()).progressBar;
        pg.setVisibility(View.VISIBLE);
        pg.setIndeterminate(true);
    }

    private void setupOkButton() {
        Button okButton = (Button)mView.findViewById(R.id.okbutton);
        okButton.setOnClickListener(this);
    }

    private void setupCancelButton() {
        Button cancelButton = (Button)mView.findViewById(R.id.cancelbutton);
        cancelButton.setOnClickListener(this);
    }

    private void setupTimePicker() {

        Calendar cal = Calendar.getInstance();
        date = new Date(cal.get(Calendar.YEAR) - dateOffset, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        TimeZone tz = Calendar.getInstance().getTimeZone();
        dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        dateFormat.setTimeZone(tz);
        String locDate = dateFormat.format(date);
        city = ((ParkenDD) getActivity().getApplication()).currentCity();
        getActivity().setTitle(city.name() + " - " + locDate);
        loadDate();
        timePicker = (TimePicker)mView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateList(hourOfDay, view);
            }
        });
    }

    private void loadDate(){
        pg.setProgress(0);
        pg.setVisibility(View.VISIBLE);
        City city = ((ParkenDD)getActivity().getApplication()).currentCity();
        ArrayList<ParkingSpot> spots = city.spots();
        ArrayList<ParkingSpot> forecastSpots = new ArrayList<>();
        for(ParkingSpot spot : spots){
            if(spot.forecast()){
                forecastSpots.add(spot);
            }
        }
        spotList = forecastSpots.toArray(new ParkingSpot[forecastSpots.size()]);
        URL[] urlList = new URL[forecastSpots.size()];
        try{
            for(int i = 0; i < forecastSpots.size(); i++){
                urlList[i] = Loader.getForecastUrl(getString(R.string.serveraddress), city, spotList[i], date);
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        pg.setMax(urlList.length + 3);
        pg.setProgress(1);
        pg.setIndeterminate(false);
        forecastLoader = new Loader(this);
        forecastLoader.execute(urlList);
    }

    private void updateList(int hour, View view){
        ArrayList<ParkingSpot> spotList = new ArrayList<>();
        try {
            date.setHours(hour);
            date.setMinutes(0);
            for(Map.Entry<ParkingSpot, Map<Date, Integer>> pair : spotmap.entrySet()){
                ParkingSpot spot = pair.getKey();
                Map<Date, Integer> dataMap = (Map) pair.getValue();
                try {
                    spot.setState("open");
                    double perc = 1 - (double)dataMap.get(date) / 100;
                    double free = (double)spot.count() * perc;
                    spot.setFree((int) free);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    spot.setState("nodata");
                }
                spotList.add(spot);
            }
            setList(spotList, view);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void setList(ArrayList<ParkingSpot> spots, View view){
        ExpandableListView spotView = (ExpandableListView)view.findViewById(R.id.listView);
        String sortOptions[] = getResources().getStringArray(R.array.setting_sort_options);
        String sortPreference = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("sorting", sortOptions[0]);
        Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_closed", true);
        Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_nodata", false);
        Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_full", true);
        final ParkingSpot[] spotArray;
        ParkingSpot[] preArray;
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
        pg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(menu != null)
        {
            menu.clear();
        }
        inflater.inflate(R.menu.menu_forecast, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_datePicker) {
            relativeLayout.setVisibility(View.INVISIBLE);
            datePickerLayout.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.okbutton :
                relativeLayout.setVisibility(View.VISIBLE);
                datePickerLayout.setVisibility(View.INVISIBLE);
                DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
                date = new Date(datePicker.getYear() - dateOffset, datePicker.getMonth(), datePicker.getDayOfMonth());
                loadDate();
                String locDate = dateFormat.format(date);
                getActivity().setTitle(city.name() + " - " + locDate);
                break;

            case R.id.cancelbutton:
                relativeLayout.setVisibility(View.VISIBLE);
                datePickerLayout.setVisibility(View.INVISIBLE);
                break;
        }
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
