package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.Bind;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.ui.fragments.StocksFragment;


public class MyStocksActivity extends AppCompatActivity {

    /**
    * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    */

    @Bind(R.id.my_stocks_toolbar) Toolbar toolbar;

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
        StocksFragment fragment = StocksFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.stocks_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
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
}
