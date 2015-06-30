/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.conor.dotaapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.conor.dotaapp.data.MatchContract.MatchEntry;
import com.example.conor.dotaapp.data.MatchContract.PlayerEntry;

/**
 * Manages a local database for weather data.
 */
public class MatchDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "weather.db";

    public MatchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        //Create a table to hold hero data e.g heo_id and localized_name
//        final String SQL_CREATE_HERO_TABLE = "CREATE TABLE " + PlayerEntry.TABLE_NAME + " (" +
//                HeroEntry._ID + " INTEGER PRIMARY KEY," +
//                HeroEntry.COLUMN_HERO_ID + " INTEGER NOT NULL, " +
//                HeroEntry.COLUMN_LOCAL_NAME + " TEXT NOT NULL, " +
//                " );";

        // Create a table to hold player data.  A player object consists of a steam account_id,
        // and a hero_id
        final String SQL_CREATE_PLAYER_TABLE = "CREATE TABLE " + PlayerEntry.TABLE_NAME + " (" +
                PlayerEntry._ID + " INTEGER PRIMARY KEY," +
                PlayerEntry.COLUMN_ACCOUNT_ID + " TEXT NOT NULL, " +
                PlayerEntry.COLUMN_HERO_ID + " TEXT NOT NULL, " +
                PlayerEntry.COLUMN_P_SLOT + " INTEGER NOT NULL " +
                ");";
                //                // Set up the account id column as a foreign key to player table.
//                " FOREIGN KEY (" + PlayerEntry.COLUMN_HERO_ID + ") REFERENCES " +
//                HeroEntry.TABLE_NAME + " (" + HeroEntry.COLUMN_HERO_ID + "), " +

        final String SQL_CREATE_MATCH_TABLE = "CREATE TABLE " + MatchEntry.TABLE_NAME + " (" +
                // Unique keys will be auto-generated .  But for DOTA 2 matches,
                // it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the match data
                // should be sorted accordingly.
                MatchEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the match entry associated with this weather data
                MatchEntry.COLUMN_MATCH_KEY + " TEXT NOT NULL, " +
                MatchEntry.COLUMN_START_TIME + " INTEGER NOT NULL, " +
                MatchEntry.COLUMN_P_ACCOUNT_ID + " TEXT NOT NULL, " +
                // Set up the account id column as a foreign key to player table.
                " FOREIGN KEY (" + MatchEntry.COLUMN_P_ACCOUNT_ID + ") REFERENCES " +
                PlayerEntry.TABLE_NAME + " (" + PlayerEntry.COLUMN_ACCOUNT_ID + "), " +

                // To assure the application have just one match entry per day
                // per player, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + MatchEntry.COLUMN_START_TIME + ", " +
                MatchEntry.COLUMN_MATCH_KEY + ") ON CONFLICT REPLACE);";

//        sqLiteDatabase.execSQL(SQL_CREATE_HERO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MATCH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeroEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlayerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
