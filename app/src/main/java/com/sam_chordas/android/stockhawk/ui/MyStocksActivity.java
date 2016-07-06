package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
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
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.ui.fragments.StockFragment;


public class MyStocksActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewItemClickListener.OnItemClickListener {

    /**
    * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    */
    private static final int CURSOR_LOADER_ID = 0;

    private QuoteCursorAdapter mCursorAdapter;


    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.recycler_view) RecyclerView recyclerView;
    @Bind(R.id.empty_recycler_view_msg) View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_stocks);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCursorAdapter = new QuoteCursorAdapter(this, null);

        recyclerView.setAdapter(mCursorAdapter);
        fab.attachToRecyclerView(recyclerView);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

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
                                    addQuoteOrNotify(input.toString());
                                }
                            })
                    .show();
        } else {
            networkToast();
        }
    }

    private void addQuoteOrNotify(final String quote){
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Cursor cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns.SYMBOL},
                        QuoteColumns.SYMBOL + "= ?",
                        new String[]{quote},
                        null);
                if (cursor != null) {
                    cursor.close();
                    return cursor.getCount() != 0;
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean stockAlreadySaved) {
                if (stockAlreadySaved) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.stock_already_in_database), Toast.LENGTH_LONG).show();
                } else {
                    Intent stockIntentService = new Intent(MyStocksActivity.this,
                            StockIntentService.class);
                    stockIntentService.putExtra(StockIntentService.EXTRA_TAG,
                            StockIntentService.ACTION_ADD);
                    stockIntentService.putExtra(StockIntentService.EXTRA_SYMBOL, quote);
                    startService(stockIntentService);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if (id == R.id.action_update_stocks){
            Intent stockIntentService = new Intent(MyStocksActivity.this,
                    StockIntentService.class);
            stockIntentService.putExtra(StockIntentService.EXTRA_TAG,
                    StockTaskService.ACTION_PERIODIC_UPDATE);
            if (isNetworkAvailable()){
                startService(stockIntentService);
            } else {
                networkToast();
            }
        }
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
                QuoteColumns.NAME,
                QuoteColumns.BIDPRICE,
                QuoteColumns.PERCENT_CHANGE,
                QuoteColumns.CHANGE,
                QuoteColumns.ISUP
        };
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI, columnsIds,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCursorAdapter != null){
            if(findViewById(R.id.fragment)!=null && data.moveToFirst()){
                String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                StockFragment fragment = StockFragment.newInstance(symbol);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, fragment, "detail")
                        .commit();
            }
            mCursorAdapter.swapCursor(data);
        }
        mEmptyView.setVisibility(data == null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(View v, int position) {
        String symbol = mCursorAdapter.getSymbol(position);
        if (findViewById(R.id.fragment) == null){
            Intent intent = new Intent(this, MyStockDetail.class);
            intent.putExtra(StockFragment.ARG_SYMBOL, symbol);
            startActivity(intent);
        } else {
            StockFragment fragment = StockFragment.newInstance(symbol);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, "detail")
                    .addToBackStack(null).commit();
        }
    }
}
