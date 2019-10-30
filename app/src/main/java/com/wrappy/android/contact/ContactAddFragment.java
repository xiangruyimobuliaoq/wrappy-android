package com.wrappy.android.contact;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;

import javax.inject.Inject;

public class ContactAddFragment extends SubFragment implements View.OnClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private static final String KEY_JID = "JID";

    private Button mButtonAddFriend;

    private ConstraintLayout mConstraintContainer;

    private EditText mEditTextSearch;

    private String mJid;

    private ImageButton mImageButtonClear;

    private ImageView mImageViewAvatar;

    private TextView mTextViewNoExist;
    private TextView mTextViewAddedFriend;
    private TextView mTextViewUserId;

    private ContactViewModel mContactViewModel;

    public static ContactAddFragment create(String userJid) {
        ContactAddFragment contactAddFragment = new ContactAddFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, userJid);
        contactAddFragment.setArguments(args);
        return contactAddFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_contact_add_page, container, false);

        mButtonAddFriend = view.findViewById(R.id.contact_add_button_add_friend);

        mImageViewAvatar = view.findViewById(R.id.contact_add_avatar);

        mEditTextSearch = view.findViewById(R.id.contact_add_searchview_search);

        mImageButtonClear = view.findViewById(R.id.contact_add_imagebutton_clear);

        mTextViewNoExist = view.findViewById(R.id.contact_add_textview_noexist);
        mTextViewAddedFriend = view.findViewById(R.id.contact_add_textview_added_friend);
        mTextViewUserId = view.findViewById(R.id.contact_add_textview_user_id);

        mConstraintContainer = view.findViewById(R.id.contact_add_constraintlayout_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.contact_add_friend_title));
        getInjector().inject(this);

        mContactViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ContactViewModel.class);

        mEditTextSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    userSearch(v.getText().toString(),"query");
                }
                return false;
            }
        });

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>0) {
                    mImageButtonClear.setVisibility(View.VISIBLE);
                } else {
                    mImageButtonClear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImageButtonClear.setOnClickListener(this);

        if (!getArguments().getString(KEY_JID).equals("")) {
            userSearch(getArguments().getString(KEY_JID), "qr");
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.contact_add_searchview_search:

                break;

            case R.id.contact_add_button_add_friend:
                mContactViewModel.addContact(mJid);
                getActivity().onBackPressed();
                break;

            case R.id.contact_add_imagebutton_clear:
                mEditTextSearch.setText("");
                break;

        }
    }

    private void userSearch(String userJid, String from) {
        mContactViewModel.getVCard(userJid).observe(ContactAddFragment.this, result -> {
            showLoadingDialog(result);
            switch(result.status) {

                case SUCCESS:
                    //Log.d("RESULT_SEARCH", result.data.toString());
                    if (result.data != null) {
                        if (result.data.username != null) {
                            Log.d("USERJID_FROM_SEARCH", result.data.username);
                            getUser(result.data.username, from);
                        } else {
                            getUser("", from);
                        }
                    } else {
                        Log.d("NULL_SEARCH_RESULT", "YEs");
                        getUser("", from);
                    }
                    break;
                case LOADING:
                    break;
                case CLIENT_ERROR:
                    Log.d("CLIENT_ERROR_SEARCH", userJid);
                    break;
                case SERVER_ERROR:
                    Log.d("SERVER_ERROR_SEARCH", userJid);
                    break;
            }
        });
    }

    private void getUser(String userJid, String from) {
        Log.d("USERJID_NULL", userJid);
        mContactViewModel.searchUser(userJid).observe(this,result-> {
            Log.d("USERJID_FROM_QR", userJid);
            switch (result.status) {

                case SUCCESS:
                    Log.d("RESULT_SEARCH", result.data);
                    if(result.data.equals("") || userJid.equals("")) {
                        mConstraintContainer.setVisibility(View.GONE);
                        mTextViewNoExist.setVisibility(View.VISIBLE);
                    } else if(result.data.equals("self")) {
                        mConstraintContainer.setVisibility(View.GONE);
                        mTextViewNoExist.setVisibility(View.GONE);
                        //mTextViewNoExist.setText("");
                    } else {
                        mConstraintContainer.setVisibility(View.VISIBLE);
                        mTextViewNoExist.setVisibility(View.GONE);
                        String[] last = result.data.split(",");
                        mJid = last[0];
                        String[] name = last[0].split("@");
                        mTextViewUserId.setText(name[0]);

                        if(last[1].equals("2")) {
                            mButtonAddFriend.setVisibility(View.GONE);
                            mTextViewAddedFriend.setVisibility(View.VISIBLE);
                            mTextViewAddedFriend.setText(R.string.contact_add_already_sent);
                        } else if(last[1].equals("1")) {
                            mButtonAddFriend.setVisibility(View.GONE);
                            mTextViewAddedFriend.setVisibility(View.VISIBLE);
                            mTextViewAddedFriend.setText(R.string.contact_add_received_request);
                        } else if(last[1].equals("0")) {
                            mButtonAddFriend.setVisibility(View.GONE);
                            mTextViewAddedFriend.setVisibility(View.VISIBLE);
                            mTextViewAddedFriend.setText(R.string.contact_add_already_friend);
                        } else {
                            mButtonAddFriend.setVisibility(View.VISIBLE);
                            mTextViewAddedFriend.setVisibility(View.GONE);
                            mButtonAddFriend.setOnClickListener(this);
                        }

                        InputUtils.loadAvatarImage(getContext(),
                                mImageViewAvatar,
                                mContactViewModel.getContactFileUrl(
                                        FileBody.Type.FILE_TYPE_AVATAR,
                                        ContactManager.getUserName(mJid)));

                    }
                    break;
                case LOADING:
                    break;
                case CLIENT_ERROR:
                    break;
                case SERVER_ERROR:
                    break;
            }

        });

    }
}
