package com.sam_chordas.android.stockhawk.data.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StocksHistoricalResult{

    @SerializedName("query")
    public Query mQuery;

    static class Query {

        @SerializedName("results")
        public Result mResult;
    }

    static class Result {

        @SerializedName("quote")
        public List<QuoteHistorical> mQuotes;
    }

    public List<QuoteHistorical> getQuotes(){
        return mQuery.mResult.mQuotes;
    }
}
