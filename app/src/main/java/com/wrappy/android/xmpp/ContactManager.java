package com.wrappy.android.xmpp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.wrappy.android.chat.ChatMessageFragment;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.utils.CryptLib;
import com.wrappy.android.common.utils.ErrorUtils;
import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatBackground;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.db.entity.Delete;
import com.wrappy.android.db.entity.DeleteAll;
import com.wrappy.android.otr.OtrManager;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.ServerConstants;
import com.wrappy.android.xmpp.badge.IQBadge;
import com.wrappy.android.xmpp.badge.IQBadgeProvider;
import com.wrappy.android.xmpp.pushnotif.IQGroupPushFlagProvider;
import com.wrappy.android.xmpp.pushnotif.IQGroupPushFlags;
import com.wrappy.android.xmpp.pushnotif.IQJidListPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQJidPushFlagProvider;
import com.wrappy.android.xmpp.pushnotif.IQJidPushFlags;
import com.wrappy.android.xmpp.pushnotif.IQJidResultPushFlag;
import com.wrappy.android.xmpp.pushnotif.IQJidSetPushFlag;
import com.wrappy.android.xmpp.pushnotif.JidPushFlag;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQResultReplyFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;
import org.jivesoftware.smackx.message_correct.element.MessageCorrectExtension;
import org.jivesoftware.smackx.privacy.PrivacyList;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.sid.element.StanzaIdElement;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ContactManager {

    private final OtrManager mOtrManager;
    private CryptLib mCryptLib;
    private AppDatabase mAppDatabase;
    private AppExecutors mAppExecutors;
    private XMPPManager mXMPPManager;
    private AuthRepository mAuthRepository;
    private Roster mRoster;
    private UserSearchManager mUserSearchManager;
    private BlockingCommandManager mBCManager;
    private MamManager mMamManager;
    private PrivacyListManager mPrivacyListManager;
    private String USER_SEARCH_SERVICE = "search.openfire-test.newsupplytech.com";

    private boolean LOGIN_STATE;

    public static final String BLOCK = "Block";

    MutableLiveData<Resource<Boolean>> mLoadStatus = new MutableLiveData<>();

    private String mAESKey;

    public ContactManager(AppDatabase appDatabase,
                          XMPPManager xmppManager,
                          AppExecutors appExecutors,
                          OtrManager otrManager,
                          AuthRepository authRepository,
                          CryptLib cryptLib) {

        this.mAppDatabase = appDatabase;
        this.mAppExecutors = appExecutors;
        this.mXMPPManager = xmppManager;
        this.mAuthRepository = authRepository;
        this.mCryptLib = cryptLib;
        mOtrManager = otrManager;
        mUserSearchManager = mXMPPManager.getUserSearchManager();
        mRoster = mXMPPManager.getRosterManager();
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        mPrivacyListManager = mXMPPManager.getPrivacyListManager();
        mBCManager = mXMPPManager.getBCManager();
        mMamManager = mXMPPManager.getMamManager();

        ProviderManager.addIQProvider(IQJidPushFlags.ELEMENT, IQJidPushFlags.NAMESPACE, new IQJidPushFlagProvider());
        ProviderManager.addIQProvider(IQGroupPushFlags.ELEMENT, IQGroupPushFlags.NAMESPACE, new IQGroupPushFlagProvider());
        ProviderManager.addIQProvider(IQBadge.ELEMENT, IQBadge.NAMESPACE, new IQBadgeProvider());

        initListeners();

    }

    private void initListeners() {

        mRoster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    Log.d("ADDED", jid.toString());
                    mAppExecutors.diskIO().execute(() -> {
                        addContact(mRoster.getEntry(jid.asBareJid()), mRoster.getPresence(jid.asBareJid()));
                    });
                }
            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    Log.d("UPDATED", jid.toString());
                    RosterEntry entry = mRoster.getEntry(jid.asBareJid());
                    Log.d("ENTRY_UPDATED", String.valueOf(entry.isApproved()));
                    Log.d("ENTRY_UPDATED", String.valueOf(entry.getType()));
                    if (mRoster.getEntry(jid.asBareJid()).getType().equals(RosterPacket.ItemType.both)) {
                        mAppExecutors.diskIO().execute(() -> {
                            addContact(mRoster.getEntry(jid.asBareJid()), mRoster.getPresence(jid.asBareJid()));
                        });
                    }
                }
            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    Log.d("DELETED", jid.toString());
                    removeContact(jid.asBareJid().toString());
                }
            }

            @Override
            public void presenceChanged(Presence presence) {
                Log.d("PRESENCE", presence.getFrom().toString());
                if (!presence.getFrom().asBareJid().toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                    BareJid jid = presence.getFrom().asBareJid();
                    setContactStatus(jid, presence);
                }
            }
        });

        mRoster.addSubscribeListener(new SubscribeListener() {
            @Override
            public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
                addRequest(from);
                return null;
            }
        });

        mRoster.addPresenceEventListener(new PresenceEventListener() {
            @Override
            public void presenceAvailable(FullJid address, Presence availablePresence) {
                if (address != null) {
                    if (!address.asBareJid().equals(mXMPPManager.mConnection.getUser().asBareJid())) {
                        if (!address.toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                            Log.d(address.asBareJid().toString(), availablePresence.getType().toString());
                            setContactStatus(address.asBareJid(), availablePresence);
                        }
                    }
                }
            }

            @Override
            public void presenceUnavailable(FullJid address, Presence presence) {
                if (address != null) {
                    if (!address.asBareJid().equals(mXMPPManager.mConnection.getUser().asBareJid())) {
                        if (!address.toString().contains(ServerConstants.XMPP_MUC_DOMAIN)) {
                            Log.d(address.asBareJid().toString(), "unavailable");
                            setContactStatus(address.asBareJid(), presence);
                            mOtrManager.endSession(address.asBareJid().toString());
                        }
                    } else {
                        /**try {
                         mXMPPManager.getConnection().disconnect();
                         mXMPPManager.getConnection().connect();
                         mXMPPManager.getConnection().login(mAuthRepository.getLocalUsername(), mAuthRepository.getLocalPassword(), Resourcepart.from(mAuthRepository.getLocalUsername()));
                         } catch (IOException e) {
                         e.printStackTrace();
                         } catch (InterruptedException e) {
                         e.printStackTrace();
                         } catch (SmackException e) {
                         e.printStackTrace();
                         } catch (XMPPException e) {
                         e.printStackTrace();
                         }**/
                    }
                }
            }

            @Override
            public void presenceError(Jid address, Presence errorPresence) {

            }

            @Override
            public void presenceSubscribed(BareJid address, Presence subscribedPresence) {
                //acceptedRequest(address);
                if (subscribedPresence.hasExtension("urn:xmpp:delay", "delay")) {
                    DelayInformation delayInformation = (DelayInformation) subscribedPresence.getExtension("urn:xmpp:delay");
                    Log.d("Subscribed", delayInformation.getStamp().toString());
                }
            }

            @Override
            public void presenceUnsubscribed(BareJid address, Presence unsubscribedPresence) {
                removeContact(address.toString());
            }
        });

        mRoster.addRosterLoadedListener(new RosterLoadedListener() {
            @Override
            public void onRosterLoaded(Roster roster) {
                Log.d("Roster", "Roster Loaded Successfully");

                mAppExecutors.networkIO().execute(() -> {
                    setContacts(roster.getEntries());
                });
            }

            @Override
            public void onRosterLoadingFailed(Exception exception) {
                mLoadStatus.postValue(Resource.serverError(exception.toString(), false));
            }
        });
    }

    public LiveData<Resource<Boolean>> loadContacts() {

        try {
            mRoster.reloadAndWait();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mLoadStatus;
    }

    public void setContacts(Collection<RosterEntry> rosterEntries) {
        List<Jid> blockList = getBlockListasJid();
        mLoadStatus.postValue(Resource.loading(false));
        List<JidPushFlag> jidPushFlags = new ArrayList<>();


        for (RosterEntry rosterEntry : rosterEntries) {
            Log.d("Entry" + rosterEntry.getJid().toString(), rosterEntry.getType().toString());
            String name = rosterEntry.getJid().getLocalpartOrNull().toString();
            /**if (rosterEntry.getType() == RosterPacket.ItemType.to) {
             acceptedRequest(rosterEntry.getJid());
             }**/
            if (!name.equals(mXMPPManager.getConnection().getUser().asEntityBareJid().getLocalpart().toString())) {

                try {

                    String presence = "";

                    int Type = -1;

                    if (rosterEntry.getName() != null) {
                        name = rosterEntry.getName();
                    }

                    if (rosterEntry.getType().equals(RosterPacket.ItemType.to) ||
                            rosterEntry.getType().equals(RosterPacket.ItemType.none)) {
                        if (rosterEntry.getType().equals(RosterPacket.ItemType.none)) {
                            sendRequest(rosterEntry.getJid().toString());
                            Log.d("TYPE_NONE", rosterEntry.getJid().toString());
                        }
                        Type = 2;
                    } else if (rosterEntry.getType().equals(RosterPacket.ItemType.from)) {
                        Type = 1;
                    } else {
                        Type = 0;
                        presence = mRoster.getPresence(rosterEntry.getJid()).getType().toString();
                    }

                    Contact contact = new Contact(rosterEntry.getJid().toString(),
                            presence, name, Type);
                    if (blockList != null) {
                        if (blockList.contains(rosterEntry.getJid())) {
                            contact.setContactIsBlocked(true);
                            Block block = new Block();
                            block.setBlockId(rosterEntry.getJid().toString());
                            block.setBlockName(name);
                            mAppExecutors.diskIO().execute(() -> {
                                mAppDatabase.blockDao().insert(block);
                            });
                        } else {
                            contact.setContactIsBlocked(false);
                        }
                    } else {
                        contact.setContactIsBlocked(false);
                    }
                    String contactName = name;
                    mAppExecutors.networkIO().execute(() -> {
                        setInitialMessage(rosterEntry.getJid().asEntityBareJidOrThrow(), contactName);
                    });
                    mAppExecutors.diskIO().execute(() -> {
                        mAppDatabase.contactDao().insert(contact);
                    });
                } catch (ConcurrentModificationException e) {
                    try {
                        mRoster.reload();
                    } catch (SmackException.NotLoggedInException e1) {
                        e1.printStackTrace();
                    } catch (SmackException.NotConnectedException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
        // ADD BADGE


        // ADD Push Notif
        IQJidListPushFlag iqJidPushFlags = new IQJidListPushFlag();
        try {
            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqJidPushFlags, new IQResultReplyFilter(iqJidPushFlags, mXMPPManager.getConnection()), new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    if (packet instanceof IQJidListPushFlag) {
                        mAppExecutors.diskIO().execute(() -> {
                            IQJidListPushFlag iqJidPushFlags1 = (IQJidListPushFlag) packet;
                            for (JidPushFlag jidPushFlag : iqJidPushFlags1.getJidPushFlags()) {
                                Log.d("PUSHFLAGS", jidPushFlag.getJid() + " : " + jidPushFlag.isPushFlag());
                                mAppDatabase.chatDao().toggleNotif(jidPushFlag.isPushFlag(), jidPushFlag.getJid().asBareJid().toString());
                            }
                        });
                    }
                }
            });
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLoadStatus.postValue(Resource.success(true));

    }

    public void setInitialMessage(EntityBareJid chatJid, String name) {
        Log.d("MAM_ONE_TO_ONE", "Start");
        Chat chat = new Chat();
        chat.setChatName(name);
        chat.setChatWhoIs(mXMPPManager.getConnection().getUser().asBareJid().toString());
        Log.d("WHO_IS", mXMPPManager.getConnection().getUser().asBareJid().toString());
        Log.d("CHAT", chatJid.toString());
        chat.setChatId(chatJid.asBareJid().toString());
        chat.setChatUnreadCount(0);
        chat.setChatNotification(true);
        chat.setChatType("chat");

        //if(mAppDatabase.chatDao().hasMessage(chatJid.asBareJid().toString())==null) {
        ChatBackground chatBackground = new ChatBackground(chatJid.asBareJid().toString(), mXMPPManager.getConnection().getUser().asBareJid().toString(), null);

        mAppDatabase.chatDao().insert(chat);
        mAppDatabase.chatBackgroundDao().insert(chatBackground);

        boolean isEmpty = true;
        int initMax = 1;
        do {
            try {
                Log.d(chatJid.toString(), String.valueOf(initMax));
                //MamManager.MamQueryResult mamQueryResult = mMamManager.mostRecentPage(chatJid, initMax);
                MamManager.MamQuery mamQuery = mMamManager.queryMostRecentPage(chatJid, initMax);
                if (mamQuery.getMessageCount() == 0) {
                    Log.d("EMPTY", chatJid.toString());
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
                                Log.d("Delete", stanza.toString());
                                MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                                // TODO: Save to DB
                                //if(null != replace.getIdInitialMessage()) {
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
                                //}
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

                    if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                        /**from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER)? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                         try {
                         chatJid = JidCreate.entityBareFrom(messageXMPP.getFrom().asBareJid().toString());
                         } catch (XmppStringprepException e) {
                         e.printStackTrace();
                         }**/
                        if (initMax > forwardedList.size()) {
                            if (forwardedList.indexOf(forwarded) == forwardedList.size() - 1) {
                                isEmpty = false;
                                break;
                            }
                        }
                        if (initMax == 1) {
                            initMax = 10;
                            break;
                        } else {
                            initMax = initMax + 5;
                        }
                        continue;
                    }

                    if (messageXMPP.hasExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE)) {
                        if (messageXMPP.getFrom().asBareJid().equals(mXMPPManager.mConnection.getUser().asBareJid())) {
                            MessageCorrectExtension replace = messageXMPP.getExtension(MessageCorrectExtension.ELEMENT, MessageCorrectExtension.NAMESPACE);
                            // TODO: Save to DB
                            //if(null != replace.getIdInitialMessage()) {
                            if (messageXMPP.getSubject().equals("deletedall")) {
                                mAppDatabase.deleteAllDao().insert(new DeleteAll(replace.getIdInitialMessage()));
                                isEmpty = false;
                                break;
                            } else if (messageXMPP.getSubject().equals("deleted")) {
                                mAppDatabase.deleteDao().insert(new Delete(replace.getIdInitialMessage()));
                                mAppDatabase.messageDao().deleteMessage(replace.getIdInitialMessage());
                                if (initMax > forwardedList.size()) {
                                    if (forwardedList.indexOf(forwarded) == forwardedList.size() - 1) {
                                        isEmpty = false;
                                        break;
                                    }
                                }
                                if (initMax == 1) {
                                    initMax = 10;
                                } else {
                                    initMax = initMax + 5;
                                }
                                continue;
                            }
                            // }
                        } else {
                            if (initMax > forwardedList.size()) {
                                if (forwardedList.indexOf(forwarded) == forwardedList.size() - 1) {
                                    isEmpty = false;
                                    break;
                                }
                            }
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
                                if (initMax > forwardedList.size()) {
                                    if (forwardedList.indexOf(forwarded) == forwardedList.size() - 1) {
                                        isEmpty = false;
                                        break;
                                    }
                                }
                                if (initMax == 1) {
                                    initMax = 10;
                                } else {
                                    initMax = initMax + 5;
                                }
                                continue;
                            }
                        } else {
                            if (messageXMPP.getType().equals(Message.Type.chat)) {
                                isEmpty = false;
                            }
                            String from;
                            if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                                /**from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER)? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                                 try {
                                 chatJid = JidCreate.entityBareFrom(messageXMPP.getFrom().asBareJid().toString());
                                 } catch (XmppStringprepException e) {
                                 e.printStackTrace();
                                 }
                                 if(initMax>forwardedList.size()) {
                                 if(forwardedList.indexOf(forwarded)==forwardedList.size()-1) {
                                 isEmpty = false;
                                 break;
                                 }
                                 }
                                 if (initMax == 1) {
                                 initMax = 10;
                                 break;
                                 } else {
                                 initMax = initMax + 5;
                                 }**/
                                continue;
                            } else {
                                from = messageXMPP.getFrom().asBareJid().toString();
                            }

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
                                            from,
                                            delayInformation.getStamp(),
                                            2);
                            if (mamResultExtension != null) {
                                if (mamResultExtension.getId() != null) {
                                    message.setArchiveId(mamResultExtension.getId());
                                }
                            }
                            mAppDatabase.messageDao().upsert(message);

                            if (messageXMPP.getSubject() != null) {
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    mAppDatabase.chatDao().changeChatLastMessage(
                                            messageXMPP.getSubject(),
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                }
                                Log.d("HAS_ITEM1", messageXMPP.getFrom().getResourceOrEmpty().toString());
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    isEmpty = false;
                                }
                            } else {
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    mAppDatabase.chatDao().changeChatLastMessage(messageBody,
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                }
                                Log.d("HAS_ITEM1", messageXMPP.getFrom().toString());
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    isEmpty = false;
                                }
                            }
                            Log.d("MAM", messageXMPP.toString());

                        }
                        Log.d("CHATJID_ONETOONE", chatJid.asBareJid().toString());
                        Log.d("COUNT", String.valueOf(initMax));
                    } else {
                        Log.d("NO_SUBJECT", messageXMPP.getFrom().getResourceOrEmpty().toString());
                        if (mAppDatabase.deleteAllDao().isExist(messageXMPP.getStanzaId()) != null) {
                            isEmpty = false;
                            break;
                        } else if (mAppDatabase.deleteDao().isExist(messageXMPP.getStanzaId()) != null) {
                            if (initMax == 1) {
                                initMax = 10;
                                break;
                            } else {
                                initMax = initMax + 5;
                            }
                            continue;
                        } else {
                            String from;
                            if (messageXMPP.getType().equals(Message.Type.groupchat)) {
                                /**from = messageXMPP.getFrom().getResourceOrEmpty().toString().contains(ServerConstants.XMPP_SERVER)? messageXMPP.getFrom().getResourceOrEmpty().toString() : messageXMPP.getFrom().getResourceOrEmpty().toString() + "@" + ServerConstants.XMPP_SERVER;
                                 try {
                                 chatJid = JidCreate.entityBareFrom(messageXMPP.getFrom().asBareJid().toString());
                                 } catch (XmppStringprepException e) {
                                 e.printStackTrace();
                                 }
                                 if(initMax>forwardedList.size()) {
                                 if(forwardedList.indexOf(forwarded)==forwardedList.size()-1) {
                                 isEmpty = false;
                                 break;
                                 }
                                 }
                                 if (initMax == 1) {
                                 initMax = 10;
                                 break;
                                 } else {
                                 initMax = initMax + 5;
                                 }**/
                                continue;
                            } else {
                                from = messageXMPP.getFrom().asBareJid().toString();
                            }

                            String messageBody = messageXMPP.getBody();
//                                String messageBody = decryptMessage(messageXMPP.getBody());
//                                Log.d("MESSAGE_BODY", messageBody);

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
                            Log.d("INSERT_MESSAGE", messageXMPP.getFrom().toString());
                            mAppDatabase.messageDao().upsert(message);

                            if (messageXMPP.getSubject() != null) {
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    mAppDatabase.chatDao().changeChatLastMessage(
                                            messageXMPP.getSubject(),
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                }
                                Log.d("HAS_ITEM", messageXMPP.getFrom().toString());
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    isEmpty = false;
                                }
                            } else {
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    mAppDatabase.chatDao().changeChatLastMessage(messageBody,
                                            delayInformation.getStamp(),
                                            chatJid.asBareJid().toString());
                                }
                                Log.d("HAS_ITEM", messageXMPP.getFrom().toString());
                                if (messageXMPP.getType().equals(Message.Type.chat)) {
                                    isEmpty = false;
                                }
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
            }

        } while (isEmpty);
        //}

    }

    public LiveData<Resource<Contact>> getContact(String userJid) {
        MutableLiveData<Resource<Contact>> contact = new MutableLiveData<>();

        mAppExecutors.diskIO().execute(() -> {
            contact.postValue(Resource.success(mAppDatabase.contactDao().getContact(userJid)));
        });

        return contact;
    }

    public LiveData<List<Contact>> getContacts() {
        return mAppDatabase.contactDao().getContacts();
    }

    public int getContactCount() {
        return mAppDatabase.contactDao().getContactCount();
    }

    public void addContact(RosterEntry rosterEntry, Presence presence) {
        List<Jid> blockList = getBlockListasJid();
        if (rosterEntry != null) {
            String name = rosterEntry.getJid().getLocalpartOrNull().toString();

            String presence1 = "";

            int Type = -1;

            if (rosterEntry.getName() != null) {
                name = rosterEntry.getName();
            }

            if (rosterEntry.getType().equals(RosterPacket.ItemType.to) ||
                    rosterEntry.getType().equals(RosterPacket.ItemType.none)) {
                Type = 2;
            } else if (rosterEntry.getType().equals(RosterPacket.ItemType.from)) {
                Type = 1;
            } else {
                Type = 0;
                presence1 = presence.getType().toString();
            }

            Contact contact = new Contact(rosterEntry.getJid().toString(),
                    presence1, name, Type);

            if (blockList != null) {
                if (blockList.contains(rosterEntry.getJid())) {
                    contact.setContactIsBlocked(true);
                } else {
                    contact.setContactIsBlocked(false);
                }
            } else {
                contact.setContactIsBlocked(false);
            }
            if (contact.getContactType() == 0) {
                String contactName = name;
                mAppExecutors.networkIO().execute(() -> {
                    setInitialMessage(rosterEntry.getJid().asEntityBareJidOrThrow(), contactName);
                });
            }

            mAppDatabase.contactDao().insert(contact);
        }
    }

    public LiveData<List<Contact>> getQueryContacts(String query) {
        return mAppDatabase.contactDao().getQueryContacts(query);

    }

    public void setContactStatus(BareJid jid, Presence presence) {
        mAppDatabase.contactDao().setContactStatus(jid.toString(), presence.getType().toString());
    }

    public String searchContact(String userJid) {
        String result = "";

        String self = getUserName(mXMPPManager.getConnection().getUser().asBareJid().toString());
        Log.d("USERJID1111", userJid);
        Log.d("SELF", self);
        if (userJid.equalsIgnoreCase(self)) {
            result = "self";
        } else {

            /**DomainBareJid searchService = JidCreate.domainBareFrom(USER_SEARCH_SERVICE);
             Form answerForm = mUserSearchManager.getSearchForm(searchService)
             .createAnswerForm();
             answerForm.getField("Username").addValue("1");
             answerForm.setAnswer("search", userJid);

             ReportedData resultData = mUserSearchManager.getSearchResults(answerForm, searchService);

             for (ReportedData.Row row : resultData.getRows()) {
             result = row.getValues("jid").get(0).toString();
             break;
             }**/
            result = userJid;
            Log.d("DOMAIN", mXMPPManager.getConnection().getXMPPServiceDomain().toString());
            if (!result.equals("")) {
                result = result + "@" + mXMPPManager.getConnection().getXMPPServiceDomain().toString();
                Contact contact = mAppDatabase.contactDao().getContact(result.toLowerCase());
                Log.d("CONTACT", result.toString());
                if (contact != null) {
                    Log.d("CONTACT0", String.valueOf(contact.getContactType()));
                    result = result + "," + String.valueOf(contact.getContactType());
                } else {
                    result = result + ",-1";
                }
            }
        }
        return result;
    }

    public void addRequest(Jid jidFrom) {
        BareJid jid = toBareJid(jidFrom.toString());
        try {
            if (mRoster.getEntry(jidFrom.asBareJid()) != null) {
                Presence subscribed = new Presence(Presence.Type.subscribed);
                subscribed.setTo(jidFrom.asBareJid());
                subscribed.setFrom(mXMPPManager.getConnection().getUser().asBareJid());
                mXMPPManager.getConnection().sendStanza(subscribed);
                Log.d("REQUEST_SUBSCRIBED", jidFrom.toString());
                //Contact contact = new Contact(jid.toString(), "", jid.getLocalpartOrNull().toString(), 2);
                //mAppDatabase.contactDao().insert(contact);
            } else {
                mRoster.createEntry(jidFrom.asBareJid(), getUserName(jidFrom.asBareJid().toString()), null);
                Contact contact = new Contact(jid.toString(), "", jid.getLocalpartOrNull().toString(), 1);
                mAppDatabase.contactDao().insert(contact);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String userJid) {
        BareJid jid = toBareJid(userJid);
        try {
            mRoster.createEntry(jid, null, null);
            //mRoster.reload();
            mAppExecutors.diskIO().execute(() -> {
                addContact(mRoster.getEntry(jid.asBareJid()), mRoster.getPresence(jid.asBareJid()));
            });
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
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

    public void acceptedRequest(BareJid userJid) {
        try {
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(userJid);
            subscribed.setFrom(mXMPPManager.getConnection().getUser().asBareJid());
            mXMPPManager.getConnection().sendStanza(subscribed);
            mRoster.createEntry(userJid, null, null);
            Contact contact = new Contact(userJid.toString(), mRoster.getPresence(userJid).toString(), userJid.getLocalpartOrNull().toString(), 0);
            mRoster.reloadAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void acceptRequest(String userJid) {
        BareJid jid = toBareJid(userJid);
        try {
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(jid);
            subscribed.setFrom(mXMPPManager.getConnection().getUser().asBareJid());
            mXMPPManager.getConnection().sendStanza(subscribed);
            mRoster.createEntry(toBareJid(userJid), getUserName(userJid), null);
            //mRoster.reloadAndWait();
            mAppExecutors.diskIO().execute(() -> {
                Contact contact = new Contact(userJid, "unavailable", getUserName(userJid), 0);
                contact.setContactIsBlocked(false);
                mAppDatabase.contactDao().insert(contact);
                Chat chat = new Chat();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void cancelContact(String userJid) {
        BareJid jid = toBareJid(userJid);
        Presence unsubscribe = new Presence(Presence.Type.unsubscribed);
        unsubscribe.setTo(jid);
        unsubscribe.setFrom(mXMPPManager.getConnection().getUser());
        RosterEntry rosterEntry = mRoster.getEntry(jid);
        try {
            mXMPPManager.getConnection().sendStanza(unsubscribe);
            mAppDatabase.contactDao().delete(userJid);
            if (mRoster.getEntry(jid) != null) {
                mRoster.removeEntry(mRoster.getEntry(jid));
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
    }

    public void removeContact(String userJid) {
        BareJid jid = toBareJid(userJid);
        Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
        unsubscribed.setTo(jid);
        unsubscribed.setFrom(mXMPPManager.getConnection().getUser());
        RosterEntry rosterEntry = mRoster.getEntry(jid);
        try {
            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.contactDao().delete(userJid);
            });
            if (rosterEntry != null) {
                mXMPPManager.getConnection().sendStanza(unsubscribed);
                if (mRoster.getEntry(toBareJid(userJid)) != null) {
                    mRoster.removeEntry(mRoster.getEntry(toBareJid(userJid)));
                }
                //mRoster.reloadAndWait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void blockContact(String userJid, String name) {
        BareJid jid = toBareJid(userJid);
        List<Jid> blockList = new ArrayList<>();
        blockList.add(jid);
        try {
            List<PrivacyItem> privacyItemList = new ArrayList<>();
            try {
                privacyItemList = mPrivacyListManager.getPrivacyList(BLOCK).getItems();
            } catch (Exception e) {

            }
            PrivacyItem privacyItem;
            int order = 0;
            if (privacyItemList.size() >= 1) {

                order = privacyItemList.size() - 1;
                PrivacyItem privacyItemLast = privacyItemList.get(order);
                privacyItem = new PrivacyItem(PrivacyItem.Type.jid, userJid, true, privacyItemLast.getOrder() + 1);

            } else {
                privacyItem = new PrivacyItem(PrivacyItem.Type.jid, userJid, true, order);
            }

            privacyItemList.add(privacyItem);
            mPrivacyListManager.createPrivacyList(BLOCK, privacyItemList);

            /**mBCManager.blockContacts(blockList);**/
            IQJidSetPushFlag iqJidSetPushFlag = new IQJidSetPushFlag(0, ContactManager.toBareJid(userJid));

            mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqJidSetPushFlag, new IQResultReplyFilter(iqJidSetPushFlag, mXMPPManager.getConnection()),
                    new StanzaListener() {
                        @Override
                        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                            if (packet instanceof IQJidResultPushFlag) {
                                IQJidResultPushFlag iqJidResultPushFlag = (IQJidResultPushFlag) packet;
                                if (iqJidResultPushFlag.getStatusResult()) {
                                    mAppDatabase.chatDao().toggleNotif(false, userJid);
                                } else {

                                }
                            }
                        }
                    });

            mAppExecutors.diskIO().execute(() -> {
                mAppDatabase.contactDao().blockContact(userJid);
                Block block = new Block();
                block.setBlockId(userJid);
                block.setBlockName(name);
                mAppDatabase.blockDao().insert(block);
            });


            //mPrivacyListManager.setDefaultListName(BLOCK);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            try {
                mRoster.reloadAndWait();
            } catch (SmackException.NotLoggedInException e1) {
                e1.printStackTrace();
            } catch (SmackException.NotConnectedException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<Block>> getBlockList() {
        return mAppDatabase.blockDao().getBlockList();
    }

    public LiveData<List<Block>> getBlockListQuery(String query) {
        return mAppDatabase.blockDao().searchBlock(query);
    }

    public List<Jid> getBlockListasJid() {
        List<Jid> blockList = new ArrayList<>();
        try {
            List<PrivacyItem> privacyItemList = new ArrayList<>();
            if (mPrivacyListManager.getPrivacyLists().isEmpty()) {
                return blockList;
            }

            //mPrivacyListManager.createPrivacyList(BLOCK,privacyItemList);

            PrivacyList privacyList = mPrivacyListManager.getPrivacyList(BLOCK);
            if (privacyList != null) {
                privacyItemList = privacyList.getItems();
                for (PrivacyItem privacyItem : privacyItemList) {
                    blockList.add(toBareJid(privacyItem.getValue()));
                }
            } else {
                //mPrivacyListManager.createPrivacyList(BLOCK, privacyItemList);
            }

            /**blockList = mBCManager.getBlockList();**/

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return blockList;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("NULLPOINTER", "GETBLOCKLISTASJID", e);
        }

        return blockList;

    }

    /**
     * public LiveData<List<Block>> {
     * <p>
     * }
     **/

    public void unblockContact(List<Block> userJid) {
        //mAppExecutors.diskIO().execute(() -> {
        try {
            PrivacyList privacyList = mPrivacyListManager.getPrivacyList(BLOCK);
            List<PrivacyItem> privacyItemList = privacyList.getItems();
            List<PrivacyItem> toRemoveList = new ArrayList<>();
            List<Block> unblockList = new ArrayList<>(userJid);
            for (Block unblock : userJid) {
                PrivacyItem privacyItem = new PrivacyItem(PrivacyItem.Type.jid, ContactManager.toBareJid(unblock.getBlockId()), true, 0);
                //privacyItemList.remove(privacyItem);
                for (PrivacyItem privacyItem1 : privacyItemList) {
                    if (privacyItem1.getValue().equals(unblock.getBlockId())) {
                        toRemoveList.add(privacyItem1);
                        //mAppDatabase.contactDao().unblockContact(unblock.asBareJid().toString());
                    }
                }
            }
            privacyItemList.removeAll(toRemoveList);
            mPrivacyListManager.updatePrivacyList(BLOCK, privacyItemList);
            /**mBCManager.unblockContacts(userJid);**/
            for (Block unblock : userJid) {
                IQJidSetPushFlag iqJidSetPushFlag = new IQJidSetPushFlag(1, ContactManager.toBareJid(unblock.getBlockId()));

                mXMPPManager.getConnection().sendStanzaWithResponseCallback(iqJidSetPushFlag, new IQResultReplyFilter(iqJidSetPushFlag, mXMPPManager.getConnection()),
                        new StanzaListener() {
                            @Override
                            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                                if (packet instanceof IQJidResultPushFlag) {
                                    IQJidResultPushFlag iqJidResultPushFlag = (IQJidResultPushFlag) packet;
                                    if (iqJidResultPushFlag.getStatusResult()) {
                                        mAppDatabase.chatDao().toggleNotif(true, unblock.getBlockId());
                                    } else {

                                    }
                                }
                            }
                        });
            }

            //mRoster.reloadAndWait();
            mAppExecutors.diskIO().execute(() -> {
                for (Block unblock : unblockList) {
                    mAppDatabase.contactDao().unblockContact(unblock.getBlockId());
                    mAppDatabase.blockDao().delete(unblock);
                    Log.d("UNBLOCKED", unblock.getBlockId());
                }
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
        //});
    }

    public LiveData<Resource<Boolean>> changeName(String userJid, String name) {
        BareJid jid = toBareJid(userJid);
        MutableLiveData<Resource<Boolean>> mld = new MutableLiveData<>();
        mld.postValue(Resource.loading(null));
        mAppExecutors.networkIO().execute(() -> {
            try {
                mRoster.getEntry(jid).setName(name);
                mAppExecutors.diskIO().execute(() -> {
                    mAppDatabase.contactDao().setContactName(userJid, name);
                    mAppDatabase.chatDao().changeChatName(name, userJid);
                    mld.postValue(Resource.success(true));
                });
            } catch (SmackException.NoResponseException e) {
                mld.postValue(Resource.serverError(e.getLocalizedMessage(), false));
            } catch (Exception e) {
                mld.postValue(Resource.clientError(ErrorUtils.ERROR_CLIENT + "(" + e.getMessage() + ")", false));
            }
        });
        return mld;
    }

    public LiveData<String> getContactStatus(String userJid) {
        return mAppDatabase.contactDao().getContactStatus(userJid);
    }

    public LiveData<List<Contact>> getAllContacts() {
        return mAppDatabase.contactDao().getAllContacts();
    }

    public void logoutContacts() {
        mAppDatabase.contactDao().deleteAll();
    }

    public static BareJid toBareJid(String Jid) {
        BareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(Jid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return jid;
    }

    public static String getUserName(String userID) {
        return toBareJid(userID).getLocalpartOrNull().toString();
    }

    public LiveData<Contact> getContactUpdate(String userId) {
        return mAppDatabase.contactDao().getContactUpdate(userId);
    }

    public void setAESKey(String aesKey) {
        mAESKey = aesKey;
    }

//    public String decryptMessage(String message) {
//        try {
//            message = mCryptLib.decryptCipherTextWithRandomIV(message, mAESKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return message;
//    }
}
