package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.models.IYahooStockQuotesAPI;

import com.sam_chordas.android.stockhawk.data.models.Quote;
import com.sam_chordas.android.stockhawk.data.models.StockResult;
import com.sam_chordas.android.stockhawk.data.models.StocksResult;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;;

import java.util.Arrays;
import java.util.List;

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

    public StockTaskService(){}

    public StockTaskService(Context context){
        mContext = context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IYahooStockQuotesAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        yahooStockQuotesAPI = retrofit.create(IYahooStockQuotesAPI.class);
    }

    @Override
    public int onRunTask(final TaskParams params) {
        if (mContext == null) {
            mContext = this;
        }
        if (params.getTag().equals(StockIntentService.ACTION_INIT)){
            Call<StocksResult> stockQuotesQuery = yahooStockQuotesAPI.getStocks(getQuery(params));
            stockQuotesQuery.enqueue(new StockQuotesCallBack());
        } else {
            Call<StockResult> stockQuoteQuery = yahooStockQuotesAPI.getStock(getQuery(params));
            stockQuoteQuery.enqueue(new StockQuoteCallBack());
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private String getQuery(TaskParams params){
        final StringBuilder urlStringBuilder = new StringBuilder();
        Cursor initQueryCursor;
        if (params.getTag().equals(StockIntentService.ACTION_INIT) ||
                params.getTag().equals("periodic")){
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver()
                    .query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[] { "Distinct " + QuoteColumns.SYMBOL }, null, null, null);

            if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
                urlStringBuilder.append(INIT_STOCKS);

            } else if (initQueryCursor != null){
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                String symbol;
                for (int i = 0; i < initQueryCursor.getCount(); i++){
                    symbol = initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"));
                    mStoredSymbols.append("\"" + symbol + "\",");
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

    private class StockQuoteCallBack implements Callback<StockResult> {

        @Override
        public void onResponse(Call<StockResult> call, Response<StockResult> response) {
            Quote quote = response.body().getQuote();
            if (quote != null) {
                List<Quote> quoteList = Arrays.asList(quote);
                try {
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            Utils.quoteJsonToContentVals(quoteList));
                } catch (RemoteException | OperationApplicationException | NullPointerException e) {
                    String msg = mContext.getString(R.string.non_existent_stock_symbol);
                    Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                }

            }
        }

        @Override
        public void onFailure(Call<StockResult> call, Throwable t) {

        }
    }

    private class StockQuotesCallBack implements Callback<StocksResult> {

        @Override
        public void onResponse(Call<StocksResult> call, Response<StocksResult> response) {
            ContentValues contentValues = new ContentValues();
            // update ISCURRENT to 0 (false) so new data is current
            if (isUpdate){
                contentValues.put(QuoteColumns.ISCURRENT, 0);
                mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                        null, null);
            }
            try {
                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.quoteJsonToContentVals(response.body().getQuotes()));
            } catch (RemoteException | OperationApplicationException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<StocksResult> call, Throwable t) {

        }
    }
}
