package com.wrappy.android.server.account.body.request;


import com.wrappy.android.common.utils.Base64ImageFile;

public class GroupFileBody extends FileBody {
    private String groupId;

    public GroupFileBody(Type type, Base64ImageFile file) {
        this(null, type, file);
    }

    public GroupFileBody(String groupId, Type type, Base64ImageFile file) {
        super(type, file);
        this.groupId = groupId;
    }
}
