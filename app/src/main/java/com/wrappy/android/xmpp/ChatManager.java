package com.wrappy.android.xmpp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.instacart.library.truetime.TrueTime;
import com.wrappy.android.R;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.chat.ChatMessageFragment;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.chat.AuthorViewObject;
import com.wrappy.android.common.utils.CryptLib;
import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.ChatBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.db.entity.Delete;
import com.wrappy.android.db.entity.DeleteAll;
import com.wrappy.android.db.entity.MessageView;
import com.wrappy.android.db.entity.TranslateSetting;
import com.wrappy.android.otr.OtrManager;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.xmpp.aws.AWSCertificate;
import com.wrappy.android.xmpp.aws.IQAWSCertificate;
import com.wrappy.android.xmpp.aws.IQAWSCertificateProvider;
import com.wrappy.android.xmpp.badge.Badge;
import com.wrappy.android.xmpp.badge.IQBadge;
import com.wrappy.android.xmpp.muc.IQMucGetSubscriptionProvider;
import com.wrappy.android.xmpp.muc.IQMucGetSubscriptions;
import com.wrappy.android.xmpp.muc.IQMucLeave;
import com.wrappy.android.xmpp.muc.IQMucSubscribe;
import com.wrappy.android.xmpp.muc.MessageElementProvider;
import com.wrappy.android.xmpp.muc.MessageExtensionElement;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;
import com.wrappy.android.xmpp.pushnotif.GroupPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQGroupListPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQGroupResultPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQGroupSetPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQJidResultPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQJidSetPushFlag;
import com.wrappy.android.xmpp.pushnotif.NaturalNameElement;
import com.wrappy.android.xmpp.pushnotif.NickNameElement;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.filter.IQResultReplyFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.SimpleIQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;
import org.jivesoftware.smackx.message_correct.element.MessageCorrectExtension;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.filter.MUCUserStatusCodeFilter;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.sid.element.StanzaIdElement;
import org.jivesoftware.smackx.time.EntityTimeManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatManager {

    AppDatabase mAppDatabase;
    AppExecutors mAppExecutors;
    XMPPManager mXMPPManager;
    MamManager mMamManager;
    EntityTimeManager mEntityTimeManager;

    CryptLib mCryptLib;

    Chat mChat;
    MultiUserChat mMUC;

    private MutableLiveData<Resource<Boolean>> istyping = new MutableLiveData<>();
    private MutableLiveData<Resource<Boolean>> istypingMUC = new MutableLiveData<>();

    private SharedPreferences mSharedPreferences;

    private MutableLiveData<Resource<List<RoomMUCExtend>>> mMUCRooms = new MutableLiveData<>();

    private static Calendar sCalendar = Calendar.getInstance();

    private String mGoogleTranslateKey;
    private String mAESKey;
    private final OtrManager mOtrManager;

    public ChatManager(AppDatabase appDatabase,
                       AppExecutors appExecutors,
                       XMPPManager xmppManager,
                       SharedPreferences sharedPreferences,
                       OtrManager otrManager,
                       CryptLib cryptLib) {

        mAppDatabase = appDatabase;
        mAppExecutors = appExecutors;
        mXMPPManager = xmppManager;
        mSharedPreferences = sharedPreferences;
        mCryptLib = cryptLib;
        mOtrManager = otrManager;
        mMamManager = mXMPPManager.getMamManager();
        mXMPPManager.getMUCManager().setAutoJoinOnReconnect(true);

        ProviderManager.addExtensionProvider("message", "jabber:client", new MessageElementProvider());
        ProviderManager.addIQProvider(IQMucGetSubscriptions.ELEMENT, IQMucGetSubscriptions.NAMESPACE, new IQMucGetSubscriptionProvider());
        ProviderManager.addIQProvider(IQAWSCertificate.ELEMENT_NAME, IQAWSCertificate.NAMESPACE, new IQAWSCertificateProvider());

        mXMPPManager.getChatManager().addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                mAppExecutors.diskIO().execute(() -> {
                    if (message.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                        if (from.asBareJid().equals(mXMPPManager.getConnection().getUser().asBareJid())) {
                            MessageCorrectExtension replace = message.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                            if (message.getSubject().equals("deleted")) {
                                mAppDatabase.deleteDao().insert(new Delete(message.getStanzaId()));
                                mAppDatabase.messageDao().deleteMessage(message.getStanzaId());
                            } else if (message.getSubject().equals("deletedall")) {
                                mAppDatabase.deleteAllDao().insert(new DeleteAll(message.getStanzaId()));
                            }
                        }
                    } else if (message.hasExtension(MamElements.MamResultExtension.ELEMENT, "urn:xmpp:mam:1")) {
                        Log.d("FORWARDED", message.getStanzaId());
                    } else {
                        final String chatId = message.getFrom().asBareJid().toString();
                        if (message.hasExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE)) {
                            DelayInformation delayInformation = message.getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
                            addMessage(from, message, chatId, delayInformation.getStamp(), 2);
                        } else {
                            try {
                                addMessage(from, message, chatId, getServerTime(), 2);
                                mAppDatabase.chatDao().increaseChatUnreadCount(chatId);
                                if (message.getSubject() == null) {
                                    TranslateSetting translateSetting = mAppDatabase.chatBackgroundDao().getTranslateSetting(chatId);
                                    if (translateSetting.isAutoTranslate()) {
                                        translateText(message.getStanzaId(),
                                                message.getBody(),
//                                                decryptMessage(message.getBody()),
                                                translateSetting.getLanguage());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("MessageMUC", message.getStanzaId());
                        Log.d("ONEonONE", message.getBody());
                        //addMessage(from, message, chat.getXmppAddressOfChatPartner().asBareJid().toString(), new Date(), 2);
                    }
                });
            }
        });

        mXMPPManager.getChatStateManager().addChatStateListener(new ChatStateListener() {
            @Override
            public void stateChanged(Chat chat, ChatState state, Message message) {
                if (mChat != null) {
                    if (mChat.getXmppAddressOfChatPartner() == chat.getXmppAddressOfChatPartner() && state.equals(ChatState.composing)) {
                        istyping.postValue(Resource.success(true));
                    } else if (mChat.getXmppAddressOfChatPartner() == chat.getXmppAddressOfChatPartner() && state.equals(ChatState.paused)) {
                        istyping.postValue(Resource.success(false));
                    }
                }
            }
        });

        mXMPPManager.getChatManager().addOutgoingListener(new OutgoingChatMessageListener() {
            @Override
            public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
                appExecutors.diskIO().execute(() -> {
                    ChatStateExtension composing = new ChatStateExtension(ChatState.composing);
                    ChatStateExtension paused = new ChatStateExtension(ChatState.paused);
                    if (message.hasExtension(composing.getElementName(), composing.getNamespace())) {
                        //
                    } else if (message.hasExtension(paused.getElementName(), paused.getNamespace())) {
                        //
                    } else if (message.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                        try {
                            MessageCorrectExtension replace = message.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                            if (null != replace.getIdInitialMessage()) {
                                if (message.getSubject().equals("deleted")) {
                                    mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                    mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                } else if (message.getSubject().equals("deletedall")) {
                                    mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (!message.getBody().startsWith("?OTR"))
                                addMessage(mXMPPManager.getConnection().getUser().asEntityBareJid(),
                                        message,
                                        chat.getXmppAddressOfChatPartner().asBareJid().toString(),
                                        getServerTime(),
                                        1);
//                            ackStanza(message.getStanzaId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        mXMPPManager.getMUCManager().addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection conn, MultiUserChat room, EntityJid inviter, String reason, String password, Message message, MUCUser.Invite invitation) {
                joinGroup(room);
            }
        });


        mXMPPManager.getConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                if (packet instanceof Message) {
                    if (((Message) packet).getBody() == null) {
                        if (packet.hasExtension("x", "http://jabber.org/protocol/muc#user")) {
                            MUCUser mucUser = packet.getExtension("x", "http://jabber.org/protocol/muc#user");
                            if (mucUser.getStatus().contains(MUCUser.Status.create(104))) {
                                Log.d("MUC", "Change name");
                                setRoomNameToDB(packet.getFrom().asBareJid().toString());
                            }
                        }
                    }
                    Message message1 = (Message) packet;
                    ChatStateExtension composing = new ChatStateExtension(ChatState.composing);
                    ChatStateExtension paused = new ChatStateExtension(ChatState.paused);
                    if (mMUC != null) {
                        if (message1.getFrom().asBareJid().equals(mMUC.getRoom().asBareJid()) &&
                                !message1.getFrom().getResourceOrEmpty().equals(Resourcepart.EMPTY) &&
                                !message1.getFrom().getResourceOrEmpty().toString().equals(mXMPPManager.getConnection().getUser().asBareJid().toString())) {
                            if (message1.hasExtension(composing.getElementName(), composing.getNamespace())) {
                                Log.d("Type", "true");
                                istypingMUC.postValue(Resource.success(true));
                            } else if (message1.hasExtension(paused.getElementName(), paused.getNamespace())) {
                                Log.d("Type", "false");
                                istypingMUC.postValue(Resource.success(false));
                            }
                        }
                    }
                    if (((Message) packet).getBody() != null) {
                        if (!((Message) packet).getBody().equals("") ||
                                packet.getFrom() != null) {

                            Message message = (Message) packet;
                            Log.d("MUCMESSAGE", message.getBody());
                            EntityBareJid from;
                            if (packet.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                                from = packet.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? toEntityBareJidMUC(packet.getFrom().getResourceOrEmpty().toString()) : toEntityBareJidMUC(packet.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER);
                            } else {
                                from = toEntityBareJidMUC(packet.getFrom().asBareJid().toString());
                            }

                            mAppExecutors.diskIO().execute(() -> {
                                if (message.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                                    if (from.asBareJid().equals(mXMPPManager.getConnection().getUser().asBareJid())) {
                                        try {
                                            MessageCorrectExtension replace = message.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                            if (null != replace.getIdInitialMessage()) {
                                                if (message.getSubject().equals("deleted")) {
                                                    mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                                    mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                                } else if (message.getSubject().equals("deletedall")) {
                                                    mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else if (message.hasExtension(MamElements.MamResultExtension.ELEMENT, "urn:xmpp:mam:1")) {
                                    Log.d("FORWARDED", message.getStanzaId() + Forwarded.NAMESPACE);
                                } else {
                                    final String chatId = message.getFrom().asBareJid().toString();
                                    if (message.hasExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE)) {
                                        DelayInformation delayInformation = message.getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
                                        addMessage(from, message, chatId, delayInformation.getStamp(), 2);
                                    } else {
                                        try {
                                            addMessage(from, message, chatId, getServerTime(), 2);
                                            if (!JidCreate.bareFrom(message.getFrom().getResourceOrEmpty()).asBareJid().equals(mXMPPManager.getConnection().getUser().asBareJid())) {
                                                mAppDatabase.chatDao().increaseChatUnreadCount(chatId);
                                                if (message.getSubject() == null) {
                                                    TranslateSetting translateSetting = mAppDatabase.chatBackgroundDao().getTranslateSetting(chatId);
                                                    if (translateSetting.isAutoTranslate()) {
                                                        translateText(message.getStanzaId(),
//                                                                decryptMessage(message.getBody()),
                                                                message.getBody(),
                                                                translateSetting.getLanguage());
                                                    }
                                                }
                                            }
                                        } catch (XmppStringprepException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.d("MessageMUC", message.getStanzaId());
                                }
                            });
                        }
                    }
                }
            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                if (stanza.getFrom() != null && !stanza.hasExtension(PubSubElementType.ITEM_EVENT.getNamespace().getXmlns())) {
                    if (stanza.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });

        /**mXMPPManager.getConnection().addAsyncStanzaListener(new StanzaListener() {
        @Override public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
        EventElement event = EventElement.from(packet);

        if (event.getEventType().equals(EventElementType.items)) {
        ItemsExtension items = (ItemsExtension) event.getEvent();
        String nodeName = items.getNode();
        if (nodeName.equals("urn:xmpp:mucsub:nodes:messages")) {
        Message message = ((PayloadItem<MessageExtensionElement>) items.getItems().get(0))
        .getPayload()
        .getMessage();
        Log.d("MUCSUB", message.getBody());
        EntityBareJid from = toEntityBareJidMUC(message.getFrom().getResourceOrEmpty().toString());
        ChatStateExtension composing = new ChatStateExtension(ChatState.composing);
        ChatStateExtension paused = new ChatStateExtension(ChatState.paused);
        if (message.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
        if (from.asBareJid().equals(mXMPPManager.getConnection().getUser().asBareJid())) {
        try {
        MessageCorrectExtension replace = message.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
        if( null != replace.getIdInitialMessage()) {
        if (message.getSubject().equals("deleted")) {
        mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
        } else if (message.getSubject().equals("deletedall")) {
        mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
        }
        }
        } catch (Exception e) {
        e.printStackTrace();
        }
        }
        } else if (message.hasExtension(composing.getElementName(), composing.getNamespace())) {
        //istyping.postValue(Resource.success(true));
        } else if (message.hasExtension(paused.getElementName(), paused.getNamespace())) {
        //istyping.postValue(Resource.success(false));
        } else if (mAppDatabase.deleteDao().isExist(message.getStanzaId()) != null || mAppDatabase.deleteAllDao().isExist(message.getStanzaId()) != null) {

        } else {
        addMessage(from, message, message.getFrom().asBareJid().toString(), new Date(), 2);
        }
        } else if (nodeName.equals("urn:xmpp:mucsub:nodes:presences")) {**/
        /* subscription update (user add, leave)*/
        /**} else if (nodeName.equals("urn:xmpp:mucsub:nodes:subscribers")) {

         } else {
         Log.d("NodeName", nodeName);
         }
         }

         }
         }, (stanza) ->
         stanza.hasExtension(PubSubElementType.ITEM_EVENT.getNamespace().getXmlns()));**/

        mXMPPManager.getConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                if (packet.getFrom().getResourceOrEmpty().toString().equals(mXMPPManager.getConnection().getUser().asBareJid().toString())) {
                    if (mMUC != null) {
                        if (packet.getFrom().asBareJid().equals(mMUC.getRoom())) {
                            istypingMUC.postValue(Resource.serverError("You are kicked from the group", null));
                        }
                    }
                    deleteMessages(packet.getFrom().asBareJid().toString(), "groupchat", mXMPPManager.getConnection().getUser().asBareJid().toString());
                }
            }
        }, new OrFilter(new MUCUserStatusCodeFilter(321), new MUCUserStatusCodeFilter(307)));

    }

    public void joinGroup(MultiUserChat room) {
        try {

            Log.d("ROOM", room.getRoom().toString());

            //mMUC = mXMPPManager.getMUCManager().getMultiUserChat(room.getRoom());
            try {
                room.join(Resourcepart.from(mXMPPManager.getConnection().getUser().asBareJid().toString()));
                String roomname = room.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue();
                Log.d("ROOMNAME", roomname);
                setInitialMessage(room.getRoom(),
                        room.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue(),
                        "groupchat",
                        1,
                        true,
                        mXMPPManager.getConnection().getUser().asBareJid().toString());
            } catch (MultiUserChatException.NotAMucServiceException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            if (e.getStanzaError().getCondition() == StanzaError.Condition.bad_request) {
                //joinGroup(room);
            }

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public static String getPresigned(String key, AWSCertificate awsCertificate) {

        AmazonS3Client amazonS3Client = new AmazonS3Client(new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return awsCertificate.getAccessKey();
            }

            @Override
            public String getAWSSecretKey() {
                return awsCertificate.getSecretID();
            }
        });

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(awsCertificate.getBucketName(), key)
                .withMethod(HttpMethod.GET);
        String preSignedURL = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
        Log.d("PRESIGNED_URL", preSignedURL);
        return preSignedURL;
    }

    public LiveData<Resource<Boolean>> isTyping(String chatId) {
        return istyping;
    }

    public LiveData<Resource<Boolean>> isTypingMUC() {
        return istypingMUC;
    }

    public LiveData<Resource<ChatAndBackground>> getChat(String chatId) {
        MutableLiveData<Resource<ChatAndBackground>> chat = new MutableLiveData<>();
        chat.postValue(Resource.loading(null));
        mAppExecutors.diskIO().execute(() -> {
            chat.postValue(Resource.success(mAppDatabase.chatDao().getChat(chatId)));
        });
        return chat;
    }

    public void setInitialMessage(EntityBareJid chatJid, String name, String type, int max, String whoIs) {
        setInitialMessage(chatJid, name, type, max, false, whoIs);
    }

    public void setInitialMessage(EntityBareJid chatJid, String name, String type, int max, boolean start, String whoIs) {
        Log.d("MAM", "Start");
        com.wrappy.android.db.entity.Chat chat = new com.wrappy.android.db.entity.Chat();
        chat.setChatName(name);
        chat.setChatId(chatJid.asBareJid().toString());
        chat.setChatWhoIs(whoIs);
        chat.setChatUnreadCount(0);
        chat.setChatNotification(true);
        if (type.equals("groupchat")) {
            chat.setChatLastMessage(" ");
        }
        chat.setChatType(type);
        ChatBackground chatBackground = new ChatBackground(chatJid.asBareJid().toString(), whoIs, null);
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.chatDao().insert(chat);
            mAppDatabase.chatBackgroundDao().insert(chatBackground);
        });
        mAppExecutors.diskIO().execute(() -> {
            try {
                MamManager.MamQuery mamQuery;

                MamManager.MamQueryArgs.Builder mamBuilder = new MamManager.MamQueryArgs.Builder();
                mamBuilder.queryLastPage();
                mamBuilder.setResultPageSize(max);


                if (type.equals("chat")) {
                    //mamQueryResult = mMamManager.mostRecentPage(chatJid, 20);
                    mamBuilder.limitResultsToJid(chatJid);
                    mamQuery = mMamManager.queryArchive(mamBuilder.build());
                } else {
                    Log.d("MUCJID", chatJid.toString());
                    MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(chatJid);
                    if (!muc.isJoined()) {
                        muc.join(Resourcepart.from(whoIs));
                    }
                    //mamQueryResult = MamManager.getInstanceFor(muc).mostRecentPage(chatJid,20);
                    //mamBuilder.limitResultsToJid(chatJid);
                    String roomname = muc.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue();
                    mAppDatabase.chatDao().changeChatName(roomname, muc.getRoom().asBareJid().toString());

                    MamManager mamMUC = MamManager.getInstanceFor(mXMPPManager.getMUCManager().getMultiUserChat(chatJid));

                    mamQuery = mamMUC.queryArchive(mamBuilder.build());

                }

                List<Forwarded> forwardedList = new ArrayList<>(mamQuery.getPage().getForwarded());
                List<MamElements.MamResultExtension> mamResultExtensionList = new ArrayList<>(mamQuery.getMamResultExtensions());
                if (!forwardedList.isEmpty()) {
                    //Collections.reverse(forwardedList);
                    Collections.sort(forwardedList, new Comparator<Forwarded>() {
                        @Override
                        public int compare(Forwarded o1, Forwarded o2) {
                            return o2.getDelayInformation().getStamp().compareTo(o1.getDelayInformation().getStamp());
                        }
                    });
                    //Collections.reverse(forwardedList);
                }

                if (!mamResultExtensionList.isEmpty()) {
                    Collections.sort(mamResultExtensionList, new Comparator<MamElements.MamResultExtension>() {
                        @Override
                        public int compare(MamElements.MamResultExtension o1, MamElements.MamResultExtension o2) {
                            return o2.getForwarded().getDelayInformation().getStamp().compareTo(o1.getForwarded().getDelayInformation().getStamp());
                        }
                    });
                }

                Log.d("CHAT_JID", chatJid.toString());
                for (Forwarded forwarded : forwardedList) {
                    Message messageXMPP = (Message) forwarded.getForwardedStanza();

                    String subject = "";
//                    String message = decryptMessage(messageXMPP.getBody());
                    String message = messageXMPP.getBody();
                    String timestamp = forwarded.getDelayInformation().getStamp().toString();

                    if (messageXMPP.getSubject() != null) {
                        subject = messageXMPP.getSubject();
                    } else {
                        subject = "TEXT";
                    }

                    Log.d(subject, message + " : " + timestamp);
                }

                for (Forwarded forwarded : forwardedList) {

                    Stanza stanza = forwarded.getForwardedStanza();
                    Log.d("Delete", stanza.toString());

                    Message messageXMPP = (Message) forwarded.getForwardedStanza();

                    String from;
                    if (messageXMPP.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                        from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                    } else {
                        from = messageXMPP.getFrom().asBareJid().toString();
                    }

                    if (messageXMPP.getSubject() != null) {
                        if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                            if (from.equals(mXMPPManager.mConnection.getUser().asBareJid().toString())) {
                                try {
                                    MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                    // TODO: Save to DB
                                    if (null != replace.getIdInitialMessage()) {
                                        if (messageXMPP.getSubject().equals("deletedall")) {
                                            mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                            Log.d("Deletedall", replace.getIdInitialMessage());
                                            break;
                                        } else if (messageXMPP.getSubject().equals("deleted")) {
                                            Log.d("Deleted", replace.getIdInitialMessage());
                                            mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                            mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }

                for (MamElements.MamResultExtension mamResultExtension : mamResultExtensionList) {
                    Forwarded forwarded = mamResultExtension.getForwarded();
                    DelayInformation delayInformation = forwarded.getDelayInformation();

                    Stanza stanza = forwarded.getForwardedStanza();
                    Log.d("Stanza", stanza.toString());

                    Message messageXMPP = (Message) forwarded.getForwardedStanza();
                    if (messageXMPP.getSubject() != null) {
                        Log.d("Subject", messageXMPP.getSubject());
                    }

                    if (type.equals("chat")) {
                        if (messageXMPP.getType().equals(Message.Type.groupchat))
                            continue;
                    } else {
                        if (messageXMPP.getType().equals(Message.Type.chat)) {
                            continue;
                        }
                    }

                    //Log.d("Message", messageXMPP.getBody());

                    //Log.d(messageXMPP.getFrom().toString(), messageXMPP.getBody());

                    StanzaIdElement stanzaIdElement = messageXMPP.getExtension("stanza-id", "urn:xmpp:sid:0");

                    String from;
                    if (messageXMPP.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                        from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                    } else {
                        from = messageXMPP.getFrom().asBareJid().toString();
                    }
                    if (messageXMPP.getSubject() != null) {
                        if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                            if (from.equals(whoIs)) {
                                try {
                                    MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                    // TODO: Save to DB
                                    if (null != replace.getIdInitialMessage()) {
                                        if (messageXMPP.getSubject().equals("deletedall")) {
                                            mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                            Log.d("Deletedall", replace.getIdInitialMessage());
                                            break;
                                        } else if (messageXMPP.getSubject().equals("deleted")) {
                                            Log.d("Deleted", replace.getIdInitialMessage());
                                            mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                            mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                continue;
                            }
                        }
                    }


                    /**if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                     break;
                     }

                     if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                     continue;
                     }**/

                    int messageType = 0;

                    if (messageXMPP.getType().equals(Message.Type.chat)) {
                        messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_CHAT;
                    } else if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                        messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_GROUP;
                    }

                    if (messageXMPP.getSubject() != null) {
                        if (messageXMPP.getSubject().equals("deleted") || messageXMPP.getSubject().equals("deletedall")
                                || mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null
                                || mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                            if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                                break;
                            }

                            if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                                continue;
                            }
                        } else {

                            String messageBody = messageXMPP.getBody();
//                            String messageBody = decryptMessage(messageXMPP.getBody());
                            Log.d("MESSAGE_BODY", "" + messageBody);
                            try {
                                if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":image%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":voice%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                    String remove = messageBody.substring(0, 8);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":map%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":stamp%", "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            com.wrappy.android.db.entity.Message message =
                                    new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                            chatJid.asBareJid().toString(),
                                            messageType,
                                            messageBody,
                                            messageXMPP.getSubject(),
                                            from,
                                            delayInformation.getStamp(),
                                            2);

                            Log.d("TIME_MESSAGE", delayInformation.getStamp().toString());
                            if (mamResultExtension != null) {
                                if (null != mamResultExtension.getId()) {
                                    message.setArchiveId(mamResultExtension.getId());
                                }
                            }

                            final String decryptedBody = messageBody;
                            mAppExecutors.diskIO().execute(() -> {
                                if (messageXMPP.getStanzaId() != null) {
                                    mAppDatabase.messageDao().upsert(message);
                                }
                                if (type.equals("groupchat") && max == 1) {
                                    if (messageXMPP.getSubject() != null) {
                                        mAppDatabase.chatDao().changeChatLastMessage(
                                                messageXMPP.getSubject(),
                                                delayInformation.getStamp(),
                                                chatJid.asBareJid().toString());
                                    } else {

                                        mAppDatabase.chatDao().changeChatLastMessage(
                                                decryptedBody,
                                                delayInformation.getStamp(),
                                                chatJid.asBareJid().toString());
                                    }
                                }
                                Log.d("MAM", messageXMPP.toString());
                            });
                        }
                    } else {
                        if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                            Log.d("MESSAGE_DELETEDALL", messageXMPP.getStanzaId());
                            break;
                        } else if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                            Log.d("MESSAGE_DELETED", messageXMPP.getStanzaId());
                            continue;
                        } else {

//                            String messageBody = decryptMessage(messageXMPP.getBody());
                            String messageBody = messageXMPP.getBody();
                            Log.d("MESSAGE_BODY", messageBody);

                            try {

                                if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":image%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":voice%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                    String remove = messageBody.substring(0, 8);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":map%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":stamp%", "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            com.wrappy.android.db.entity.Message message =
                                    new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                            chatJid.asBareJid().toString(),
                                            messageType,
                                            messageBody,
                                            messageXMPP.getSubject(),
                                            from,
                                            delayInformation.getStamp(),
                                            2);
                            if (mamResultExtension != null) {
                                if (null != mamResultExtension.getId()) {
                                    message.setArchiveId(mamResultExtension.getId());
                                }
                            }

                            final String decryptedBody = messageBody;
                            mAppExecutors.diskIO().execute(() -> {
                                if (messageXMPP.getStanzaId() != null) {
                                    mAppDatabase.messageDao().upsert(message);
                                }
                                if (type.equals("groupchat") && max == 1) {
                                    if (messageXMPP.getSubject() != null) {
                                        mAppDatabase.chatDao().changeChatLastMessage(
                                                messageXMPP.getSubject(),
                                                delayInformation.getStamp(),
                                                chatJid.asBareJid().toString());
                                    } else {

                                        mAppDatabase.chatDao().changeChatLastMessage(
                                                decryptedBody,
                                                delayInformation.getStamp(),
                                                chatJid.asBareJid().toString());
                                    }
                                }
                                Log.d("MAM", messageXMPP.toString());
                            });
                        }
                    }
                }
                mAppExecutors.diskIO().execute(() -> {
                    if (mamQuery.getMessageCount() == 0 && type.equals("groupchat")) {
                        if (start) {
                            try {
                                mAppDatabase.chatDao().changeChatLastMessage(" ", getServerTime(), chatJid.asBareJid().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();

            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (MultiUserChatException.NotAMucServiceException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        });

    }

    public void setInitMUCMessage(EntityBareJid chatJid, String name) {
        Log.d("MAM", "Start");
        com.wrappy.android.db.entity.Chat chat = new com.wrappy.android.db.entity.Chat();
        chat.setChatName(name);
        chat.setChatId(chatJid.asBareJid().toString());
        chat.setChatWhoIs(mXMPPManager.getConnection().getUser().asBareJid().toString());
        chat.setChatUnreadCount(0);
        chat.setChatNotification(true);
        chat.setChatType("chat");

        ChatBackground chatBackground = new ChatBackground(chatJid.asBareJid().toString(), mXMPPManager.getConnection().getUser().asBareJid().toString(), null);

        if (mAppDatabase.chatDao().hasMessage(chatJid.asBareJid().toString()) == null) {

            mAppDatabase.chatDao().insert(chat);
            mAppDatabase.chatBackgroundDao().insert(chatBackground);

            boolean isEmpty = true;
            int initMax = 1;
            do {
                try {
                    MamManager.MamQuery mamQuery;
                    MamManager.MamQueryArgs.Builder mamBuilder = new MamManager.MamQueryArgs.Builder();
                    mamBuilder.queryLastPage();
                    mamBuilder.setResultPageSize(initMax);
                    MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(chatJid);
                    if (!muc.isJoined()) {
                        muc.join(Resourcepart.from(mXMPPManager.getConnection().getUser().asBareJid().toString()));
                    }
                    //mamQueryResult = MamManager.getInstanceFor(muc).mostRecentPage(chatJid,20);
                    //mamBuilder.limitResultsToJid(chatJid);

                    MamManager mamMUC = MamManager.getInstanceFor(mXMPPManager.getMUCManager().getMultiUserChat(chatJid));

                    mamQuery = mamMUC.queryArchive(mamBuilder.build());
                    if (mamQuery.getMessageCount() == 0) {
                        isEmpty = false;
                    }

                    List<Forwarded> forwardedList = new ArrayList<>(mamQuery.getPage().getForwarded());
                    if (!forwardedList.isEmpty()) {
                        //Collections.reverse(forwardedList);
                        Collections.sort(forwardedList, new Comparator<Forwarded>() {
                            @Override
                            public int compare(Forwarded o1, Forwarded o2) {
                                return o2.getDelayInformation().getStamp().compareTo(o1.getDelayInformation().getStamp());
                            }
                        });
                        Collections.reverse(forwardedList);
                    }

                    for (Forwarded forwarded : forwardedList) {

                        Stanza stanza = forwarded.getForwardedStanza();
                        Log.d("Delete", stanza.toString());

                        Message messageXMPP = (Message) forwarded.getForwardedStanza();

                        String from;
                        if (messageXMPP.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                            from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                        } else {
                            from = messageXMPP.getFrom().asBareJid().toString();
                        }

                        if (messageXMPP.getSubject() != null) {
                            if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                                if (from.equals(mXMPPManager.mConnection.getUser().asBareJid().toString())) {
                                    try {
                                        MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                        // TODO: Save to DB
                                        if (null != replace.getIdInitialMessage()) {
                                            if (messageXMPP.getSubject().equals("deletedall")) {
                                                mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                                Log.d("Deletedall", messageXMPP.getStanzaId());
                                                //break;
                                            } else if (messageXMPP.getSubject().equals("deleted")) {
                                                Log.d("Deleted", messageXMPP.getStanzaId());
                                                mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                                mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                                //continue;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                    }

                    for (MamElements.MamResultExtension mamResultExtension : mamQuery.getMamResultExtensions()) {
                        Forwarded forwarded = mamResultExtension.getForwarded();
                        DelayInformation delayInformation = forwarded.getDelayInformation();


                        Stanza stanza = forwarded.getForwardedStanza();
                        Log.d("Stanza", stanza.toString());

                        Message messageXMPP = (Message) forwarded.getForwardedStanza();

                        StanzaIdElement stanzaIdElement = messageXMPP.getExtension("stanza-id", "urn:xmpp:sid:0");

                        // TODO: Save to Delete/DeleteAllDB

                        if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                            if (messageXMPP.getFrom().asBareJid().equals(mXMPPManager.mConnection.getUser().asBareJid())) {
                                try {
                                    MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                    // TODO: Save to DB
                                    if (null != replace.getIdInitialMessage()) {
                                        if (messageXMPP.getSubject().equals("deletedall")) {
                                            mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                            isEmpty = false;
                                            break;
                                        } else if (messageXMPP.getSubject().equals("deleted")) {
                                            mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                            mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                            if (initMax == 1) {
                                                initMax = 10;
                                            } else {
                                                initMax = initMax + 5;
                                            }
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (initMax == 1) {
                                    initMax = 10;
                                } else {
                                    initMax = initMax + 5;
                                }
                                continue;
                            }
                        }


                        /**if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                         isEmpty = false;
                         break;
                         }

                         if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                         if (initMax == 1) {
                         initMax = 10;
                         } else {
                         initMax = initMax + 5;
                         }
                         continue;
                         }**/

                        int messageType = 0;

                        if (messageXMPP.getType().equals(Message.Type.chat)) {
                            messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_CHAT;
                        } else if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                            messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_GROUP;
                        }

                        if (messageXMPP.getSubject() != null) {
                            if (messageXMPP.getSubject().equals("deleted") || messageXMPP.getSubject().equals("deletedall")
                                    || mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null
                                    || mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                                if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                                    isEmpty = false;
                                    break;
                                }

                                if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                                    if (initMax == 1) {
                                        initMax = 10;
                                    } else {
                                        initMax = initMax + 5;
                                    }
                                    continue;
                                }
                            } else {
                                isEmpty = false;

//                                String messageBody = decryptMessage(messageXMPP.getBody());
                                String messageBody = messageXMPP.getBody();
                                Log.d("MESSAGE_BODY", messageBody);

                                try {

                                    if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":image%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":voice%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                        String remove = messageBody.substring(0, 8);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":map%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":stamp%", "");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                com.wrappy.android.db.entity.Message message =
                                        new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                                chatJid.asBareJid().toString(),
                                                messageType,
                                                messageBody,
                                                messageXMPP.getSubject(),
                                                messageXMPP.getFrom().asBareJid().toString(),
                                                delayInformation.getStamp(),
                                                2);
                                if (mamResultExtension != null) {
                                    if (mamResultExtension.getId() != null) {
                                        message.setArchiveId(mamResultExtension.getId());
                                    }
                                }
                                mAppDatabase.messageDao().upsert(message);

                                if (messageXMPP.getSubject() != null) {
                                    mAppDatabase.chatDao().changeChatLastMessage(
                                            messageXMPP.getSubject(),
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                    Log.d("HAS_ITEM", "Attachment");
                                    isEmpty = false;
                                } else {
                                    mAppDatabase.chatDao().changeChatLastMessage(messageBody,
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                    Log.d("HAS_ITEM", "Attachment");
                                    isEmpty = false;
                                }
                                Log.d("MAM", messageXMPP.toString());

                            }
                            Log.d("CHATJID", chatJid.asBareJid().toString());
                            Log.d("COUNT", String.valueOf(initMax));
                        } else {
                            if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                                isEmpty = false;
                                break;
                            } else if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                                if (initMax == 1) {
                                    initMax = 10;
                                } else {
                                    initMax = initMax + 5;
                                }
                                continue;
                            } else {

                                String messageBody = messageXMPP.getBody();
//                                String messageBody = decryptMessage(messageXMPP.getBody());
                                Log.d("MESSAGE_BODY", messageBody);

                                try {

                                    if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":image%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":voice%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                        String remove = messageBody.substring(0, 8);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":map%", "");
                                    } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                        String remove = messageBody.substring(0, 10);
                                        Log.d("REMOVE", remove);
                                        messageBody = messageBody.replace(remove, "");
                                        messageBody = messageBody.replace(":stamp%", "");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                com.wrappy.android.db.entity.Message message =
                                        new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                                chatJid.asBareJid().toString(),
                                                messageType,
                                                messageBody,
                                                messageXMPP.getSubject(),
                                                messageXMPP.getFrom().asBareJid().toString(),
                                                delayInformation.getStamp(),
                                                2);
                                if (mamResultExtension != null) {
                                    if (mamResultExtension.getId() != null) {
                                        message.setArchiveId(mamResultExtension.getId());
                                    }
                                }
                                mAppDatabase.messageDao().upsert(message);

                                if (messageXMPP.getSubject() != null) {
                                    mAppDatabase.chatDao().changeChatLastMessage(
                                            messageXMPP.getSubject(),
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                    Log.d("HAS_ITEM", "Attachment");
                                    isEmpty = false;
                                } else {
                                    mAppDatabase.chatDao().changeChatLastMessage(messageBody,
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                    Log.d("HAS_ITEM", "Attachment");
                                    isEmpty = false;
                                }
                                Log.d("MAM", messageXMPP.toString());
                            }
                        }
                        Log.d("CHATJID", chatJid.asBareJid().toString());
                        Log.d("COUNT", String.valueOf(initMax));
                    }
                } catch (XMPPException.XMPPErrorException e) {
                    isEmpty = false;
                    e.printStackTrace();
                } catch (SmackException.NotLoggedInException e) {
                    isEmpty = false;
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    isEmpty = false;
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (MultiUserChatException.NotAMucServiceException e) {
                    e.printStackTrace();
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }

            } while (isEmpty);
        }
    }

    public LiveData<Resource<List<MessageView>>> pageChat(EntityBareJid chatJid, int offset, String lastMessageId, String type, String keyword) {
        MutableLiveData<Resource<List<MessageView>>> pagedMessages = new MutableLiveData<>();
        pagedMessages.postValue(Resource.loading(null));
        mAppExecutors.diskIO().execute(() -> {
            List<MessageView> initPagedMessages;
            if (keyword != null) {
                initPagedMessages = mAppDatabase.messageDao().pageSearchMessage(chatJid.toString(), keyword, offset);
            } else {
                initPagedMessages = mAppDatabase.messageDao().pageMessage(chatJid.toString(), offset);
            }

            if (initPagedMessages.isEmpty()) {
                mAppExecutors.networkIO().execute(() -> {
                    queryMessageArchive(chatJid, lastMessageId, type, null);
                });
                if (keyword != null) {
                    pagedMessages.postValue(Resource.success(mAppDatabase.messageDao().pageSearchMessage(chatJid.toString(), keyword, offset)));
                } else {
                    pagedMessages.postValue(Resource.success(mAppDatabase.messageDao().pageMessage(chatJid.toString(), offset)));
                }
            } else {
                pagedMessages.postValue(Resource.success(initPagedMessages));
            }
        });
        return pagedMessages;
    }

    public void queryMessageArchive(EntityBareJid chatJid, String lastMessageId, String type, String subject) {
        try {
            //MamManager.MamPrefsResult prefsResult = mMamManager.retrieveArchivingPreferences();
            //MamManager.MamQueryResult mamQueryResult = mMamManager.pageAfter(chatJid, lastMessageId, 1);
            MamManager.MamQueryArgs.Builder builder = new MamManager.MamQueryArgs.Builder();
            builder.beforeUid(lastMessageId);
            builder.setResultPageSize(30);

            if (subject != null && !subject.isEmpty()) {
                FormField formField = new FormField("subject");
                formField.addValue(subject);
                builder.withAdditionalFormField(formField);
            }

            MamManager.MamQuery mamQuery;

            if (type.equals("chat")) {
                builder.limitResultsToJid(chatJid);
                mamQuery = mMamManager.queryArchive(builder.build());
            } else {
                MamManager mamMUC = MamManager.getInstanceFor(mXMPPManager.getMUCManager().getMultiUserChat(chatJid));
                mamQuery = mamMUC.queryArchive(builder.build());
            }

            List<Forwarded> forwardedList = new ArrayList<>(mamQuery.getPage().getForwarded());
            List<MamElements.MamResultExtension> mamResultExtensionList = new ArrayList<>(mamQuery.getMamResultExtensions());
            if (!forwardedList.isEmpty()) {
                //Collections.reverse(forwardedList);
                Collections.sort(forwardedList, new Comparator<Forwarded>() {
                    @Override
                    public int compare(Forwarded o1, Forwarded o2) {
                        return o2.getDelayInformation().getStamp().compareTo(o1.getDelayInformation().getStamp());
                    }
                });
                //Collections.reverse(forwardedList);
            }

            if (!mamResultExtensionList.isEmpty()) {
                Collections.sort(mamResultExtensionList, new Comparator<MamElements.MamResultExtension>() {
                    @Override
                    public int compare(MamElements.MamResultExtension o1, MamElements.MamResultExtension o2) {
                        return o2.getForwarded().getDelayInformation().getStamp().compareTo(o1.getForwarded().getDelayInformation().getStamp());
                    }
                });
            }

            mAppExecutors.diskIO().execute(() -> {
                for (Forwarded forwarded : forwardedList) {

                    Stanza stanza = forwarded.getForwardedStanza();
                    Log.d("Delete", stanza.toString());

                    Message messageXMPP = (Message) forwarded.getForwardedStanza();

                    String from;
                    if (messageXMPP.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                        from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                    } else {
                        from = messageXMPP.getFrom().asBareJid().toString();
                    }

                    if (messageXMPP.getSubject() != null) {
                        if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                            if (from.equals(mXMPPManager.mConnection.getUser().asBareJid().toString())) {
                                try {
                                    MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                    // TODO: Save to DB
                                    if (null != replace.getIdInitialMessage()) {
                                        if (messageXMPP.getSubject().equals("deletedall")) {
                                            mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                            Log.d("Deletedall", messageXMPP.getStanzaId());
                                        } else if (messageXMPP.getSubject().equals("deleted")) {
                                            Log.d("Deleted", messageXMPP.getStanzaId());
                                            mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                            mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }

                for (MamElements.MamResultExtension mamResultExtension : mamResultExtensionList) {
                    Forwarded forwarded = mamResultExtension.getForwarded();
                    DelayInformation delayInformation = forwarded.getDelayInformation();
                    Message messageXMPP = (Message) forwarded.getForwardedStanza();

                    if (type.equals("chat")) {
                        if (messageXMPP.getType().equals(Message.Type.groupchat))
                            continue;
                    } else {
                        if (messageXMPP.getType().equals(Message.Type.chat)) {
                            continue;
                        }
                    }

                    //StanzaIdElement stanzaIdElement = messageXMPP.getExtension("stanza-id", "urn:xmpp:sid:0");

                    String from;
                    if (messageXMPP.getFrom().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                        from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER) ? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                    } else {
                        from = messageXMPP.getFrom().asBareJid().toString();
                    }

                    /** Save to Delete/DeleteAllDB **/
                    if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                        if (from.equals(mXMPPManager.mConnection.getUser().asBareJid().toString())) {
                            try {
                                MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                // TODO: Save to DB
                                if (null != replace.getIdInitialMessage()) {
                                    if (messageXMPP.getSubject().equals("deletedall")) {
                                        mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                        break;
                                    } else if (messageXMPP.getSubject().equals("deleted")) {
                                        mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                        mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            continue;
                        }
                    }


                    if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                        break;
                    }

                    if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                        continue;
                    }

                    int messageType = 0;

                    if (messageXMPP.getType().equals(Message.Type.chat)) {
                        messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_CHAT;
                    } else if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                        messageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_GROUP;
                    }

                    if (messageXMPP.getSubject() != null) {

                        if (messageXMPP.getSubject().equals("deleted") || messageXMPP.getSubject().equals("deletedall")
                                || mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null
                                || mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                            if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                                break;
                            }

                            if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                                continue;
                            }
                        } else {

//                            String messageBody = decryptMessage(messageXMPP.getBody());
                            String messageBody = messageXMPP.getBody();
                            Log.d("MESSAGE_BODY", messageBody);

                            try {

                                if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":image%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":voice%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                    String remove = messageBody.substring(0, 8);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":map%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":stamp%", "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            com.wrappy.android.db.entity.Message message =
                                    new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                            chatJid.asBareJid().toString(),
                                            messageType,
                                            messageBody,
                                            messageXMPP.getSubject(),
                                            from,
                                            delayInformation.getStamp(),
                                            2);
                            if (mamResultExtension != null) {
                                if (mamResultExtension.getId() != null) {
                                    message.setArchiveId(mamResultExtension.getId());
                                }
                            }

                            if (messageXMPP.getStanzaId() != null) {
                                mAppDatabase.messageDao().upsert(message);
                            }
                        }
                    } else {
                        if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                            break;
                        } else if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                            continue;
                        } else {

//                            String messageBody = decryptMessage(messageXMPP.getBody());
                            String messageBody = messageXMPP.getBody();
                            Log.d("MESSAGE_BODY", messageBody);

                            try {

                                if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":image%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":voice%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                                    String remove = messageBody.substring(0, 8);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":map%", "");
                                } else if (messageXMPP.getSubject() != null && messageXMPP.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                                    String remove = messageBody.substring(0, 10);
                                    Log.d("REMOVE", remove);
                                    messageBody = messageBody.replace(remove, "");
                                    messageBody = messageBody.replace(":stamp%", "");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            com.wrappy.android.db.entity.Message message =
                                    new com.wrappy.android.db.entity.Message(messageXMPP.getStanzaId(),
                                            chatJid.asBareJid().toString(),
                                            messageType,
                                            messageBody,
                                            messageXMPP.getSubject(),
                                            from,
                                            delayInformation.getStamp(),
                                            2);
                            if (mamResultExtension != null) {
                                if (mamResultExtension.getId() != null) {
                                    message.setArchiveId(mamResultExtension.getId());
                                }
                            }

                            mAppDatabase.messageDao().upsert(message);
                        }
                    }
                }
            });
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getChatList(String whoIs) {
        return mAppDatabase.chatDao().getChatList(whoIs);
    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getOnetoOneChatList(String whoIs) {
        return mAppDatabase.chatDao().getOnetoOneChatList(whoIs);
    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getQueryOnetoOneChatList(String query, String whoIs) {
        return mAppDatabase.chatDao().getQueryOnetoOneChatList(query, whoIs);
    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getGroupChatList(String whoIs) {
        return mAppDatabase.chatDao().getGroupChatList(whoIs);
    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getQueryGroupChatList(String query, String whoIs) {
        return mAppDatabase.chatDao().getQueryGroupChatList(query, whoIs);
    }

    public LiveData<Resource<ChatAndBackground>> startChat(String userJid, String chatName, String type, String whoIs) {
        MutableLiveData<Resource<ChatAndBackground>> mld = new MutableLiveData<>();
        mld.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            if (type.equals("chat")) {
                mChat = mXMPPManager.getChatManager().chatWith(ContactManager.toBareJid(userJid).asEntityBareJidOrThrow());
                setInitialMessage(mChat.getXmppAddressOfChatPartner(), chatName, type, 30, whoIs);
            } else {
                mMUC = mXMPPManager.getMUCManager().getMultiUserChat(toEntityBareJidMUC(userJid));
                try {
                    if (!mMUC.isJoined()) {
                        mMUC.join(Resourcepart.from(mXMPPManager.getConnection().getUser().asBareJid().toString()));
                    }
                    /**for (Affiliate affiliate : mMUC.getOwners()) {
                     Log.d("MEMBERS", affiliate.getJid().toString());
                     }**/

                    String roomname = mMUC.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue();
                    mAppDatabase.chatDao().changeChatName(roomname, mMUC.getRoom().asBareJid().toString());
                    istypingMUC.postValue(Resource.success(false));
                    setInitialMessage(mMUC.getRoom(), chatName, type, 30, whoIs);
                } catch (SmackException.NoResponseException e) {
                    mld.postValue(Resource.serverError(e.getMessage(), null));
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (MultiUserChatException.NotAMucServiceException e) {
                    e.printStackTrace();
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            mAppExecutors.diskIO().execute(() -> {
                mld.postValue(Resource.success(mAppDatabase.chatDao().getChat(userJid)));
            });
        });


        //chat = mAppDatabase.chatDao().getChat(userJid);

        return mld;
    }

    public LiveData<Resource<String>> createMUC(List<String> userJids) {
        MutableLiveData<Resource<String>> mutableLiveData = new MutableLiveData();
        mutableLiveData.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            userJids.add(mXMPPManager.getConnection().getUser().asBareJid().toString());
            Collections.sort(userJids);
            String chatJid;

            for (String userjid : userJids) {
                Log.d("USERJID_MUC", userjid);
            }

            EntityBareJid MUCRoom = toEntityBareJidMUC(hashMUC(getIdMUC(userJids)) + ServerConstants.XMPP_MUC_DOMAIN);

            mMUC = mXMPPManager.getMUCManager().getMultiUserChat(MUCRoom);

            try {
                Resourcepart nickname = Resourcepart.from(mXMPPManager.getConnection().getUser().asBareJid().toString());
                mMUC.create(nickname);
                Form answer = mMUC.getConfigurationForm().createAnswerForm();
                FormField formField = new FormField("muc#roomconfig_roomname");
                formField.setLabel("房间名称");
                formField.setType(FormField.Type.text_single);
                Log.d("ROOMCONF_ROOMNAME", mMUC.getConfigurationForm().getField("muc#roomconfig_roomname").toString());
                answer.getField("muc#roomconfig_persistentroom").addValue("1");
                answer.getField("muc#roomconfig_membersonly").addValue("0");
                answer.setAnswer("muc#roomconfig_roomname", getNameMUC(getIdMUC(userJids)));
                Log.d("MUC_NAME", getNameMUC(getIdMUC(userJids)));
                answer.setAnswer("muc#roomconfig_roomowners", userJids);
                mMUC.sendConfigurationForm(answer);


                //subscribetoMUC(mMUC.getRoom());
                //userJids.remove(mXMPPManager.getConnection().getUser().asBareJid().toString());
                for (String jidString : userJids) {
                    addMember(jidString);
                }
                chatJid = mMUC.getRoom().toString();
                com.wrappy.android.db.entity.Chat chat = new com.wrappy.android.db.entity.Chat();
                chat.setChatName(getNameMUC(getIdMUC(userJids)));
                chat.setChatId(MUCRoom.toString());
                chat.setChatUnreadCount(0);
                chat.setChatWhoIs(mXMPPManager.getConnection().getUser().asBareJid().toString());
                chat.setChatNotification(true);
                chat.setChatLastMessage(" ");
                chat.setChatLastDate(getServerTime());
                chat.setChatType("groupchat");
                ChatBackground chatBackground = new ChatBackground(MUCRoom.toString(), mXMPPManager.getConnection().getUser().asBareJid().toString(), null);

                mAppExecutors.diskIO().execute(() -> {
                    mAppDatabase.chatDao().insert(chat);
                    mAppDatabase.chatBackgroundDao().insert(chatBackground);
                    mutableLiveData.postValue(Resource.success(chatJid));
                });
            } catch (MultiUserChatException.MissingMucCreationAcknowledgeException | MultiUserChatException.MucAlreadyJoinedException e) {
                mutableLiveData.postValue(Resource.serverError(WrappyApp.getInstance().getString(R.string.contact_error_group_exist), ""));
                e.printStackTrace();
            } catch (Exception e) {
                mutableLiveData.postValue(Resource.serverError(WrappyApp.getInstance().getString(R.string.chat_no_reponse_message), ""));
                e.printStackTrace();
            }
        });
        return mutableLiveData;

    }

    public void setMUCSubscriptions(Stanza stanza) {
        IQMucGetSubscriptions iqMucGetSubscriptions = (IQMucGetSubscriptions) stanza;
        for (RoomMUCExtend subscription : iqMucGetSubscriptions.getSubscriptions()) {
            Log.d("MUC_" + subscription.toString(), subscription.toString());
            /**if(!subscription.getNickname().toString().equals(mXMPPManager.getConnection().getUser().asBareJid().toString())) {
             continue;
             }**/
            Log.d("MUCJID", subscription.toString());
            MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(subscription.getRoomJid().asEntityBareJidOrThrow());
            try {
                joinMUC(muc, subscription.getRoomName());
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                setMUCSubscriptions(stanza);
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                if (e.getStanzaError().getCondition().equals(StanzaError.Condition.conflict)) {
                    e.printStackTrace();
                    try {
                        Log.d("LEAVE", muc.getRoom().toString());
                        muc.leave();
                        joinMUC(muc, subscription.getRoomName());
                    } catch (SmackException.NotConnectedException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    } catch (SmackException.NoResponseException e1) {
                        e1.printStackTrace();
                    } catch (XmppStringprepException e1) {
                        e1.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e1) {
                        e1.printStackTrace();
                    } catch (MultiUserChatException.NotAMucServiceException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MultiUserChatException.NotAMucServiceException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }

        getChatBadges();

        IQGroupListPushFlag iqGroupListPushFlag = new IQGroupListPushFlag();

        try {
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqGroupListPushFlag
                    , new IQResultReplyFilter(
                            iqGroupListPushFlag
                            , mXMPPManager.getConnection())
                    , new StanzaListener() {
                        @Override
                        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                            mAppExecutors.diskIO().execute(() -> {
                                Log.d("GROUPLISTPUSHFLAG", packet.getStanzaId());
                                IQGroupListPushFlag iqGroupListPushFlag1 = (IQGroupListPushFlag) packet;
                                for (GroupPushFlag groupPushFlag : iqGroupListPushFlag1.getGroupPushFlags()) {
                                    Log.d("GROUPPUSHFLAG", groupPushFlag.getGroupJid() + ":" + groupPushFlag.isPushFlag());
                                    mAppDatabase.chatDao().toggleNotif(groupPushFlag.isPushFlag(), groupPushFlag.getGroupJid().asBareJid().toString());
                                }
                            });
                        }
                    });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void getChatBadges() {
        // ADD BADGE
        IQBadge iqBadge = new IQBadge();
        try {
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqBadge, new IQResultReplyFilter(iqBadge, mXMPPManager.getConnection()), new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    mAppExecutors.diskIO().execute(() -> {
                        IQBadge iqBadge1 = (IQBadge) packet;
                        for (Badge badge : iqBadge1.getBadgeList()) {
                            Log.d("BADGE", badge.getChatJid() + " - COUNT: " + badge.getBadgeCount());
                            mAppDatabase.chatDao().updateChatUnreadCount(badge.getChatJid().toString(), badge.getBadgeCount());
                        }
                    });
                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public List<RoomMUCExtend> getMUCRoom(Stanza stanza) {
        IQMucGetSubscriptions iqMucGetSubscriptions = (IQMucGetSubscriptions) stanza;
        List<RoomMUCExtend> roomMUCExtends = new ArrayList<>();
        for (RoomMUCExtend subscription : iqMucGetSubscriptions.getSubscriptions()) {
            Log.d("MUC_" + subscription.toString(), subscription.toString());
            /**if(!subscription.getNickname().toString().equals(mXMPPManager.getConnection().getUser().asBareJid().toString())) {
             continue;
             }**/
            Log.d("MUCJID_GET_MUC_ROOM", subscription.toString());
            MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(subscription.getRoomJid().asEntityBareJidOrThrow());
            try {
                joinMUC(muc, muc.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue());
                subscription.setRoomStatus("Joined");
                roomMUCExtends.add(subscription);
            } catch (SmackException.NoResponseException e) {
                //setMUCSubscriptions(stanza);
                e.printStackTrace();
                subscription.setRoomStatus("No Response");
                roomMUCExtends.add(subscription);

            } catch (XMPPException.XMPPErrorException e) {
                if (e.getStanzaError().getCondition().equals(StanzaError.Condition.conflict)) {
                    e.printStackTrace();
                    subscription.setRoomStatus("Conflict - 409");
                    roomMUCExtends.add(subscription);
                } else if (e.getStanzaError().getCondition().equals(StanzaError.Condition.forbidden)) {
                    e.printStackTrace();
                    subscription.setRoomStatus("Forbidden - 403");
                    roomMUCExtends.add(subscription);

                } else {
                    e.printStackTrace();
                    subscription.setRoomStatus(e.getStanzaError().getConditionText());
                    roomMUCExtends.add(subscription);
                }
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MultiUserChatException.NotAMucServiceException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
        return roomMUCExtends;
    }

    public void joinMUC(MultiUserChat muc, String roomName) throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.NotAMucServiceException {
        if (!muc.isJoined()) {
            muc.join(Resourcepart.from(mXMPPManager.getConnection().getUser().asBareJid().toString()));
        }
        setInitialMessage(muc.getRoom(), roomName, "groupchat", 1, mXMPPManager.getConnection().getUser().asBareJid().toString());
        Log.d("JOINED", muc.getRoom().toString());
    }

    public void getMUCSubscriptions() {
        IQMucGetSubscriptions iqMucGetSubscriptions = new IQMucGetSubscriptions();

        IQ iq = new SimpleIQ("query", "im:iq:group") {
            @Override
            protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                xml.rightAngleBracket();
                return xml;
            }
        };

        iq.setType(IQ.Type.get);
        iq.setStanzaId();
        try {
            /**iqMucGetSubscriptions.setTo(JidCreate.domainBareFrom(ServerConstants.XMPP_MUC_DOMAIN.substring(1)));
             mXMPPManager.getConnection().addAsyncStanzaListener(new StanzaListener() {
            @Override public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
            try {
            setMUCSubscriptions(packet);
            mXMPPManager.getConnection().removeAsyncStanzaListener(this);
            } catch (Exception e) {
            e.printStackTrace();
            }

            }
            }, new IQResultReplyFilter(iqMucGetSubscriptions, mXMPPManager.getConnection()));**/
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iq,
                    new IQResultReplyFilter(iq, mXMPPManager.getConnection()),
                    new StanzaListener() {
                        @Override
                        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                            setMUCSubscriptions(packet);
                            //mMUCRooms.postValue(Resource.success(getMUCRoom(packet)));
                            mXMPPManager.getConnection().removeAsyncStanzaListener(this);
                            Log.d("MUCGETSUBSCRIPTION", "DONE");
                        }
                    });
            Log.d("MUCGETSUBSCRIPTION", "YES");
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Resource<List<RoomMUCExtend>>> getMUCRooms() {
        return mMUCRooms;
    }

    public void setChatBackground(String path, String chatId) {
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.chatBackgroundDao().setChatBackground(path, chatId, mXMPPManager.getConnection().getUser().asBareJid().toString());
        });
    }

    public LiveData<Resource<Boolean>> setChatNotification(boolean isNotif, String chatId, String chatType) {
        MutableLiveData<Resource<Boolean>> mld = new MutableLiveData<>();
        mld.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            if (chatType.equals("chat")) {
                IQJidSetPushFlag iqJidSetPushFlag = new IQJidSetPushFlag(isNotif ? 1 : 0, ContactManager.toBareJid(chatId));
                try {
                    mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqJidSetPushFlag, new IQResultReplyFilter(iqJidSetPushFlag, mXMPPManager.getConnection()),
                            new StanzaListener() {
                                @Override
                                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                                    if (packet instanceof IQJidResultPushFlag) {
                                        mAppExecutors.diskIO().execute(() -> {
                                            IQJidResultPushFlag iqJidResultPushFlag = (IQJidResultPushFlag) packet;
                                            if (iqJidResultPushFlag.getStatusResult()) {
                                                mAppDatabase.chatDao().toggleNotif(isNotif, chatId);
                                                mld.postValue(Resource.success(iqJidResultPushFlag.getStatusResult()));
                                            } else {
                                                mld.postValue(Resource.serverError("Something went wrong", null));
                                            }
                                        });
                                    }
                                }
                            });
                } catch (SmackException.NotConnectedException e) {
                    mld.postValue(Resource.clientError("Something went wrong", null));
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    mld.postValue(Resource.clientError("Something went wrong", null));
                    e.printStackTrace();
                }
            } else {
                IQGroupSetPushFlag iqGroupSetPushFlag = new IQGroupSetPushFlag(ContactManager.toBareJid(chatId), isNotif ? 1 : 0);
                try {
                    mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqGroupSetPushFlag, new IQResultReplyFilter(iqGroupSetPushFlag, mXMPPManager.getConnection()),
                            new StanzaListener() {
                                @Override
                                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                                    if (packet instanceof IQGroupResultPushFlag) {
                                        mAppExecutors.diskIO().execute(() -> {
                                            IQGroupResultPushFlag iqGroupResultPushFlag = (IQGroupResultPushFlag) packet;
                                            if (iqGroupResultPushFlag.getStatusResult()) {
                                                mAppDatabase.chatDao().toggleNotif(isNotif, chatId);
                                                mld.postValue(Resource.success(iqGroupResultPushFlag.getStatusResult()));
                                            } else {
                                                mld.postValue(Resource.serverError("Something went wrong", null));
                                            }
                                        });
                                    }
                                }
                            });
                } catch (SmackException.NotConnectedException e) {
                    mld.postValue(Resource.clientError("Something went wrong", null));
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    mld.postValue(Resource.clientError("Something went wrong", null));
                    e.printStackTrace();
                }
            }
        });
        return mld;
    }

    public void subscribetoMUC(EntityBareJid to) {
        IQMucSubscribe iqMucSubscribe = new IQMucSubscribe(mXMPPManager.getConnection().getUser().asBareJid().toString());
        iqMucSubscribe.setTo(to);
        try {
            mXMPPManager.getConnection().sendStanza(iqMucSubscribe);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void subscribetoMUC(EntityBareJid to, String userJid) {
        IQMucSubscribe iqMucSubscribe = new IQMucSubscribe(userJid, userJid);
        iqMucSubscribe.setTo(to);
        try {
            mXMPPManager.getConnection().sendStanza(iqMucSubscribe);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addMember(String userJid) {
        try {
            if (!userJid.equals(mXMPPManager.getConnection().getUser().asBareJid().toString())) {
                Log.d("INVITED", userJid);
                mMUC.invite(toEntityBareJidMUC(userJid), "");
                Log.d("GRANT_OWNER", userJid);
                mMUC.grantOwnership(ContactManager.toBareJid(userJid));
                //subscribetoMUC(mMUC.getRoom(), userJid);
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromMUC(EntityBareJid chatJid) {
        IQMucLeave iqMucLeave = new IQMucLeave();
        iqMucLeave.setTo(chatJid);
        try {
            mXMPPManager.getConnection().sendStanza(iqMucLeave);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromMUC(EntityBareJid chatJid, String userJid) {
        IQMucLeave iqMucLeave = new IQMucLeave(userJid);
        iqMucLeave.setTo(chatJid);
        try {
            mXMPPManager.getConnection().sendStanza(iqMucLeave);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void leaveMUC(String chatJid) {
        try {
            IQ deleteUser = new IQ("roomUser", "iq:roomuser:delete") {
                @Override
                protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                    xml.rightAngleBracket();
                    xml.halfOpenElement("user");
                    xml.attribute("delete", mXMPPManager.getConnection().getUser().asBareJid().toString());
                    xml.attribute("roomName", chatJid);
                    xml.rightAngleBracket();
                    xml.closeElement("user");
                    return xml;
                }
            };
            mXMPPManager.getConnection().sendStanza(deleteUser);
            MultiUserChat multiUserChat = mXMPPManager.getMUCManager().getMultiUserChat(toEntityBareJidMUC(chatJid));
            //multiUserChat.revokeOwnership(mXMPPManager.getConnection().getUser());
            multiUserChat.revokeMembership(mXMPPManager.getConnection().getUser());
            multiUserChat.leave();
            //unsubscribeFromMUC(toEntityBareJidMUC(chatJid));
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.chatDao().deleteChat(chatJid);
                mAppDatabase.messageDao().deleteMessages(chatJid);
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public boolean removeMember(String userJid) {
        boolean isSelf = false;
        if (toEntityBareJidMUC(userJid).equals(mXMPPManager.getConnection().getUser().asEntityBareJid())) {
            isSelf = true;
        } else {
            try {
                IQ deleteUser = new IQ("roomUser", "iq:roomuser:delete") {
                    @Override
                    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                        xml.rightAngleBracket();
                        xml.halfOpenElement("user");
                        xml.attribute("delete", userJid);
                        xml.attribute("roomName", mMUC.getRoom().asBareJid().toString());
                        xml.rightAngleBracket();
                        xml.closeElement("user");
                        return xml;
                    }
                };
                mXMPPManager.getConnection().sendStanza(deleteUser);
                //mMUC.revokeOwnership(toEntityBareJidMUC(userJid));
                mMUC.revokeMembership(toEntityBareJidMUC(userJid));
                //unsubscribeFromMUC(mMUC.getRoom(), userJid);
                mMUC.kickParticipant(Resourcepart.from(userJid), "");

            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            } finally {
                if (toEntityBareJidMUC(userJid).equals(mXMPPManager.getConnection().getUser().asEntityBareJid())) {
                    deleteMessages(mMUC.getRoom().asBareJid().toString(), "groupchat", mXMPPManager.getConnection().getUser().asBareJid().toString());
                    isSelf = true;
                    mMUC = null;
                } else {
                    isSelf = false;
                }
            }
        }
        return isSelf;
    }

    public void setRoomName(String roomName) {
        try {
            Form configForm = mMUC.getConfigurationForm().createAnswerForm();
            configForm.setAnswer("muc#roomconfig_roomname", roomName);
            mMUC.sendConfigurationForm(configForm);
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.chatDao().changeChatName(roomName, mMUC.getRoom().asBareJid().toString());
            });
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setRoomNameToDB(String mucJid) {
        mAppExecutors.diskIO().execute(() -> {
            try {
                MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(toEntityBareJidMUC(mucJid));
                String name = muc.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue();
                mAppDatabase.chatDao().changeChatName(name, mucJid);
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    public String getRoomName() {
        String name = null;
        try {
            name = mMUC.getConfigurationForm().getField("muc#roomconfig_roomname").getFirstValue();
            //mAppDatabase.chatDao().changeChatName(name, mMUC.getRoom().asBareJid().toString());
            return name;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name;
    }

    public EntityBareJid toEntityBareJidMUC(String mucId) {
        EntityBareJid mucJid = null;
        try {
            mucJid = JidCreate.entityBareFrom(mucId);
            return mucJid;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return mucJid;
    }

    public List<String> getIdMUC(List<String> userJids) {
        List<String> names = new ArrayList<>();

        for (String name : userJids) {
            names.add(ContactManager.getUserName(name));
            Log.d("NAMES", ContactManager.getUserName(name));
        }

        return names;
    }

    public String getNameMUC(List<String> userIds) {
        StringBuilder nameBuilder = new StringBuilder();

        for (String s : userIds) {
            nameBuilder.append(s);
            nameBuilder.append(",");
        }

        String names = nameBuilder.toString();

        if (names.endsWith(",")) {
            names = names.substring(0, names.length() - 1);
        }

        return names;

    }

    public String hashMUC(List<String> userJids) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : userJids) {
            stringBuilder.append(name);
        }
        Log.d("STRINGNAME", stringBuilder.toString());
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(stringBuilder.toString().getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public LiveData<Resource<String>> sendMessage(String message, String subject, String chatType, String chatName) {
        MutableLiveData mld = new MutableLiveData<Resource<String>>();
        Message messageXMPP = new Message();
        messageXMPP.setFrom(mXMPPManager.getConnection().getUser().asBareJid());
//        messageXMPP.setBody(encryptMessage(message));


        messageXMPP.setSubject(subject);
        messageXMPP.addExtension(new DeliveryReceiptRequest());
        try {
            if (chatType.equals("chat")) {
                messageXMPP.setType(Message.Type.chat);
                messageXMPP.addExtension(new NickNameElement(chatName));
                messageXMPP.setStanzaId();
                String otrMsg = mOtrManager.transformSending(mChat.getXmppAddressOfChatPartner().asEntityBareJidString(), message);
                messageXMPP.setBody(otrMsg);
                mChat.send(messageXMPP);
                mld.postValue(Resource.success(messageXMPP));
                if (!TextUtils.isEmpty(otrMsg)) {
                    mAppExecutors.diskIO().execute(()->{
                    messageXMPP.setBody(message);
                        try {
                            addMessage(mXMPPManager.getConnection().getUser().asEntityBareJid(), messageXMPP, mChat.getXmppAddressOfChatPartner().asEntityBareJidString(), getServerTime(), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                messageXMPP.setBody(message);
                messageXMPP.setType(Message.Type.groupchat);
                messageXMPP.addExtension(new NaturalNameElement(chatName));
                mMUC.sendMessage(messageXMPP);
                mld.postValue(Resource.success(message));
                /**com.wrappy.android.db.entity.Chat chat = new com.wrappy.android.db.entity.Chat();
                 chat.setChatId(mMUC.getRoom().toString());
                 chat.setChatType(chatType);

                 chat.setChatLanguage("English");
                 chat.setChatNotification(true);
                 chat.setChatUnreadCount(0);
                 chat.setChatName(getRoomName());
                 mAppExecutors.diskIO().execute(() -> {
                 mAppDatabase.chatDao().insert(chat);
                 });**/
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            mld.postValue(Resource.clientError(e.getMessage(), message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mld;
    }

    public void addMessage(EntityBareJid from, Message message, String chatId, Date date, int status) {
        Log.d("Message", message.getBody());

        if (mAppDatabase.deleteDao().isExist(message.getStanzaId()) != null) {
            return;
        }

        StanzaIdElement stanzaIdElement = message.getExtension("stanza-id", "urn:xmpp:sid:0");

        MamElements.MamResultExtension mamResultExtension = message.getExtension("result", "urn:xmpp:mam:1");

        String messageId;

        int MessageType = 0;

        if (message.getType().equals(Message.Type.chat)) {
            MessageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_CHAT;
        } else if (message.getType().equals(Message.Type.groupchat)) {
            MessageType = com.wrappy.android.db.entity.Message.MESSAGE_TYPE_GROUP;
        }

//        String messageBody = decryptMessage(message.getBody());
        String messageBody = message.getBody();
        if (messageBody.startsWith("?OTR")) {
            messageBody = mOtrManager.transformReceiving(from.asEntityBareJidString(), messageBody);
            if (TextUtils.isEmpty(messageBody)) {
                return;
            }
        }
        Log.d("MESSAGE_BODY", messageBody);

        try {

            if (message.getSubject() != null && message.getSubject().equals("image") && messageBody.contains(ChatMessageFragment.IMAGE_PREFIX)) {
                String remove = messageBody.substring(0, 10);
                Log.d("REMOVE", remove);
                messageBody = messageBody.replace(remove, "");
                messageBody = messageBody.replace(":image%", "");
            } else if (message.getSubject() != null && message.getSubject().equals("voice") && messageBody.contains(ChatMessageFragment.VOICE_PREFIX)) {
                String remove = messageBody.substring(0, 10);
                Log.d("REMOVE", remove);
                messageBody = messageBody.replace(remove, "");
                messageBody = messageBody.replace(":voice%", "");
            } else if (message.getSubject() != null && message.getSubject().equals("location") && messageBody.contains(ChatMessageFragment.MAP_PREFIX)) {
                String remove = messageBody.substring(0, 8);
                Log.d("REMOVE", remove);
                messageBody = messageBody.replace(remove, "");
                messageBody = messageBody.replace(":map%", "");
            } else if (message.getSubject() != null && message.getSubject().equals("stamp") && messageBody.contains(ChatMessageFragment.STAMP_PREFIX)) {
                String remove = messageBody.substring(0, 10);
                Log.d("REMOVE", remove);
                messageBody = messageBody.replace(remove, "");
                messageBody = messageBody.replace(":stamp%", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        com.wrappy.android.db.entity.Message message1 =
                new com.wrappy.android.db.entity.Message(
                        message.getStanzaId(),
                        chatId,
                        MessageType,
                        messageBody,
                        message.getSubject(),
                        from.toString(),
                        date,
                        status);

        mAppDatabase.messageDao().upsert(message1);

        if (message.getSubject() != null) {
            mAppDatabase.chatDao()
                    .changeChatLastMessage(
                            message.getSubject(),
                            date,
                            chatId);
            Log.d("HAS_SUBJECT", "YES");
        } else {
            mAppDatabase.chatDao()
                    .changeChatLastMessage(
                            messageBody,
                            date,
                            chatId);
            Log.d("NO_SUBJECT", "YES");
        }

        if (mamResultExtension != null) {
            messageId = mamResultExtension.getId();

            mAppDatabase.messageDao()
                    .setArchiveId(
                            messageId,
                            message.getStanzaId());

        } else {
            messageId = message.getStanzaId();
        }

    }

    public void ackStanza(String id) {
        try {
            mXMPPManager.getConnection().addStanzaIdAcknowledgedListener(id, new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) {
                    //StanzaIdElement stanzaIdElement = packet.getExtension("stanza-id","urn:xmpp:sid:0");
                    Message message = (Message) packet;
                    Log.d("FROM", message.getTo().toString());
                    Log.d("MESSAGE", message.getBody());
                    Log.d("TO", message.getTo().toString());
                    //addMessage(message.getFrom().asEntityBareJidIfPossible(), message, packet.getTo().asBareJid().toString(), new Date(), 2);
                }
            });
        } catch (StreamManagementException.StreamManagementNotEnabledException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Resource<List<MessageView>>> getInitialMessages(String chatJid) {
        MutableLiveData<Resource<List<MessageView>>> initialMessages = new MutableLiveData<>();
        initialMessages.postValue(Resource.loading(null));
        mAppExecutors.diskIO().execute(() -> {
            initialMessages.postValue(Resource.success(mAppDatabase.messageDao().getInitialMessages(chatJid)));
        });
        return initialMessages;
    }

    public LiveData<MessageView> getMessages(EntityBareJid chatJid) {
        return mAppDatabase.messageDao().getMessages(chatJid.asBareJid().toString());
    }

    public List<Contact> getParticipantsViewObject(String chatJid, String type) {
        List<AuthorViewObject> participants = new ArrayList<>();
        List<Contact> participantsContact = new ArrayList<>();

        if (mChat != null && type.equals("chat")) {
            AuthorViewObject currentUser = new AuthorViewObject();
            currentUser.setId(mXMPPManager.getConnection().getUser().asBareJid().toString());
            currentUser.setName(mXMPPManager.getConnection().getUser().getLocalpart().toString());
            participants.add(currentUser);
            AuthorViewObject otherUser = new AuthorViewObject();
            otherUser.setId(mChat.getXmppAddressOfChatPartner()
                    .asBareJid()
                    .toString());
            otherUser.setName(mAppDatabase.contactDao()
                    .getContact(mChat.getXmppAddressOfChatPartner()
                            .asBareJid()
                            .toString())
                    .getContactName());
            participants.add(otherUser);
        } else {
            MultiUserChat muc = mXMPPManager.getMUCManager().getMultiUserChat(toEntityBareJidMUC(chatJid));
            try {
                for (Affiliate affiliate : muc.getOwners()) {
                    Contact owner = new Contact(affiliate.getJid().asBareJid().toString(), "", ContactManager.getUserName(affiliate.getJid().asBareJid().toString()), -1);
                    Log.d("OWNERS", affiliate.getJid().toString());
                    participantsContact.add(owner);
                }
                for (Affiliate occupant : muc.getMembers()) {
                    Contact member = new Contact(occupant.getJid().asBareJid().toString(), "", ContactManager.getUserName(occupant.getJid().asBareJid().toString()), -1);
                    Log.d("MEMBER1", occupant.getJid().toString());
                    participantsContact.add(member);
                }
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(participantsContact,
                Collections.reverseOrder(
                        (c1, c2) -> c1.getContactId().compareToIgnoreCase(c2.getContactId())
                ));
        return participantsContact;
    }

    public Message addMUCSubMessage(ItemsExtension items) {
        Message message = ((PayloadItem<MessageExtensionElement>) items.getItems().get(0))
                .getPayload()
                .getMessage();
        System.out.println(message.getBody());
        return message;
    }


    public void logoutChat() {
        mAppDatabase.messageDao().deleteAll();
        mAppDatabase.chatDao().deleteAll();
    }

    public LiveData<Resource<List<MessageView>>> searchMessage(EntityBareJid chatJid, String keyword, String type, String name) {
        //MamManager.MamPrefsResult prefsResult = mMamManager.retrieveArchivingPreferences();
        //MamManager.MamQueryResult mamQueryResult = mMamManager.pageAfter(chatJid, lastMessageId, 1);
        MutableLiveData<Resource<List<MessageView>>> messageSearchList = new MutableLiveData<>();
        Log.d("SEARCH", "Start");
        messageSearchList.postValue(Resource.loading(null));
        mAppExecutors.diskIO().execute(() -> {
            messageSearchList.postValue(Resource.success(mAppDatabase.messageDao().searchMessage(chatJid.toString(), "%" + keyword + "%")));
        });


        return messageSearchList;

    }

    public LiveData<List<com.wrappy.android.db.entity.Chat>> getQueryChats(String query, String whoIs) {
        return mAppDatabase.chatDao().getQueryChats(query, whoIs);

    }

    public void setChatState(String chatId, String type, boolean isTyping) {
        if (isTyping) {

            Message message = new Message();
            message.addExtension(new ChatStateExtension(ChatState.composing));
            try {
                if (type.equals("groupchat")) {
                    message.setType(Message.Type.groupchat);
                    mMUC.sendMessage(message);
                } else if (type.equals("chat")) {
                    message.setType(Message.Type.chat);
                    mChat.send(message);
                }

                Log.d("Message111", message.toString());
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            Message message = new Message();
            message.addExtension(new ChatStateExtension(ChatState.paused));
            try {
                if (type.equals("groupchat")) {
                    message.setType(Message.Type.groupchat);
                    mMUC.sendMessage(message);
                } else if (type.equals("chat")) {
                    message.setType(Message.Type.chat);
                    mChat.send(message);
                }

                Log.d("Message111", message.toString());
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public LiveData<com.wrappy.android.db.entity.Chat> getChatUpdate(String chatId) {
        return mAppDatabase.chatDao().getChatUpdate(chatId);
    }

    public void deleteMessage(String id, String message, String type, String chat_id) {
        Message message1 = new Message();
        message1.setStanzaId();
        message1.setBody(message);
        message1.setSubject("deleted");
        MessageCorrectExtension messageCorrectExtension = new MessageCorrectExtension(id);
        message1.addExtension(messageCorrectExtension);
        try {
            if (type.equals("groupchat")) {
                message1.setType(Message.Type.groupchat);
                mMUC.sendMessage(message1);
            } else if (type.equals("chat")) {
                message1.setType(Message.Type.chat);
                mChat.send(message1);
            }
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.messageDao().deleteMessage(id);
                com.wrappy.android.db.entity.Message message2 = mAppDatabase.messageDao().getLatestMessage(chat_id);
                if (type.equals("groupchat") && mAppDatabase.messageDao().getMessagesCount(chat_id) == 0) {
                    try {
                        mAppDatabase.chatDao().overrideChatLastMessage(" ", getServerTime(), chat_id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mAppDatabase.chatDao().overrideChatLastMessage(
                            message2.getMessageSubject() != null ? message2.getMessageSubject() : message2.getMessageText(),
                            message2.getCreatedAt(),
                            chat_id);
                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessages(String chatId, String type, String from) {
        mAppExecutors.diskIO().execute(() -> {
            if (type.equals("chat")) {
                com.wrappy.android.db.entity.Message LatestMessage = mAppDatabase.messageDao().getLatestMessage(chatId);
                if (LatestMessage == null) {
                    return;
                }

                Message message = new Message();
                message.setStanzaId();
                message.setSubject("deletedall");
                MessageCorrectExtension messageCorrectExtension = new MessageCorrectExtension(LatestMessage.getMessageId());
                message.setBody(LatestMessage.getMessageText());
                message.addExtension(messageCorrectExtension);
                try {
                    if (type.equals("groupchat")) {
                        mMUC = mXMPPManager.getMUCManager().getMultiUserChat(toEntityBareJidMUC(chatId));
                        message.setType(Message.Type.groupchat);
                        mMUC.sendMessage(message);
                        leaveMUC(chatId);
                        mAppDatabase.chatDao().deleteChat(chatId);
                    } else if (type.equals("chat")) {
                        mChat = mXMPPManager.getChatManager().chatWith(toEntityBareJidMUC(chatId));
                        message.setType(Message.Type.chat);
                        mChat.send(message);
                        try {
                            mAppDatabase.chatDao().changeChatLastMessage(null, getServerTime(), chatId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    mAppDatabase.messageDao().deleteMessages(chatId);

                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                leaveMUC(chatId);
                mAppDatabase.chatDao().deleteChat(chatId);
                mAppDatabase.messageDao().deleteMessages(chatId);
            }

            updateReadStatus(chatId, type, from);
        });
    }

    public void updateReadStatus(String chatId, String type, String from) {
        mAppExecutors.networkIO().execute(() -> {
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.chatDao().updateChatUnreadCount(chatId, 0);
            });
            IQ iq = new SimpleIQ(IQ.QUERY_ELEMENT, "urn:xmpp:badge") {
                @Override
                protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                    xml.rightAngleBracket();
                    xml.openElement("classFlag");
                    if (type.equals("groupchat")) {
                        xml.append("ofmucaffiliation");
                    } else {
                        xml.append("ofroster");
                    }
                    xml.closeElement("classFlag");
                    xml.openElement("to");
                    xml.append(chatId);
                    xml.closeElement("to");
                    return xml;
                }
            };
            try {
                iq.setFrom(JidCreate.bareFrom(from));
                //iq.setTo(toEntityBareJidMUC(chatId));
                iq.setType(Type.set);
                Log.d("READSTATUS", iq.toXML("").toString());
                mXMPPManager.getConnection().sendStanza(iq);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        });
    }

    public static long getDateTime(Date dateofMessage) {
        long diff = dateofMessage.getTime() - sCalendar.getTime().getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        return diff / daysInMilli;
    }

    public Date getServerTime() throws IOException {
        if (!TrueTime.isInitialized()) {
            TrueTime.build().initialize();
        }
        return TrueTime.now();
    }

    public void setGoogleTranslateKey(String apiKey) {
        mGoogleTranslateKey = apiKey;
    }

    public void translateText(String messageId, String text, final String targetLanguage) {
        mAppExecutors.networkIO().execute(() -> {
            Translate translationService = TranslateOptions.newBuilder()
                    .setApiKey(mGoogleTranslateKey)
                    .build()
                    .getService();
            Translation translation = translationService.translate(text, Translate.TranslateOption.targetLanguage(targetLanguage));
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.messageDao()
                        .addMessageTranslation(messageId, Html.fromHtml(translation.getTranslatedText()).toString());
            });
        });
    }

    public void removeTranslation(String messageId) {
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.messageDao()
                    .removeMessageTranslation(messageId);
        });
    }

    public LiveData<MessageView> getSingleMessage(String messageId) {
        return mAppDatabase.messageDao().getSingleMessage(messageId);
    }

    public boolean isSupportedLanguage(String chatLanguage) {
        switch (chatLanguage) {
            case "en":
            case "fr":
            case "es":
            case "pt":
            case "de":
            case "ja":
            case "zh":
            case "zh-TW":
            case "ko":
            case "tl":
            case "ceb":
            case "th":
            case "vi":
            case "my":
            case "km":
            case "lo":
            case "id":
            case "ms":
            case "bn":
            case "hi":
            case "ne":
            case "fa":
            case "ar":
            case "tr":
            case "ru":
            case "et":
                return true;
        }
        return false;
    }

    public LiveData<String> setChatLanguage(String chatId, String chatLanguage) {
        MutableLiveData<String> ld = new MutableLiveData();
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.chatBackgroundDao().setChatLanguage(chatId, chatLanguage);
            ld.postValue(chatLanguage);
        });
        return ld;
    }

    public LiveData<Boolean> setChatAutoTranslate(String chatId, boolean autoTranslate) {
        MutableLiveData<Boolean> ld = new MutableLiveData();
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.chatBackgroundDao().setAutoTranslate(chatId, autoTranslate);
            ld.postValue(autoTranslate);
        });
        return ld;
    }

    public LiveData<PagedList<MessageView>> getImageMessages(String chatId) {
        PagedList.Config pagingConfig = new PagedList.Config.Builder()
                .setPageSize(10)
                .setPrefetchDistance(10)
                .build();

        return new LivePagedListBuilder<>(mAppDatabase.messageDao().getImageMessages(chatId), pagingConfig)
                .setFetchExecutor(mAppExecutors.diskIO())
                .build();
    }

    public LiveData<Integer> getImagePosition(String chatId, Date messageCreatedAt) {
        MutableLiveData<Integer> ld = new MutableLiveData<>();
        mAppExecutors.diskIO().execute(() -> {
            ld.postValue(mAppDatabase.messageDao().getImageMessagePosition(chatId, messageCreatedAt));
        });
        return ld;
    }

    public LiveData<Boolean> loadMoreImages(String chatJid, String type, String lastMessageId) {
        MutableLiveData<Boolean> ld = new MutableLiveData<>();
        mAppExecutors.networkIO().execute(() -> {
            queryMessageArchive(ContactManager.toBareJid(chatJid).asEntityBareJidOrThrow(), lastMessageId, type, "image");
            ld.postValue(true);
        });
        return ld;
    }

    public void setAESKey(String aesKey) {
        mAESKey = aesKey;
    }

//    private String encryptMessage(String message) {
//        try {
//            message = mCryptLib.encryptPlainTextWithRandomIV(message, mAESKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return message;
//    }

//    public String decryptMessage(String message) {
//        try {
//            message = mCryptLib.decryptCipherTextWithRandomIV(message, mAESKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return message;
//    }
}
