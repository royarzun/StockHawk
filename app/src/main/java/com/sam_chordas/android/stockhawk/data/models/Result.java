package com.sam_chordas.android.stockhawk.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Result {

    @SerializedName("results")
    public Queries mQueries;

    public static class Queries {

        @SerializedName("quote")
        public List<Quote> mQuotes;
    }
}
