package com.wrappy.android.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tubb.smrv.SwipeHorizontalMenuLayout;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.common.zxing.WrappyQRCaptureActivity;
import com.wrappy.android.contact.ContactFragment;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.XMPPRepository.ConnectionStatus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class HomeContactsFragment extends SubFragment implements View.OnClickListener {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private RecyclerView mContactRecyclerView;
    private ContactListAdapter mContactListAdapter;

    private ConstraintLayout mConstraintLayoutControls;

    private ConstraintLayout mConstraintLayoutAddFriend;

    private List<Contact> mContactList;

    private List<Contact> mSelectedContacts;

    private HomeViewModel mHomeViewModel;

    private BottomSheetDialog mBottomSheetDialog;

    private SearchView mSearchView;

    private TextView mTextViewInstruction;

    private TextView mTextViewSearchId;
    private TextView mTextViewReadQR;
    private TextView mTextViewShowQR;
    private TextView mTextViewCancel;

    private TextView mTextViewControlsCancel;
    private TextView mTextViewControlsOk;

    private LiveData<Resource<ConnectionStatus>> mConnectionStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home_contacts_page, container, false);

        mConstraintLayoutAddFriend = view.findViewById(R.id.contacts_constraintlayout_add_friend);
        mConstraintLayoutControls = view.findViewById(R.id.contacts_constraintlayout_controls);

        mContactRecyclerView = view.findViewById(R.id.contacts_recyclerview_contact_list);

        mSearchView = view.findViewById(R.id.contacts_searchview);

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        View sheetView = inflater.inflate(R.layout.frag_home_contacts_add_dialog, null);

        mBottomSheetDialog.setContentView(sheetView);

        mTextViewInstruction = view.findViewById(R.id.contacts_textview_instruction);

        mTextViewSearchId = sheetView.findViewById(R.id.reg_profile_image_textview_take);
        mTextViewReadQR = sheetView.findViewById(R.id.reg_profile_image_textview_select);
        mTextViewShowQR = sheetView.findViewById(R.id.reg_profile_image_textview_select_stock);
        mTextViewCancel = sheetView.findViewById(R.id.reg_profile_image_textview_delete);

        mTextViewControlsCancel = view.findViewById(R.id.contacts_textview_cancel);
        mTextViewControlsOk = view.findViewById(R.id.contacts_textview_ok);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mHomeViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(HomeViewModel.class);
        mConnectionStatus = mHomeViewModel.getConnectionStatus();

        mConstraintLayoutAddFriend.setOnClickListener(this);

        mContactList = new ArrayList<>();

        mSelectedContacts = new ArrayList<>();

        mTextViewSearchId.setText(R.string.home_contact_search_id);
        mTextViewReadQR.setText(R.string.home_contact_read_qr);
        mTextViewShowQR.setText(R.string.home_contact_show_qr);
        mTextViewCancel.setText(R.string.dialog_cancel);


        mTextViewSearchId.setOnClickListener(this);
        mTextViewReadQR.setOnClickListener(this);
        mTextViewShowQR.setOnClickListener(this);
        mTextViewCancel.setOnClickListener(this);

        mTextViewControlsCancel.setOnClickListener(this);
        mTextViewControlsOk.setOnClickListener(this);

        mContactListAdapter = new ContactListAdapter(mContactList, this);
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mContactRecyclerView.setAdapter(mContactListAdapter);

        mHomeViewModel.getAllContacts().observe(getViewLifecycleOwner(), result -> {
            mContactList = result;
            if (result.size() <= 0) {
                mTextViewInstruction.setVisibility(View.VISIBLE);
            } else {
                mTextViewInstruction.setVisibility(View.GONE);
            }
            mContactListAdapter.setContactList(mContactList);
        });

        mSearchView.setOnClickListener(this);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("") || newText.equals(null)) {
                    mContactListAdapter.setContactList(mContactList);
                } else {
                    LiveData<List<Contact>> contactData = mHomeViewModel.getQueryContacts(newText + "%");
                    contactData.observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
                        @Override
                        public void onChanged(@Nullable List<Contact> result) {
                            mContactListAdapter.setContactList(result);
                            contactData.removeObserver(this);
                        }
                    });
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // QR Code parsing
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            mNavigationManager.showContactPage(ContactFragment.TYPE_ADD, result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.contacts_constraintlayout_add_friend: /** ADD FRIEND from HOME_CONTACTS */
                mBottomSheetDialog.show();
                //mNavigationManager.showContactPage(ContactFragment.TYPE_ADD, "");
                break;

            case R.id.contact_list_menu_edit: /** EDIT FRIEND */
                if(mConnectionStatus.getValue().data==ConnectionStatus.AUTHENTICATED) {
                    mNavigationManager.showContactPage(ContactFragment.TYPE_EDIT, v.getTag().toString());
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                break;

            case R.id.contact_list_accept: /** ACCEPT FRIEND REQUEST **/
                if(mConnectionStatus.getValue().data==ConnectionStatus.AUTHENTICATED) {
                    mHomeViewModel.acceptRequest(v.getTag().toString());
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                break;

            case R.id.contact_list_reject: /** REJECT FRIEND REQUEST **/
                if(mConnectionStatus.getValue().data==ConnectionStatus.AUTHENTICATED) {
                    mHomeViewModel.removeContact(v.getTag().toString());
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                break;

            case R.id.contact_list_cancel_request: /** CANCEL FRIEND REQUEST **/
                if(mConnectionStatus.getValue().data==ConnectionStatus.AUTHENTICATED) {
                    mHomeViewModel.cancelRequest(v.getTag().toString());
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                break;

            case R.id.sml: /** SELECT CONTACT **/

                if (!mSelectedContacts.contains(v.getTag())) {
                    mSelectedContacts.add((Contact) v.getTag());

                } else if (mSelectedContacts.contains(v.getTag())) {
                    mSelectedContacts.remove(v.getTag());

                }
                if (mSelectedContacts.size() > 0) {
                    mConstraintLayoutControls.setVisibility(View.VISIBLE);
                    if(mSelectedContacts.size() > 1) {
                        mTextViewControlsOk.setText(getString(R.string.group_chat));
                    } else {
                        mTextViewControlsOk.setText(getString(R.string.dialog_ok));
                    }
                } else {
                    mConstraintLayoutControls.setVisibility(View.GONE);
                }
                mContactListAdapter.notifyDataSetChanged();
                break;

            case R.id.reg_profile_image_textview_take: /** SEARCH **/
                mNavigationManager.showContactPage(ContactFragment.TYPE_ADD, "");
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_select: /** READ QR **/
                IntentIntegrator.forSupportFragment(this)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setBeepEnabled(false)
                        .setOrientationLocked(true)
                        .setPrompt("")
                        .setCaptureActivity(WrappyQRCaptureActivity.class)
                        .initiateScan();
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_select_stock: /** SHOW QR **/
                mNavigationManager.showMyQRCodePage(mHomeViewModel.getUserId());
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_delete: /** CANCEL **/
                mBottomSheetDialog.dismiss();
                break;

            case R.id.contacts_searchview:
                mSearchView.setIconified(false);
                break;

            case R.id.contacts_textview_ok:
                if(mSelectedContacts.size()>1) {
                    if(mConnectionStatus.getValue().data==ConnectionStatus.AUTHENTICATED) {
                        ArrayList<String> participants = new ArrayList<>();
                        for (Contact contact : mSelectedContacts) {
                            participants.add(contact.getContactId());
                        }

                        mHomeViewModel.createMUC(participants).observe(getViewLifecycleOwner(), result -> {
                            showLoadingDialog(result);
                            switch (result.status) {

                                case SUCCESS:
                                    mNavigationManager.showChatPage(result.data, mHomeViewModel.getRoomName(), "groupchat");
                                    mConstraintLayoutControls.setVisibility(View.GONE);
                                    break;
                                case LOADING:
                                    break;
                                case CLIENT_ERROR:
                                    break;
                                case SERVER_ERROR:
                                    showAlertDialog(result.message, null);
                                    mSelectedContacts.clear();
                                    mConstraintLayoutControls.setVisibility(View.GONE);
                                    mContactListAdapter.notifyDataSetChanged();
                                    break;
                            }
                        });
                    } else {
                        showAlertDialog(getString(R.string.no_internet_connection), null);
                    }

                } else {
                    mNavigationManager.showChatPage(mSelectedContacts.get(0).getContactId(), mSelectedContacts.get(0).getContactName(), "chat");
                    mConstraintLayoutControls.setVisibility(View.GONE);
                }
                break;

            case R.id.contacts_textview_cancel:
                mSelectedContacts.removeAll(mSelectedContacts);
                mConstraintLayoutControls.setVisibility(View.GONE);
                mContactListAdapter.notifyDataSetChanged();
                break;

        }
    }

    public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

        private List<Contact> mContactList;
        View.OnClickListener mOnClickListener;

        ContactListAdapter(List<Contact> contactList, View.OnClickListener onClickListener) {
            mContactList = contactList;
            mOnClickListener = onClickListener;
        }


        @NonNull
        @Override
        public ContactListAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = getLayoutInflater().inflate(R.layout.home_contact_list_item, parent, false);
                    break;
                case 1:
                    view = getLayoutInflater().inflate(R.layout.home_contact_invite_list_item, parent, false);
                    break;

                case 2:
                    view = getLayoutInflater().inflate(R.layout.home_contact_sent_list_item, parent, false);
                    break;

                default:
                    return null;

            }

            return new ContactViewHolder(view, mOnClickListener);
        }

        public void setContactList(List<Contact> contactList) {
            mContactList = contactList;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            if(mContactList.size()-1>position) {
                if (mContactList.get(position).getContactType() != mContactList.get(position + 1).getContactType()) {
                    holder.bindTo(mContactList.get(position), true);
                } else {
                    holder.bindTo(mContactList.get(position), false);
                }
            } else {
                holder.bindTo(mContactList.get(position), true);
            }
        }

        @Override
        public int getItemCount() {
            return mContactList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mContactList.get(position).getContactType();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {

            SwipeHorizontalMenuLayout mContainerLayout;

            ImageView imageViewAvatar;
            ImageView imageViewCheckBox;
            ImageView imageViewStatus;

            ImageButton imageButtonAccept;
            ImageButton imageButtonEdit;
            ImageButton imageButtonCancelRequest;
            ImageButton imageButtonReject;

            TextView textViewUserId;
            TextView textViewStatus;

            View borderFull;

            View.OnClickListener mOnClickListener;

            public ContactViewHolder(View itemView, View.OnClickListener onClickListener) {
                super(itemView);

                mContainerLayout = itemView.findViewById(R.id.sml);

                imageViewAvatar = itemView.findViewById(R.id.contact_list_avatar);
                imageViewCheckBox = itemView.findViewById(R.id.contact_list_check_box);
                imageViewStatus = itemView.findViewById(R.id.contact_list_imageview_status);

                imageButtonAccept = itemView.findViewById(R.id.contact_list_accept);
                imageButtonEdit = itemView.findViewById(R.id.contact_list_menu_edit);
                imageButtonCancelRequest = itemView.findViewById(R.id.contact_list_cancel_request);
                imageButtonReject = itemView.findViewById(R.id.contact_list_reject);

                textViewUserId = itemView.findViewById(R.id.contact_list_user_id);
                textViewStatus = itemView.findViewById(R.id.contact_list_status);

                borderFull = itemView.findViewById(R.id.end_border_full);

                mOnClickListener = onClickListener;

            }

            void bindTo(Contact contact, boolean isLast) {
                textViewUserId.setText(contact.getContactName());

                if(!isLast) {
                    borderFull.setVisibility(View.GONE);
                } else {
                    borderFull.setVisibility(View.VISIBLE);
                    Log.d("LAST", contact.getContactName());
                }

                InputUtils.loadAvatarImage(getContext(),
                        imageViewAvatar,
                        mHomeViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(contact.getContactId())));

                switch (contact.getContactType()) {
                    case 0:
                        mContainerLayout.setOnClickListener(mOnClickListener);
                        mContainerLayout.setTag(contact);
                        imageButtonEdit.setOnClickListener(mOnClickListener);
                        imageButtonEdit.setTag(contact.getContactId());
                        if (contact.getContactPresence().equals("available")) {
                            imageViewStatus.setImageDrawable(getContext().getDrawable(R.drawable.status_active));
                            textViewStatus.setTextColor(getResources().getColor(R.color.status_online));
                            textViewStatus.setText(getString(R.string.status_online));
                        } else if (contact.getContactPresence().equals("unavailable")) {
                            imageViewStatus.setImageDrawable(getContext().getDrawable(R.drawable.status_disable));
                            textViewStatus.setTextColor(getResources().getColor(R.color.status_offline));
                            textViewStatus.setText(getString(R.string.status_offline));
                        }
                        if (mSelectedContacts.contains(contact)) {
                            mContainerLayout.setBackgroundColor(getResources().getColor(R.color.selected_contact));
                            imageViewCheckBox.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_selected));
                        } else if (!mSelectedContacts.contains(contact)) {
                            mContainerLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
                            imageViewCheckBox.setImageDrawable(getResources().getDrawable(R.drawable.checkbox));
                        }
                        break;

                    case 1:
                        // TODO: UI for requests
                        imageButtonAccept.setOnClickListener(mOnClickListener);
                        imageButtonAccept.setTag(contact.getContactId());
                        imageButtonReject.setOnClickListener(mOnClickListener);
                        imageButtonReject.setTag(contact.getContactId());
                        //textViewStatus.setText("People you may know");
                        break;

                    case 2:
                        // TODO: UI for sent invites
                        imageButtonCancelRequest.setOnClickListener(mOnClickListener);
                        imageButtonCancelRequest.setTag(contact.getContactId());
                        break;
                }


            }

        }

    }


}
