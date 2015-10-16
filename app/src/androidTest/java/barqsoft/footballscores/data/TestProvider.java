package barqsoft.footballscores.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log; 
import barqsoft.footballscores.data.DatabaseContract.*;

/**
 * Created by frank on 15.10.15.
 */
public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(ScoreEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(TeamEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                ScoreEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Records not deleted from Score table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                TeamEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Records not deleted from Team table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecordsFromDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(ScoreEntry.TABLE_NAME, null, null);
        db.delete(TeamEntry.TABLE_NAME, null, null);
        db.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromDB();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                ScoresProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: ScoresProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DatabaseContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DatabaseContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: ScoresProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testBasicScoreQuery() {
        // insert our test records into the database
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // test teams
        ContentValues testValuesTeam = TestUtils.createHomeTeamValues();
        long teamRowId = db.insert(TeamEntry.TABLE_NAME, null, testValuesTeam);
        assertTrue(teamRowId > 0);
        db.close();

        // Test the basic content provider query
        Cursor teamCursor = mContext.getContentResolver().query(
                TeamEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
//        TestUtils.validateCursor("testBasicTeamQuery", teamCursor, testValuesTeam);

        // test scores
        db = dbHelper.getWritableDatabase();
        ContentValues testValuesScores = TestUtils.createScoreValues();
        long scoreRowId = db.insert(ScoreEntry.TABLE_NAME, null, testValuesScores);
        assertTrue(scoreRowId > 0);
        db.close();

        // Test the basic content provider query
        Cursor scoreCursor = mContext.getContentResolver().query(
                ScoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertTrue(scoreCursor.getCount()>0);
        // Make sure we get the correct cursor out of the database
//        TestUtils.validateCursor("testBasicScoreQuery", scoreCursor, testValuesScores);
    }


    public void testInsertReadProvider() {

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtils.TestContentObserver tco = TestUtils.getTestContentObserver();


        // team data
        ContentValues teamValues = TestUtils.createHomeTeamValues();

        mContext.getContentResolver().registerContentObserver(
                TeamEntry.CONTENT_URI, true, tco);

        Uri teamInsertUri = mContext.getContentResolver().insert(
                TeamEntry.CONTENT_URI, teamValues);
        assertTrue(teamInsertUri != null);

        teamInsertUri = mContext.getContentResolver().insert(
                TeamEntry.CONTENT_URI, TestUtils.createAwayTeamValues()
        );
        assertTrue(teamInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor teamCursor = mContext.getContentResolver().query(
                TeamEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        assertTrue("missing teams after insert (just " + teamCursor.getCount() + ")",
                teamCursor.getCount() == 2);

        TestUtils.printAllCursorEntries(teamCursor, " 2 teams should be inserted");
        teamCursor.close();

//        TestUtils.validateCursor("testInsertReadProvider. Error validating TeamEntry insert.",
//                teamCursor, teamValues);



        // score data
        ContentValues scoreValues = TestUtils.createScoreValues();

        tco = TestUtils.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(
                ScoreEntry.CONTENT_URI, true, tco);

        Uri scoreInsertUri = mContext.getContentResolver().insert(
                ScoreEntry.CONTENT_URI, scoreValues);
        assertTrue(scoreInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Log.v(LOG_TAG, "testInsertReadProvider, " + " before query");
        // A cursor is your primary interface to the query results.
        Cursor scoreCursor = mContext.getContentResolver().query(
                ScoreEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        assertTrue("empty after insert", scoreCursor.getCount() > 0);


        TestUtils.printAllCursorEntries(scoreCursor, "1 match should be inserted");
        scoreCursor.close();

//        TestUtils.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
//                scoreCursor, scoreValues);


        // test joins
        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
//        scoreValues.putAll(teamValues);


        //TODO

        // insert second game (on same date)
        ContentValues scoreValues2 = TestUtils.createSecondScoreValues();

        tco = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ScoreEntry.CONTENT_URI, true, tco);

        Uri scoreInsertUri2 = mContext.getContentResolver().insert(
                ScoreEntry.CONTENT_URI, scoreValues2);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
        assertTrue(scoreInsertUri2 != null);

        // get all
         scoreCursor = mContext.getContentResolver().query(
                ScoreEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtils.printAllCursorEntries(scoreCursor, "get all score-entries");
        assertTrue(scoreCursor.getCount() == 2);
        scoreCursor.close();

        // Get the joined match and team data by id
        scoreCursor = mContext.getContentResolver().query(
                ScoreEntry.buildScoreAndTeamsUri(TestUtils.getMatchId()),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtils.printAllCursorEntries(scoreCursor, "joined match by id with 2 teams should work");
        assertTrue(scoreCursor.getCount() == 1);
        scoreCursor.close();

        // Get the joined match and team data by date
        scoreCursor = mContext.getContentResolver().query(
                ScoreEntry.buildScoreAndTeamsUri(TestUtils.getMatchDate()),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtils.printAllCursorEntries(scoreCursor, "joined match by date with 2 teams should work");
        assertTrue(scoreCursor.getCount() == 2);
        scoreCursor.close();

    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our team delete.
        TestUtils.TestContentObserver teamObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                TeamEntry.CONTENT_URI, true, teamObserver);

        // Register a content observer for our score delete.
        TestUtils.TestContentObserver scoreObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ScoreEntry.CONTENT_URI, true, scoreObserver);

        deleteAllRecordsFromProvider();

        teamObserver.waitForNotificationOrFail();
        scoreObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(teamObserver);
        mContext.getContentResolver().unregisterContentObserver(scoreObserver);
    }

    public void testBulkInsert() {

        // team
        ContentValues[] bulkInsertContentValues = createBulkInsertTeamValues();

        // Register a content observer for our bulk insert.
        TestUtils.TestContentObserver weatherObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                TeamEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(
                TeamEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                TeamEntry.CONTENT_URI, null, null, null, null);

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
//            TestUtils.validateCurrentRecord("testBulkInsert.  Error validating TeamEntry " + i,
//                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();


        // score
        bulkInsertContentValues = createBulkInsertScoreValues();

        // Register a content observer for our bulk insert.
        weatherObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                ScoreEntry.CONTENT_URI, true, weatherObserver);

        insertCount = mContext.getContentResolver().bulkInsert(
                ScoreEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                ScoreEntry.CONTENT_URI, null, null, null, null);

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
//        cursor.moveToFirst();
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
////            TestUtils.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
////                    cursor, bulkInsertContentValues[i]);
//        }
        cursor.close();
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    private ContentValues[] createBulkInsertTeamValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            returnContentValues[i] = DatabaseHelper.buildTeamCVs(
                    "25" + i,
                    "CD Tenerife" + i,
                    "http://upload.wikimedia.org/wikipedia/de/f/f4/CD_Tenerife_Logo" + i + ".svg"
            );
        }
        return returnContentValues;
    }


    private ContentValues[] createBulkInsertScoreValues() {

        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            returnContentValues[i] = DatabaseHelper.buildMatchCVs(
                    "2015-10-10",
                    10+i + ":00",
                    "7" + i,
//                    "Swansea City FC" + i,
                    "8" + i,
//                    "Stoke City FC" + i,
                    "39" + i,
                    (11 - i) + "" ,
                    i + "" ,
                    "14700" + i,
                    "9"
            );
        }
        return returnContentValues;


    }


}
