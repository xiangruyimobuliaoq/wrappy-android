package com.wrappy.android.chat.chatkit;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.wrappy.android.R;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;

import java.text.SimpleDateFormat;

/**
 * Created by Dan Chua on 2019-05-14
 */
public class OutcomingStickerMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<MessageViewObject> {

    private View layout;
    private ImageView imageView;

    private TextView time;
    private TextView name;

    public OutcomingStickerMessageViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        imageView = layout.findViewById(R.id.image);
        time = layout.findViewById(R.id.messageTime);
        name = layout.findViewById(R.id.messageUserName);
    }

    @Override
    public void onBind(MessageViewObject message) {
        super.onBind(message);
        layout.setTag(this);

        long elapsedDays = ChatManager.getDateTime(message.getCreatedAt());
        if(elapsedDays==0) {
            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            //time.setText(simpleDateFormat.format(message.getCreatedAt()));
            time.setText(DateFormat.getTimeFormat(time.getContext()).format(message.getCreatedAt()));
            //time.setText(DateFormat.format("HH:mm:ss", message.getCreatedAt()));
            //} else if(elapsedDays > -7) {
            //time.setText(DateFormat.format("EEE", message.getCreatedAt()));
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd");
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            time.setText(simpleDateFormat.format(message.getCreatedAt()) + " " + DateFormat.getTimeFormat(time.getContext()).format(message.getCreatedAt()));
            //time.setText(DateFormat.format("yy/MM/dd HH:mm", message.getCreatedAt()));
            //time.setText(DateFormat.format("yy/MM/dd", message.getCreatedAt()));
        }

        if(name!=null) {
            if(message.getUser().getName()==null) {
                if(message.getType()== Message.MESSAGE_TYPE_GROUP) {
                    name.setText(ContactManager.getUserName(message.getUser().getId()));
                } else {
                    name.setVisibility(View.GONE);
                }
            } else {
                if(message.getType() == Message.MESSAGE_TYPE_GROUP) {
                    name.setText(message.getUser().getName());
                } else {
                    name.setVisibility(View.GONE);
                }
            }
        }
        Glide.with(WrappyApp.getInstance()).
                load("file:///android_asset/sticker/" + message.getText() + ".png").
                apply(RequestOptions.centerCropTransform()).
                into(imageView);

    }

}
