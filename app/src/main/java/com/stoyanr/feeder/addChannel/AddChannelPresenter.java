package com.stoyanr.feeder.addChannel;

import android.content.Context;
import com.stoyanr.feeder.sync.Synchronizer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddChannelPresenter {

    private AddChannelInterface acInterface;

    public AddChannelPresenter(AddChannelInterface addChannelInterface) {
        acInterface = addChannelInterface;
    }

    public void init() {
        acInterface.init();
    }

    public void destroy() {
        acInterface = null;
    }

    public void hideProgress() {
        acInterface.hideProgress();
    }

    public void addChannel(String url) {
        acInterface.showProgress();
        handleSyncChannel(url);
    }

    private long addAndSyncChannel(String url) {
        Synchronizer refresh = new Synchronizer(acInterface.getContext());
        try {
            return refresh.sync(-1, url);
        } catch (Exception e) {
            return -1;
        }
    }

    private void handleSyncChannel(String url) {
        Single.fromCallable(() -> addAndSyncChannel(url) )
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeOn(Schedulers.io())
              .subscribe(t -> {
                  if (t > 0) {
                      acInterface.addChannelSuccess(t);
                  } else {
                      acInterface.addChannelFailure("Error adding channel");
                  }
              });
    }

    public interface AddChannelInterface {
        void showProgress();
        void hideProgress();
        void init();
        void addChannelSuccess(long id);
        void addChannelFailure(String msg);
        Context getContext();
    }
}
