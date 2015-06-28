package de.jkliemann.parkendd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity{
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //Server s = new Server();
        //s.execute(this);
        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        addPreferencesFromResource(R.xml.pref_container);
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.header_general);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_general);
        ListPreference citylist = (ListPreference)findPreference("city");
        if(GlobalSettings.getGlobalSettings().getCitylist().length > 0) {
            citylist.setEntries(GlobalSettings.getGlobalSettings().getCitylist());
            citylist.setEntryValues(GlobalSettings.getGlobalSettings().getCitylist());
        }else{
            String[] def = new String[1];
            def[0] = getString(R.string.default_city);
            citylist.setEntryValues(def);
            citylist.setEntries(def);
        }
        bindPreferenceSummaryToValue(citylist);
        ListPreference sortList = (ListPreference)findPreference("sorting");
        Resources res = getResources();
        sortList.setEntryValues(res.getStringArray(R.array.setting_sort_options));
        sortList.setEntries(res.getStringArray(R.array.setting_sort_options));
        bindPreferenceSummaryToValue(sortList);
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.header_network);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_network);
        bindResetToDefault(findPreference("reset"));
        EditTextPreference url = (EditTextPreference)findPreference("fetch_url");
        if(url.getText().equals(getString(R.string.old_fetch_url))){
            url.setText(getString(R.string.default_fetch_url));
        }
        bindPreferenceSummaryToValue(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return  Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            preference.setSummary(value.toString());
            return true;
        }
    };

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
                    ListPreference city = (ListPreference)preferenceManager.findPreference("city");
                    city.setValue(context.getString(R.string.default_city));
                    city.setSummary(context.getString(R.string.default_city));
                    EditTextPreference url = (EditTextPreference)preferenceManager.findPreference("fetch_url");
                    url.setText(context.getString(R.string.default_fetch_url));
                    url.setSummary(context.getString(R.string.default_fetch_url));
                    CheckBoxPreference use_location = (CheckBoxPreference)preferenceManager.findPreference("use_location");
                    use_location.setChecked(true);
                    CheckBoxPreference hide_closed = (CheckBoxPreference)preferenceManager.findPreference("hide_closed");
                    hide_closed.setChecked(true);
                    CheckBoxPreference hide_nodata = (CheckBoxPreference)preferenceManager.findPreference("hide_nodata");
                    hide_nodata.setChecked(false);
                    CheckBoxPreference hide_full = (CheckBoxPreference)preferenceManager.findPreference("hide_full");
                    hide_full.setChecked(true);
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
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */

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

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
}
