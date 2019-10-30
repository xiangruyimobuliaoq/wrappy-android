package com.wrappy.android.chat.chatkit;

import java.text.SimpleDateFormat;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.wrappy.android.R;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.common.ui.WPRoundedImageView;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;

/**
 * Created by Dan Chua on 2019-05-14
 */
public class OutcomingImageMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<MessageViewObject> {

    private View layout;
    private WPRoundedImageView imageView;

    private TextView time;
    private TextView name;

    public OutcomingImageMessageViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        imageView = layout.findViewById(R.id.image);
        imageView.setCorners(
                R.dimen.message_bubble_corners_radius,
                R.dimen.message_bubble_corners_radius,
                R.dimen.message_bubble_corners_radius,
                R.dimen.message_bubble_corners_radius);
        time = layout.findViewById(R.id.messageTime);
        name = layout.findViewById(R.id.messageUserName);
    }

    @Override
    public void onBind(MessageViewObject message) {
        super.onBind(message);
        layout.setTag(this);
        Glide.with(WrappyApp.getInstance())
                .load(message.getText())
                .apply(RequestOptions.downsampleOf(DownsampleStrategy.CENTER_INSIDE))
                .into(imageView);
        Log.d("PS_URL", message.getText());
        if(name!=null) {
            if(message.getUser().getName()==null) {
                if(message.getType() == Message.MESSAGE_TYPE_GROUP) {
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

    }

}
