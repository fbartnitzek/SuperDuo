package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.Map;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {

    private static DatabaseHelper mOpenHelper;
    private static final String LOG_TAG = ScoresProvider.class.getName();

    //    private static final String SCORES_BY_LEAGUE = DatabaseContract.ScoreEntry.LEAGUE_COL + " = ?";
//    private static final String SCORES_BY_DATE = DatabaseContract.ScoreEntry.DATE_COL + " LIKE ?";
//    private static final String SCORES_BY_ID = DatabaseContract.ScoreEntry.MATCH_ID_COL + " = ?";

    private static final int MATCHES = 100;
//    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int TEAMS = 200;
    private static final int MATCHES_AND_TEAMS= 300;

    private UriMatcher mUriMatcher = buildUriMatcher();

//    private static final SQLiteQueryBuilder ScoreQuery = new SQLiteQueryBuilder();
////barqsoft.footballscores/date
    static UriMatcher buildUriMatcher() {
        Log.v(LOG_TAG, "buildUriMatcher, " + "");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

////        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
//        matcher.addURI(authority, null, MATCHES);
//        matcher.addURI(authority, "league", MATCHES_WITH_LEAGUE);
//        matcher.addURI(authority, "id", MATCHES_WITH_ID);
//        matcher.addURI(authority, "date", MATCHES_WITH_DATE);
//        matcher.addURI(authority, "teams", TEAMS);
//        matcher.addURI(authority, "matches_teams", MATCHES_AND_TEAMS);

        // ALL MATCHES
        matcher.addURI(authority, DatabaseContract.PATH_SCORE, MATCHES);
        // ALL MATCH WITH DATE
        matcher.addURI(authority, DatabaseContract.PATH_SCORE + "/*", MATCHES_WITH_DATE);
        // MATCH WITH ID
        //TODO
        // TEAMS
//        matcher.addURI(authority, DatabaseContract.PATH_TEAM, TEAMS); //TODO
        // MATCH WITH TEAMS
//        matcher.addURI(authority, DatabaseContract.PATH_SCORE_WITH_TEAMS, MATCHES_AND_TEAMS); //TODO: create

        return matcher;
    }

//    private int matchUri(Uri uri) {
//        Log.v(LOG_TAG, "matchUri, " + "uri = [" + uri + "]");
//        if (uri != null){
//            String link = uri.toString();
//            if (link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString())) {
//                return MATCHES;
//            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithDate().toString())) {
//                return MATCHES_WITH_DATE;
//            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithId().toString())) {
//                return MATCHES_WITH_ID;
//            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithLeague().toString())) {
//                return MATCHES_WITH_LEAGUE;
//            }
//        }
//
//        return -1;
//    }

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "onCreate, " + "");
        mOpenHelper = new DatabaseHelper(getContext());
        return true; //successfully loaded
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "update, " + "uri = [" + uri + "], values = [" + values + "], selection = ["
                + selection + "], selectionArgs = [" + selectionArgs + "]");
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        Log.v(LOG_TAG, "getType, " + "uri = [" + uri + "]");
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
//            case MATCHES_WITH_LEAGUE:
//                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
//            case MATCHES_WITH_ID:
//                return DatabaseContract.ScoreEntry.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query, " + "uri = [" + uri + "], projection = [" + splitValues(projection)
                + "], selection = [" + selection + "], selectionArgs = [" + splitValues(selectionArgs)
                + "], sortOrder = [" + sortOrder + "]");

        Cursor cursor;
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
//        int match = matchUri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));

        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                cursor = db.query( DatabaseContract.SCORES_TABLE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                //    private static final String SCORES_BY_DATE = DatabaseContract.ScoreEntry.DATE_COL + " LIKE ?";
//                cursor = db.query( DatabaseContract.SCORES_TABLE,
//                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);
//                cursor = getMatchesByDate(uri, projection, sortOrder);
                String date = uri.getPathSegments().get(1);
                String[] dateArg = new String[]{date};
                Log.v(LOG_TAG, "query, " + "MATCHES_WITH_DATE, date: " + splitValues(dateArg));
                cursor = db.query(DatabaseContract.SCORES_TABLE,
                        projection,
                        DatabaseContract.ScoreEntry.DATE_COL + " LIKE ?",
                        dateArg,
                        null,
                        null,
                        sortOrder);
                Log.v(LOG_TAG, "query, " + "MATCHES_WITH_DATE, date: " + splitValues(dateArg) + ", result: " + cursor.getCount());

                break;
//            case MATCHES_WITH_ID:
//                cursor = db.query( DatabaseContract.SCORES_TABLE,
//                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
//                break;
//            case MATCHES_WITH_LEAGUE:
//                cursor = db.query( DatabaseContract.SCORES_TABLE,
//                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
//                break;
            default:    //Caused by: Unknown Uricontent://barqsoft.footballscores/date
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(LOG_TAG, "insert, " + "uri = [" + uri + "], values = [" + values + "]");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case MATCHES: {
                long _id = db.insert(
                        DatabaseContract.SCORES_TABLE,
                        null,
                        values);
                if (_id > 0) {
                    returnUri = DatabaseContract.ScoreEntry.buildScoreUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(mUriMatcher.match(uri)));
        final int match = mUriMatcher.match(uri);
        Log.v(LOG_TAG, "bulkInsert, " + "match: " + match + ", " +
                "uri = [" + uri + "], values = [" + splitValues(values) + "]");
        switch (match) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        Log.v(LOG_TAG, "bulkInsert, values: " + printCV(value));
                        long _id = db.insert(
                                DatabaseContract.SCORES_TABLE,
                                null,
                                value);
//                        long _id = db.insertWithOnConflict(
//                                DatabaseContract.SCORES_TABLE,
//                                null,
//                                value,
//                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    Log.v(LOG_TAG, "bulkInsert, successful ");
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.v(LOG_TAG, "bulkInsert, returning ...");
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private String printCV(ContentValues contentValues) {
        String result = "";
        for (Map.Entry<String, Object> entry : contentValues.valueSet()){
            result += entry.getKey() + "=" + entry.getValue() + ",";
        }
        return result;
    }

    private String splitValues(Object[] values) {
        String result = "";
        if (values!=null){
            for (Object value : values) {
                result += value.toString() + ", ";
            }
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "delete, " + "uri = [" + uri + "], selection = [" + selection + "], selectionArgs = [" + selectionArgs + "]");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int deletedRows;

        // this makes delete all rows return the number of rows deleted (see spec of db.delete)
        if (null == selection) selection = "1";

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        switch (match) {
            case MATCHES: {
                deletedRows = db.delete(DatabaseContract.SCORES_TABLE, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRows>0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Student: return the actual rows deleted
        return deletedRows;
    }
}
