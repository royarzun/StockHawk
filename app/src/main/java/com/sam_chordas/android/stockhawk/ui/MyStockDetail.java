package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.fragments.StockFragment;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MyStockDetail extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String symbol = getIntent().getStringExtra(StockFragment.ARG_SYMBOL);
        setContentView(R.layout.activity_my_stock_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(symbol);

        if (savedInstanceState == null) {
            StockFragment fragment = StockFragment.newInstance(symbol);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_fragment, fragment)
                    .commit();
        }
    }

}
