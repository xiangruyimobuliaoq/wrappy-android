package com.wrappy.android.xmpp;

import android.util.Log;

import com.wrappy.android.BuildConfig;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.server.ServerConstants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.UnparseableStanza;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.parsing.ParsingExceptionCallback;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sm.packet.StreamManagement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.caps.EntityCapsManager;
import org.jivesoftware.smackx.chat_markers.ChatMarkersManager;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.time.EntityTimeManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.UnknownHostException;

public class XMPPManager {

    XMPPTCPConnection mConnection;
    XMPPTCPConnectionConfiguration.Builder mBuilder;
    UserSearchManager mUserSearchManager;
    StreamManagement mStreamManagement;
    Roster mRosterManager;
    ChatManager mChatManager;
    MultiUserChatManager mMUCManager;
    BlockingCommandManager mBCManager;
    VCardManager mVCardManager;
    ChatMarkersManager mChatMarkersManager;
    private ChatStateManager mChatStateManager;
    private PingManager mPingManager;

    private AppExecutors mAppExecutors;

    private ReconnectionManager mReconnectionManager;
    private PrivacyListManager mPrivacyListManager;
    private MamManager mMamManager;
    private MamManager mMUCMAM;
    private DeliveryReceiptRequest mDeliveryReceiptRequest;
    private EntityTimeManager mEntityTimeManager;

    public XMPPManager(/*AppExecutors appExecutors*/) {
        //mAppExecutors = appExecutors;
        try {
            configConnection();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void configConnection() throws XmppStringprepException, UnknownHostException {

            mBuilder = XMPPTCPConnectionConfiguration.builder();
            mBuilder.setConnectTimeout(10000)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setXmppDomain(JidCreate.domainBareFrom(ServerConstants.XMPP_SERVER))
                    //.setHostAddress(InetAddress.getByName(ServerConstants.XMPP_SERVER))
                    .setSendPresence(false);

            if (BuildConfig.DEBUG) {
                mBuilder.enableDefaultDebugger();
            }

            mConnection = new XMPPTCPConnection(mBuilder.build());
            mConnection.setUseStreamManagement(true);
            mConnection.setUseStreamManagementResumption(true);
            mConnection.setPreferredResumptionTime(15);
            mConnection.setReplyTimeout(20000);
            mConnection.setParsingExceptionCallback(new ParsingExceptionCallback() {
                @Override
                public void handleUnparsableStanza(UnparseableStanza stanzaData) throws Exception {
                    stanzaData.getParsingException().printStackTrace();
                }
            });
            initManagers();

    }

    private void initManagers() {

        EntityCapsManager.getInstanceFor(mConnection).disableEntityCaps();
        mUserSearchManager = new UserSearchManager(mConnection);
        mRosterManager = Roster.getInstanceFor(mConnection);
        mRosterManager.setRosterLoadedAtLogin(false);
        mChatManager = ChatManager.getInstanceFor(mConnection);
        mMUCManager = MultiUserChatManager.getInstanceFor(mConnection);
        mBCManager = BlockingCommandManager.getInstanceFor(mConnection);
        mVCardManager = VCardManager.getInstanceFor(mConnection);
        mPrivacyListManager = PrivacyListManager.getInstanceFor(mConnection);
        mMamManager = MamManager.getInstanceFor(mConnection);
        mReconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        mReconnectionManager.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
        mReconnectionManager.enableAutomaticReconnection();
        mReconnectionManager.setFixedDelay(10);
        mStreamManagement = new StreamManagement();
        mConnection.setUseStreamManagementResumption(false);
        mConnection.setUseStreamManagement(false);
        mChatMarkersManager = ChatMarkersManager.getInstanceFor(mConnection);
        mChatStateManager = ChatStateManager.getInstance(mConnection);
        mPingManager = PingManager.getInstanceFor(mConnection);
        mEntityTimeManager = EntityTimeManager.getInstanceFor(mConnection);
        DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(mConnection);
        //deliveryReceiptManager.autoAddDeliveryReceiptRequests();


    }

    public XMPPTCPConnection getConnection() {

        return mConnection;

    }

    public UserSearchManager getUserSearchManager() {
        return mUserSearchManager;
    }

    public ChatManager getChatManager() {
        return mChatManager;

    }

    public Roster getRosterManager() {
        return mRosterManager;

    }

    public MultiUserChatManager getMUCManager() {
        return mMUCManager;

    }

    public BlockingCommandManager getBCManager() {
        return mBCManager;
    }

    public void setVCard() {
        VCard vCard = new VCard();
        vCard.setJabberId(mConnection.getUser().asBareJid());
        vCard.setField("USERNAME", ContactManager.getUserName(mConnection.getUser().asBareJid().toString()));
        vCard.setNickName(ContactManager.getUserName(mConnection.getUser().asBareJid().toString()));
        try {
            mVCardManager.saveVCard(vCard);

            Log.d("JABBERID", mVCardManager.loadVCard().getJabberId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
    }

    public MamManager getMamManager() {
        return mMamManager;
    }

    public PrivacyListManager getPrivacyListManager() {
        return mPrivacyListManager;
    }

    public ReconnectionManager getReconnectionManager() {
        return mReconnectionManager;
    }

    public MamManager initMUCMam(MultiUserChat multiUserChat) {
        mMUCMAM = MamManager.getInstanceFor(multiUserChat);
        return mMUCMAM;
    }

    public ChatStateManager getChatStateManager() {
        return mChatStateManager;
    }

    public PingManager getPingManager() {
        return mPingManager;
    }

    public EntityTimeManager getEntityTimeManager() {
        return mEntityTimeManager;
    }
}
