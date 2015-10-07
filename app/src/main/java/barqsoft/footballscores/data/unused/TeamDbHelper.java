package barqsoft.footballscores.data.unused;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by frank on 05.10.15.
 */
public class TeamDbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "Teams.db";
    private static final int DATABASE_VERSION = 1;
    private static final String LOG_TAG = TeamDbHelper.class.getName();

    public TeamDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.v(LOG_TAG, "onCreate, " + "sqLiteDatabase = [" + sqLiteDatabase + "]");
//        final String SQL_CREATE_TEAM_TABLE =
//                "CREATE TABLE " + DatabaseContract.TeamEntry.TABLE_NAME + "("
//                + DatabaseContract.TeamEntry._ID + " INTEGER PRIMARY KEY, "
//                + DatabaseContract.TeamEntry.COLUMN_TEAM_ID + " TEXT UNIQUE NOT NULL, "
//                + DatabaseContract.TeamEntry.COLUMN_TEAM_NAME + " TEXT NOT NULL, "
//                + DatabaseContract.TeamEntry.COLUMN_TEAM_ICON + " TEXT NOT NULL "
//                + "UNIQUE (" + DatabaseContract.TeamEntry.COLUMN_TEAM_ID + ") ON CONFLICT REPLACE);";
//
//        sqLiteDatabase.execSQL(SQL_CREATE_TEAM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // just for caching online content, on upgrades discard old version
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TeamEntry.TABLE_NAME);
//        onCreate(sqLiteDatabase);
    }
}
