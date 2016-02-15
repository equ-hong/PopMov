package com.example.android.popmov.app.popmov;

/**
 * Created by i on 2016-02-05.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class MovFragment extends Fragment {

    private MovAdapter mMovAdapter;

    public MovFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //FetchWeatherTask weatherTask = new FetchWeatherTask();
            //weatherTask.execute();
            updateMov();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Mov class have the attributes for detail view
        List<Mov> mMovList = new ArrayList<Mov>();
        mMovAdapter = new MovAdapter(getActivity(), mMovList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.mov_gridview);
        gridView.setAdapter(mMovAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mov extraMov = (Mov) mMovAdapter.getItem(position);
                String extraText = extraMov.getPosterPath();
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, extraText);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class MovAdapter extends ArrayAdapter {
        final String MOV_BASE_POSTER_PATH = "http://image.tmdb.org/t/p/w185/";
        private Context context;
        private List<Mov> movs;
        private LayoutInflater inflater;

        public MovAdapter(Context context, List<Mov> movs) {
            super(context, R.layout.list_item_mov, movs);
            this.context = context;
            this.movs = movs;

            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return movs.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.list_item_mov, parent, false);
            }

            Picasso
                    .with(context)
                    .load(MOV_BASE_POSTER_PATH + movs.get(position).getPosterPath())
                    .into((ImageView) convertView);

            return convertView;
        };
    }

    private void updateMov() {
        FetchMovTask movTask = new FetchMovTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        movTask.execute(sort);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMov();
    }

    public class FetchMovTask extends AsyncTask<String, Void, Mov[]> {

        private final String LOG_TAG = FetchMovTask.class.getSimpleName();

        // private String[] getMovDataFromJson(String movJsonStr, int numDays)
        private Mov[] getMovDataFromJson(String movJsonStr)

        throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOV_RESULTS = "results";
            final String MOV_TITLE = "title";
            final String MOV_OVERVIEW = "overview";
            final String MOV_RELEASE_DATE = "release_date";
            final String MOV_VOTE_AVERAGE = "vote_average";
            final String MOV_POSTER_PATH = "poster_path";

            JSONObject movJson = new JSONObject(movJsonStr);
            JSONArray movArray = movJson.getJSONArray(MOV_RESULTS);

            Mov[] resultMovs = new Mov[movArray.length()];
            int j = 0;
            for(int i = 0; i < movArray.length(); i++) {

                String title;
                String overview;
                String releaseDate;
                double voteAverage;
                String posterPath;

                JSONObject movObject = movArray.getJSONObject(i);

                title = movObject.getString(MOV_TITLE);
                overview =  movObject.getString(MOV_OVERVIEW);
                releaseDate = movObject.getString(MOV_RELEASE_DATE);
                voteAverage = movObject.getDouble(MOV_VOTE_AVERAGE);
                posterPath = movObject.getString(MOV_POSTER_PATH);

                if(posterPath != "null") {
                    Mov mov = new Mov();
                    mov.setTitle(title);
                    mov.setOverview(overview);
                    mov.setReleaseDate(releaseDate);
                    mov.setVoteAverage(voteAverage);
                    mov.setPosterPath(posterPath);
                    MovLab.get().addMov(mov);
                    resultMovs[j] = mov;
                    j = j + 1;
                }
            }

            // Use resultMovs2 because resultMovs have vacant items on its tail.
            Mov[] resultMovs2 = new Mov[j];

            System.arraycopy(resultMovs, 0, resultMovs2, 0, resultMovs2.length);

            return resultMovs2;
        }

        @Override
        protected Mov[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movJsonStr = null;

            String format = "json";

            try {
                // Construct the URL for the themoviedb query
                // Possible parameters are avaiable at themoviedb API page, at
                // http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get?console=1
                // URL : http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
                final String MOV_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String QUERY_PARAM = "sort_by";

                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOV_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
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
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovDataFromJson(movJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Mov[] result) {
            if (result != null) {
                    mMovAdapter.clear();
                for(Mov mov : result) {
                    mMovAdapter.add(mov);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
}
