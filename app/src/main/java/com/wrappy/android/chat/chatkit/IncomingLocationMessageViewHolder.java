package com.wrappy.android.chat.chatkit;


import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageHolders.BaseIncomingMessageViewHolder;
import com.wrappy.android.R;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Taken from
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/LiteListDemoActivity.java
 */
public class IncomingLocationMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<MessageViewObject> implements OnMapReadyCallback {
    private View layout;
    private GoogleMap map;
    private MapView mapView;

    private TextView time;
    private TextView name;

    public IncomingLocationMessageViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        mapView = layout.findViewById(R.id.map);
        time = layout.findViewById(R.id.messageTime);
        name = layout.findViewById(R.id.messageUserName);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.setClickable(false);
            mapView.getMapAsync(this);
        }
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
        String[] locationData = message.getText().split(",");

        try {
            LatLng item = new LatLng(
                    Double.valueOf(locationData[0]),
                    Double.valueOf(locationData[1]));

            layout.setTag(this);
            mapView.setTag(item);
            setMapLocation();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(WrappyApp.getInstance());
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        setMapLocation();
    }

    public void clearMap() {
        if (map != null) {
            map.clear();
            map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    private void setMapLocation() {
        if (map == null) return;

        LatLng data = (LatLng) mapView.getTag();
        if (data == null) return;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(data, 15f));
        map.addMarker(new MarkerOptions().position(data));

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
