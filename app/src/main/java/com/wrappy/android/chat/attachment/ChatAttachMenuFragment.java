package com.wrappy.android.chat.attachment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.wrappy.android.R;

public class ChatAttachMenuFragment extends Fragment implements OnClickListener {

    private OnClickListener mOnClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_attach_menu, container, false);
        view.findViewById(R.id.chat_attach_select_image).setOnClickListener(this);
        view.findViewById(R.id.chat_attach_take_photo).setOnClickListener(this);
        view.findViewById(R.id.chat_attach_location).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
