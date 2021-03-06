package barqsoft.footballscores.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract.*;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FootballScores.db";
    private static final int DATABASE_VERSION = 4;

    private static final String LOG_TAG = DatabaseHelper.class.getName();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v(LOG_TAG, "DatabaseHelper, " + "context = [" + context + "]");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "onCreate, " + "db = [" + db + "]");

        //Matches
        final String createScoresTable = "CREATE TABLE " + ScoreEntry.TABLE_NAME + " ("
                + ScoreEntry._ID + " INTEGER PRIMARY KEY,"

                + ScoreEntry.DATE_COL + " TEXT NOT NULL,"
                + ScoreEntry.TIME_COL + " INTEGER NOT NULL," //+ " TEXT NOT NULL,"
                + ScoreEntry.HOME_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.LEAGUE_COL + " INTEGER NOT NULL," //+ " TEXT NOT NULL,"
                + ScoreEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.MATCH_ID_COL + " INTEGER NOT NULL,"
                + ScoreEntry.MATCH_DAY_COL + " INTEGER NOT NULL,"

                // foreign key home
                + "FOREIGN KEY (" + ScoreEntry.HOME_ID_COL +  ") REFERENCES "
                + TeamEntry.TABLE_NAME + " (" + TeamEntry.TEAM_ID_COL + "), "

                // foreign key home
                + "FOREIGN KEY (" + ScoreEntry.AWAY_ID_COL +  ") REFERENCES "
                + TeamEntry.TABLE_NAME + " (" + TeamEntry.TEAM_ID_COL + "), "

                + " UNIQUE (" + ScoreEntry.MATCH_ID_COL + ") ON CONFLICT REPLACE"
                + " );";


        //Teams
        final String createTeamsTable = "CREATE TABLE " + TeamEntry.TABLE_NAME + " ("
                + TeamEntry._ID + " INTEGER PRIMARY KEY,"
                + TeamEntry.TEAM_ID_COL+ " TEXT NOT NULL,"
                + TeamEntry.TEAM_NAME_COL+ " TEXT NOT NULL,"
                + TeamEntry.TEAM_ICON_COL+ " TEXT NOT NULL,"
                + " UNIQUE (" + TeamEntry.TEAM_ID_COL + ") ON CONFLICT REPLACE"
                + " );";


        db.execSQL(createTeamsTable);
        db.execSQL(createScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(LOG_TAG, "onUpgrade, " + "db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + ScoreEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TeamEntry.TABLE_NAME);
        onCreate(db);
    }


    public static ContentValues buildTeamCVs(String id, String name, String url) {
        ContentValues teamValues = new ContentValues();
        teamValues.put(TeamEntry.TEAM_ID_COL, id);
        teamValues.put(TeamEntry.TEAM_NAME_COL, name);
        teamValues.put(TeamEntry.TEAM_ICON_COL, url);
        return teamValues;
    }

    public static ContentValues buildMatchCVs(String date, String time, String homeId,
                                              String awayId, String league, String homeGoals,
                                              String awayGoals, String matchId, String matchDay) {
        ContentValues matchValues = new ContentValues();
        matchValues.put(ScoreEntry.DATE_COL, date);
        matchValues.put(ScoreEntry.TIME_COL, time);
        matchValues.put(ScoreEntry.HOME_ID_COL, homeId);
        matchValues.put(ScoreEntry.HOME_GOALS_COL, homeGoals);
        matchValues.put(ScoreEntry.AWAY_ID_COL, awayId);
        matchValues.put(ScoreEntry.AWAY_GOALS_COL, awayGoals);
        matchValues.put(ScoreEntry.LEAGUE_COL, league);
        matchValues.put(ScoreEntry.MATCH_ID_COL, matchId);
        matchValues.put(ScoreEntry.MATCH_DAY_COL, matchDay);
        return matchValues;
    }

}
