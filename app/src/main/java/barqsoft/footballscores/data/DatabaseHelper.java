package barqsoft.footballscores.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract.ScoreEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 3;

    private static final String LOG_TAG = DatabaseHelper.class.getName();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v(LOG_TAG, "DatabaseHelper, " + "context = [" + context + "]");
    }

    public static final int COL_MATCH_DATE = 1;
    public static final int COL_MATCH_TIME = 2;
    public static final int COL_MATCH_HOME_ID = 3;
    public static final int COL_MATCH_HOME = 4;
    public static final int COL_MATCH_AWAY_ID = 5;
    public static final int COL_MATCH_AWAY = 6;
    public static final int COL_MATCH_LEAGUE = 7;
    public static final int COL_HOME_GOALS = 8;
    public static final int COL_AWAY_GOALS = 9;
    public static final int COL_MATCH_ID = 10;
    public static final int COL_MATCH_DAY = 11;

    public static final int COL_TEAM_ID = 1;
    public static final int COL_TEAM_NAME = 2;
    public static final int COL_TEAM_ICON = 3;

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "onCreate, " + "db = [" + db + "]");

        //Matches
        final String createScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + ScoreEntry._ID + " INTEGER PRIMARY KEY,"

//                + ScoreEntry.DATE_COL + " TEXT NOT NULL,"
//                + ScoreEntry.TIME_COL + " INTEGER NOT NULL,"
//                + ScoreEntry.HOME_ID_COL + " TEXT NOT NULL,"
//                + ScoreEntry.HOME_COL + " TEXT NOT NULL,"
//                + ScoreEntry.AWAY_ID_COL + " TEXT NOT NULL,"
//                + ScoreEntry.AWAY_COL + " TEXT NOT NULL,"
//                + ScoreEntry.LEAGUE_COL + " INTEGER NOT NULL,"
//                + ScoreEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
//                + ScoreEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
//                + ScoreEntry.MATCH_ID_COL + " INTEGER NOT NULL,"
//                + ScoreEntry.MATCH_DAY_COL + " INTEGER NOT NULL,"
//                + " UNIQUE (" + ScoreEntry.MATCH_ID_COL + ") ON CONFLICT REPLACE"
//                + " );";

        + ScoreEntry.DATE_COL + " TEXT NOT NULL,"
                + ScoreEntry.TIME_COL + " TEXT NOT NULL,"
                + ScoreEntry.HOME_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.HOME_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_COL + " TEXT NOT NULL,"
                + ScoreEntry.LEAGUE_COL + " TEXT NOT NULL,"
                + ScoreEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.MATCH_ID_COL + " INTEGER NOT NULL,"
                + ScoreEntry.MATCH_DAY_COL + " INTEGER NOT NULL,"
                + " UNIQUE (" + ScoreEntry.MATCH_ID_COL + ") ON CONFLICT REPLACE"
                + " );";


//        //Teams
//        final String createTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
//                + DatabaseContract.TeamEntry._ID + " INTEGER PRIMARY KEY,"
//                + DatabaseContract.TeamEntry.TEAM_ID_COL+ " TEXT NOT NULL,"
//                + DatabaseContract.TeamEntry.TEAM_NAME_COL+ " INTEGER NOT NULL,"
//                + DatabaseContract.TeamEntry.TEAM_ICON_COL+ " TEXT NOT NULL,"
//                + " UNIQUE (" + DatabaseContract.TeamEntry.TEAM_ID_COL + ") ON CONFLICT REPLACE"
//                + " );";
//
//
//        db.execSQL(createTeamsTable);
        db.execSQL(createScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(LOG_TAG, "onUpgrade, " + "db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
        onCreate(db);
    }


//    public static ContentValues buildTeamCVs(String id, String name, String url) {
//        ContentValues teamValues = new ContentValues();
//        teamValues.put(DatabaseContract.TeamEntry.TEAM_ID_COL, id);
//        teamValues.put(DatabaseContract.TeamEntry.TEAM_NAME_COL, name);
//        teamValues.put(DatabaseContract.TeamEntry.TEAM_ICON_COL, url);
//        return teamValues;
//    }

    public static ContentValues buildMatchCVs(String date, String time, String homeId, String homeName,
                                              String awayId, String awayName, String league, String homeGoals,
                                              String awayGoals, String matchId, String matchDay) {
        ContentValues matchValues = new ContentValues();
        matchValues.put(DatabaseContract.ScoreEntry.DATE_COL, date);
        matchValues.put(DatabaseContract.ScoreEntry.TIME_COL, time);
        matchValues.put(DatabaseContract.ScoreEntry.HOME_ID_COL, homeId);
        matchValues.put(DatabaseContract.ScoreEntry.HOME_COL, homeName);
        matchValues.put(DatabaseContract.ScoreEntry.HOME_GOALS_COL, homeGoals);
        matchValues.put(DatabaseContract.ScoreEntry.AWAY_ID_COL, awayId);
        matchValues.put(DatabaseContract.ScoreEntry.AWAY_COL, awayName);
        matchValues.put(DatabaseContract.ScoreEntry.AWAY_GOALS_COL, awayGoals);
        matchValues.put(DatabaseContract.ScoreEntry.LEAGUE_COL, league);
        matchValues.put(DatabaseContract.ScoreEntry.MATCH_ID_COL, matchId);
        matchValues.put(DatabaseContract.ScoreEntry.MATCH_DAY_COL, matchDay);
        return matchValues;
    }


}
