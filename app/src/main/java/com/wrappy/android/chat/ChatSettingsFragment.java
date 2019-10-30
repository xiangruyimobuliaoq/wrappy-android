package com.wrappy.android.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.tubb.smrv.SwipeHorizontalMenuLayout;
import com.wrappy.android.ImageCropperActivity;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.ui.WrappyFilteredEditText;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.wrappy.android.chat.ChatFragment.KEY_JID;

public class ChatSettingsFragment extends SubFragment implements View.OnClickListener {

    private static final int REQUEST_ACTIVITY_GALLERY = 0;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ChatNavigationManager mChatNavigationManager;

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    AppExecutors mAppExecutors;

    private ChatViewModel mChatViewModel;

    private EditText mEditTextName;

    private List<Contact> mMemberList;

    private BottomSheetDialog mBottomSheetDialog;

    private BackgroundListAdapter mBackgroundListAdapter;
    private MemberListAdapter mMemberListAdapter;

    private ImageView mImageViewGroupEdit;
    private ImageView mImageViewGroupAvatar;

    private RecyclerView mRecyclerViewBackgrounds;
    private RecyclerView mRecyclerViewMembers;

    private RelativeLayout mRelativeLayoutGroup;
    private RelativeLayout mRelativeLayoutSearch;
    private RelativeLayout mRelativeLayoutNotification;
    private RelativeLayout mRelativeLayoutBackground;
    private RelativeLayout mRelativeLayoutLanguage;
    private RelativeLayout mRelativeLayoutAdd;

    private SwitchCompat mSwitchCompatNotif;

    private TextView mTextViewGroupName;

    private ChatAndBackground mChat;

