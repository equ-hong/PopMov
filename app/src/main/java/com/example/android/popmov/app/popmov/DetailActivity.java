package com.example.android.popmov.app.popmov;

/**
 * Created by i on 2016-02-10.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        final String MOV_BASE_POSTER_PATH = "http://image.tmdb.org/t/p/w780/";
        private Mov mMov;

        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);
            TextView titleView = (TextView) rootView.findViewById(R.id.detail_title);
            TextView releaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
            TextView voteRateView = (TextView) rootView.findViewById(R.id.detail_vote_rate);
            TextView synopsisView = (TextView) rootView.findViewById(R.id.detail_synopsis);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
                mMov = MovLab.get().getMov(extraText);
            }

            Picasso
                    .with(getActivity())
                    .load(MOV_BASE_POSTER_PATH + mMov.getPosterPath())
                    .into(imageView);

            titleView.setText(mMov.getTitle());
            releaseDateView.setText(mMov.getReleaseDate());
            voteRateView.setText("" + mMov.getVoteAverage());
            synopsisView.setText(mMov.getOverview());

            return rootView;
        }
    }
}
