package com.wrappy.android.otr;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.wrappy.android.common.Resource;
import com.wrappy.android.entity.NestedMap;
import com.wrappy.android.xmpp.XMPPManager;

import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.OtrException;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.OtrPolicyImpl;
import net.java.otr4j.crypto.OtrCryptoEngineImpl;
import net.java.otr4j.session.FragmenterInstructions;
import net.java.otr4j.session.InstanceTag;
import net.java.otr4j.session.Session;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionImpl;
import net.java.otr4j.session.SessionStatus;

import org.jivesoftware.smack.SmackException;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * 创建者     彭龙
 * 创建时间   2019-11-05 10:06
 * 描述
 * <p>
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
public class OtrManager implements OtrEngineHost, OtrEngineListener {
    XMPPManager mXMPPManager;

    private final NestedMap<Session> sessions;
    private final NestedMap<MutableLiveData<Boolean>> sessionStatus;

    public OtrManager(XMPPManager xmppManager) {
        mXMPPManager = xmppManager;
        sessions = new NestedMap<>();
        sessionStatus = new NestedMap<>();
    }

    private Session getSession(String account, String user) {
        Session session = sessions.get(account, user);
        if (null == session) {
            session = new SessionImpl(new SessionID(account, user, "Xmpp"), this);
            session.addOtrEngineListener(this);
            sessions.put(account, user, session);
        }
        return session;
    }

    public LiveData<Boolean> isOtrEncyption(String user) {
        MutableLiveData<Boolean> ld = sessionStatus.get(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user);
        if (ld == null) {
            ld = new MutableLiveData<>();
            sessionStatus.put(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user, ld);
        }
        return ld;
    }

    @Override
    public void injectMessage(SessionID sessionID, String msg) throws OtrException {
        Log.e("injectMessage", sessionID.getAccountID() + "     " + sessionID.getUserID());
        try {
            mXMPPManager.getChatManager().chatWith(JidCreate.entityBareFrom(sessionID.getUserID())).send(msg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    public void startSession(String user) {
        try {
            getSession(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user).startSession();
        } catch (OtrException e) {
            e.printStackTrace();
        }
    }

    public void endSession(String user) {
        try {
            getSession(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user).endSession();
        } catch (OtrException e) {
            e.printStackTrace();
        }
    }

    public void refreshSession(String user) {
        try {
            getSession(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user).refreshSession();
        } catch (OtrException e) {
            e.printStackTrace();
        }
    }

    public String transformSending(String user, String content) {
        Log.e("加密前", content);
        try {
            Session session = getSession(mXMPPManager.getConnection().getUser().asEntityBareJidString(), user);
            Log.e("session状态", String.valueOf(session.getSessionStatus()));
            if (session.getSessionStatus() != SessionStatus.ENCRYPTED) {
                return content;
            }
            String[] strings = session.transformSending(content);
            Log.e("加密后", strings[0]);
            return strings[0];
        } catch (OtrException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String transformReceiving(String from, String content) {
        Log.e("解密前", content);
        try {
            String s = getSession(mXMPPManager.getConnection().getUser().asEntityBareJidString(), from).transformReceiving(content);
            Log.e("解密后", s + "");
            return s;
        } catch (OtrException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void unreadableMessageReceived(SessionID sessionID) throws OtrException {
        Log.e("unreadableReceived", "unreadableMessageReceived");

    }

    @Override
    public void unencryptedMessageReceived(SessionID sessionID, String msg) throws OtrException {
        Log.e("unencryptedReceived", "unencryptedMessageReceived");

    }

    @Override
    public void showError(SessionID sessionID, String error) throws OtrException {
        Log.e("showError", "showError");

    }

    @Override
    public void smpError(SessionID sessionID, int tlvType, boolean cheated) throws OtrException {
        Log.e("smpError", "smpError");

    }

    @Override
    public void smpAborted(SessionID sessionID) throws OtrException {
        Log.e("smpAborted", "smpAborted");

    }

    @Override
    public void finishedSessionMessage(SessionID sessionID, String msgText) throws OtrException {
        Log.e("finishedSessionMessage", "finishedSessionMessage");

    }

    @Override
    public void requireEncryptedMessage(SessionID sessionID, String msgText) throws OtrException {
        Log.e("requireEncryptedMessage", "requireEncryptedMessage");

    }

    @Override
    public OtrPolicy getSessionPolicy(SessionID sessionID) {
        return new OtrPolicyImpl(OtrPolicy.OTRL_POLICY_ALWAYS);
    }

    @Override
    public FragmenterInstructions getFragmenterInstructions(SessionID sessionID) {
        return null;
    }

    @Override
    public KeyPair getLocalKeyPair(SessionID sessionID) throws OtrException {
        Log.e("getLocalKeyPair", "getLocalKeyPair");
        try {
            return KeyPairGenerator.getInstance("DSA").genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getLocalFingerprintRaw(SessionID sessionID) {
        try {
            return new OtrCryptoEngineImpl().getFingerprintRaw(getLocalKeyPair(sessionID).getPublic());
        } catch (OtrException e) {
        }
        return new byte[0];
    }

    @Override
    public void askForSecret(SessionID sessionID, InstanceTag receiverTag, String question) {
        Log.e("askForSecret", question);
    }

    @Override
    public void verify(SessionID sessionID, String fingerprint, boolean approved) {
        Log.e("verify", approved + "fingerprint");

    }

    @Override
    public void unverify(SessionID sessionID, String fingerprint) {
        Log.e("unverify", fingerprint);

    }

    @Override
    public String getReplyForUnreadableMessage(SessionID sessionID) {
        Log.e("ReplyForUnreadable", "getReplyForUnreadableMessage");
        return null;
    }

    @Override
    public String getFallbackMessage(SessionID sessionID) {
        Log.e("getFallbackMessage", "getFallbackMessage");

        return null;
    }

    @Override
    public void messageFromAnotherInstanceReceived(SessionID sessionID) {
        Log.e("FromAnotherInstance", "messageFromAnotherInstanceReceived");

    }

    @Override
    public void sessionStatusChanged(SessionID sessionID) {
        Log.e("sessionStatusChanged", "sessionStatusChanged");
        MutableLiveData<Boolean> mld = sessionStatus.get(sessionID.getAccountID(), sessionID.getUserID());
        if (null == mld) {
            mld = new MutableLiveData<>();
        }
        mld.postValue(sessions.get(sessionID.getAccountID(), sessionID.getUserID()).getSessionStatus() == SessionStatus.ENCRYPTED);
    }

    @Override
    public void multipleInstancesDetected(SessionID sessionID) {
        Log.e("multipleInstances", "multipleInstances");

    }

    @Override
    public void outgoingSessionChanged(SessionID sessionID) {
        Log.e("outgoingSessionChanged", "outgoingSessionChanged");

    }


}
