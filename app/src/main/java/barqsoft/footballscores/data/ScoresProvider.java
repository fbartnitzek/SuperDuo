package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.Map;

import barqsoft.footballscores.data.DatabaseContract.*;


/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {

    private static DatabaseHelper mOpenHelper;
    private static final String LOG_TAG = ScoresProvider.class.getName();

    //    private static final String SCORES_BY_LEAGUE = ScoreEntry.LEAGUE_COL + " = ?";
//    private static final String SCORES_BY_DATE = ScoreEntry.DATE_COL + " LIKE ?";
//    private static final String SCORES_BY_ID = ScoreEntry.MATCH_ID_COL + " = ?";

    private static final int MATCHES = 100;
    //    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int TEAMS = 200;
    private static final int MATCHES_AND_TEAMS = 300;
    private static final int MATCHES_AND_TEAMS_WITH_ID = 301;
    private static final int MATCHES_AND_TEAMS_WITH_DATE = 303;

    private UriMatcher mUriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {
        Log.v(LOG_TAG, "buildUriMatcher, " + "");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        // ALL MATCHES
        matcher.addURI(authority, DatabaseContract.PATH_SCORE, MATCHES);
        // ALL MATCH WITH DATE
        matcher.addURI(authority, DatabaseContract.PATH_SCORE + "/*", MATCHES_WITH_DATE);
        // MATCH WITH ID
        //TODO
        // TEAMS
        matcher.addURI(authority, DatabaseContract.PATH_TEAM, TEAMS);

        // ALL MATCHES WITH TEAMS
        matcher.addURI(authority, DatabaseContract.PATH_SCORE_WITH_TEAMS, MATCHES_AND_TEAMS);
        // MATCH WITH TEAMS BY ID
        matcher.addURI(authority, DatabaseContract.PATH_SCORE_WITH_TEAMS + "/#",
                MATCHES_AND_TEAMS_WITH_ID);
        // MATCH WITH TEAMS BY DATE
        matcher.addURI(authority, DatabaseContract.PATH_SCORE_WITH_TEAMS + "/*",
                MATCHES_AND_TEAMS_WITH_DATE);

        return matcher;
    }

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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int impactedRows;

        switch (match) {
            case MATCHES: {
                impactedRows = db.update(ScoreEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            }

            case TEAMS: {
                impactedRows = db.update(TeamEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (impactedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return impactedRows;
    }

    @Override
    public String getType(Uri uri) {
        Log.v(LOG_TAG, "getType, " + "uri = [" + uri + "]");
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return ScoreEntry.CONTENT_TYPE;
            case TEAMS:
                return TeamEntry.CONTENT_TYPE;
//            case MATCHES_WITH_LEAGUE:
//                return ScoreEntry.CONTENT_TYPE;
//            case MATCHES_WITH_ID:
//                return ScoreEntry.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return ScoreEntry.CONTENT_TYPE;
            case MATCHES_AND_TEAMS:
                return ScoreEntry.CONTENT_TYPE;
            case MATCHES_AND_TEAMS_WITH_DATE:
                return ScoreEntry.CONTENT_TYPE;
            case MATCHES_AND_TEAMS_WITH_ID:
                return ScoreEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    private static final SQLiteQueryBuilder sMatchesWithTeamsQueryBuilder;

    static {
        sMatchesWithTeamsQueryBuilder = new SQLiteQueryBuilder();

        // 2 inner joins with aliases
        sMatchesWithTeamsQueryBuilder.setTables(
//                ScoreEntry.TABLE_NAME
//                        + " INNER JOIN " + TeamEntry.TABLE_NAME +
//                        " ON " + ScoreEntry.TABLE_NAME + "." + ScoreEntry.HOME_ID_COL + " = " + TeamEntry.TABLE_NAME + "." + TeamEntry.TEAM_ID_COL
//                        + " INNER JOIN " + TeamEntry.TABLE_NAME +
//                        " ON " + ScoreEntry.TABLE_NAME + "." + ScoreEntry.AWAY_ID_COL + " = " + TeamEntry.TABLE_NAME + "." + TeamEntry.TEAM_ID_COL);
                ScoreEntry.TABLE_NAME
                        + " INNER JOIN " + TeamEntry.TABLE_NAME + TeamEntry.ALIAS_HOME +
                        " ON " + ScoreEntry.TABLE_NAME + "." + ScoreEntry.HOME_ID_COL + " = " + TeamEntry.ALIAS_HOME + "." + TeamEntry.TEAM_ID_COL
                        + " INNER JOIN " + TeamEntry.TABLE_NAME + TeamEntry.ALIAS_AWAY +
                        " ON " + ScoreEntry.TABLE_NAME + "." + ScoreEntry.AWAY_ID_COL + " = " + TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_ID_COL);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query, " + "uri = [" + uri + "], projection = [" + splitValues(projection)
                + "], selection = [" + selection + "], selectionArgs = [" + splitValues(selectionArgs)
                + "], sortOrder = [" + sortOrder + "]");

        Cursor cursor;
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = mUriMatcher.match(uri);
        String date = null;
        String[] whereArg = null;
        switch (match) {
            case MATCHES:
                cursor = db.query(ScoreEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                date = uri.getPathSegments().get(1);
                whereArg = new String[]{date};
                cursor = db.query(ScoreEntry.TABLE_NAME,
                        projection,
                        ScoreEntry.DATE_COL + " LIKE ?",
                        whereArg,
                        null,
                        null,
                        sortOrder);
                break;
            case TEAMS:
                cursor = db.query(TeamEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case MATCHES_AND_TEAMS:
//                cursor = getMatchesWithTeamsQuery(uri, projection, sortOrder);
                cursor = sMatchesWithTeamsQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MATCHES_AND_TEAMS_WITH_ID:
                String id  = uri.getPathSegments().get(1);
                whereArg = new String[]{id};
                cursor = sMatchesWithTeamsQueryBuilder.query(db,
                        projection,
                        ScoreEntry.MATCH_ID_COL + " LIKE ?",
                        whereArg,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCHES_AND_TEAMS_WITH_DATE:
                date = uri.getPathSegments().get(1);
                whereArg = new String[]{date};
                cursor = sMatchesWithTeamsQueryBuilder.query(db,
                        projection,
                        ScoreEntry.DATE_COL + " LIKE ?",
                        whereArg,
                        null,
                        null,
                        sortOrder);
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
                long _id = db.insert(ScoreEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ScoreEntry.buildScoreUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case TEAMS: {
                long _id = db.insert(TeamEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TeamEntry.buildTeamUri(_id);
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
//        Log.v(LOG_TAG, "bulkInsert, " + "match: " + match + ", " +
//                "uri = [" + uri + "], values = [" + splitValues(values) + "]");
        int returnCount = 0;
        switch (match) {
            case MATCHES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
//                        Log.v(LOG_TAG, "bulkInsert, values: " + printCV(value));
//                        long _id = db.insert(
//                                ScoreEntry.TABLE_NAME,
//                                null,
//                                value);
                        long _id = db.insertWithOnConflict(
                                ScoreEntry.TABLE_NAME,
                                null,
                                value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
//                    Log.v(LOG_TAG, "bulkInsert, successful ");
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case TEAMS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(
                                TeamEntry.TABLE_NAME,
                                null,
                                value,
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

    private String printCV(ContentValues contentValues) {
        String result = "";
        for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
            result += entry.getKey() + "=" + entry.getValue() + ",";
        }
        return result;
    }

    private String splitValues(Object[] values) {
        String result = "";
        if (values != null) {
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
                deletedRows = db.delete(ScoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TEAMS: {
                deletedRows = db.delete(TeamEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }
}
