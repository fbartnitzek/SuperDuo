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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.Constants;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    private static final String LOG_TAG = FetchService.class.getName();
    private static final String FIXTURES = "fixtures";

    public FetchService() {
        super("FetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent, " + "intent = [" + intent + "]");
        getData("n2");
        getData("p2");

        return;
    }

    private void getData(String timeFrame) {
        Log.v(LOG_TAG, "getData, " + "timeFrame = [" + timeFrame + "]");
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri uri = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        Log.v(LOG_TAG, "getData, uri: " + uri.toString()); //log spam
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String jsonData = null;
        //Opening Connection
        try {
            // to many api-users
            // get jsonData with key:
            // frank ~ $ curl --header "X-Auth-Token: fb7b308235244d839b3996aa6494eebf" http://api.football-data.org/alpha/fixtures?timeFrame=n2 | python -m json.tool

            URL fetch = new URL(uri.toString());
            connection = (HttpURLConnection) fetch.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
                // Stream was empty.  No point in parsing.
//                return;
            }
            jsonData = buffer.toString();
        } catch (MalformedURLException e1) {
            Log.w(LOG_TAG, "getData, " + "malformed URL " + e1.getMessage());
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            Log.w(LOG_TAG, "getData, " + "wrong protocol " + e1.getMessage());
            e1.printStackTrace();
        } catch (IOException e1) {
            Log.w(LOG_TAG, "getData, " + "IO Exception" + e1.getMessage());
            e1.printStackTrace();
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
        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    Log.w(LOG_TAG, "getData, just showing dummy data...");
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }

                Log.v(LOG_TAG, "getData, with real data ...");
                processJSONdata(jsonData, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONdata(String jsonData, Context mContext, boolean isReal) {
        Log.v(LOG_TAG, "processJSONdata, " + "jsonData = [" + jsonData + "], mContext = [" + mContext + "], isReal = [" + isReal + "]");
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
//        final String BUNDESLIGA1 = "394";
//        final String BUNDESLIGA2 = "395";
//        final String LIGUE1 = "396";
//        final String LIGUE2 = "397";
//        final String PREMIER_LEAGUE = "398";
//        final String PRIMERA_DIVISION = "399";
//        final String SEGUNDA_DIVISION = "400";
//        final String SERIE_A = "401";
//        final String PRIMERA_LIGA = "402";
//        final String BUNDESLIGA3 = "403";
//        final String EREDIVISIE = "404";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String homeGoals = null;
        String awayGoals = null;
        String matchId = null;
        String matchDay = null;


        try {
            JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject matchData = matches.getJSONObject(i);
                league = matchData.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (Utilies.isSupportedLeague(Integer.parseInt(league))){

                    matchId = matchData.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId = matchId + Integer.toString(i);
                    }

                    mDate = matchData.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * Constants.DAY_IN_MILLIS));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = matchData.getString(HOME_TEAM);
                    Away = matchData.getString(AWAY_TEAM);

                    homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);
//                    Log.v(LOG_TAG,homeGoals);
//                    if (homeGoals == null || Constants.GOALS_UNKNOWN.equals(homeGoals)) {
//                        homeGoals = mContext.getString(R.string.unknown_goals);
//                    }

                    awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);
//                    Log.v(LOG_TAG,awayGoals);
//                    if (awayGoals == null || Constants.GOALS_UNKNOWN.equals(awayGoals)){
//                        awayGoals = mContext.getString(R.string.unknown_goals);
//                    }
                    matchDay = matchData.getString(MATCH_DAY);
                    ContentValues matchValues = new ContentValues();
                    matchValues.put(DatabaseContract.scores_table.MATCH_ID, matchId);
                    matchValues.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    matchValues.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    matchValues.put(DatabaseContract.scores_table.HOME_COL, Home);
                    matchValues.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    matchValues.put(DatabaseContract.scores_table.HOME_GOALS_COL, homeGoals);
                    matchValues.put(DatabaseContract.scores_table.AWAY_GOALS_COL, awayGoals);
                    matchValues.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                    matchValues.put(DatabaseContract.scores_table.MATCH_DAY, matchDay);
                    //log spam

                    //Log.v(LOG_TAG,matchId);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);


                    values.add(matchValues);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}

