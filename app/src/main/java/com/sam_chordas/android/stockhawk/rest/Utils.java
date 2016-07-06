package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoricalColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.models.Quote;
import com.sam_chordas.android.stockhawk.data.models.QuoteHistorical;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(List<Quote> quotes){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        if (quotes.size() == 1){
            batchOperations.add(buildBatchOperation(quotes.get(0)));
        } else{
            for (Quote q: quotes)
                batchOperations.add(buildBatchOperation(q));
        }
        return batchOperations;
    }

    public static ArrayList quoteHistoricalToContentValues(List<QuoteHistorical> quotes){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        if (quotes.size() == 1){
            batchOperations.add(buildBatchOperation(quotes.get(0)));
        } else {
            for (QuoteHistorical q: quotes) {
                batchOperations.add(buildBatchOperation(q));
            }
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice){
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange){
        String weight = change.substring(0,1);
        String ampersand = "";
        if (isPercentChange){
          ampersand = change.substring(change.length() - 1, change.length());
          change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    private static ContentProviderOperation buildBatchOperation(Quote quote){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);

        String change = quote.getChange();
        builder.withValue(QuoteColumns.SYMBOL, quote.getSymbol());
        builder.withValue(QuoteColumns.NAME, quote.getName());
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(quote.getBid()));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                quote.getChangeInPercent(), true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);

        if (change.charAt(0) == '-'){
            builder.withValue(QuoteColumns.ISUP, 0);
        }else{
            builder.withValue(QuoteColumns.ISUP, 1);
        }

        return builder.build();
    }

    private static ContentProviderOperation buildBatchOperation(QuoteHistorical quote) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.QuotesHistoricData.CONTENT_URI);
        builder.withValue(QuoteHistoricalColumns.SYMBOL, quote.mSymbol);
        builder.withValue(QuoteHistoricalColumns.BIDPRICE, quote.mBid);
        builder.withValue(QuoteHistoricalColumns.DATE, quote.mDate);
        return builder.build();
    }
}