    public static ChatSettingsFragment create(String chatJid) {
        ChatSettingsFragment chatSettingsFragment = new ChatSettingsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, chatJid);
        chatSettingsFragment.setArguments(args);
        return chatSettingsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_settings_page, container, false);

        View bottomView = inflater.inflate(R.layout.frag_chat_change_background_dialog, null);

        mBottomSheetDialog = new BottomSheetDialog(getContext());
        mBottomSheetDialog.setContentView(bottomView);

        mEditTextName = new EditText(getContext());

        mImageViewGroupEdit = view.findViewById(R.id.chat_settings_imageview_edit);
        mImageViewGroupAvatar = view.findViewById(R.id.chat_settings_imageview_group_avatar);

        mRecyclerViewBackgrounds = bottomView.findViewById(R.id.chat_change_background_recyclerview);
        mRecyclerViewMembers = view.findViewById(R.id.chat_settings_recyclerview_members);

        mRelativeLayoutGroup = view.findViewById(R.id.chat_settings_container_group_details);
        mRelativeLayoutSearch = view.findViewById(R.id.chat_settings_container_search);
        mRelativeLayoutNotification = view.findViewById(R.id.chat_settings_container_notification);
        mRelativeLayoutBackground = view.findViewById(R.id.chat_settings_container_background);
        mRelativeLayoutLanguage = view.findViewById(R.id.chat_settings_container_language);
        mRelativeLayoutAdd = view.findViewById(R.id.chat_settings_container_add);

        mSwitchCompatNotif = view.findViewById(R.id.chat_settings_switchcompat_notification);

        mTextViewGroupName = view.findViewById(R.id.chat_settings_textview_group_name);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        setToolbarTitle(getString(R.string.chat_message_toolbar_setting));

        mMemberList = new ArrayList<>();
        mMemberListAdapter = new MemberListAdapter(mMemberList, this);

        mRecyclerViewMembers.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerViewMembers.setAdapter(mMemberListAdapter);

        mChatViewModel = ViewModelProviders
                .of(getParentFragment(), mViewModelFactory)
                .get(ChatViewModel.class);

        mChatViewModel.getChat(getArguments().getString(KEY_JID)).observe(this, chat -> {
            showLoadingDialog(chat);
            switch (chat.status) {

                case SUCCESS:
                    mChat = chat.data;
                    initChatSettings();
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

    public void initChatSettings() {
        mImageViewGroupEdit.setOnClickListener(this);

        mRelativeLayoutSearch.setOnClickListener(this);
        mRelativeLayoutNotification.setOnClickListener(this);
        mRelativeLayoutBackground.setOnClickListener(this);
        mRelativeLayoutLanguage.setOnClickListener(this);
        mRecyclerViewBackgrounds.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));

        try {
            mBackgroundListAdapter = new BackgroundListAdapter(getContext(), Arrays.asList(getActivity().getAssets().list("bg")), this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecyclerViewBackgrounds.setAdapter(mBackgroundListAdapter);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mEditTextName.setLayoutParams(lp);

        if (mChat.getChatType().equals("chat")) {
            Contact currentUser = new Contact(mChatViewModel.getUserId(), "available", mChatViewModel.getUserName(), 2);
            Contact otherUser = new Contact(mChat.getChatId(), "unavailable", mChat.getChatName(), 2);
            mMemberList.add(currentUser);
            mMemberList.add(otherUser);
            Log.d("ID", mChat.getChatId());
            Log.d("NAME", mChat.getChatName());
            Log.d("MemberSize", String.valueOf(mMemberList.size()));
            mMemberListAdapter.setMemberList(mMemberList);
        } else {
            // TODO: Set Avatar, Banner and Group Name
            mRelativeLayoutGroup.setVisibility(View.VISIBLE);
            mRelativeLayoutAdd.setVisibility(View.VISIBLE);
            mRelativeLayoutAdd.setOnClickListener(this);

            mTextViewGroupName.setText(mChat.getChatName());

            mMemberList = mChatViewModel.getPariticipants(mChat.getChatId(), mChat.getChatType());

            mMemberListAdapter.setMemberList(mMemberList);

            InputUtils.loadAvatarImage(
                    getContext(),
                    mImageViewGroupAvatar,
                    mChatViewModel.getContactFileUrl(
                            FileBody.Type.FILE_TYPE_AVATAR,
                            ContactManager.getUserName(mChat.getChatId()),
                            true));

        }

        if (mChat.isChatNotification()) {
            mSwitchCompatNotif.setChecked(true);
        } else {
            mSwitchCompatNotif.setChecked(false);
        }
        mSwitchCompatNotif.jumpDrawablesToCurrentState();

        mSwitchCompatNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Notif", String.valueOf(isChecked));
                LiveData<Resource<Boolean>> toggleNotif = mChatViewModel.setChatNotification(isChecked, mChat.getChatId(), mChat.getChatType());
                toggleNotif.observe(ChatSettingsFragment.this, result -> {
                    showLoadingDialog(result);
                    switch(result.status) {

                        case SUCCESS:
                            break;
                        case LOADING:
                            break;
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            mSwitchCompatNotif.setOnCheckedChangeListener(null);
                            mSwitchCompatNotif.setChecked(!isChecked);
                            mSwitchCompatNotif.setOnCheckedChangeListener(this);
                            break;
                    }
                });
            }
        });


    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder;
        switch (v.getId()) {

            case R.id.chat_settings_container_search:
                mChatViewModel.setSearch(true);
                Log.d("Search", String.valueOf(mChatViewModel.getSearch()));
                getFragmentManager().popBackStack();
                break;

            case R.id.chat_settings_imageview_edit:
                alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(R.string.chat_group_detail_dialog_group_name);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.chat_edit_dialog, null, false);
                WrappyFilteredEditText editText = view.findViewById(R.id.chat_settings_edittext_edit);
                alertDialogBuilder.setView(view);
                alertDialogBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChatViewModel.setRoomName(editText.getText().toString());
                        mTextViewGroupName.setText(editText.getText().toString());
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.dialog_cancel, null);
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.length()>0 && !s.toString().trim().isEmpty()) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                break;

            case R.id.chat_settings_container_add:
                mChatNavigationManager.showChatAddMembersPage((ArrayList<Contact>) mMemberList);
                break;

            case R.id.chat_settings_container_background:
                mBottomSheetDialog.show();
                break;

            case R.id.chat_settings_container_language:
                mChatNavigationManager.showChatTranslatePage(getArguments().getString(KEY_JID), false);
                break;

            case R.id.imageBackground_container:
                if (v.getTag().equals("add")) {
                    startActivityForResult(ImageCropperActivity.createIntent(getContext(),
                            ImageCropperActivity.FROM_BACKGROUND,
                            ImageCropperActivity.TYPE_GALLERY),
                            0);
                } else {
                    Log.d("BG_FILE_NAME", v.getTag().toString());
                    alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(R.string.app_name);
                    alertDialogBuilder.setMessage(R.string.chat_setting_dialog_confirm_bg_select);
                    alertDialogBuilder.setPositiveButton(R.string.chat_setting_dialog_bg_select_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mChatViewModel.setChatBackground(v.getTag().toString(), mChat.getChatId());
                            Log.d("SAVE_TO_DB", v.getTag().toString());
                            getParentFragment().getChildFragmentManager().popBackStackImmediate(null,0);
                        }
                    });
                    alertDialogBuilder.setNegativeButton(R.string.chat_setting_dialog_bg_select_no, null);
                    alertDialogBuilder.show();
                    mBottomSheetDialog.dismiss();
                    onBackPressed();

                }
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.e("Request_Code", String.valueOf(requestCode));
            if (data != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(R.string.app_name);
                alertDialogBuilder.setMessage(R.string.chat_setting_dialog_confirm_bg_select);
                alertDialogBuilder.setPositiveButton(R.string.chat_setting_dialog_bg_select_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = getActivity().getFilesDir();
                        String fn = mChat.getChatId() + "_" + UUID.randomUUID().toString() + "_" + data.getData().getLastPathSegment();
                        //File img = new File(file, data.getData().toString());
                        String path = getActivity().getFilesDir().toString() + "/" + fn;
                        Log.d("PATH", path);
                        File img = new File(file, fn);
                        mAppExecutors.diskIO().execute(() -> {
                            try {
                                OutputStream output = new FileOutputStream(img);
                                InputStream input = new BufferedInputStream(getContext().getContentResolver().openInputStream(data.getData()));
                                byte data1[] = new byte[1024];
                                int count;
                                while ((count = input.read(data1)) != -1) {
                                    output.write(data1, 0, count);
                                }

                                output.close();
                                input.close();

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        mChatViewModel.setChatBackground("file://" + path, mChat.getChatId());
                        getParentFragment().getChildFragmentManager().popBackStackImmediate(null,0);
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.chat_setting_dialog_bg_select_no, null);
                alertDialogBuilder.show();
                mBottomSheetDialog.dismiss();

            } else {

            }
        }
        mBottomSheetDialog.dismiss();
    }

    public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MemberViewHolder> {

        List<Contact> mMemberList;
        View.OnClickListener mOnClickListener;

        public MemberListAdapter(List<Contact> memberList, View.OnClickListener onClickListener) {
            mMemberList = memberList;
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public MemberListAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.chat_member_list_item, parent, false);
            return new ChatSettingsFragment.MemberListAdapter.MemberViewHolder(view, mOnClickListener);
        }

        public void setMemberList(List<Contact> memberList) {
            mMemberList = memberList;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull MemberListAdapter.MemberViewHolder holder, int position) {
            holder.bindTo(mMemberList.get(position));
        }

        @Override
        public int getItemCount() {
            return mMemberList.size();
        }

        public class MemberViewHolder extends RecyclerView.ViewHolder {

            View.OnClickListener mOnClickListener;

            ImageButton imageButtonDelete;

            ImageView imageViewAvatar;
            ImageView imageViewStatus;

            SwipeHorizontalMenuLayout sml;

            TextView textViewChatName;

            public MemberViewHolder(View itemView, View.OnClickListener onClickListener) {
                super(itemView);

                mOnClickListener = onClickListener;

                imageButtonDelete = itemView.findViewById(R.id.chat_member_menu_delete);

                imageViewAvatar = itemView.findViewById(R.id.home_chat_list_imageview_avatar);
                imageViewStatus = itemView.findViewById(R.id.home_chat_list_imageview_status);

                sml = itemView.findViewById(R.id.sml_chat_member);

                textViewChatName = itemView.findViewById(R.id.home_chat_list_textview_name);

            }

            void bindTo(Contact member) {
                textViewChatName.setText(member.getContactName());

                Log.d("NAMEe", member.getContactName());
                if (member.getContactName().equals(mChatViewModel.getUserId())) {

                }

                if (mChat.getChatType().equals("chat")) {
                    sml.setSwipeEnable(false);
                }

                mChatViewModel.getContactUpdate(member.getContactId()).observe(ChatSettingsFragment.this, result -> {
                    if (result != null) {
                        textViewChatName.setText(result.getContactName());
                        if (result.getContactPresence().equals("available")) {
                            imageViewStatus.setImageResource(R.drawable.status_active);
                        } else if (result.getContactPresence().equals("unavailable")) {
                            imageViewStatus.setImageResource(R.drawable.status_disable);
                        }
                    } else {

                    }
                });

                imageButtonDelete.setTag(member.getContactId());
                imageButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() != null) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setTitle(R.string.app_name);
                            alertDialogBuilder.setMessage(R.string.chat_setting_dialog_confirm_remove_member);
                            alertDialogBuilder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(mChatViewModel.removeMember(v.getTag().toString())) {
                                        //mNavigationManager.showHomePage(false);
                                        getParentFragment().getChildFragmentManager().popBackStackImmediate(null, 0);
                                        getParentFragment().getFragmentManager().popBackStackImmediate();
                                        mChatViewModel.deleteMessages(mChat.getChatId(), mChat.getChatType());
                                    } else {
                                        mMemberList.remove(new Contact(v.getTag().toString(), "unavailable", "", 0));
                                        mMemberList = mChatViewModel.getPariticipants(mChat.getChatId(), mChat.getChatType());
                                        mMemberListAdapter.setMemberList(mMemberList);
                                        mMemberListAdapter.notifyDataSetChanged();
                                        showAlertDialog(getString(R.string.chat_setting_dialog_remove_success), null);
                                    }
                                }
                            });
                            alertDialogBuilder.setNegativeButton(R.string.dialog_no, null);
                            alertDialogBuilder.show();
                            sml.smoothCloseMenu(0);
                        }
                    }
                });


                if (mChatViewModel.getUserId().equals(member.getContactId())) {
                    imageViewStatus.setImageResource(R.drawable.status_active);
                    InputUtils.loadAvatarImage(getContext(),
                            imageViewAvatar,
                            mChatViewModel.getUserFileUrl(FileBody.Type.FILE_TYPE_AVATAR));
                } else {
                    InputUtils.loadAvatarImage(getContext(),
                            imageViewAvatar,
                            mChatViewModel.getContactFileUrl(
                                    FileBody.Type.FILE_TYPE_AVATAR,
                                    ContactManager.getUserName(member.getContactId())));

                }

            }

        }

    }


    /**
     * BACKGROUND ADAPTER
     */

    public class BackgroundListAdapter extends RecyclerView.Adapter<BackgroundListAdapter.ViewHolder> {

        private List<String> mBackgrounds;
        private Context mContext;
        private View.OnClickListener mClickListener;

        public BackgroundListAdapter(Context context, List<String> backgrounds, View.OnClickListener onClickListener) {
            this.mBackgrounds = new ArrayList<>(backgrounds);
            mBackgrounds.add(0, "add");
            this.mContext = context;
            this.mClickListener = onClickListener;
        }

        @Override
        public BackgroundListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.chat_background_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(BackgroundListAdapter.ViewHolder holder, int position) {
            //Get current sport
            String currentBackground = mBackgrounds.get(position);
            //Populate the textviews with data
            holder.bindTo(currentBackground);

        }

        @Override
        public int getItemCount() {
            return mBackgrounds.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            //Member Variables for the TextViews
            private ImageView mBackgroundImage;

            ViewHolder(View itemView) {
                super(itemView);

                //Initialize the views
                mBackgroundImage = itemView.findViewById(R.id.imageBackground);
                itemView.setOnClickListener(mClickListener);

            }

            void bindTo(String currentSticker) {
                //Populate the textviews with data
                //mBackgroundImage.setImageResource(mContext.getResources().getIdentifier(currentSticker,"drawable", mContext.getPackageName()));

                if (currentSticker.equals("add")) {
                    mBackgroundImage.setImageResource(R.drawable.add_photo);
                    //mBackgroundImage.setBackgroundColor(getResources().getColor(R.color.black));
                    itemView.setTag(currentSticker);
                } else {
                    Glide.with(getContext())
                            .setDefaultRequestOptions(new RequestOptions().centerCrop().signature(new ObjectKey("thumb")))
                            .load(Uri.parse("file:///android_asset/bg/" + currentSticker))
                            .into(mBackgroundImage);
                    itemView.setTag("file:///android_asset/bg/" + currentSticker);
                }


            }

            @Override
            public void onClick(View view) {
            /*Users currentUser = mUsers.get(getAdapterPosition());
            Intent detailIntent = new Intent(mContext, MainActivity.class);
            detailIntent.putExtra("id", currentUser.getId());
            detailIntent.putExtra("name", currentUser.getName());
            mContext.startActivity(detailIntent);*/
            }
        }

    }


}
