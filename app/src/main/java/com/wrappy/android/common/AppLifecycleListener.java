package com.wrappy.android.common;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.xmpp.XMPPRepository;

public class AppLifecycleListener implements LifecycleObserver {

    private AppExecutors mAppExecutors;
    private AppDatabase mAppDatabase;

    private XMPPRepository mXMPPRepository;

    public AppLifecycleListener(XMPPRepository xmppRepository, AppExecutors appExecutors, AppDatabase appDatabase) {
        mAppExecutors = appExecutors;
        mAppDatabase = appDatabase;
        mXMPPRepository = xmppRepository;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMovetoForeground() {
        mXMPPRepository.setIsForeground(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMovetoBackground() {
        //mXMPPRepository.disconnectXMPP();
        mXMPPRepository.setIsForeground(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mXMPPRepository.disconnectXMPP();
    }

}
