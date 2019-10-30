package com.wrappy.android.server.account.body.request;


import com.wrappy.android.common.utils.Base64ImageFile;

public class FileBody {
    public enum Type {
        FILE_TYPE_AVATAR("avatar"),
        FILE_TYPE_BACKGROUND("background_image");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private String type;
    private Base64ImageFile file;

    public FileBody(Type type, Base64ImageFile file) {
        this.type = type.toString();
        this.file = file;
    }
}
