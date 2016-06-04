package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.fragments.StockFragment;


public class MyStockDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stock_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            StockFragment fragment = StockFragment.newInstance(getIntent().getStringExtra(
                    StockFragment.ARG_SYMBOL));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_fragment, fragment)
                    .commit();
        }
    }

}
