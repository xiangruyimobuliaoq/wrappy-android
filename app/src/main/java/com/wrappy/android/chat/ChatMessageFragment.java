package com.wrappy.android.chat;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.bumptech.glide.util.LruCache;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageHolders.ContentChecker;
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder;
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.messages.MessagesListAdapter.OnLoadMoreListener;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.wrappy.android.R;
import com.wrappy.android.chat.attachment.ChatAttachLocationFragment;
import com.wrappy.android.chat.attachment.ChatAttachMenuFragment;
import com.wrappy.android.chat.attachment.ChatStickerFragment;
import com.wrappy.android.chat.chatkit.ContentType;
import com.wrappy.android.chat.chatkit.IncomingImageMessageViewHolder;
import com.wrappy.android.chat.chatkit.IncomingLocationMessageViewHolder;
import com.wrappy.android.chat.chatkit.IncomingStickerMessageViewHolder;
import com.wrappy.android.chat.chatkit.IncomingTextViewMessage;
import com.wrappy.android.chat.chatkit.IncomingTypingViewHolder;
import com.wrappy.android.chat.chatkit.OutcomingImageMessageViewHolder;
import com.wrappy.android.chat.chatkit.OutcomingLocationMessageViewHolder;
import com.wrappy.android.chat.chatkit.OutcomingStickerMessageViewHolder;
import com.wrappy.android.chat.chatkit.OutcomingTextViewMessage;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.chat.AuthorViewObject;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.common.utils.KeyStoreUtils;
import com.wrappy.android.common.utils.NotificationID;
import com.wrappy.android.common.utils.VoiceFile;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.Message;
import com.wrappy.android.db.entity.MessageView;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.aws.AWSCertificate;

import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import rm.com.audiowave.AudioWaveView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.wrappy.android.chat.ChatFragment.KEY_JID;
import static com.wrappy.android.chat.ChatFragment.KEY_NAME;
import static com.wrappy.android.chat.ChatFragment.KEY_TYPE;
import static com.wrappy.android.server.AuthRepository.PREF_KEY_AWS_BUCKET_NAME;
import static com.wrappy.android.server.AuthRepository.PREF_KEY_AWS_SECRET_ID;

public class ChatMessageFragment extends SubFragment implements OnLoadMoreListener, OnClickListener, ContentChecker, DateFormatter.Formatter {

    //private static final int REQUEST_PERMISSION = 0;
    private static final int REQUEST_ACTIVITY_GALLERY = 0;
    private static final int REQUEST_ACTIVITY_CAMERA = 1;

    public static final String IMAGE_PREFIX = "%image-";
    public static final String IMAGE_SUFFIX = ":image%";

    public static final String VOICE_PREFIX = "%voice-";
    public static final String VOICE_SUFFIX = ":voice%";

    public static final String MAP_PREFIX = "%map-";
    public static final String MAP_SUFFIX = ":map%";

    public static final String STAMP_PREFIX = "%stamp-";
    public static final String STAMP_SUFFIX = ":stamp%";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ChatNavigationManager mChatNavigationManager;

    @Inject
    AppExecutors mAppExecutors;

    @Inject
    LruCache<String, String> mPresignedCache;

    ChatAttachMenuFragment mChatAttachMenuFragment;
    ChatAttachLocationFragment mChatAttachLocationFragment;

    ChatStickerFragment mChatStickerFragment;

    boolean mIsAttachViewVisible;

    MessagesList mMessagesList;

    MessagesListAdapter<MessageViewObject> mMessagesListAdapter;

    ChatViewModel mChatViewModel;

    private ChatAndBackground mChat;

    private MessageViewObject mTyping;

    private MultiUserChat mMUC;

    private CardView mCardViewRecord;

    private View mChatInput;
    private View mChatSearch;

    private ImageView mImageViewToolbarAvatar;
    private TextView mTextViewToolbarChatName;
    private TextView mTextViewToolbarStatus;

    private Button mButtonRecordButton;

    private EditText mEditTextMessage;
    private ImageButton mImageButtonAttachment;
    private ImageButton mImageButtonSend;
    private ImageButton mImageButtonSticker;

    private EditText mEditTextSearch;

    private ImageButton mImageButtonClear;

    private ImageView mImageButtonRecord;
    private ImageView mImageViewBackground;

    private TextView mTextViewRecord;

    private ImageView mImageViewRecordBanner;

    private View mViewToolbar;

    private ActionMode mActionMode;

    private File mPictureFile;

    /**
     * VOICE RECORD
     */

    boolean mIsRecording;

    MediaRecorder mMediaRecorder;

    MediaPlayer mMediaPlayer;
    private boolean isPreparing;

    private ImageButton playingButton;
    private AudioWaveView playingWave;
    private MessageViewObject playingMessage;

    private boolean isLoadingMore = false;
    private boolean isTyping = false;

    LiveData<Resource<List<MessageView>>> pageMessage;
    LiveData<Resource<List<MessageView>>> mInitialMessages;
    LiveData<MessageView> mGetMessages;
    String mLastMessageId;
    private boolean mIsAttachLocationViewVisible;
    private boolean mIsStickerViewVisible;

    private LiveData<Resource<XMPPRepository.ConnectionStatus>> mConnectionStatus;

    private String mLocale;

    private String mUserId;
    private String mUserName;

    private AWSCertificate mAWSCertificate;

