package com.wrappy.android.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dan Chua on 02/04/2019
 */
public class NotificationID {
    private final static AtomicInteger chatId = new AtomicInteger(1);

    private final static Map<String, Integer> notifGroupIdMap = new HashMap<>();

    public static int getID() {
        return chatId.incrementAndGet();
    }

    public static int getGroupID(String jid) {
        Integer groupIdFromMap = notifGroupIdMap.get(jid);

        int groupId;
        if (groupIdFromMap == null) {
            groupId = getID();
            notifGroupIdMap.put(jid, groupId);
        } else {
            groupId = groupIdFromMap;
        }

        return groupId;
    }
}
