package com.sam_chordas.android.stockhawk.data.models;

import com.google.gson.annotations.SerializedName;


@SuppressWarnings("unused")
public class Quote {

    @SerializedName("Change")
    public String mChange;

    @SerializedName("symbol")
    public String mSymbol;

    @SerializedName("Name")
    public String mName;

    @SerializedName("Bid")
    public String mBid;

    @SerializedName("ChangeinPercent")
    public String mChangeInPercent;

    public String getChange() {
        return mChange;
    }

    public String getBid() {
        return mBid;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public String getChangeInPercent() {
        return mChangeInPercent;
    }

    public String getName() {
        return mName;
    }
}