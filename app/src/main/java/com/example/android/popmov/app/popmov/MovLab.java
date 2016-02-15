package com.example.android.popmov.app.popmov;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i on 2016-02-11.
 * This container is holder list for Movs.
 */
public class MovLab {
    private static MovLab sMovLab;

    private ArrayList<Mov> mMovs;

    public static MovLab get() {
        if (sMovLab == null) {
            sMovLab = new MovLab();
        }
        return sMovLab;
    }

    private MovLab() {
        mMovs = new ArrayList<>();
    }

    public List<Mov> getMovs() {
        return mMovs;
    }

    public Mov getMov(String posterPath) {
        for (Mov mov : mMovs) {
            if (mov.getPosterPath().equals(posterPath)) {
                return mov;
            }
        }
        return null;
    }

    public void addMov(Mov mov) {
        mMovs.add(mov);
    }
}
