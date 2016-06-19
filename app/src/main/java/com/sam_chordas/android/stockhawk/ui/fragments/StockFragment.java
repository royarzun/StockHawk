package com.sam_chordas.android.stockhawk.ui.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoricalColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;


public class StockFragment extends Fragment implements LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = StockFragment.class.getSimpleName();
    public static final String ARG_SYMBOL = "symbol";

    private static final int QUOTE_LOADER_ID = 200;
    private static final int QUOTE_HISTORICAL_ID = 201;

    private String mSymbol;

    @Bind(R.id.detail_stock_symbol) TextView stockSymbolTV;
    @Bind(R.id.detail_stock_name) TextView stockNameTV;
    @Bind(R.id.detail_stock_bid_price) TextView stockBidPriceTV;
    @Bind(R.id.detail_stock_change) TextView stockChangeTV;
    //@Bind(R.id.detail_stock_percent_change) TextView stockPercentChangeTV;
    @Bind(R.id.detail_stock_hist_chart) LineChart stockHistoricChartLC;

    private OnFragmentInteractionListener mListener;

    public StockFragment() {
        Bundle args = new Bundle();
        args.putString(ARG_SYMBOL, "YHOO");
        this.setArguments(args);
    }

    public static StockFragment newInstance(String symbol) {
        StockFragment fragment = new StockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SYMBOL, symbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSymbol = getArguments().getString(ARG_SYMBOL);
        }
        getLoaderManager().initLoader(QUOTE_LOADER_ID, null, this);
        getLoaderManager().initLoader(QUOTE_HISTORICAL_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_start, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case QUOTE_LOADER_ID:
                String[] quoteColumns = new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.NAME,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP};
                String selectQuoteArgs = QuoteColumns.SYMBOL + " = \"" + mSymbol + "\"";
                return new CursorLoader(getActivity(),
                        QuoteProvider.Quotes.CONTENT_URI,
                        quoteColumns, selectQuoteArgs, null, null);
            case QUOTE_HISTORICAL_ID:
                String[] columnsIds = new String[]{
                        QuoteHistoricalColumns._ID,
                        QuoteHistoricalColumns.SYMBOL,
                        QuoteHistoricalColumns.BIDPRICE,
                        QuoteHistoricalColumns.DATE
                };
                return new CursorLoader(getActivity(),
                        QuoteProvider.QuotesHistoricData.CONTENT_URI,
                        columnsIds, QuoteHistoricalColumns.SYMBOL + " =\"" + mSymbol + "\"",
                        null, "DATE ASC LIMIT 20");
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case QUOTE_LOADER_ID:
                if (data.moveToFirst()) {
                    stockSymbolTV.setText(data.getString(
                            data.getColumnIndex(QuoteColumns.SYMBOL)));
                    stockNameTV.setText(data.getString(
                            data.getColumnIndex(QuoteColumns.NAME)));
                    stockBidPriceTV.setText(data.getString(
                            data.getColumnIndex(QuoteColumns.BIDPRICE)));
                    stockChangeTV.setText(data.getString(
                            data.getColumnIndex(QuoteColumns.CHANGE)));
                    //stockPercentChangeTV.setText(data.getString(
                    //        data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));

                    if (data.getInt(data.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                        stockChangeTV.setTextColor(Color.GREEN);
                      //  stockPercentChangeTV.setTextColor(Color.GREEN);
                    } else {
                        stockChangeTV.setTextColor(Color.RED);
                      //  stockPercentChangeTV.setTextColor(Color.RED);
                    }

                }
                break;
            case QUOTE_HISTORICAL_ID:
                if (data.moveToFirst()) {
                    stockHistoricChartLC.setData(getChartData(data));
                    stockHistoricChartLC.animateXY(1000, 1000);
                    break;
                }
        }
    }

    private LineData getChartData(Cursor historicDataCursor) {
        int count = 0;
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        while (historicDataCursor.moveToNext()){
            Float price = Float.valueOf(historicDataCursor.getString(
                    historicDataCursor.getColumnIndex(QuoteHistoricalColumns.BIDPRICE)));
            String date = historicDataCursor.getString(
                    historicDataCursor.getColumnIndex(QuoteHistoricalColumns.DATE));
            entries.add(new Entry(price, count));
            labels.add(date);
            count++;
        }

        LineDataSet dataSet = new LineDataSet(entries,
                getString(R.string.detail_stock_graph_description));
        return new LineData(labels, dataSet);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
