/*
 * $Id: $
 *
 * Copyright (C) 2012 Stoyan Rachev (stoyanr@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */

package com.stoyanr.feeder.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.addChannel.AddChannelPresenter;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.sync.Synchronizer;
import com.stoyanr.feeder.util.DialogUtils;

public class AddChannelActivity extends Activity implements AddChannelPresenter.AddChannelInterface {

    private static final String TAG = "AddChannelActivity";

    private Handler handler;
    private EditText urlEditText;
    private Button addButton;
    private ProgressDialog progressDialog;
    private AddChannelPresenter presenter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.add_channel_activity);
        handler = new Handler();
        presenter = new AddChannelPresenter(this);
        presenter.init();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        presenter.hideProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
        presenter = null;
    }

    @Override
    public void init() {
        urlEditText = (EditText) findViewById(R.id.urlEditText);
        addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlEditText.getText().toString();
                Log.d(TAG, "Adding new channel with URL " + url);
                presenter.addChannel(url);
            }
        });
    }

    @Override
    public void showProgress() {
        progressDialog = ProgressDialog.show(AddChannelActivity.this,
            getResources().getText(R.string.please_wait), 
            getResources().getText(R.string.reading_feed), true, false);
    }

    @Override
    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private void setResult(long id) {
        getIntent().setData(ContentManager.getChannelUri(id));
        setResult(RESULT_OK, getIntent());
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void addChannelSuccess(long id) {
        presenter.hideProgress();
        setResult(id);
        finish();
    }

    @Override
    public void addChannelFailure(String msg) {
        presenter.hideProgress();
        DialogUtils.showErrorDialog(AddChannelActivity.this, msg);

    }


    private class AddChannelRunnable implements Runnable {
        private final String url;

        public AddChannelRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                final long id = addChannel(url);
                assert (id >= 0);
                postFinish(id);
            } catch (final Exception e) {
                postShowError(e.toString());
            }
        }

        private long addChannel(String url) throws Exception {
            Synchronizer refresh = new Synchronizer(AddChannelActivity.this);
            return refresh.sync(-1, url);
        }

        private void postFinish(final long id) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    presenter.hideProgress();
                    setResult(id);
                    finish();
                }
            });
        }

        private void postShowError(final String msg) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    presenter.hideProgress();
                    DialogUtils.showErrorDialog(AddChannelActivity.this, msg);
                }
            });
        }
    }


}
