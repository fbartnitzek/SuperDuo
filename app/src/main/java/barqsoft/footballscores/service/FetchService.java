package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.Constants;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    private static final String LOG_TAG = FetchService.class.getName();
    private static final String FIXTURES = "fixtures";

    private static final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
    private static final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days

    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.service.ACTION_DATA_UPDATED";

    public FetchService() {
        super(FetchService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent, " + "intent = [" + intent + "]");

        getData("n" + Constants.FUTURE_DAYS);
        getData("p" + Constants.PAST_DAYS);
    }

    private String fetchJsonData(String timeFrame) throws IOException {
        Uri uri = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        Log.v(LOG_TAG, "getData, uri: " + uri.toString()); //log spam
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String jsonData = null;

        // to many api-users - get jsonData with key:
        // frank ~ $ curl --header "X-Auth-Token: fb7b308235244d839b3996aa6494eebf" http://api.football-data.org/alpha/fixtures?timeFrame=n2 | python -m json.tool

        URL url = null;
        try{
            url = new URL(uri.toString());

            //Opening Connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
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
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                Log.w(LOG_TAG, "getData, " + "empty buffer - no response from server");
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

    private void getData(String timeFrame) {
        Log.v(LOG_TAG, "getData, " + "timeFrame = [" + timeFrame + "]");

        Context appContext = getApplicationContext();
        String jsonData = null;
        try {
            jsonData = fetchJsonData(timeFrame);
        } catch (IOException e) {
            Log.w(LOG_TAG, "getData, " + "IO Exception" + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches.
                // If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);
                ArrayList<ContentValues> values;
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
//                    Log.w(LOG_TAG, "getData, just showing dummy data...");
                    Log.v(LOG_TAG, "getData, " + "ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
                    // TODO: show error message - no jsonData / matches found...
//                    values = processJSONdata(getString(R.string.dummy_data), appContext, false);
                    return;
                }

                Log.v(LOG_TAG, "getData, with real data ...");
                values = processJSONdata(jsonData, appContext);

                if (values != null) {
                    if (values.size() > 0) {
                        int inserted_data = 0;
                        ContentValues[] insert_data = new ContentValues[values.size()];
                        values.toArray(insert_data);
                        inserted_data = appContext.getContentResolver().bulkInsert(
                                DatabaseContract.BASE_CONTENT_URI, insert_data);

                        Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));

                        updateWidgets(appContext);
                    } else {
                        // TODO: no matches in that time ...?
                        Log.v(LOG_TAG, "getData, " + "ALSO ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
                    }
                }

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
                Log.v(LOG_TAG, "getData, " + "ANOTHER ERROR_NO_MATCH timeFrame = [" + timeFrame + "]");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void updateWidgets(Context context) {
        Log.v(LOG_TAG, "updateWidgets, " + "context = [" + context + "]");
        // only soccer app can receive broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private static final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private static final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    private static final String LINKS = "_links";
    private static final String SOCCER_SEASON = "soccerseason";
    private static final String SELF = "self";
    private static final String MATCH_DATE = "date";
    final String HOME_TEAM = "homeTeamName";
    final String AWAY_TEAM = "awayTeamName";
    private static final String RESULT = "result";
    private static final String HOME_GOALS = "goalsHomeTeam";
    private static final String AWAY_GOALS = "goalsAwayTeam";
    private static final String MATCH_DAY = "matchday";
    private static final String AWAY_TEAM_OBJ = "awayTeam";
    private static final String HOME_TEAM_OBJ = "homeTeam";
    private static final String HREF = "href";

    private ArrayList<ContentValues> processJSONdata(String jsonData, Context context) {
        Log.v(LOG_TAG, "processJSONdata, " + "jsonData = [" + jsonData + "], context = [" + context + "]");

        //Match data
        String league = null;
        String date = null;
        String time = null;
        String home = null;
        String away = null;
        String homeGoals = null;
        String awayGoals = null;
        String matchId = null;
        String matchDay = null;
        ArrayList<ContentValues> values = null;

        try {
            JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            values = new ArrayList<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject matchData = matches.getJSONObject(i);
                JSONObject links = matchData.getJSONObject(LINKS);
                league = links.getJSONObject(SOCCER_SEASON).
                        getString(HREF);
                league = league.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (Utilities.isSupportedLeague(Integer.parseInt(league))){

                    matchId = links.getJSONObject(SELF).
                            getString(HREF);
                    matchId = matchId.replace(MATCH_LINK, "");

                    // both teams
                    String awayLink = links.getJSONObject(AWAY_TEAM_OBJ).getString(HREF);
                    String homeLink = links.getJSONObject(HOME_TEAM_OBJ).getString(HREF);
                    String awayId = Utilities.extractTeamIdFromLink(awayLink);
                    String homeId = Utilities.extractTeamIdFromLink(homeLink);


                    date = matchData.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat utcDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    utcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = utcDate.parse(date + time);
                        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        newDate.setTimeZone(TimeZone.getDefault());
                        date = newDate.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    home = matchData.getString(HOME_TEAM);
                    away = matchData.getString(AWAY_TEAM);

                    homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);

                    awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);

                    Log.v(LOG_TAG, "processJSONdata, homeId: " + homeId + ", awayId: " + awayId
                            + ", homeGoals: " + homeGoals + ", awayGoals: " + awayGoals);

                    matchDay = matchData.getString(MATCH_DAY);
                    ContentValues matchValues = new ContentValues();

                    matchValues.put(DatabaseContract.ScoreEntry.DATE_COL, date);
                    matchValues.put(DatabaseContract.ScoreEntry.TIME_COL, time);
                    matchValues.put(DatabaseContract.ScoreEntry.HOME_COL, home);
                    matchValues.put(DatabaseContract.ScoreEntry.AWAY_COL, away);
                    matchValues.put(DatabaseContract.ScoreEntry.HOME_ID_COL, homeId);
                    matchValues.put(DatabaseContract.ScoreEntry.AWAY_ID_COL, awayId);
                    matchValues.put(DatabaseContract.ScoreEntry.LEAGUE_COL, league);
                    matchValues.put(DatabaseContract.ScoreEntry.HOME_GOALS_COL, homeGoals);
                    matchValues.put(DatabaseContract.ScoreEntry.AWAY_GOALS_COL, awayGoals);
                    matchValues.put(DatabaseContract.ScoreEntry.MATCH_ID_COL, matchId);
                    matchValues.put(DatabaseContract.ScoreEntry.MATCH_DAY_COL, matchDay);

                    values.add(matchValues);

                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return values;
    }


}

