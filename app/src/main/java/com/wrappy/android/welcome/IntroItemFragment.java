package com.wrappy.android.welcome;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wrappy.android.R;

import org.w3c.dom.Text;

public class IntroItemFragment extends Fragment {

    public static final String IMAGE_ID = "imageId";
    public static final String STRING_ID_TITLE = "stringIdTitle";
    public static final String STRING_ID_TEXT = "stringIdText";

    public static IntroItemFragment create(int imageId, String stringIdTitle, String stringIdText) {
        Bundle bundle = new Bundle();
        bundle.putInt(IMAGE_ID, imageId);
        bundle.putString(STRING_ID_TITLE, stringIdTitle);
        bundle.putString(STRING_ID_TEXT, stringIdText);
        IntroItemFragment introItemFragment = new IntroItemFragment();
        introItemFragment.setArguments(bundle);
        return introItemFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_welcome_intro_item, container, false);
        ImageView imageView = view.findViewById(R.id.intro_imageview);
        TextView textViewTitle = view.findViewById(R.id.intro_textview_title);
        TextView textViewText = view.findViewById(R.id.intro_textview_text);
        if(getArguments().getInt(IMAGE_ID)==getResources().getIdentifier("logo","drawable", getActivity().getPackageName())) {
        }
        imageView.setImageResource(getArguments().getInt(IMAGE_ID));
        textViewTitle.setText(getArguments().getString(STRING_ID_TITLE));
        textViewText.setText(getArguments().getString(STRING_ID_TEXT));
        return view;
    }
}
