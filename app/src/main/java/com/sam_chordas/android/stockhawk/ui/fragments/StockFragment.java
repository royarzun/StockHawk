package com.sam_chordas.android.stockhawk.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;


public class StockFragment extends Fragment implements LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = StockFragment.class.getSimpleName();
    public static final String ARG_SYMBOL = "symbol";

    private static final int QUOTE_LOADER_ID = 200;

    private String mSymbol;

    @Bind(R.id.detail_stock_symbol) TextView stockSymbolTV;
    @Bind(R.id.detail_stock_name) TextView stockNameTV;
    @Bind(R.id.detail_stock_bid_price) TextView stockBidPriceTV;
    @Bind(R.id.detail_stock_change) TextView stockChangeTV;
    @Bind(R.id.detail_stock_hist_chart) LineChart stockHistoricChartLC;

    private OnFragmentInteractionListener mListener;

    public StockFragment() {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock, container, false);
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
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case QUOTE_LOADER_ID:
                data.moveToFirst();
                stockSymbolTV.setText(data.getString(
                        data.getColumnIndex(QuoteColumns.SYMBOL)));
                stockNameTV.setText(data.getString(
                        data.getColumnIndex(QuoteColumns.NAME)));
                stockBidPriceTV.setText(data.getString(
                        data.getColumnIndex(QuoteColumns.BIDPRICE)));
                stockChangeTV.setText(data.getString(
                        data.getColumnIndex(QuoteColumns.CHANGE)));

                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(new Entry(4f, 0));
                entries.add(new Entry(8f, 1));
                entries.add(new Entry(6f, 2));
                entries.add(new Entry(12f, 3));
                entries.add(new Entry(18f, 4));
                entries.add(new Entry(9f, 5));
                LineDataSet dataSet = new LineDataSet(entries, "Stock Price over time");
                ArrayList<String> labels = new ArrayList<String>();
                labels.add("January");
                labels.add("February");
                labels.add("March");
                labels.add("April");
                labels.add("May");
                labels.add("June");
                stockHistoricChartLC.setData(new LineData(labels, dataSet));
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
