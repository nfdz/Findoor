package io.github.nfdz.findoordemoapp.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public final static String LAST_LOCATION = "last_location";
    public final static int LAST_LOCATION_DEFAULT = 0;

    public static int getLastLocationCache(Context context) {
        return getSharedPreferences(context).getInt(LAST_LOCATION, LAST_LOCATION_DEFAULT);
    }

    public static void setLastLocationCache(Context context, int location) {
        getSharedPreferences(context).edit().putInt(LAST_LOCATION, location).apply();
    }

    private static final String LOCATION_ALIAS_PREFERENCES = "location_alias_prefs";

    public static void setLocationAlias(Context context, int location, String alias) {
        SharedPreferences prefs = context.getSharedPreferences(LOCATION_ALIAS_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(String.valueOf(location), alias == null ? "" : alias).apply();
    }

    public static String getLocationAlias(Context context, int location) {
        SharedPreferences prefs = context.getSharedPreferences(LOCATION_ALIAS_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(String.valueOf(location), "");
    }

    public static void clearLocationAlias(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LOCATION_ALIAS_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

}
