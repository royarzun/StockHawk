package com.sam_chordas.android.stockhawk.data.models;

import com.google.gson.annotations.SerializedName;


public class QuoteHistorical {

    @SerializedName("Symbol")
    public String mSymbol;

    @SerializedName("Name")
    public String mName;

    @SerializedName("Close")
    public String mBid;

    @SerializedName("Date")
    public String mDate;
}
