package barqsoft.footballscores.data.unused;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by frank on 05.10.15.
 * seems to be easier with a separate Provider instead with 2 joins for each match
 */
public class TeamProvider extends ContentProvider {
    private static TeamDbHelper mOpenHelper;
    private static final String TABLE_NAME = "teams";

    static final int TEAM = 200;
    static final int TEAM_WITH_ID = 201;

//    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

//    private static final String TEAM_BY_ID = TeamEntry.COLUMN_TEAM_ID + " = ?";
    private static final String LOG_TAG = TeamProvider.class.getName();

//    static UriMatcher buildUriMatcher() {
//        Log.v(LOG_TAG, "buildUriMatcher, " + "");
//        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
//        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
//        matcher.addURI(authority, DatabaseContract.PATH_TEAM, TEAM);
//        matcher.addURI(authority, DatabaseContract.PATH_TEAM + "/#", TEAM_WITH_ID);
//
//        return matcher;
//    }

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "onCreate, " + "");
        mOpenHelper = new TeamDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.v(LOG_TAG, "query, " + "uri = [" + uri + "], projection = [" + projection + "], selection = [" + selection + "], selectionArgs = [" + selectionArgs + "], sortOrder = [" + sortOrder + "]");
        Cursor retCursor = mOpenHelper.getReadableDatabase().query(
                DatabaseContract.TEAM_TABLE,
                projection,
                selection, selectionArgs,
                null, null, sortOrder
        );

        if (retCursor != null) {
            return retCursor;
        }

        return null;

    }

    @Override
    public String getType(Uri uri) {
        Log.v(LOG_TAG, "getType, " + "uri = [" + uri + "]");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
//        Log.v(LOG_TAG, "insert, " + "uri = [" + uri + "], contentValues = [" + contentValues + "]");
//        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        Uri returnUri;
//
//        long _id = db.insert(TeamEntry.TABLE_NAME, null, contentValues);
//        if (_id > 0) {
//            returnUri = DatabaseContract.TeamEntry.buildTeamUri(_id);
//        } else {
//            throw new android.database.SQLException("Failed to insert row into " + uri);
//        }
//
//        getContext().getContentResolver().notifyChange(uri, null);
//        return returnUri;
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//
//        if (null == selection) selection = "1";
//
//        int deletedRows = db.delete(TeamEntry.TABLE_NAME, selection, selectionArgs);
//        if (deletedRows > 0) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//        return deletedRows;
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        int rows = mOpenHelper.getWritableDatabase().update(TeamEntry.TABLE_NAME,
//                values, selection, selectionArgs);
//
//        if (rows > 0) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//
//        return rows;
        return 0;
    }
}
