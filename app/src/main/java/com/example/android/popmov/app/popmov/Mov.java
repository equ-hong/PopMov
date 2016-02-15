package com.example.android.popmov.app.popmov;

import java.util.Date;
import java.util.UUID;

/**
 * Created by i on 2016-02-10.
 * This container is for detail view.
 */
public class Mov {
    private String mTitle;
    private String mOverview;
    private String mReleaseDate;
    private Double mVoteAverage;
    private String mPosterPath;

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        mVoteAverage = voteAverage;
    }
}
