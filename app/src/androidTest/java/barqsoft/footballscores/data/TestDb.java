package barqsoft.footballscores.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by frank on 15.10.15.
 */
public class TestDb extends AndroidTestCase {

    private static final String LOG_TAG = TestDb.class.getName();

    void deleteDb() {
        mContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        deleteDb();
    }

    public void testCreateDb(){
        final HashSet<String> tableNameHashSet = new HashSet<String>();
//        tableNameHashSet.add(DatabaseContract.TEAMS_TABLE);
        tableNameHashSet.add(DatabaseContract.SCORES_TABLE);

        mContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        SQLiteDatabase db = new DatabaseHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DatabaseContract.SCORES_TABLE+ ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(DatabaseContract.ScoreEntry._ID);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.DATE_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.TIME_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.HOME_ID_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.HOME_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.AWAY_ID_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.AWAY_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.LEAGUE_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.HOME_GOALS_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.AWAY_GOALS_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.MATCH_ID_COL);
        locationColumnHashSet.add(DatabaseContract.ScoreEntry.MATCH_DAY_COL);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testScoreTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

//        long locationRowId = insertTeam();
//        assertTrue("location entry was not inserted", locationRowId != -1L);

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues testValues = TestUtils.createScoreValues();

        // Insert ContentValues into database and get a row ID back
        long insertedRows = db.insert(DatabaseContract.SCORES_TABLE, null, testValues);
        assertTrue(insertedRows > 0);


        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                DatabaseContract.SCORES_TABLE,
                null,   //select
                null,   //where keys
                null,   //where values
                null,   //group by
                null,   //having
                null);  //order by

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

        assertTrue("wrong entry count...? ", cursor.getCount() == 1);

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }





}
