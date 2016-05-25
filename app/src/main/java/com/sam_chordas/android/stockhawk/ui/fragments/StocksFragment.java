package com.sam_chordas.android.stockhawk.ui.fragments;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.service.StockIntentService;


public class StocksFragment extends Fragment implements OnSharedPreferenceChangeListener,
        LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;


    private OnFragmentInteractionListener mListener;

    public StocksFragment() {
        // Required empty public constructor
    }

    public static StocksFragment newInstance() {
        StocksFragment fragment = new StocksFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stocks, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCursorAdapter = new QuoteCursorAdapter(getActivity(), null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View v, int position) {
                        //TODO:
                        // do something on item click
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isConnected){
                    new MaterialDialog.Builder(getActivity()).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getActivity().getContentResolver()
                                                            .query(QuoteProvider.Quotes.CONTENT_URI,
                                                                    new String[] {
                                                                            QuoteColumns.SYMBOL
                                                                    }, QuoteColumns.SYMBOL + "= ?",
                                                                    new String[] {
                                                                            input.toString()
                                                                    }, null);
                                    if (c.getCount() != 0) {
                                        Toast toast = Toast.makeText(getActivity(),
                                                "This stock is already saved!", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra(StockIntentService.EXTRA_TAG, "add");
                                        mServiceIntent.putExtra(StockIntentService.EXTRA_SYMBOL,
                                                input.toString());
                                        startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    //networkToast();
                }

            }
        });*/
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // TODO: implement OnFragmentInteractionListener
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        String[] columnsIds = new String[]{
                QuoteColumns._ID,
                QuoteColumns.SYMBOL,
                QuoteColumns.BIDPRICE,
                QuoteColumns.PERCENT_CHANGE,
                QuoteColumns.CHANGE, QuoteColumns.ISUP
        };
        return new CursorLoader(getActivity(), QuoteProvider.Quotes.CONTENT_URI, columnsIds,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCursorAdapter != null){
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
