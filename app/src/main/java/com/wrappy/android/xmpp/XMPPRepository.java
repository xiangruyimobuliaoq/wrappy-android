package com.wrappy.android.xmpp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PagedList;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.instacart.library.truetime.TrueTime;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.db.entity.MessageView;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.xmpp.aws.AWSCertificate;
import com.wrappy.android.xmpp.aws.IQAWSCertificate;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyConnectedException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQResultReplyFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.SimpleIQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class XMPPRepository {

    private AppExecutors mAppExecutors;
    private XMPPManager mXMPPManager;
    private AbstractXMPPConnection mConnection;
    private Roster mRoster;

    private ContactManager mContactManager;
    private ChatManager mChatManager;

    private ReconnectionManager mReconnectionManager;

    private AuthRepository mAuthRepository;

    private MutableLiveData<Resource<ConnectionStatus>> mConnectionStatus = new MutableLiveData<>();

    private MutableLiveData<String> mUserJid = new MutableLiveData<>();

    private Runnable mRunnable;

    private String mFirebaseToken;

    private boolean mIsForeground;

    private CountDownLatch mKeyLatch;




    public enum ConnectionStatus {
        CONNECTING, CONNECTED, AUTHENTICATED, RECONNECTING, DISCONNECTED, NOCONNECTION
    }

    public XMPPRepository(AppExecutors appExecutors,
                          XMPPManager xmppManager,
                          ContactManager contactManager,
                          ChatManager chatManager,
                          AuthRepository authRepository) {

        mAppExecutors = appExecutors;
        mXMPPManager = xmppManager;

        mConnection = mXMPPManager.getConnection();
        mRoster = mXMPPManager.getRosterManager();
        mReconnectionManager = mXMPPManager.getReconnectionManager();

        mContactManager = contactManager;
        mChatManager = chatManager;

        mAuthRepository = authRepository;

        final String userJid = getLocalUserJid();
        if (!userJid.isEmpty()) {
            mUserJid.postValue(userJid);
        }

        mXMPPManager.getConnection().addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Log.d("CONNECTION","AUTHENTICATED");
                // delay threads / writing to DB until we get keys from server
                mKeyLatch = new CountDownLatch(1);
                mAppExecutors.diskIO().execute(() -> {
                    try {
                        mKeyLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                mConnectionStatus.postValue(Resource.success(ConnectionStatus.AUTHENTICATED));
                if (!mConnection.getUser().asBareJid().toString().equals(mUserJid.getValue())) {
                    mUserJid.postValue(mConnection.getUser().asBareJid().toString());
                }
                initFirebase();
                //getHostRoom();
                mXMPPManager.getPingManager().setPingInterval(60);
                mXMPPManager.getPingManager().pingServerIfNecessary();
                //mXMPPManager.setVCard();
                //showJoinedRooms();
                mAppExecutors.networkIO().execute(() -> {
                    initNTP();
                    setAWSCertificate();
                    try {
                        mKeyLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendAvailablePresence();
                    mContactManager.loadContacts();
                    getMUCSubscriptions();
                    getChatBadges();
                });
            }

            @Override
            public void connectionClosed() {
                // mUserJid.postValue("");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                //mConnectionStatus.postValue(Resource.clientError("You are disconnected from the server",ConnectionStatus.DISCONNECTED));
                if( e instanceof XMPPException.StreamErrorException) {
                    XMPPException.StreamErrorException asd = (XMPPException.StreamErrorException) e;
                    if(asd.getStreamError().getCondition().equals(StreamError.Condition.conflict)
                    ||asd.getStreamError().getCondition().equals(StreamError.Condition.not_authorized)) {
                        mUserJid.postValue("");
                        logoutXMPP();
                        mAuthRepository.logout(true);
                    }
                }
            }

        });

        mReconnectionManager.addReconnectionListener(new ReconnectionListener() {
            @Override
            public void reconnectingIn(int seconds) {
                Log.d("Reconnecting in", String.valueOf(seconds));
                if(mConnectionStatus.getValue().data!=ConnectionStatus.RECONNECTING) {
                    mConnectionStatus.postValue(Resource.clientError(null, ConnectionStatus.RECONNECTING));
                }
            }

            @Override
            public void reconnectionFailed(Exception e) {
                if(mConnectionStatus.getValue().data!=ConnectionStatus.RECONNECTING) {
                    mConnectionStatus.postValue(Resource.clientError(e.getMessage(), ConnectionStatus.RECONNECTING));
                }
                mReconnectionManager.enableAutomaticReconnection();
            }
        });

    }

    public LiveData<Resource<ConnectionStatus>> getConnectionStatus() {
        return mConnectionStatus;
    }

    public LiveData<Resource<ConnectionStatus>> loginXMPP(String username, String password) {
        mAppExecutors.networkIO().execute(() -> {
            try {
                if(mConnectionStatus.getValue()==null ||
                        mConnectionStatus.getValue().data!=ConnectionStatus.CONNECTING) {
                    mConnectionStatus.postValue(Resource.loading(ConnectionStatus.CONNECTING));
                }
                mConnection.connect();
                mConnection.login(username, password, Resourcepart.from(username));

            } catch (AlreadyConnectedException | AlreadyLoggedInException e) {
                mConnectionStatus.postValue(Resource.success(ConnectionStatus.AUTHENTICATED));
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                //loginXMPP(username,password);
                e.printStackTrace();
                mConnectionStatus.postValue(Resource.clientError("Connection timed out.", ConnectionStatus.DISCONNECTED));
//                mAuthRepository.logout();
            } catch (SASLErrorException e) {
                mConnectionStatus.postValue(Resource.serverError(e.getMessage(), ConnectionStatus.DISCONNECTED));
                mAuthRepository.logout();
                e.printStackTrace();
            } catch (SmackException.ConnectionException e) {
                if(mConnectionStatus.getValue()==null || mConnectionStatus.getValue().data != ConnectionStatus.NOCONNECTION) {
                    mConnectionStatus.postValue(Resource.clientError("Cannot connect to the server.", ConnectionStatus.NOCONNECTION));
                    Log.d("FROM111", "LOGIN_XMPP");
                    mUserJid.postValue(getLocalUserJid());
                }
            } catch (Exception e) {
                //mConnectionStatus.postValue(Resource.serverError(e.getMessage(), ConnectionStatus.DISCONNECTED));
                e.printStackTrace();
            }
        });
        return mConnectionStatus;
    }

    public boolean isForeground() {
        return mIsForeground;
    }

    public void setIsForeground(boolean isForeground) {
        this.mIsForeground = isForeground;
    }

    public void sendAvailablePresence() {
        mAppExecutors.networkIO().execute(() -> {
            try {
                mConnection.sendStanza(new Presence(Presence.Type.available));
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void initNTP() {
        try {
            TrueTime.build().initialize();
            Date date = TrueTime.now();
            Log.d("DATE_NOW_NTP", date.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String showUserName() {
//        if(mConnectionStatus.getValue().data==ConnectionStatus.NOCONNECTION) {
//            return mAuthRepository.getLocalUsername();
//        } else {
//            return mConnection.getUser().asBareJid().getLocalpartOrNull().toString();
//        }
        return mAuthRepository.getLocalUsername();
    }

    public LiveData<String> getUserJid() {
        return mUserJid;
    }

    public String showUserId() {
//        if(mConnectionStatus.getValue().data!=ConnectionStatus.AUTHENTICATED) {
//            Log.d("FROM111", "SHOW_USER_ID");
//            return getLocalUserJid();
//        } else {
//            return mConnection.getUser().asBareJid().toString();
//        }
        Log.d("FROM111", "SHOW_USER_ID");
        return getLocalUserJid();
    }

    public LiveData<Resource<Boolean>> logoutXMPP() {
        MutableLiveData<Resource<Boolean>> mldLogout = new MutableLiveData();
        mldLogout.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            invalidateToken();
            mContactManager.logoutContacts();
            mChatManager.logoutChat();
            mConnection.disconnect();
            mUserJid.postValue("");
            mldLogout.postValue(Resource.success(true));
            mConnectionStatus.postValue(Resource.success(ConnectionStatus.NOCONNECTION));
        });
        return mldLogout;
    }

    public void disconnectXMPP() {
        mAppExecutors.networkIO().execute(() -> {
            try {
                mConnection.disconnect(new Presence(Presence.Type.unavailable));
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        });
    }

    public void initMessages(Fragment fragment) {
        mContactManager.mLoadStatus.observe(fragment, result -> {
            switch (result.status) {
                case SUCCESS:

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

    public void getHostRoom() {
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
            mConnection.sendStanzaWithResponseCallback(iq, new StanzaFilter() {
                @Override
                public boolean accept(Stanza stanza) {
                    return false;
                }
            }, new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {

                }
            });
            Log.d("SEND", "MUCEXTEND");
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

    public boolean isConnectionAuth() {
        return mConnection.isAuthenticated();
    }

    public LiveData<Resource<ConnectionStatus>> isConnected() {
        return mConnectionStatus;
    }

    public LiveData<List<Contact>> getQueryContacts(String query) {
        return mContactManager.getQueryContacts(query);
    }

    public LiveData<List<Chat>> getQueryChats(String query, String whoIs) {
        return mChatManager.getQueryChats("%"+query+"%", whoIs);
    }

    public LiveData<Resource<String>> searchContact(String userJid) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            String lastResult = mContactManager.searchContact(userJid);
            if (!lastResult.equals("")) {
                result.postValue(Resource.success(lastResult));
            } else {
                result.postValue(Resource.success(lastResult));
            }
        });
        return result;
    }

    public LiveData<List<Contact>> getContacts() {
        return mContactManager.getContacts();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return mContactManager.getAllContacts();
    }

    public LiveData<Resource<Contact>> getContact(String userJid) {
        return mContactManager.getContact(userJid);
    }

    public int getContactCount() {
        return mContactManager.getContactCount();
    }

    public void sendRequest(String userJid) {
        mContactManager.sendRequest(userJid);
    }

    public void acceptContact(String userJid) {
        mContactManager.acceptRequest(userJid);
    }

    public LiveData<Resource<Boolean>> changeContactName(String userJid, String name) {
        return mContactManager.changeName(userJid, name);
    }

    public void removeContact(String userJid) {
        mAppExecutors.networkIO().execute(() -> {
            mChatManager.deleteMessages(userJid, "chat", showUserId());
            mContactManager.removeContact(userJid);
        });
    }

    public void cancelRequest(String userJid) {
        mAppExecutors.networkIO().execute(() -> {
            mContactManager.cancelContact(userJid);
        });
    }

    public void blockContact(String userJid, String name) {
        mContactManager.blockContact(userJid, name);
    }

    public LiveData<List<Block>> getBlockList() {
        return mContactManager.getBlockList();
    }

    public LiveData<List<Block>> getBlockListQuery(String query) {
        return mContactManager.getBlockListQuery(query);
    }

    public List<Jid> getBlockListasJid() {
        return mContactManager.getBlockListasJid();
    }

    public void unblockContact(List<Block> userJids) {
        mContactManager.unblockContact(userJids);
    }

    /**
     * CHAT
     **/

    public LiveData<Resource<ChatAndBackground>> getChat(String chatId) {
        return mChatManager.getChat(chatId);
    }

    public LiveData<List<Chat>> getChatList(String whoIs) {
        return mChatManager.getChatList(whoIs);
    }

    public LiveData<List<Chat>> getOnetoOneChatList(String whoIs) {
        return mChatManager.getOnetoOneChatList(whoIs);
    }

    public LiveData<List<Chat>> getQueryOnetoOneChatList(String query, String whoIs) {
        return mChatManager.getQueryOnetoOneChatList(query, whoIs);
    }

    public LiveData<List<Chat>> getGroupChatList(String whoIs) {
        return mChatManager.getGroupChatList(whoIs);
    }

    public LiveData<List<Chat>> getQueryGroupChatList(String query, String whoIs) {
        return mChatManager.getQueryGroupChatList(query, whoIs);
    }

    public LiveData<Resource<ChatAndBackground>> startChat(String userJid, String chatName, String type) {
        String whoIs;
        if(mConnectionStatus.getValue().data!=ConnectionStatus.AUTHENTICATED) {
            //whoIs = getLocalUserJid();
        } else {
            //whoIs = mXMPPManager.getConnection().getUser().asBareJid().toString();
        }
        return mChatManager.startChat(userJid, chatName, type, getLocalUserJid());
    }

    public LiveData<Resource<String>> createMUC(List<String> userJids) {
        return mChatManager.createMUC(userJids);
    }

    public boolean removeMember(String userJid) {
        return mChatManager.removeMember(userJid);
    }

    public void getMUCSubscriptions() {
        mChatManager.getMUCSubscriptions();
    }

    public void getChatBadges() {
        mChatManager.getChatBadges();
    }

    public LiveData<Resource<List<RoomMUCExtend>>> getMUCRooms() {
        return mChatManager.getMUCRooms();
    }

    public void setChatBackground(String path, String chatId) {
        mChatManager.setChatBackground(path, chatId);
    }

    public LiveData<Resource<Boolean>> setChatNotification(boolean notification, String chatId, String chatType) {
        return mChatManager.setChatNotification(notification, chatId, chatType);
    }

    public String getRoomName(){
        return mChatManager.getRoomName();
    }

    public LiveData<Resource<List<MessageView>>> getInitialMessages(String chatJid) {
        return mChatManager.getInitialMessages(chatJid);
    }

    public LiveData<String> getContactStatus(String userJid) {
        return mContactManager.getContactStatus(userJid);
    }

    public LiveData<MessageView> getMessages(EntityBareJid chatJid) {
        return mChatManager.getMessages(chatJid);
    }

    public LiveData<Resource<String>> sendMessage(String message, String subject, String type, String chatName) {
        return mChatManager.sendMessage(message, subject, type, chatName);
    }

    public LiveData<Resource<List<MessageView>>> pageChat(EntityBareJid chatJid, int offset, String lastMessageId, String type, String keyword) {
        return mChatManager.pageChat(chatJid, offset, lastMessageId, type, keyword);
    }

    public List<Contact> getParticipantsViewObject(String chatJid, String type) {
        if(mConnectionStatus.getValue().data==ConnectionStatus.NOCONNECTION) {
            return new ArrayList<Contact>();
        } else {
            return mChatManager.getParticipantsViewObject(chatJid, type);
        }
    }

    public void showJoinedRooms(){
        for(EntityBareJid room : mXMPPManager.getMUCManager().getJoinedRooms()) {
            Log.d("ROOMNAME", room.toString());
        }
    }

    public void addMember(String userJid) {
        mChatManager.addMember(userJid);
    }

    public void setRoomName(String roomName) {
        mChatManager.setRoomName(roomName);
    }

    public LiveData<Resource<List<MessageView>>> searchMessage(EntityBareJid chatJid, String keyword, String type, String name) {
        return mChatManager.searchMessage(chatJid, keyword, type, name);
    }

    public void setChatState(String chatId, String type, boolean isTyping) {
        mChatManager.setChatState(chatId, type, isTyping);
    }

    public LiveData<Resource<Boolean>> isTyping(String chatId) {
        return mChatManager.isTyping(chatId);
    }

    public LiveData<Resource<Boolean>> isTypingMUC() {
        return mChatManager.isTypingMUC();
    }

    public LiveData<Chat> getChatUpdate(String chatId) {
        return mChatManager.getChatUpdate(chatId);
    }

    public LiveData<Contact> getContactUpdate(String userId) {
        return mContactManager.getContactUpdate(userId);
    }

    public void deleteMessage(String id, String message, String type, String chat_id) {
        mChatManager.deleteMessage(id, message, type, chat_id);
    }

    public void deleteMessages(String chatId, String type) {
        mChatManager.deleteMessages(chatId, type, showUserId());
    }

    public void updateReadStatus(String chatId, String type) {
        mChatManager.updateReadStatus(chatId, type, showUserId());
    }

    public void sendFirebaseToken(String token) {
        if (token == null || token.equals(mFirebaseToken)) {
            return;
        }

        mAppExecutors.networkIO().execute(() -> {
            IQ iq = new IQ("query", "urn:xmpp:push") {
                @Override
                protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                    xml.rightAngleBracket();
                    xml.openElement("token");
                    xml.append(token);
                    xml.closeElement("token");
                    xml.openElement("tokenType");
                    xml.append("Android");
                    xml.closeElement("tokenType");
                    return xml;
                }
            };
            iq.setType(IQ.Type.set);
            try {
                mXMPPManager.getConnection().sendStanza(iq);
                mFirebaseToken = token;
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void setAWSCertificate() {
        IQAWSCertificate iqawsCertificate = new IQAWSCertificate();
        try {
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqawsCertificate, new IQResultReplyFilter(iqawsCertificate, mXMPPManager.getConnection()), new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    IQAWSCertificate iqawsCertificateResult = (IQAWSCertificate) packet;
                    AWSCertificate awsCertificate = iqawsCertificateResult.getAWSCertificate();
                    Log.d("ACCESSKEY", awsCertificate.getAccessKey());
                    Log.d("SECRET_ID", awsCertificate.getSecretID());
                    Log.d("BUCKETNAME", awsCertificate.getBucketName());
                    mAuthRepository.storeAWSCertificate(awsCertificate);

                    mChatManager.setGoogleTranslateKey(iqawsCertificateResult.getGoogleTranslateKey());
                    mChatManager.setAESKey(iqawsCertificateResult.getAESKey());
                    mContactManager.setAESKey(iqawsCertificateResult.getAESKey());
                    mKeyLatch.countDown();
                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public AWSCertificate getAWSCertificate() {
        return mAuthRepository.getAWSCertificate();
    }

    public void initFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(result -> sendFirebaseToken(result.getToken()));
    }

    public String getLocalUserJid() {
        String localUserJid = mAuthRepository.getLocalUsername() + "@" + ServerConstants.XMPP_SERVER;
        Log.d("LOCAL_USER", localUserJid);
        return localUserJid;

    }

    private void invalidateToken() {
        if (!mXMPPManager.getConnection().isAuthenticated()) {
            return;
        }

        IQ iq = new IQ("query", "urn:xmpp:delete") {
            @Override
            protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                xml.rightAngleBracket();
                xml.openElement("token");
                xml.append(mFirebaseToken);
                xml.closeElement("token");
                return xml;
            }
        };
        iq.setType(IQ.Type.set);
        try {
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iq, new IQResultReplyFilter(iq, mXMPPManager.getConnection()), new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    mFirebaseToken = null;
                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeTranslation(String messageId) {
        mChatManager.removeTranslation(messageId);
    }

    public void translateText(String messageId, String message, String languageCode) {
        mChatManager.translateText(messageId, message, languageCode);
    }

    public LiveData<MessageView> getSingleMessage(String messageId) {
        return mChatManager.getSingleMessage(messageId);
    }

    public boolean isSupportedLanguage(String chatLanguage) {
        return mChatManager.isSupportedLanguage(chatLanguage);
    }

    public LiveData<String> setChatLanguage(String chatId, String chatLanguage) {
        return mChatManager.setChatLanguage(chatId, chatLanguage);
    }

    public LiveData<Boolean> setChatAutoTranslate(String chatId, boolean autoTranslate) {
        return mChatManager.setChatAutoTranslate(chatId, autoTranslate);
    }

    public LiveData<PagedList<MessageView>> getImageMessages(String chatId) {
        return mChatManager.getImageMessages(chatId);
    }

    public LiveData<Integer> getImagePosition(String chatId, Date messageCreatedAt) {
        return mChatManager.getImagePosition(chatId, messageCreatedAt);
    }

    public LiveData<Boolean> loadMoreImages(String chatJid, String type, String lastMessageId) {
        return mChatManager.loadMoreImages(chatJid, type, lastMessageId);
    }
    public LiveData<Boolean> isOtrEncyption() {
        return mChatManager.isOtrEncyption() ;
    }

    public void startOtr() {
            mChatManager.startOtr();
    }

    public void endOtr() {
        mChatManager.endOtr();
    }
}
