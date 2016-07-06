package com.sam_chordas.android.stockhawk.data.models;

import com.google.gson.annotations.SerializedName;


public class StockResult {

    @SerializedName("query")
    private Query mQuery;

    static class Query {

        @SerializedName("results")
        public Result mResult;
    }

    static class Result {

        @SerializedName("quote")
        public Quote mQuote;
    }

    public Quote getQuote() {
        return mQuery.mResult.mQuote;
    }
}
