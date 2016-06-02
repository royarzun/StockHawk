package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.models.IYahooStockQuotesAPI;
import com.sam_chordas.android.stockhawk.data.models.Query;

import com.squareup.okhttp.OkHttpClient;;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
    private static final String LOG_TAG = StockTaskService.class.getSimpleName();
    private static final String INIT_STOCKS = "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\"";

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;
    private IYahooStockQuotesAPI yahooStockQuotesAPI;
    private Call<Query> stockQuotesQuery;

    public StockTaskService(){}

    public StockTaskService(Context context){
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams params) {
        if (mContext == null) {
            mContext = this;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IYahooStockQuotesAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        yahooStockQuotesAPI = retrofit.create(IYahooStockQuotesAPI.class);
        stockQuotesQuery = yahooStockQuotesAPI.getStocks(getQuery(params));
        stockQuotesQuery.enqueue(new Callback<Query>() {
            @Override
            public void onResponse(Call<Query> call, Response<Query> response) {
                // TODO: Store data
            }

            @Override
            public void onFailure(Call<Query> call, Throwable t) {
                // TODO: Handling failure
                Log.d(LOG_TAG, t.getMessage());
            }
        });
        return 1;
    }

    private String getQuery(TaskParams params){
        final StringBuilder urlStringBuilder = new StringBuilder();
        Cursor initQueryCursor;
        if (params.getTag().equals("init") || params.getTag().equals("periodic")){
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver()
                    .query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[] { "Distinct " + QuoteColumns.SYMBOL }, null, null, null);

            if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
                urlStringBuilder.append(INIT_STOCKS);

            } else if (initQueryCursor != null){
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++){
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) +
                            "\",");
                    initQueryCursor.moveToNext();
                }
                initQueryCursor.close();
                mStoredSymbols.deleteCharAt(mStoredSymbols.length() - 1);
                urlStringBuilder.append(mStoredSymbols.toString());
            }
        } else if (params.getTag().equals(StockIntentService.ACTION_ADD)){
            isUpdate = false;
            String stockInput = params.getExtras().getString(StockIntentService.EXTRA_SYMBOL);
            urlStringBuilder.append("\"" + stockInput + "\"");
        }

        Log.d(LOG_TAG, urlStringBuilder.toString());
        return "select * from yahoo.finance.quotes where symbol in (" +
                urlStringBuilder.toString() + ")";
    }
}
