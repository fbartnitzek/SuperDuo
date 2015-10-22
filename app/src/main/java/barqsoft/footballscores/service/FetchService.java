package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import barqsoft.footballscores.Constants;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    private static final String LOG_TAG = FetchService.class.getName();


    private static final String BASE_SCORE_URL = "http://api.football-data.org/alpha/fixtures";
    private static final String BASE_SEASON_URL = "http://api.football-data.org/alpha/soccerseasons";
    private static final String TEAMS_POSTFIX = "teams";
    private static final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days

    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.service.ACTION_DATA_UPDATED";

    public FetchService() {
        super(FetchService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent, " + "intent = [" + intent + "]");

        // TODO: better fetching & syncing strategy
        // teams: once per season at first start of program
        // matches:
        //  just next day (last FUTURE_DAYS) at first sync of the day
        //  current day: every 30min after a game started until 120min after last game started
        // "intelligent syncAdapter ..."

        getMatchData("n" + Constants.FUTURE_DAYS);
        getMatchData("p" + Constants.PAST_DAYS);
        for (int league : Utilities.getSupportedLeagues()){
            getTeamData(league);
        }

    }

    private String fetchJsonData(Uri uri) throws IOException{
        Log.v(LOG_TAG, "fetchJsonData, uri: " + uri.toString()); //log spam
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String jsonData = null;

        // to many api-users - get jsonData with key:
        // frank ~ $ curl --header "X-Auth-Token: fb7b308235244d839b3996aa6494eebf" http://api.football-data.org/alpha/fixtures?timeFrame=n2 | python -m json.tool

        try{
            URL url = new URL(uri.toString());

            //Opening Connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                Log.w(LOG_TAG, "fetchJsonData, " + "empty buffer - no response from server");
                return null;
            }

            jsonData = buffer.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }

        return jsonData;
    }

    private void getTeamData(int league) {

        Context appContext = getApplicationContext();
        String jsonData = null;
        try {
            Uri uri = Uri.parse(BASE_SEASON_URL).buildUpon().
                    appendPath(Integer.toString(league)).
                    appendPath(TEAMS_POSTFIX).
                    build();
            Log.v(LOG_TAG, "getTeamData, " + "league = " + league + "], uri: " + uri.toString());

            // sample: http://api.football-data.org/alpha/soccerseasons/403/teams

            jsonData = fetchJsonData(uri);
        } catch (IOException e) {
            Log.w(LOG_TAG, "getTeamData, " + "IO Exception" + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches.
                JSONArray matches = new JSONObject(jsonData).getJSONArray(JsonExtractor.TEAMS);
                ArrayList<ContentValues> values;
                if (matches.length() == 0) {
                    Log.v(LOG_TAG, "getTeamData, " + "ERROR_NO_MATCH league = [" + league + "]");
                    // TODO: show error message - no jsonData / matches found...
                    return;
                }

                Log.v(LOG_TAG, "getMatchData, with real data ...");
                values = JsonExtractor.processTeamsJsonData(jsonData, appContext);

                if (values != null) {
                    if (values.size() > 0) {

                        ContentValues[] insertData = new ContentValues[values.size()];
                        values.toArray(insertData);
                        int insertedData = appContext.getContentResolver().bulkInsert(
                                DatabaseContract.TeamEntry.CONTENT_URI, insertData);

                        Log.v(LOG_TAG,"Successfully Inserted : " + String.valueOf(insertedData));

                        updateWidgets(appContext);
                    } else {
                        // TODO: no matches in that time ...?
                        Log.v(LOG_TAG, "getTeamData, " + "ALSO ERROR_NO_MATCH league = [" + league + "]");
                    }
                }

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
                Log.v(LOG_TAG, "getTeamData, " + "ANOTHER ERROR_NO_MATCH timeFrame = [" + league + "]");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    private void getMatchData(String timeFrame) {
        Context appContext = getApplicationContext();
        String jsonData = null;
        try {
            Uri uri = Uri.parse(BASE_SCORE_URL).buildUpon().
                    appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();

            Log.v(LOG_TAG, "getMatchData, " + "timeFrame = [" + timeFrame + "], uri: " + uri.toString());

            // sample: http://api.football-data.org/alpha/soccerseasons/teams?timeFrame=395

            jsonData = fetchJsonData(uri);
        } catch (IOException e) {
            Log.w(LOG_TAG, "getMatchData, " + "IO Exception" + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches.
                // If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(jsonData).getJSONArray(JsonExtractor.FIXTURES);
                ArrayList<ContentValues> values;
                if (matches.length() == 0) {
                    Log.v(LOG_TAG, "getMatchData, " + "ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
                    // TODO: show error message - no jsonData / matches found...
                    return;
                }

                Log.v(LOG_TAG, "getMatchData, with real data ...");
                values = JsonExtractor.processScoresJsonData(jsonData, appContext);

                if (values != null) {
                    if (values.size() > 0) {

                        ContentValues[] insertData = new ContentValues[values.size()];
                        values.toArray(insertData);
                        int insertedData = appContext.getContentResolver().bulkInsert(
                                DatabaseContract.ScoreEntry.CONTENT_URI, insertData);

                        Log.v(LOG_TAG,"Successfully Inserted : " + String.valueOf(insertedData));

                        updateWidgets(appContext);
                    } else {
                        // TODO: no matches in that time ...?
                        Log.v(LOG_TAG, "getMatchData, " + "ALSO ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
                    }
                }

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
                Log.v(LOG_TAG, "getMatchData, " + "ANOTHER ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

//    private String print(ContentValues[] insertData) {
//        String all = "";
//        for (ContentValues data : insertData){
//            String row = "";
//            for( Map.Entry<String, Object> entry : data.valueSet()) {
//                row += entry.getKey() + "=" + entry.getValue() + ",";
//            }
//            all += row + ";";
//        }
//        return all;
//    }

    private void updateWidgets(Context context) {
        Log.v(LOG_TAG, "updateWidgets, " + "context = [" + context + "]");
        // only soccer app can receive broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(
                context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}

