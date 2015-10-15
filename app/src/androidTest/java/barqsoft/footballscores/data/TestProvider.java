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

/**
 * Created by frank on 15.10.15.
 */
public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                DatabaseContract.ScoreEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                DatabaseContract.ScoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Score table during delete", 0, cursor.getCount());
        cursor.close();

    }

    public void deleteAllRecordsFromDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseContract.SCORES_TABLE, null, null);
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
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DatabaseContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DatabaseContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testBasicScoreQuery() {
        // insert our test records into the database
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtils.createScoreValues();
        long locationRowId = db.insert(DatabaseContract.SCORES_TABLE, null, testValues);
        assertTrue(locationRowId > 0);
        db.close();

        // Test the basic content provider query
        Cursor scoreCursor = mContext.getContentResolver().query(
                DatabaseContract.ScoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtils.validateCursor("testBasicScoreQuery", scoreCursor, testValues);
    }


    public void testInsertReadProvider() {
        Log.v(LOG_TAG, "testInsertReadProvider, " + "");


        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtils.TestContentObserver tco = TestUtils.getTestContentObserver();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues scoreValues = TestUtils.createScoreValues();

        mContext.getContentResolver().registerContentObserver(
                DatabaseContract.ScoreEntry.CONTENT_URI, true, tco);

        Uri scoreInsertUri = mContext.getContentResolver().insert(
                DatabaseContract.ScoreEntry.CONTENT_URI, scoreValues);
        assertTrue(scoreInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Log.v(LOG_TAG, "testInsertReadProvider, " + " before query");
        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                DatabaseContract.ScoreEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        assertTrue("empty after insert", weatherCursor.getCount() > 0);

        weatherCursor.moveToFirst();
        TestUtils.printCurrentCursorEntry(weatherCursor);

        TestUtils.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                weatherCursor, scoreValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtils.TestContentObserver scoreObserver = TestUtils.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, locationObserver);

        // Register a content observer for our weather delete.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                DatabaseContract.ScoreEntry.CONTENT_URI, true, scoreObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        scoreObserver.waitForNotificationOrFail();
//        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(scoreObserver);
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }


    public void testBulkInsert() {


        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertScoreValues();

        // Register a content observer for our bulk insert.
        TestUtils.TestContentObserver weatherObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                DatabaseContract.ScoreEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(
                DatabaseContract.ScoreEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                DatabaseContract.ScoreEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtils.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    private ContentValues[] createBulkInsertScoreValues() {
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            returnContentValues[i] = DatabaseHelper.buildMatchCVs(
                    "2015-10-10",
                    10+i + ":00",
                    "7" + i,
                    "Swansea City FC" + i,
                    "8" + i,
                    "Stoke City FC" + i,
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
