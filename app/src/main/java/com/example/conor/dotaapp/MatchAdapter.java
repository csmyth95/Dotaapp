package com.example.conor.dotaapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        int matchId = cursor.getInt(MatchFragment.COL_MATCH_KEY);
        TextView matchIdView = (TextView) view.findViewById(R.id.list_item_matchId_textView);
        matchIdView.setText(matchId);

        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.mipmap.ic_launcher);

        //read data from cursor
        int dateinUTC = cursor.getInt(MatchFragment.COL_START_TIME);
        //find textview
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textView);
        dateView.setText(Utility.formatDate(dateinUTC));

        //read match data from cursor
        String playerHero = cursor.getString(MatchFragment.COL_HERO_ID);
        //find textview and set match data to it
        TextView matchView = (TextView) view.findViewById(R.id.list_item_heroId_textView);

        matchView.setText(playerHero);
    }
}
