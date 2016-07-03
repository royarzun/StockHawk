package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  private static final String LOG_TAG = StockIntentService.class.getSimpleName();

  public static final String EXTRA_TAG = "tag";
  public static final String EXTRA_SYMBOL = "symbol";

  public static final String ACTION_ADD = "add";
  public static final String ACTION_INIT = "init";
  public static final String ACTION_PERIODIC_UPDATE = "periodic";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(LOG_TAG, "Launching the StockTaskService");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra(EXTRA_TAG).equals(ACTION_ADD)){
      args.putString(EXTRA_SYMBOL, intent.getStringExtra(EXTRA_SYMBOL));
      Log.d(LOG_TAG, "Adding extra symbol to the list of stock quotes");
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(EXTRA_TAG), args));
  }
}
