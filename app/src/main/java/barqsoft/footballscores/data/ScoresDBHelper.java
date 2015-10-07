package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract.ScoreEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;

    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_ID = 5;
    public static final int COL_AWAY_ID = 6;
    public static final int COL_LEAGUE = 7;
    public static final int COL_HOME_GOALS = 8;
    public static final int COL_AWAY_GOALS = 9;
    public static final int COL_ID = 10;
    public static final int COL_MATCHDAY = 11;


    private static final String LOG_TAG = ScoresDBHelper.class.getName();

    public ScoresDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v(LOG_TAG, "ScoresDBHelper, " + "context = [" + context + "]");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "onCreate, " + "db = [" + db + "]");
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + ScoreEntry._ID + " INTEGER PRIMARY KEY,"
                + ScoreEntry.DATE_COL + " TEXT NOT NULL,"
                + ScoreEntry.TIME_COL + " INTEGER NOT NULL,"
                + ScoreEntry.HOME_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_COL + " TEXT NOT NULL,"
                + ScoreEntry.HOME_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_ID_COL + " TEXT NOT NULL,"
                + ScoreEntry.LEAGUE_COL + " INTEGER NOT NULL,"
                + ScoreEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + ScoreEntry.MATCH_ID_COL + " INTEGER NOT NULL,"
                + ScoreEntry.MATCH_DAY_COL + " INTEGER NOT NULL,"
                + " UNIQUE (" + ScoreEntry.MATCH_ID_COL + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(LOG_TAG, "onUpgrade, " + "db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
    }
}
