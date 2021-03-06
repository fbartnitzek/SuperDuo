package barqsoft.footballscores.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by frank on 15.10.15.
 */
public class TestUtils extends AndroidTestCase{

    private static final String LOG_TAG = TestUtils.class.getName();


    // sampleData

    public static ContentValues createHomeTeamValues() {
        return DatabaseHelper.buildTeamCVs("35", "Dynamo Dresden",
                "http://upload.wikimedia.org/wikipedia/de/e/e1/Logo_SG_Dynamo_Dresden_neu.svg");
    }

    public static ContentValues createAwayTeamValues() {
        return DatabaseHelper.buildTeamCVs("23", "Energie Cottbus",
                "https://upload.wikimedia.org/wikipedia/en/1/12/Fcenergie.png");
    }

    public static long getHomeTeamId(){
        return 35L;
    }

    public static long getAwayTeamId(){
        return 23L;
    }

    public static long getMatchId(){
        return 148783;
    }

    public static String getMatchDate() {
        return "2015-10-17";
    }

    public static ContentValues createScoreValues() {
//        return DatabaseHelper.buildMatchCVs(
//                "2015-10-15", "18:00", "123", "teamA", "543", "teamB", "123", "2", "1",
//                "1234", "1");
        //away_id=70 away=Stoke City FC home_goals=-1 time=21:00 date=2015-10-19 away_goals=-1
        // match_day=9 matchId=147004 league=398 home_id=72 home=Swansea City FC
//        Log.v(LOG_TAG, "createScoreValues, " + "");
        return DatabaseHelper.buildMatchCVs(
                "2015-10-17", "14:00", "35", "23", "403",
                "-1", "-1", "148783", "13"
        );
    }

    public static ContentValues createSecondScoreValues() {
        return DatabaseHelper.buildMatchCVs(
                "2015-10-17", "20:00", "35", "23", "403",
                "4", "4", "148784", "14"
        );
    }

    // TODO: seems to be wrong without adaption
//    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
//        Log.v(LOG_TAG, "validateCursor, " + "error = [" + error + "], valueCursor = [" + valueCursor + "], expectedValues = [" + expectedValues + "]");
//
//        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
//        validateCurrentRecord(error, valueCursor, expectedValues);
//        valueCursor.close();
//    }
//
//    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
//        Log.v(LOG_TAG, "validateCurrentRecord, " + "error = [" + error + "], valueCursor = [" + valueCursor + "], expectedValues = [" + expectedValues + "]");
//        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
//        for (Map.Entry<String, Object> entry : valueSet) {
//            String columnName = entry.getKey();
//            int idx = valueCursor.getColumnIndex(columnName);
//            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
//            String expectedValue = entry.getValue().toString();
//            Log.v(LOG_TAG, "validateCurrentRecord, " + "error = [" + error + "], valueCursor = [" + valueCursor + "], expectedValues = [" + expectedValues + "]");
//            assertEquals("Value '" + entry.getValue().toString() +
//                    "' did not match the expected value '" +
//                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
//        }
//    }

    public static void printCurrentCursorEntry(Cursor cursor) {
        String result = "";
        Log.v(LOG_TAG, "printCurrentCursorEntry, " + "cursor = [" + cursor + "]");
        for (int i = 0 ; i < cursor.getColumnCount() ; ++i) {
            result += i + ": " + cursor.getString(i) + ", ";
        }
        Log.v(LOG_TAG, "printCurrentCursorEntry, " + "content= [" + result+ "]");
    }

    public static void printAllCursorEntries(Cursor teamCursor, String msg) {
        Log.v(LOG_TAG, "printAllCursorEntries, " + "msg = [" + msg + "]");
        while (teamCursor.moveToNext()){
            printCurrentCursorEntry(teamCursor);
        }
    }


    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
