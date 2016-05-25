package com.sam_chordas.android.stockhawk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockTaskService;


public class Utility {

  @SuppressWarnings("ResourceType")
  public static @StockTaskService.QuoteRetrieveStatus int getStockQuoteStatus(Context c){
    String pName = c.getResources().getString(R.string.pref_stock_quote_status_key);
    SharedPreferences sp =  c.getSharedPreferences("PrefsFile", Context.MODE_PRIVATE);
    return sp.getInt(pName, StockTaskService.STOCK_QUOTE_UNKNOWN);
  }
}