    private OnLayoutChangeListener mScrollAfterLayoutListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (bottom > oldBottom) {
                mMessagesList.scrollBy(0, bottom - oldBottom);
            }
            v.removeOnLayoutChangeListener(this);
        }
    };

    public static ChatMessageFragment create(String chatJid, String chatName, String chatType) {
        ChatMessageFragment chatMessageFragment = new ChatMessageFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, chatJid);
        args.putString(KEY_NAME, chatName);
        args.putString(KEY_TYPE, chatType);
        chatMessageFragment.setArguments(args);
        return chatMessageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_page, container, false);
        mViewToolbar = inflater.inflate(R.layout.chat_toolbar_layout, container, false);

        setHasOptionsMenu(true);

        mMessagesList = view.findViewById(R.id.chat_message_list);

        mChatInput = view.findViewById(R.id.chat_input);
        mChatSearch = view.findViewById(R.id.chat_search);

        mImageViewToolbarAvatar = mViewToolbar.findViewById(R.id.chat_toolbar_avatar);
        mTextViewToolbarChatName = mViewToolbar.findViewById(R.id.chat_toolbar_name);
        mTextViewToolbarStatus = mViewToolbar.findViewById(R.id.chat_toolbar_status);

        mCardViewRecord = view.findViewById(R.id.recordBanner);

        mButtonRecordButton = view.findViewById(R.id.chat_input_button_record);

        mEditTextMessage = view.findViewById(R.id.chat_input_edittext_message);
        mImageButtonAttachment = view.findViewById(R.id.chat_input_imagebutton_attachment);
        mImageButtonSend = view.findViewById(R.id.chat_input_imagebutton_send);
        mImageButtonSticker = view.findViewById(R.id.chat_input_imagebutton_sticker);
        mImageButtonRecord = view.findViewById(R.id.recordBanner_imageview);

        mEditTextSearch = view.findViewById(R.id.chat_message_search);

        mImageButtonClear = view.findViewById(R.id.chat_search_imagebutton_clear);

        mImageViewBackground = view.findViewById(R.id.chat_message_background);

        mTextViewRecord = view.findViewById(R.id.recordbanner_textview);
        mImageViewRecordBanner = view.findViewById(R.id.recordBanner_imageview);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mChatViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ChatViewModel.class);
        mConnectionStatus = mChatViewModel.getConnectionStatus();

        mMessagesList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsAttachViewVisible) {
                    while (mIsAttachViewVisible) {
                        toggleAttachMenu();
                    }
                }
                if (mIsStickerViewVisible) {
                    toggleStickerMenu();
                }
                if (getActivity().getCurrentFocus()!=null) {
                    InputUtils.hideKeyboard(getActivity());
                }
                return false;
            }
        });

        initMessageInput();

        setToolbarCustomView(mViewToolbar);
        showToolbar(true);

        mPictureFile = new File(getActivity().getExternalCacheDir() + "/temppic.png");

        if (Locale.getDefault().getLanguage().contains("zh")) {
            mLocale = "zh";
        } else if (Locale.getDefault().getLanguage().contains("ja")) {
            mLocale = "ja";
        } else {
            mLocale = "en";
        }
        Log.d("LOCALE", mLocale);

        mUserId = mChatViewModel.getUserId();
        mUserName = mChatViewModel.getUserName();

        mChatViewModel.isConnected().observe(getViewLifecycleOwner(), result -> {
            switch (result.data) {
                case CONNECTING:
                    break;
                case CONNECTED:
                    break;
                case AUTHENTICATED:
                    startchat();
                    break;
                case RECONNECTING:
                    startchat();
                    break;
                case DISCONNECTED:
                    break;
                case NOCONNECTION:
                    startchat();
                    break;

            }
        });
    }


    public void startchat() {
        initTyping();
        mChatViewModel.startChat(
                getArguments().getString(KEY_JID),
                getArguments().getString(KEY_NAME),
                getArguments().getString(KEY_TYPE)).observe(getViewLifecycleOwner(), result -> {
            showLoadingDialog(result);
            switch (result.status) {
                case SUCCESS:
                    mChat = result.data;
                    initChat();
                    break;
                case LOADING:
                    break;
                case CLIENT_ERROR:
                    showAlertDialog(result.message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        }
                    });
                    break;
                case SERVER_ERROR:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.chat_no_reponse_message);
                    builder.setPositiveButton(R.string.dialog_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startchat();
                        }
                    });
                    builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                    break;
            }
        });

        if (mChatViewModel.getSearch()) {
            mChatSearch.setVisibility(View.VISIBLE);
            mChatInput.setVisibility(View.GONE);
        } else {
            mChatSearch.setVisibility(View.GONE);
            mChatInput.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getFragmentSoftInputMode() {
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
    }

    public void initChat() {
        InputUtils.loadAvatarImage(getContext(),
                mImageViewToolbarAvatar,
                mChatViewModel.getContactFileUrl(
                        FileBody.Type.FILE_TYPE_AVATAR,
                        ContactManager.getUserName(getArguments().getString(KEY_JID)),
                        mChat.getChatType().equals("groupchat")));

        mAWSCertificate = mChatViewModel.getAWSCertificate();

        mTextViewToolbarChatName.setText(mChat.getChatName());

        mMediaRecorder = new MediaRecorder();
        mMediaPlayer = new MediaPlayer();

        mEditTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("FOCUS", "EDITTEXT");
                    if (mIsAttachViewVisible) {
                        while (mIsAttachViewVisible) {
                            toggleAttachMenu();
                        }
                    }
                    if (mIsStickerViewVisible) {
                        toggleStickerMenu();
                    }

                    mMessagesList.scrollToPosition(0);
                    mEditTextMessage.setCompoundDrawables(null, null, null, null);
                } else {
                }
            }
        });

        Log.d("DATETIME", android.text.format.DateFormat.getTimeFormat(getContext()).format(new Date()));

        if (getArguments().getString(KEY_TYPE).equals("chat")) {
            mChatViewModel.getContactStatus(
                    getArguments().getString(KEY_JID))
                    .observe(getViewLifecycleOwner(), result -> {
                        if ("available".equals(result)) {
                            mTextViewToolbarStatus.setTextColor(getResources().getColor(R.color.status_online));
                            mTextViewToolbarStatus.setText(getString(R.string.status_online));
                        } else if ("unavailable".equals(result)) {
                            mTextViewToolbarStatus.setTextColor(getResources().getColor(R.color.status_offline));
                            mTextViewToolbarStatus.setText(getString(R.string.status_offline));
                        }
                    });
        } else {
            mImageViewToolbarAvatar.setOnClickListener(this);
            mTextViewToolbarStatus.setText("");
        }

        // register view holder for location
        MessageHolders holders = new MessageHolders()
                .registerContentType(
                        ContentType.TEXT,
                        IncomingTextViewMessage.class,
                        R.layout.chat_item_incoming_message,
                        OutcomingTextViewMessage.class,
                        R.layout.chat_item_outcoming_message,
                        this)
                .registerContentType(
                        ContentType.LOCATION,
                        IncomingLocationMessageViewHolder.class,
                        R.layout.chat_item_incoming_location_message,
                        OutcomingLocationMessageViewHolder.class,
                        R.layout.chat_item_outcoming_location_message,
                        this)
                .registerContentType(ContentType.IMAGE,
                        IncomingImageMessageViewHolder.class,
                        R.layout.chat_item_incoming_image,
                        OutcomingImageMessageViewHolder.class,
                        R.layout.chat_item_outcoming_image,
                        this)
                .registerContentType(ContentType.VOICE,
                        ChatMessageFragment.VoiceMessageViewHolder.class,
                        R.layout.chat_item_incoming_voice,
                        R.layout.chat_item_outcoming_voice,
                        this)
                .registerContentType(ContentType.STICKER,
                        IncomingStickerMessageViewHolder.class,
                        R.layout.chat_item_incoming_sticker,
                        OutcomingStickerMessageViewHolder.class,
                        R.layout.chat_item_outcoming_sticker,
                        this)
                .registerContentType(ContentType.TYPING,
                        IncomingTypingViewHolder.class,
                        R.layout.chat_typing,
                        R.layout.chat_typing,
                        this);

        mMessagesList.setRecyclerListener(viewHolder -> {
            if (viewHolder instanceof IncomingLocationMessageViewHolder) {
                ((IncomingLocationMessageViewHolder) viewHolder).clearMap();
            } else if (viewHolder instanceof OutcomingLocationMessageViewHolder) {
                ((OutcomingLocationMessageViewHolder) viewHolder).clearMap();
            } else if (viewHolder instanceof IncomingTextMessageViewHolder
                    || viewHolder instanceof OutcomingTextMessageViewHolder) {
                viewHolder.itemView.removeOnLayoutChangeListener(mScrollAfterLayoutListener);
            }
        });

        mMessagesListAdapter = new MessagesListAdapter<>(
                mUserId,
                holders,
                (imageView, url) -> {
                    InputUtils.loadAvatarImage(getContext(),
                            imageView, url);
                });


        mMessagesListAdapter.setDateHeadersFormatter(this);

        mMessagesListAdapter.setOnMessageViewClickListener((view, message) -> {
            String subject = message.getSubject();
            if (subject == null) {
                return;
            }
            switch (subject) {
                case "location":
                    openLocation(message.getText());
                    break;
                case "image":
                    if (!mChatViewModel.isGalleryShown()) {
                        mChatViewModel.setGalleryShown(true);
                        mChatNavigationManager.showImageGallery(mChat.getChatId(),
                                getArguments().getString(KEY_TYPE),
                                message.getCreatedAt(),
                                mAWSCertificate);
                    }
                    break;
            }
        });

        mMessagesListAdapter.registerViewClickListener(R.id.translate_close, (view, message) -> {
            removeTranslation(message);
        });

        mMessagesListAdapter.registerViewClickListener(R.id.voicePlayer, new MessagesListAdapter.OnMessageViewClickListener<MessageViewObject>() {
            @Override
            public void onMessageViewClick(View view, MessageViewObject message) {
                //Log.d("Play", ChatManager.getPresigned(message.getText()));
                ImageButton buttonPlay = (ImageButton) view;
                AudioWaveView audioWaveView = ((View) view.getParent()).findViewById(R.id.voiceWave);
                if (playingButton == buttonPlay) {
                    buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_white_24dp);
                } else {
                    buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_white_24dp);
                }
                if (!isPreparing) {
                    if (mMediaPlayer.isPlaying()) {
                        if (buttonPlay == playingButton) {
                            stop(playingButton, playingWave, playingMessage);
                        } else {
                            stop(playingButton, playingWave, playingMessage);
                            play(buttonPlay, audioWaveView, ChatManager.getPresigned(message.getText(), mAWSCertificate), message);
                        }
                    } else {
                        play(buttonPlay, audioWaveView, ChatManager.getPresigned(message.getText(), mAWSCertificate), message);
                    }
                }

            }
        });

        mGetMessages = mChatViewModel.getMessages(ContactManager.toBareJid(mChat.getChatId()).asEntityBareJidOrThrow());

        //loadNewMessages();
        loadInitMessage();

        mMessagesList.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                mMessagesList.scrollBy(0, oldBottom - bottom);
            }
        });
        mMessagesList.setAdapter(mMessagesListAdapter);
        ((LinearLayoutManager) mMessagesList.getLayoutManager()).setStackFromEnd(true);

        mMessagesListAdapter.setLoadMoreListener(this);
        Log.d("Background", mChat.getChatId());
        if (mChat.getChatBackground() != null) {
            Log.d("Background", mChat.getChatBackground());
            Glide.with(getContext())
                    .setDefaultRequestOptions(RequestOptions.signatureOf(new ObjectKey("full")))
                    .load(Uri.parse(mChat.getChatBackground()))
                    .into(mImageViewBackground);
        } else {
            Log.d("Background", "null");
        }

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    mImageButtonClear.setVisibility(View.VISIBLE);
                } else {
                    mImageButtonClear.setVisibility(View.INVISIBLE);
                    mMessagesListAdapter.clear();
                    Log.d("INIT_MESSAGE_FROM", "ONTEXTCHANGED");
                    loadInitMessage();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String name;
                    if (mChat.getChatType().equals("chat")) {
                        name = mChat.getChatName();
                    } else {
                        name = null;
                    }

                    searchMessage(name);
                    return true;
                }
                return false;
            }
        });

        mMessagesListAdapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<MessageViewObject>() {
            @Override
            public void onMessageClick(MessageViewObject message) {
                if (mIsAttachViewVisible) {
                    while (mIsAttachViewVisible) {
                        toggleAttachMenu();
                    }
                }
                if (mIsStickerViewVisible) {
                    toggleStickerMenu();
                }
                if(getActivity().getCurrentFocus()!=null) {
                    InputUtils.hideKeyboard(getActivity());

                }
            }
        });

        mMessagesListAdapter.setOnMessageViewLongClickListener(new MessagesListAdapter.OnMessageViewLongClickListener<MessageViewObject>() {
            @Override
            public void onMessageViewLongClick(View view, MessageViewObject message) {

                View targetView;
                QuickAction quickAction = new QuickAction(getContext(), QuickAction.HORIZONTAL);

                quickAction.setColorRes(R.color.gray);
                quickAction.setTextColorRes(R.color.white);
                quickAction.setDividerColorRes(R.color.white);
                if (message.getSubject() == null) {
                    targetView = view.findViewById(R.id.bubble);
                } else if (message.getSubject().equals("location")) {
                    targetView = view.findViewById(R.id.map);
                } else if (message.getSubject().equals("voice")) {
                    targetView = view.findViewById(R.id.voiceContainer);
                } else {
                    targetView = view.findViewById(R.id.image);
                }

                if (message.getSubject() == null) {
                    quickAction.addActionItem(new ActionItem(0, getString(R.string.action_copy)));
                    quickAction.addActionItem(new ActionItem(1, getString(R.string.action_delete)));
                    quickAction.addActionItem(new ActionItem(2, getString(R.string.action_translate)));
                } else {
                    quickAction.addActionItem(new ActionItem(1, getString(R.string.action_delete)));
                }

                quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(ActionItem item) {
                        switch (item.getActionId()) {
                            case 0:
                                Log.d("MESSAGE_ACTION", "copy");
                                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip((ClipData.newPlainText("copy", message.getText())));
                                break;

                            case 1:
                                Log.d("MESSAGE_ACTION", "delete");
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage(R.string.dialog_msg_confirm_delete);
                                builder.setNegativeButton(R.string.dialog_cancel, null);
                                builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mConnectionStatus.getValue().data == XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                                            if (mMessagesListAdapter.getMessagesCount() <= 1) {
                                                if (mChat.getChatType().equals("chat")) {
                                                    mChatViewModel.deleteMessages(mChat.getChatId(), mChat.getChatType());
                                                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                                                } else {
                                                    mChatViewModel.deleteMessage(message.getId(), message.getText(), mChat.getChatType(), mChat.getChatId());
                                                    mMessagesListAdapter.deleteById(message.getId());
                                                    Log.d("GROUP_DELETE_LAST", "YES");
                                                }
                                            } else {
                                                mChatViewModel.deleteMessage(message.getId(), message.getText(), mChat.getChatType(), mChat.getChatId());
                                                mMessagesListAdapter.deleteById(message.getId());
                                                Log.d("CHAT_DELETE", "YES");
                                            }
                                            dialog.dismiss();
                                        } else {
                                            dialog.dismiss();
                                            showAlertDialog(getString(R.string.no_internet_connection), null);

                                        }

                                    }
                                });
                                builder.show();
                                break;

                            case 2:
                                translateMessage(message, mChat.getChatLanguage());
                                break;
                        }
                    }
                });

                quickAction.show(targetView);


            }
        });

        /** Observe ChatState */
        if (mChat.getChatType().equals("chat")) {
            mChatViewModel.isTyping(mChat.getChatId()).observe(getViewLifecycleOwner(), result -> {
                switch (result.status) {

                    case SUCCESS:
                        if (result.data) {
                            // TODO: show typing
                            isTyping = true;
                            mTyping.setCreatedAt(new Date());
                            if (!mMessagesListAdapter.update(mTyping)) {
                                mMessagesListAdapter.addToStart(mTyping, true);
                            }
                            //mTextViewIsTyping.setVisibility(View.VISIBLE);
                            //mContainerIsTyping.setVisibility(View.VISIBLE);
                        } else {
                            // TODO: hide typing
                            isTyping = false;
                            mMessagesListAdapter.delete(mTyping);
                            //mTextViewIsTyping.setVisibility(View.GONE);
                            //mContainerIsTyping.setVisibility(View.GONE);
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
        } else if (mChat.getChatType().equals("groupchat")) {
            mChatViewModel.isTypingMUC().observe(getViewLifecycleOwner(), result -> {
                switch (result.status) {

                    case SUCCESS:
                        if (result.data) {
                            // TODO: show typing
                            isTyping = true;
                            mTyping.setCreatedAt(new Date());
                            if (!mMessagesListAdapter.update(mTyping)) {
                                mMessagesListAdapter.addToStart(mTyping, true);
                            }
                        } else {
                            // TODO: hide typing
                            isTyping = false;
                            mMessagesListAdapter.delete(mTyping);
                        }
                        break;
                    case LOADING:
                        break;
                    case CLIENT_ERROR:
                        break;
                    case SERVER_ERROR:
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                        break;
                }
            });
        }

        mButtonRecordButton.setOnLongClickListener((v) -> {
            mButtonRecordButton.setText(R.string.release_to_stop);
            startRecording();
            return false;
        });
        Rect outRect = new Rect();
        mButtonRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsRecording) {
                    v.getDrawingRect(outRect);
                    if (outRect.contains((int) event.getX(), (int) event.getY())) {
                        mTextViewRecord.setText(R.string.slide_up_to_cancel);
                        mImageViewRecordBanner.setImageResource(R.drawable.mic_volume);
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            mButtonRecordButton.setText("Sending");
                            mButtonRecordButton.setEnabled(false);
                            stopRecording(false);
                        }
                    } else {
                        mTextViewRecord.setText(R.string.release_to_cancel);
                        mImageViewRecordBanner.setImageResource(R.drawable.resend);
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            mButtonRecordButton.setText(getString(R.string.hold_to_talk));
                            stopRecording(true);
                        }
                    }
                }
                return false;
            }
        });

        NotificationManagerCompat.from(getContext().getApplicationContext()).cancel(NotificationID.getGroupID(mChat.getChatId()));
        mChatViewModel.updateReadStatus(mChat.getChatId(), mChat.getChatType());

        if (TextUtils.isEmpty(mChat.getChatLanguage()) || !mChatViewModel.isSupportedLanguage(mChat.getChatLanguage())) {
            Locale locale = Locale.getDefault();
            String defaultLanguage = locale.getLanguage();
            String chatTranslateLanguage = mChatViewModel.isSupportedLanguage(defaultLanguage) ? defaultLanguage : "ja";

            if (chatTranslateLanguage.equals("zh") && !locale.getCountry().equals("CN")) {
                chatTranslateLanguage = "zh-TW";
            }

            mChatViewModel.setChatLanguage(mChat.getChatId(), chatTranslateLanguage).observe(getViewLifecycleOwner(), (lang) -> {
                mChat.setChatLanguage(lang);
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_message_chat_message, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.message_copy:
                Log.d("Menu", "copy");
                return true;

            case R.id.message_delete:
                Log.d("Menu", "delete");
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void initTyping() {
        AuthorViewObject authorViewObject = new AuthorViewObject();
        authorViewObject.setAvatar("typing");
        authorViewObject.setId("typing");
        authorViewObject.setName("typing");
        MessageViewObject messageViewObject = new MessageViewObject();
        messageViewObject.setUser(authorViewObject);
        messageViewObject.setSubject("typing");
        messageViewObject.setId("typing");
        messageViewObject.setText("typing");
        messageViewObject.setType(2);
        mTyping = messageViewObject;
    }

    public void loadNewMessages() {
        if (mGetMessages.hasObservers()) {
            return;
        }
        mGetMessages.observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                toMessageViewObjectAsync(result, (message) -> {
                    if (!mMessagesListAdapter.update(message)) {
                        Log.d("NEW_MESSAGE11", message.getId() + " : " + message.getText());
                        Log.d("SEARCH111", String.valueOf(mChatViewModel.getSearch()));

                        mTyping.setCreatedAt(new Date());
                        if (isTyping) {
                            mMessagesListAdapter.delete(mTyping);
                            mMessagesListAdapter.addToStart(mTyping, true);
                        }
                        //mMessagesList.scrollToPosition(0);
                        if (result.getArchiveId() == null) {
                            mMessagesListAdapter.addToStart(message, true);
                            mMessagesList.scrollToPosition(0);
                        }
                    } else if (hasContentFor(message, ContentType.TEXT)) {
                        addScrollAfterLayoutChange(0);
                    }
                });
            }
            if (mChatViewModel.getSearch()) {
                mGetMessages.removeObservers(this);
            }
        });

    }

    public void searchMessage(String name) {
        LiveData<Resource<List<MessageView>>> searchMessages = mChatViewModel.searchMessage(
                ContactManager.toBareJid(mChat.getChatId()).asEntityBareJidOrThrow(),
                mEditTextSearch.getText().toString(),
                mChat.getChatType(),
                name);
        searchMessages.observe(getViewLifecycleOwner(), result -> {
            showLoadingDialog(result);
            switch (result.status) {

                case SUCCESS:
                    toMessageViewObjectListAsync(result.data, (loadedMessages) -> {
                        mMessagesListAdapter.clear();
                        mMessagesListAdapter.addToEnd(loadedMessages, false);
                        mMessagesListAdapter.notifyDataSetChanged();
                        Log.d("SEARCH", result.data.toString());
                        searchMessages.removeObservers(this);
                    });
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

    @Override
    public void onResume() {
        super.onResume();
        if (mMessagesListAdapter != null) {
            loadInitMessage();
            //loadNewMessages();
        }
    }

    public void loadInitMessage() {
        mInitialMessages = mChatViewModel.getInitialMessages(getArguments().getString(KEY_JID));
        Log.d("INIT_MESSAGE_FROM", "NEW_START_CHAT");
        mInitialMessages.observe(getViewLifecycleOwner(), result -> {
            showLoadingDialog(result);
            switch (result.status) {

                case SUCCESS:
                    if (result.data != null && !result.data.isEmpty()) {
                        //Collections.reverse(result.data);
                        mLastMessageId = result.data.get(result.data.size() - 1).getArchiveId();
                        toMessageViewObjectListAsync(result.data, (loadedMessages) -> {
                            mMessagesListAdapter.clear();
                            mMessagesListAdapter.addToEnd(loadedMessages, false);
                            //mMessagesListAdapter.addToEnd(toMessageViewObject(result.data), true);
                            mMessagesList.scrollToPosition(0);
                            loadNewMessages();
                        });
                    } else {
                        loadNewMessages();
                    }
                    mInitialMessages.removeObservers(this);
                    for (MessageView message : result.data) {
                        Log.d("NEW_MESSAGE", message.getMessageId() + " : " + message.getMessageText());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_chat, menu);
        // Disabled Translate and Call
        menu.getItem(0).setEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.chat_input_imagebutton_send:
                if (mIsAttachLocationViewVisible) {
                    /** Send Location */
                    Log.d("location", mChatAttachLocationFragment.getLatLng());
                    sendMessage("location", MAP_PREFIX + mLocale + ":" + mChatAttachLocationFragment.getLatLng() + MAP_SUFFIX);
                    toggleAttachMenu();
                } else if (mEditTextMessage.getText().toString().equals("")) {
                    Log.d("Voice Message", "Clicked!");
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        if (mIsAttachViewVisible) {
                            toggleAttachMenu();
                        }
                        if (mIsStickerViewVisible) {
                            toggleStickerMenu();
                        }
                        if (mButtonRecordButton.getVisibility() == View.GONE) {
                            if(getActivity().getCurrentFocus()!=null) {
                                InputUtils.hideKeyboard(getActivity());
                            }
                            mImageButtonSend.setImageResource(R.drawable.keyboard);
                            mButtonRecordButton.setVisibility(View.VISIBLE);
                        } else if (mButtonRecordButton.getVisibility() == View.VISIBLE) {
                            mEditTextMessage.requestFocus();
                            InputUtils.showSoftKeyboard(mEditTextMessage.getContext());
                            mImageButtonSend.setImageResource(R.drawable.ic_voice);
                            mButtonRecordButton.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d("PERMISSION", "YES");
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 3);
                    }
                } else {
                    sendMessage(null, mEditTextMessage.getText().toString());
                    mEditTextMessage.setText("");
                }
                break;

            case R.id.chat_toolbar_avatar:
                mChatNavigationManager.showChatGroupDetailsPage(getArguments().getString(KEY_JID));
                break;

            case R.id.chat_message_list:
                if (mIsAttachViewVisible) {
                    while (mIsAttachViewVisible) {
                        toggleAttachMenu();
                    }
                }
                if (mIsStickerViewVisible) {
                    toggleStickerMenu();
                }

                InputUtils.hideKeyboard(getActivity());
                break;

            case R.id.chat_search_imagebutton_clear:
                mEditTextSearch.setText("");
                mMessagesListAdapter.clear();
                Log.d("INIT_MESSAGE_FROM", "CLEARBUTTON");
                loadInitMessage();
                break;

            case R.id.chat_input_imagebutton_attachment:
                if (getActivity().getCurrentFocus() != null) {
                    InputUtils.hideKeyboard(getActivity());
                }
                if(mIsAttachViewVisible) {
                    mImageButtonSend.setImageResource(R.drawable.ic_voice);
                }
                mImageButtonAttachment.postDelayed(this::toggleAttachMenu, 100);
                break;

            case R.id.chat_attach_location:
                showAttachLocationMap();
                break;

            case R.id.chat_attach_select_image:
                List<String> requiredPermissions1 = new ArrayList<>();
                requiredPermissions1.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requiredPermissions1.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                requiredPermissions1.add(Manifest.permission.CAMERA);
                Iterator<String> ite1 = requiredPermissions1.iterator();
                while (ite1.hasNext()) {
                    if (ContextCompat.checkSelfPermission(getContext(), ite1.next())
                            == PackageManager.PERMISSION_GRANTED) {
                        ite1.remove();
                    }
                }

                if (requiredPermissions1.isEmpty()) {
                    Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(selectPictureIntent, REQUEST_ACTIVITY_GALLERY);
                } else {
                    requestPermissions(requiredPermissions1.toArray(new String[]{}), REQUEST_ACTIVITY_GALLERY);
                }

                break;

            case R.id.chat_attach_take_photo:
                List<String> requiredPermissions = new ArrayList<>();
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                requiredPermissions.add(Manifest.permission.CAMERA);
                Iterator<String> ite = requiredPermissions.iterator();
                while (ite.hasNext()) {
                    if (ContextCompat.checkSelfPermission(getContext(), ite.next())
                            == PackageManager.PERMISSION_GRANTED) {
                        ite.remove();
                    }
                }

                if (requiredPermissions.isEmpty()) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri fileUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName() + ".provider", mPictureFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, REQUEST_ACTIVITY_CAMERA);
                } else {
                    requestPermissions(requiredPermissions.toArray(new String[]{}), REQUEST_ACTIVITY_CAMERA);
                }

                break;

            case R.id.chat_input_edittext_message:
                if (mIsAttachViewVisible) {
                    while (mIsAttachViewVisible) {
                        toggleAttachMenu();
                    }
                }
                if (mIsStickerViewVisible) {
                    toggleStickerMenu();
                }
                break;

            case R.id.chat_input_imagebutton_sticker:
                Log.d("CLICK", "Sticker");
                if (getActivity().getCurrentFocus() != null) {
                    InputUtils.hideKeyboard(getActivity());
                }
                mImageButtonSticker.postDelayed(this::toggleStickerMenu, 100);
                break;

            case R.id.chat_sticker_container:
                Log.d("STICKER", v.getTag().toString().replace(".png", ""));
                sendMessage("stamp", STAMP_PREFIX + mLocale + ":" + v.getTag().toString().replace(".png", "") + STAMP_SUFFIX);
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = grantResults.length > 0;
        for (int grantResult : grantResults) {
            granted &= grantResult == PackageManager.PERMISSION_GRANTED;
        }

        if (granted) {
            if (requestCode == REQUEST_ACTIVITY_CAMERA) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri fileUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName() + ".provider", mPictureFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_ACTIVITY_CAMERA);
            } else if (requestCode == REQUEST_ACTIVITY_GALLERY) {
                Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(selectPictureIntent, REQUEST_ACTIVITY_GALLERY);
            } else if (requestCode == 3) {
                mImageButtonSend.setImageResource(R.drawable.ic_keyboard);
                mButtonRecordButton.setVisibility(View.VISIBLE);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.permission_dialog_image_crop_title);
            builder.setMessage(R.string.permission_dialog_image_crop_desc);
            builder.setPositiveButton(R.string.permission_dialog_go_to_settings, (dialog, which) -> {
                startActivity(InputUtils.createAppSettingsIntent(getContext()));

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();

            });
            builder.setCancelable(false);
            builder.show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.chat_call:
                // TODO: Call
                return true;

            case R.id.chat_translate:
                mChatNavigationManager.showChatTranslatePage(getArguments().getString(KEY_JID), true);
                return true;

            case R.id.chat_settings:
                if (mChat.getChatType().equals("chat") || mConnectionStatus.getValue().data == XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                    mChatNavigationManager.showChatSettingsPage(getArguments().getString(KEY_JID));
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initMessageInput() {

        mImageButtonClear.setOnClickListener(this);

        mEditTextMessage.setOnClickListener(this);

        mEditTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mImageButtonSend.setImageResource(R.drawable.ic_send_chat);
                    if (s.toString().trim().isEmpty()) {
                        mImageButtonSend.setEnabled(false);
                    } else {
                        mImageButtonSend.setEnabled(true);
                    }
                } else {
                    mImageButtonSend.setImageResource(R.drawable.ic_voice);
                    mImageButtonSend.setEnabled(true);
                }
                if (count > before) {
                    mChatViewModel.setChatState(mChat.getChatId(), mChat.getChatType(), true);
                } else if (count < before) {
                    mChatViewModel.setChatState(mChat.getChatId(), mChat.getChatType(), false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImageButtonSend.setOnClickListener(this);
        mImageButtonAttachment.setOnClickListener(this);
        mImageButtonSticker.setOnClickListener(this);
    }

    public boolean sendMessage(String subject, String messageText) {
        if (mConnectionStatus.getValue().data == XMPPRepository.ConnectionStatus.AUTHENTICATED) {
            String chatName = "";
            if (mChat.getChatType().equals("groupchat")) {
                chatName = mChat.getChatName();
            }
            mChatViewModel.sendMessage(messageText, subject, getArguments().getString(KEY_TYPE), chatName).observe(getViewLifecycleOwner(), result -> {
                switch (result.status) {

                    case SUCCESS:
                        /** MESSAGE SENT **/
                        break;
                    case LOADING:
                        break;
                    case CLIENT_ERROR:
                        /** NOT CONNECTED */
                        showAlertDialog("You are not connected to the server", null);
                        break;
                    case SERVER_ERROR:
                        break;
                }
            });
        } else {
            showAlertDialog(getString(R.string.no_internet_connection), null);
        }
        mMessagesList.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (mIsAttachViewVisible) {
            toggleAttachMenu();
            return true;
        }

        if (mIsStickerViewVisible) {
            toggleStickerMenu();
            return true;
        }

        if (mChatViewModel.getSearch()) {
            mChatSearch.setVisibility(View.GONE);
            mEditTextSearch.setText("");
            mChatViewModel.setSearch(false);
            mChatInput.setVisibility(View.VISIBLE);
            mMessagesListAdapter.clear();
            Log.d("INIT_MESSAGE_FROM", "ONBACKPRESSED");
            loadInitMessage();
            //loadNewMessages();
            return true;
        }
        NotificationManagerCompat.from(getContext().getApplicationContext()).cancel(NotificationID.getGroupID(mChat.getChatId()));
        mChatViewModel.updateReadStatus(mChat.getChatId(), mChat.getChatType());
        mChatViewModel.setChatState(mChat.getChatId(), mChat.getChatType(), false);
        if(getActivity().getCurrentFocus()!=null) {
            InputUtils.hideKeyboard(getActivity());
        }
        return false;
    }

    private interface OnMessageViewObjectLoadedListener {
        void onMessageViewObjectListLoaded(MessageViewObject loadedMessage);
    }

    private interface OnMessageViewObjectListLoadedListener {
        void onMessageViewObjectListLoaded(List<MessageViewObject> loadedMessages);
    }

    private void toMessageViewObjectAsync(MessageView message, OnMessageViewObjectLoadedListener listener) {
        mAppExecutors.networkIO().execute(() -> {
            if (listener != null) {
                MessageViewObject messageViewObject = toMessageViewObject(message);
                mAppExecutors.mainThread().execute(() -> {
                    listener.onMessageViewObjectListLoaded(messageViewObject);
                });
            }
        });
    }

    private void toMessageViewObjectListAsync(List<MessageView> messageList, OnMessageViewObjectListLoadedListener listener) {
        mAppExecutors.networkIO().execute(() -> {
            if (listener != null) {
                List<MessageViewObject> loadedMessages = new ArrayList<>();
                for (MessageView message : messageList) {
                    loadedMessages.add(toMessageViewObject(message));
                }
                Log.d("DONE", "returned");
                Log.d("LIST", loadedMessages.toString());
                mAppExecutors.mainThread().execute(() -> {
                    listener.onMessageViewObjectListLoaded(loadedMessages);
                });
            }
        });
    }

    private MessageViewObject toMessageViewObject(MessageView message) {
        AuthorViewObject author = new AuthorViewObject();
        MessageViewObject messageViewObject = new MessageViewObject();
        author.setId(message.getMessageFrom());
        author.setAvatar(mChatViewModel.getContactFileUrl(
                FileBody.Type.FILE_TYPE_AVATAR,
                ContactManager.getUserName(message.getMessageFrom())));
        if (message.getContactName() != null) {
            author.setName(message.getContactName());
            Log.d("FROM", message.getContactName());
        } else {
            if (message.getMessageFrom().equals(mUserId)) {
                author.setName(mUserName);
                Log.d("FROM", author.getId());
            } else {
                Log.d("MULTIUSER", message.getMessageFrom());
                // TODO: Multi-User Chat
            }
        }
        messageViewObject.setId(message.getMessageId());
        messageViewObject.setUser(author);
        messageViewObject.setSubject(message.getMessageSubject());
        messageViewObject.setType(message.getMessageType());
        if ("image".equals(message.getMessageSubject())) {
            String imageUrl = mPresignedCache.get(message.getMessageId());
            if (TextUtils.isEmpty(imageUrl)) {
                imageUrl = ChatManager.getPresigned(message.getMessageText(), mAWSCertificate);
                mPresignedCache.put(message.getMessageId(), imageUrl);
            }
            messageViewObject.setText(imageUrl);
        } else {
            messageViewObject.setText(message.getMessageText());
        }
        messageViewObject.setCreatedAt(message.getCreatedAt());
        messageViewObject.setTranslatedText(message.getTranslatedMessageText());
        return messageViewObject;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.d("TOTAL_ITEMS", String.valueOf(totalItemsCount));
        if(!mGetMessages.hasActiveObservers() || isLoadingMore) {
            if(!mChatViewModel.getSearch()) {
                return;
            }
        } else {
            isLoadingMore = true;
        }
        String keyword;
        if (mChatViewModel.getSearch()) {
            keyword = mEditTextSearch.getText().toString();
        } else {
            keyword = null;
        }
        if (!mChatViewModel.getSearch()) {
            if (mLastMessageId != null) {
                mChatViewModel.pageChat(
                        ContactManager.toBareJid(getArguments().getString(KEY_JID))
                                .asEntityBareJidOrThrow(),
                        mMessagesListAdapter.getMessagesCount(),
                        mLastMessageId,
                        getArguments().getString(KEY_TYPE),
                        keyword).observe(getViewLifecycleOwner(), result -> {
                    switch (result.status) {

                        case SUCCESS:
                            if (mLastMessageId != null) {
                                Log.d("LastMessageID", mLastMessageId);
                            }
                            Log.d("Success", result.data.toString());
                            //Collections.copy(loadedMessages, result.data);
                            toMessageViewObjectListAsync(result.data, (loadedMessages) -> {
                                Iterator<MessageViewObject> ite = loadedMessages.iterator();
                                while (ite.hasNext()) {
                                    if (mMessagesListAdapter.update(ite.next())) {
                                        ite.remove();
                                    }
                                }
                                if (!loadedMessages.isEmpty()) {
                                    mMessagesListAdapter.addToEnd(loadedMessages, false);
                                    /**Collections.reverse(result.data);
                                     for(MessageViewObject messageViewObject : toMessageViewObject(result.data)) {
                                     mMessagesListAdapter.update(messageViewObject);
                                     }**/
                                    mLastMessageId = result.data
                                            .get(result.data.size() - 1)
                                            .getArchiveId();
                                }
                                isLoadingMore = false;
                            });
                            break;
                        case LOADING:
                            Log.d("LastMessageID", mLastMessageId);
                            Log.d("Loading", "messages");
                            break;
                        case CLIENT_ERROR:
                            break;
                        case SERVER_ERROR:
                            break;
                    }
                });
            }
        }
    }

    private void toggleAttachMenu() {
        if (mIsAttachViewVisible) {
            // close attach views
            getChildFragmentManager().popBackStackImmediate();
        } else if (mIsStickerViewVisible) {
            getChildFragmentManager().popBackStackImmediate(null, 0);
            mIsStickerViewVisible = false;
            toggleAttachMenu();
        } else {
            getChildFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    int attachViewCount = getChildFragmentManager().getBackStackEntryCount();
                    if (attachViewCount == 0) {
                        getChildFragmentManager().removeOnBackStackChangedListener(this);
                        mImageButtonAttachment.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                        mImageButtonAttachment.setImageResource(R.drawable.other_action);
                        mIsAttachViewVisible = false;
                        mIsStickerViewVisible = false;
                        mImageButtonSend.setImageResource(R.drawable.ic_voice);
                        mIsAttachLocationViewVisible = false;
                    } else if (attachViewCount > 0) {
                        mImageButtonAttachment.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.chatInputBackground)));
                        if (!mIsAttachLocationViewVisible) {
                            //mIsAttachLocationViewVisible = false;
                            mImageButtonSend.setImageResource(R.drawable.ic_voice);
                        }
                        mImageButtonAttachment.setImageResource(attachViewCount > 1 ?
                                R.drawable.back :
                                R.drawable.ic_icon_close);
                        mIsAttachViewVisible = true;
                        mButtonRecordButton.setVisibility(View.GONE);
                        if (!mIsAttachLocationViewVisible && attachViewCount > 1) {

                            mImageButtonSend.setImageResource(R.drawable.ic_voice);
                        }
                    }
                }
            });

            if (mChatAttachMenuFragment == null) {
                mChatAttachMenuFragment = new ChatAttachMenuFragment();
                mChatAttachMenuFragment.setOnClickListener(this);
            }

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.chat_attach_container, mChatAttachMenuFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
            mEditTextMessage.clearFocus();
        }
        mMessagesList.scrollToPosition(0);
    }

    public void toggleStickerMenu() {
        if (mIsAttachViewVisible) {
            if (mIsAttachLocationViewVisible) {
                getChildFragmentManager().popBackStackImmediate();
                mIsAttachLocationViewVisible = false;
            }
            getChildFragmentManager().popBackStackImmediate(null, 0);
            toggleStickerMenu();
        } else if (mIsAttachLocationViewVisible) {
            getChildFragmentManager().popBackStackImmediate(null, 0);
            mIsAttachLocationViewVisible = false;
            mIsAttachViewVisible = false;
            toggleStickerMenu();
        } else if (mIsStickerViewVisible) {
            getChildFragmentManager().popBackStackImmediate(null, 0);
        } else {
            getChildFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    int attachViewCount = getChildFragmentManager().getBackStackEntryCount();
                    if (attachViewCount == 0) {
                        getChildFragmentManager().removeOnBackStackChangedListener(this);
                        mIsStickerViewVisible = false;
                        mIsAttachViewVisible = false;
                        mIsAttachLocationViewVisible = false;
                    } else {
                        mIsStickerViewVisible = true;
                    }
                }
            });

            if (mChatStickerFragment == null) {
                mChatStickerFragment = new ChatStickerFragment();
                mChatStickerFragment.setOnClickListener(this);
            }

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.chat_attach_container, mChatStickerFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
            mEditTextMessage.clearFocus();
        }
        mMessagesList.scrollToPosition(0);
    }

    private void showAttachLocationMap() {
        if (mChatAttachLocationFragment == null) {
            mChatAttachLocationFragment = new ChatAttachLocationFragment();
        }
        mImageButtonSend.setImageResource(R.drawable.ic_send_chat);
        mIsAttachLocationViewVisible = true;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.chat_attach_container, mChatAttachLocationFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();


    }

    private void openLocation(String latLng) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:" + latLng + "?z=15.0&q=" + latLng));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String uri = String.format("https://www.google.com/maps/search/?api=1&query=%s", latLng);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }

    @Override
    public boolean hasContentFor(IMessage message, byte type) {
        MessageViewObject messageViewObject = (MessageViewObject) message;
        switch (type) {
            case ContentType.TEXT:
                return messageViewObject.getSubject() == null &&
                        TextUtils.isEmpty(messageViewObject.getSubject());
            case ContentType.LOCATION:
                return !TextUtils.isEmpty(messageViewObject.getSubject())
                        && messageViewObject.getSubject().equals("location");

            case ContentType.IMAGE:
                return !TextUtils.isEmpty(messageViewObject.getSubject())
                        && messageViewObject.getSubject().equals("image");

            case ContentType.VOICE:
                return !TextUtils.isEmpty(messageViewObject.getSubject())
                        && messageViewObject.getSubject().equals("voice");

            case ContentType.STICKER:
                return !TextUtils.isEmpty(messageViewObject.getSubject())
                        && messageViewObject.getSubject().equals("stamp");

            case ContentType.TYPING:
                return !TextUtils.isEmpty(messageViewObject.getSubject())
                        && messageViewObject.getSubject().equals("typing");


        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.e("Request_Code", String.valueOf(requestCode));
            switch (requestCode) {
                case REQUEST_ACTIVITY_CAMERA:
                    uploadWithTransferUtility("image", mPictureFile);
                    break;
                case REQUEST_ACTIVITY_GALLERY:
                    if (data != null) {
                        File file = getActivity().getCacheDir();
                        //File img = new File(file, data.getData().toString());
                        File img = new File(file, "temppic.png");
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
                                uploadWithTransferUtility("image", img);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else {

                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {

        }
    }

    public void uploadWithTransferUtility(String subject, File file) {

        String UUID = StanzaIdUtil.newStanzaId();


        String prefix;
        String suffix;

        if (subject.equals("image")) {
            prefix = IMAGE_PREFIX + mLocale + ":";
            suffix = IMAGE_SUFFIX;
        } else if (subject.equals("voice")) {
            prefix = VOICE_PREFIX + mLocale + ":";
            suffix = VOICE_SUFFIX;
        } else {
            prefix = "";
            suffix = "";
        }

        String key = UUID + (subject.equals("voice") ? ".m4a" : "");

        AmazonS3Client amazonS3Client = new AmazonS3Client(new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return mAWSCertificate.getAccessKey();
            }

            @Override
            public String getAWSSecretKey() {
                return mAWSCertificate.getSecretID();
            }
        });

        TransferUtility.builder().context(getContext()).s3Client(amazonS3Client).build();

        TransferUtility transferUtility = TransferUtility.builder().context(getContext()).s3Client(amazonS3Client).build();
        TransferObserver uploadObserver = transferUtility.upload(mAWSCertificate.getBucketName(), key, file);


        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {

                }
                switch (state) {
                    case IN_PROGRESS:
                        showLoadingDialog(Resource.loading(null));
                        break;
                    case COMPLETED:
                        Log.d("AWS", uploadObserver.getAbsoluteFilePath());
                        sendMessage(subject, prefix + key + suffix);
                        showLoadingDialog(Resource.success(null));
                        if (subject.equals("voice")) {
                            mButtonRecordButton.setText(getString(R.string.hold_to_talk));
                            mButtonRecordButton.setEnabled(true);
                        }
                        break;
                    case FAILED:
                        showLoadingDialog(Resource.clientError("No internet connection.", null));
                        showAlertDialog("No internet connection", null);
                        if (subject.equals("voice")) {
                            mButtonRecordButton.setText(getString(R.string.hold_to_talk));
                            mButtonRecordButton.setEnabled(true);
                        }
                        break;
                    case PAUSED:
                    case CANCELED:
                    case WAITING:
                    case WAITING_FOR_NETWORK:
                    default:
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
                showLoadingDialog(Resource.clientError(ex.getMessage(), null));
                showAlertDialog(ex.getMessage(), null);
            }
        });

    }

    private void play(ImageButton button, AudioWaveView audioWaveView, String url, MessageViewObject message) {
        if (mConnectionStatus.getValue().data == XMPPRepository.ConnectionStatus.AUTHENTICATED) {
            try {
                button.setEnabled(false);
                mMediaPlayer.setDataSource(url);
                isPreparing = true;
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                playingButton = button;
                playingWave = audioWaveView;
                playingMessage = message;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaPlayer.isPlaying()) {
                            Log.d(String.valueOf(mMediaPlayer.getDuration()), String.valueOf(mMediaPlayer.getCurrentPosition()));
                            float progress = 0;
                            if (mMediaPlayer.getCurrentPosition() != 0) {
                                progress = (mMediaPlayer.getCurrentPosition() / (float) mMediaPlayer.getDuration()) * 100;
                            }
                            playingWave.setProgress(progress);

                            if (progress != 100) {
                                handler.postDelayed(this, 150L);
                            }
                        } else {
                            handler.removeCallbacks(this);
                        }
                    }
                }, 150L);
                button.setTag("Playing");
                button.setImageResource(R.drawable.ic_pause_circle_filled_white_24dp);
                isPreparing = false;

                button.setEnabled(true);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stop(button, audioWaveView, message);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                showAlertDialog("File not found", null);
            }
        } else {
            showAlertDialog(getString(R.string.no_internet_connection), null);
        }
    }

    private void stop(ImageButton button, AudioWaveView audioWaveView, MessageViewObject message) {
        button.setEnabled(true);
        button.setTag("Stopped");
        button.setImageResource(R.drawable.ic_play_circle_filled_white_24dp);
        audioWaveView.setProgress(0);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    private void startRecording() {
        if (mIsRecording) {
            return;
        }
        if (mMediaPlayer.isPlaying() || isPreparing) {
            stop(playingButton, playingWave, playingMessage);
        }
        mCardViewRecord.setVisibility(View.VISIBLE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setAudioEncodingBitRate(44100);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(getAudioFileName());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mIsRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecording(boolean delete) {
        if (!mIsRecording) {
            return;
        }
        mCardViewRecord.setVisibility(View.GONE);
        mIsRecording = false;
        File file = new File(getAudioFileName());
        try {
            mMediaRecorder.stop();

            if (delete) {

                if (file.exists()) {
                    file.delete();
                }

            } else {
                Log.d("SEND VOICE", "YES");
                uploadWithTransferUtility("voice", file);
            }
        } catch (Exception e) {
            Log.d("Exception", "HACKERMAN", e);
            mButtonRecordButton.setText(getString(R.string.hold_to_talk));
            mButtonRecordButton.setEnabled(true);
            if (file.exists()) {
                file.delete();
            }
        } finally {
            mMediaRecorder.reset();
        }

    }

    private String getAudioFileName() {
        return getActivity().getFilesDir() + "/audiorecordtest";
    }

    private void translateMessage(final MessageViewObject originalMessage, String languageCode) {
        mChatViewModel.translateText(originalMessage.getId(), originalMessage.getText(), languageCode);
        LiveData<MessageView> translateMessage = mChatViewModel.getSingleMessage(originalMessage.getId());
        translateMessage.observe(getViewLifecycleOwner(), new Observer<MessageView>() {
            @Override
            public void onChanged(@Nullable MessageView changedMessage) {
                if (changedMessage == null) {
                    return;
                }
                toMessageViewObjectAsync(changedMessage, (newMessage) -> {
                    if (newMessage.getTranslatedText() != null &&
                            !newMessage.getTranslatedText()
                                    .equals(originalMessage.getTranslatedText())) {
                        if (mMessagesListAdapter.update(newMessage)) {
                            addScrollAfterLayoutChange(changedMessage.getMessageId());
                        }
                        translateMessage.removeObserver(this);
                    }
                });
            }
        });
    }

    private void removeTranslation(final MessageViewObject message) {
        mChatViewModel.removeTranslation(message.getId());
        LiveData<MessageView> translateMessage = mChatViewModel.getSingleMessage(message.getId());
        translateMessage.observe(getViewLifecycleOwner(), new Observer<MessageView>() {
            @Override
            public void onChanged(@Nullable MessageView messageView) {
                if (messageView == null) {
                    return;
                }
                toMessageViewObjectAsync(messageView, (messageViewObject) -> {
                    if (messageViewObject.getTranslatedText() == null) {
                        mMessagesListAdapter.update(messageViewObject);
                        translateMessage.removeObserver(this);
                    }
                });
            }
        });
    }

    private void addScrollAfterLayoutChange(String id) {
        Object result = InputUtils.genericInvokeMethod(mMessagesListAdapter, "getMessagePositionById", id);
        if (result == null) {
            return;
        }
        addScrollAfterLayoutChange((int) result);
    }

    private void addScrollAfterLayoutChange(int position) {
        if (position < 0) {
            return;
        }

        View itemView = mMessagesList.getLayoutManager().findViewByPosition(position);
        if (itemView != null) {
            itemView.addOnLayoutChangeListener(mScrollAfterLayoutListener);
        }
    }

    @Override
    public String format(Date date) {
        if(DateFormatter.isToday(date)) {
            return "Today";
        } else {
            return DateFormatter.format(date, "yy/MM/dd");
        }
    }

    public static class VoiceMessageViewHolder extends MessageHolders.BaseIncomingMessageViewHolder<MessageViewObject> {

        /**
         * Taken from
         * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/LiteListDemoActivity.java
         */

        private View layout;
        private ImageButton buttonVoicePlayer;
        private AudioWaveView voiceWave;
        private TextView textViewVoiceDuration;

        private TextView time;
        private TextView name;

        private SharedPreferences mSharedPreferences;
        private AWSCertificate mAwsCertificate;

        public VoiceMessageViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            buttonVoicePlayer = layout.findViewById(R.id.voicePlayer);
            voiceWave = layout.findViewById(R.id.voiceWave);
            textViewVoiceDuration = layout.findViewById(R.id.voiceDuration);
            time = layout.findViewById(R.id.messageTime);
            name = layout.findViewById(R.id.messageUserName);

            mSharedPreferences = layout.getContext()
                    .getSharedPreferences("wrappy_prefs", Context.MODE_PRIVATE);
            mAwsCertificate = new AWSCertificate(
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(AuthRepository.PREF_KEY_AWS_ACCESS_KEY, "")),
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_AWS_SECRET_ID, "")),
                    KeyStoreUtils.decrypt(mSharedPreferences.getString(PREF_KEY_AWS_BUCKET_NAME, "")));
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
                time.setText(simpleDateFormat.format(message.getCreatedAt())  + " " + DateFormat.getTimeFormat(time.getContext()).format(message.getCreatedAt()));
                //time.setText(DateFormat.format("yy/MM/dd HH:mm", message.getCreatedAt()));
                //time.setText(DateFormat.format("yy/MM/dd", message.getCreatedAt()));
            }

            if (name != null) {
                if (message.getUser().getName() == null) {
                    if (message.getType() == Message.MESSAGE_TYPE_GROUP) {
                        name.setText(ContactManager.getUserName(message.getUser().getId()));
                    } else {
                        name.setVisibility(View.GONE);
                    }
                } else {
                    if (message.getType() == Message.MESSAGE_TYPE_GROUP) {
                        name.setText(message.getUser().getName());
                    } else {
                        name.setVisibility(View.GONE);
                    }
                }
            }
            String duration1 = "";
            textViewVoiceDuration.setTag(message.getText());
            buttonVoicePlayer.setEnabled(false);
            voiceWave.setRawData(new byte[]{0});

            if (mSharedPreferences.contains(message.getText())) {
                duration1 = mSharedPreferences.getString(message.getText(), "");
                buttonVoicePlayer.setEnabled(true);
            } else {
                AsyncTask.execute(() -> {
                    String uri = ChatManager.getPresigned(message.getText(), mAwsCertificate);
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(uri, new HashMap<String, String>());
                        String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        // GET BYTES
                        mSharedPreferences.edit().putString(message.getText(), dur).apply();
                        textViewVoiceDuration.post(() -> {
                            if (textViewVoiceDuration.getTag().equals(message.getText())) {
                                textViewVoiceDuration.setText(getDuration(dur));
                                buttonVoicePlayer.setEnabled(true);
                            }
                        });
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        Log.e("ERROR", "RUNTIME" + message.getText());
                        textViewVoiceDuration.post(() -> {
                            buttonVoicePlayer.setEnabled(false);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            AsyncTask.execute(() -> {
                String uri = ChatManager.getPresigned(message.getText(), mAwsCertificate);
                try {
                    URL url = new URL(uri);
                    URLConnection ucon = url.openConnection();
                    InputStream stream = ucon.getInputStream();
                    byte[] bytes = VoiceFile.loadAudio(uri, ucon.getContentLength());
                    voiceWave.setRawData(bytes);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            textViewVoiceDuration.setText(getDuration(duration1));
            if (buttonVoicePlayer.getTag().equals("Playing")) {
                buttonVoicePlayer.setImageResource(R.drawable.ic_pause_circle_filled_white_24dp);
            } else if (buttonVoicePlayer.getTag().equals("Stopped")) {
                buttonVoicePlayer.setImageResource(R.drawable.ic_play_circle_filled_white_24dp);
            }

        }

        String getDuration(String dur) {
            if (TextUtils.isEmpty(dur)) {
                return "--:--";
            } else {
                int duration = Integer.parseInt(dur);
                Log.d("DURATION", String.valueOf(duration));
                int m = (duration / 1000) / 60;
                int s = (duration / 1000) % 60;
                if (m == 0 && s == 0) {
                    s = 1;
                }
                String mm = m < 10 ? "0" + m : String.valueOf(m);
                String ss = s < 10 ? "0" + s : String.valueOf(s);
                return mm + ":" + ss;
            }

        }

        byte[] getBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }

    }
}
