package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {
    private static ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery = new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.ScoreEntry.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE = DatabaseContract.ScoreEntry.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID = DatabaseContract.ScoreEntry.MATCH_ID_COL + " = ?";
    private static final String LOG_TAG = ScoresProvider.class.getName();

    static UriMatcher buildUriMatcher() {
        Log.v(LOG_TAG, "buildUriMatcher, " + "");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
        matcher.addURI(authority, null, MATCHES);
        matcher.addURI(authority, "league", MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, "id", MATCHES_WITH_ID);
        matcher.addURI(authority, "date", MATCHES_WITH_DATE);
        return matcher;
    }

    private int matchUri(Uri uri) {
        Log.v(LOG_TAG, "matchUri, " + "uri = [" + uri + "]");
        if (uri != null){
            String link = uri.toString();
            if (link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString())) {
                return MATCHES;
            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithDate().toString())) {
                return MATCHES_WITH_DATE;
            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithId().toString())) {
                return MATCHES_WITH_ID;
            } else if (link.contentEquals(DatabaseContract.ScoreEntry.buildScoreWithLeague().toString())) {
                return MATCHES_WITH_LEAGUE;
            }
        }

        return -1;
    }

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "onCreate, " + "");
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
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
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.ScoreEntry.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.ScoreEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query, " + "uri = [" + uri + "], projection = [" + projection
                + "], selection = [" + selection + "], selectionArgs = [" + selectionArgs
                + "], sortOrder = [" + sortOrder + "]");
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        int match = matchUri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (match) {
            case MATCHES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(LOG_TAG, "insert, " + "uri = [" + uri + "], values = [" + values + "]");
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.v(LOG_TAG, "bulkInsert, " + "uri = [" + uri + "], values = [" + splitValues(values) + "]");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (matchUri(uri)) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private String splitValues(ContentValues[] values) {
        String result = "";

        for (ContentValues value : values) {
            result += value.toString() + ", ";
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "delete, " + "uri = [" + uri + "], selection = [" + selection + "], selectionArgs = [" + selectionArgs + "]");
        return 0;
    }
}
