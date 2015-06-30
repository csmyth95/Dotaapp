package com.example.conor.dotaapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link com.example.conor.dotaapp.MatchAdapter} exposes a list of match details
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class MatchAdapter extends CursorAdapter {
    public MatchAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String formatIds(String mId, String pId) {
        String formattedIds = "Match ID: "+mId+", Player ID: "+pId;
        return formattedIds;
    }

    /*
        This is ported from FetchMatchTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String ids = formatIds(
                cursor.getString(MatchFragment.COL_MATCH_KEY),
                cursor.getString(MatchFragment.COL_STEAM_ID));

        return Utility.formatDate(MatchFragment.COL_START_TIME)+
                "\t"+ids;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_match, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
