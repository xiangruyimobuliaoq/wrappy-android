package com.wrappy.android.firebase;


import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import javax.inject.Inject;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wrappy.android.MainActivity;
import com.wrappy.android.R;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.utils.NotificationID;
import com.wrappy.android.di.ServiceProviderModule;
import com.wrappy.android.xmpp.XMPPRepository;

public class WrappyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    XMPPRepository mXmppRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        ((WrappyApp) getApplication()).getInjector()
                .plusServiceInjector(new ServiceProviderModule())
                .inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: handle message receive
        // TODO: proper bundling of messages (https://blog.danlew.net/2017/02/07/correctly-handling-bundled-android-notifications/)
        String dataTitle = remoteMessage.getData().get("title");
        String dataJid = remoteMessage.getData().get("JID");
        String dataName = remoteMessage.getData().get("name");
        String dataBody = remoteMessage.getData().get("body");
        String dataCategory = remoteMessage.getData().get("category");
        String dataBadge = remoteMessage.getData().get("chatBadge");

        Log.d("FB_TITLE", dataTitle);
        Log.d("FB_JID", dataJid);
        Log.d("FB_NAME", dataName);
        Log.d("FB_BODY", dataBody);
        //Log.d("FB_BADGE", dataBadge);

        int unreadCount = dataBadge==null? 1 : Integer.valueOf(dataBadge);

        if (dataCategory.equals("user")) {
            dataBody = dataBody.replaceFirst(dataName + ": ", "");
        }

        Intent intent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle extras = new Bundle();
        extras.putString("JID", dataJid);
        extras.putString("name", dataName);
        extras.putString("category", dataCategory);
        extras.putString("badge", dataBadge);
        intent.putExtras(extras);

        // make value unique so that PendingIntent.getActivity won't replace existing intents from other notifications
        intent.setAction(dataJid);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String groupSummaryTitle = dataTitle;
        String groupSummaryContent = "You have " + dataBadge + " unread messages from " + dataName;
        if (unreadCount == 1) {
            groupSummaryTitle = dataName;
            groupSummaryContent = dataBody;
        }

        NotificationCompat.Builder groupBuilder = new NotificationCompat.Builder(this, "WRAPPY_MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.notify_wrappy)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(groupSummaryTitle)
                .setContentText(groupSummaryContent) // for API < 24
                .setContentIntent(pendingIntent)
                .setGroupSummary(true)
                .setGroup(dataJid)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "WRAPPY_MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.notify_wrappy)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(dataName)
                .setContentText(dataBody)
                .setContentIntent(pendingIntent)
                .setGroup(dataJid)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (!mXmppRepository.isForeground()) {
            notificationManagerCompat.notify(NotificationID.getGroupID(dataJid), groupBuilder.build());
            notificationManagerCompat.notify(NotificationID.getID(), builder.build());
        }
    }

    @Override
    public void onNewToken(String s) {
        mXmppRepository.sendFirebaseToken(s);
        // TODO: send registration IQ to server
    }
}
