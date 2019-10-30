package com.wrappy.android.contact;

import android.support.v7.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;

import javax.inject.Inject;

public class ContactEditFragment extends SubFragment implements View.OnClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ContactNavigationManager mContactNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    private static final String KEY_JID = "JID";

    private Button mButtonBlock;
    private Button mButtonDelete;
    private Button mButtonSave;

    private EditText mEditTextUserName;

    private ImageView mImageViewAvatar;
    private ImageView mImageViewBanner;

    private TextView mTextViewUserId;
    private TextView mTextViewUserName;

    private ContactViewModel mContactViewModel;

    private Contact mContact;

    private AlertDialog.Builder mAlertDialog;

    public static ContactEditFragment create(String userJid) {
        ContactEditFragment contactEditFragment = new ContactEditFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, userJid);
        contactEditFragment.setArguments(args);
        return contactEditFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_contact_edit_page, container, false);

        mButtonBlock = view.findViewById(R.id.contact_edit_button_block);
        mButtonDelete = view.findViewById(R.id.contact_edit_button_delete);
        mButtonSave = view.findViewById(R.id.contact_edit_button_save);

        mImageViewAvatar = view.findViewById(R.id.contact_edit_imageview_avatar);
        mImageViewBanner = view.findViewById(R.id.contact_edit_imageview_banner);

        mEditTextUserName = view.findViewById(R.id.contact_edit_edittext_name);

        mTextViewUserId = view.findViewById(R.id.contact_edit_textview_user_id);
        mTextViewUserName = view.findViewById(R.id.contact_edit_textview_user_name);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        setToolbarTitle("");

        mContactViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ContactViewModel.class);

        mContactViewModel.getVCard(ContactManager.getUserName(getArguments().getString(KEY_JID))).observe(this, result -> {
            showLoadingDialog(result);
            switch(result.status) {

                case SUCCESS:
                    mTextViewUserName.setText(result.data.firstName);
                    break;
                case LOADING:
                    break;
                case CLIENT_ERROR:
                    break;
                case SERVER_ERROR:
                    break;
            }
        });

        mContactViewModel.getContact(getArguments().getString(KEY_JID)).observe(this, result-> {
            mContact = result.data;

            mTextViewUserId.setText("ID: " + mContact.getContactId().split("@")[0]);

            mEditTextUserName.setText(mContact.getContactName());

            InputUtils.loadAvatarImage(
                    getContext(),
                    mImageViewAvatar,
                    mContactViewModel.getContactFileUrl(
                            FileBody.Type.FILE_TYPE_AVATAR,
                            ContactManager.getUserName(mContact.getContactId())));

            InputUtils.loadBannerImage(getContext(),
                    mImageViewBanner,
                    mContactViewModel.getContactFileUrl(
                            FileBody.Type.FILE_TYPE_BACKGROUND,
                            ContactManager.getUserName(mContact.getContactId())));

            mButtonSave.setOnClickListener(this);
            mButtonBlock.setOnClickListener(this);
            mButtonDelete.setOnClickListener(this);

        });

        mAlertDialog = new AlertDialog.Builder(getContext());
        mAlertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_edit_button_save: /** SAVE BUTTON **/

                String [] name = mContact.getContactId().split("@");
                if (mEditTextUserName.getText().toString().equals("")) {
                    changeName(mContact.getContactId(), name[0]);
                } else {
                    changeName(mContact.getContactId(), mEditTextUserName.getText().toString());
                }
                break;

            case R.id.contact_edit_button_block: /** BLOCK BUTTON **/

                mAlertDialog.setTitle(R.string.contact_edit_block_confirm_title);
                mAlertDialog.setMessage(R.string.dialog_blocked_user_message);
                mAlertDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContactViewModel.blockContact(mContact.getContactId(), mContact.getContactName());
                        showAlertDialog(getString(R.string.contact_edit_block_success, mContact.getContactName()), null);
                        mContactNavigationManager.getFragmentManager().popBackStack();
                        mNavigationManager.getFragmentManager().popBackStack();
                    }
                });

                mAlertDialog.show();

                break;

            case R.id.contact_edit_button_delete: /** DELETE BUTTON **/

                mAlertDialog.setTitle(R.string.remove_friend);
                mAlertDialog.setMessage(getString(R.string.contact_edit_remove_confirm, mContact.getContactName()));
                mAlertDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContactViewModel.removeContact(mContact.getContactId());
                        showAlertDialog(getString(R.string.contact_edit_delete_success, mContact.getContactName()), null);
                        mContactNavigationManager.getFragmentManager().popBackStack();
                        mNavigationManager.getFragmentManager().popBackStack();
                    }
                });

                mAlertDialog.show();

                break;

        }
    }

    public void changeName(String contactId, String contactName) {
        mContactViewModel.changeContactName(contactId,contactName).observe(this, result -> {
            showLoadingDialog(result);
            switch(result.status) {

                case SUCCESS:
                    mEditTextUserName.setText(contactName);
                    mEditTextUserName.clearFocus();
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
