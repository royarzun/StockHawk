package com.sam_chordas.android.stockhawk.data.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Query {

    @SerializedName("query")
    public Result mResult;

    public List<Quote> getQuotes(){
        return mResult.mQueries.mQuotes;
    }
}
