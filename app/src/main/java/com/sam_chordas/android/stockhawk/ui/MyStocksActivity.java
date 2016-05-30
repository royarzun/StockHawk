package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;


public class MyStocksActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
    * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    */
    private static final int CURSOR_LOADER_ID = 0;

    private QuoteCursorAdapter mCursorAdapter;


    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.recycler_view) RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_stocks);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        Intent mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null){
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra(StockIntentService.EXTRA_TAG, StockIntentService.ACTION_INIT);
            if (isNetworkAvailable()){
                startService(mServiceIntent);
            } else{
                networkToast();
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCursorAdapter = new QuoteCursorAdapter(this, null);

        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {

                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);
        fab.attachToRecyclerView(recyclerView);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void OnClickingTheFabButton() {
        if (isNetworkAvailable()){
            new MaterialDialog.Builder(this).title(R.string.symbol_search)
                    .content(R.string.content_test)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(R.string.input_hint, R.string.input_prefill,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't
                                    // already exist in the DB and proceed accordingly
                                    addQuoteOrFailGracefully(input.toString());
                                }
                            })
                    .show();
        } else {
            networkToast();
        }
    }

    private void addQuoteOrFailGracefully(String input){
        Cursor c = getContentResolver()
                .query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[] {
                                QuoteColumns.SYMBOL
                        }, QuoteColumns.SYMBOL + "= ?",
                        new String[] {
                                input
                        }, null);
        if (c != null && c.getCount() != 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "This stock is already saved!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
            toast.show();
            c.close();
            return;
        } else {
            // Add the stock to DB
            Intent mServiceIntent = new Intent(getApplicationContext(),
                    StockIntentService.class);
            mServiceIntent.putExtra(StockIntentService.EXTRA_TAG,
                    StockIntentService.ACTION_ADD);
            mServiceIntent.putExtra(StockIntentService.EXTRA_SYMBOL, input);
            startService(mServiceIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void networkToast(){
        Toast.makeText(this, getString(R.string.network_toast), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;
        if (id == R.id.action_change_units){
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI, columnsIds,
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

}
