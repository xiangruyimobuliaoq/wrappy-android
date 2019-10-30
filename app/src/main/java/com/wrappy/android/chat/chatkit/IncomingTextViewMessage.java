package com.wrappy.android.chat.chatkit;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.wrappy.android.R;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class IncomingTextViewMessage extends MessageHolders.IncomingTextMessageViewHolder<MessageViewObject> {

    private View layout;

    private TextView time;
    private TextView name;
    private View translateClose;
    private TextView translateText;

    public IncomingTextViewMessage(View itemView) {
        super(itemView);
        layout = itemView;
        time = layout.findViewById(R.id.messageTime);
        name = layout.findViewById(R.id.messageUserName);
        translateClose = layout.findViewById(R.id.translate_close);
        translateText = layout.findViewById(R.id.translate_text);
    }

    @Override
    public void onBind(MessageViewObject message) {
        super.onBind(message);
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
        Log.d("DATE_COMPARE", message.getText() + " : " + ChatManager.getDateTime(message.getCreatedAt()));
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

        if (!TextUtils.isEmpty(message.getTranslatedText())) {
            translateText.setText(message.getTranslatedText());
            translateClose.setVisibility(View.VISIBLE);
            translateText.setVisibility(View.VISIBLE);
        } else {
            translateClose.setVisibility(View.GONE);
            translateText.setVisibility(View.GONE);
        }
    }


}
