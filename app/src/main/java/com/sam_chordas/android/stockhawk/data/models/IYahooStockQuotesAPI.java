package com.sam_chordas.android.stockhawk.data.models;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IYahooStockQuotesAPI {

    String BASE_URL = "https://query.yahooapis.com";

    @GET("/v1/public/yql?" +
            "format=json&diagnostics=true&" +
            "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<com.sam_chordas.android.stockhawk.data.models.Query> getStocks(@Query("q") String query);
}
