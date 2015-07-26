package com.example.conor.dotaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

public class Utility {
    public static String getPreferredNumMatches(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.num_matches_key),
                context.getString(R.string.num_matches_default));
    }

    public static String getSteamAccountId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.steam_id_key),
                context.getString(R.string.steam_id_default));
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        //SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return DateFormat.getDateInstance().format(date);
    }

}
