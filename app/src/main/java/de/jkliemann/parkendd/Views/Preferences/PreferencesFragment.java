package de.jkliemann.parkendd.Views.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jkliemann.parkendd.R;

public class PreferencesFragment extends PreferenceFragment {

    private OnFragmentInteractionListener mListener;

    public PreferencesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSimplePreferencesScreen();
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

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_container);
        addPreferencesFromResource(R.xml.pref_general);
        ListPreference sortList = (ListPreference)findPreference("sorting");
        Resources res = getResources();
        sortList.setEntryValues(res.getStringArray(R.array.setting_sort_options));
        sortList.setEntries(res.getStringArray(R.array.setting_sort_options));
        bindPreferenceSummaryToValue(sortList);
        bindPreferenceWarning(findPreference("active_support"));
        bindResetToDefault(findPreference("reset"));
    }

    private static void bindResetToDefault(Preference preference){
        preference.setOnPreferenceClickListener(setDefault);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindPreferenceWarning(Preference preference){
        preference.setOnPreferenceClickListener(showWarning);
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            preference.setSummary(value.toString());
            return true;
        }
    };

    private static Preference.OnPreferenceClickListener showWarning = new Preference.OnPreferenceClickListener(){

        @Override
        public boolean onPreferenceClick(Preference preference){
            Context context = preference.getContext();
            if(preference instanceof CheckBoxPreference){
                supportWarning(context, preference.getPreferenceManager());
            }
            return true;
        }

        private void supportWarning(final Context context, final PreferenceManager preferenceManager){
            if(!preferenceManager.getSharedPreferences().getBoolean("active_support", true)) {
                AlertDialog.Builder warning = new AlertDialog.Builder(context);
                warning.setMessage(context.getString(R.string.alert_active_support));
                warning.setPositiveButton(context.getString(R.string.positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                warning.create().show();
            }
        }
    };

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

    private static Preference.OnPreferenceClickListener setDefault = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            resetDialog(preference.getContext(), preference.getPreferenceManager());
            return true;
        }

        private void resetDialog(final Context context, final PreferenceManager preferenceManager){
            AlertDialog.Builder resetDialog = new AlertDialog.Builder(context);
            resetDialog.setMessage(context.getString(R.string.alert_reset));
            resetDialog.setPositiveButton(context.getString(R.string.positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CheckBoxPreference hide_closed = (CheckBoxPreference) preferenceManager.findPreference("hide_closed");
                    hide_closed.setChecked(true);
                    CheckBoxPreference hide_nodata = (CheckBoxPreference) preferenceManager.findPreference("hide_nodata");
                    hide_nodata.setChecked(false);
                    CheckBoxPreference hide_full = (CheckBoxPreference) preferenceManager.findPreference("hide_full");
                    hide_full.setChecked(true);
                    CheckBoxPreference active_support = (CheckBoxPreference) preferenceManager.findPreference("active_support");
                    active_support.setChecked(true);
                }
            });
            resetDialog.setNegativeButton(context.getString(R.string.negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            resetDialog.create().show();
        }
    };
}
