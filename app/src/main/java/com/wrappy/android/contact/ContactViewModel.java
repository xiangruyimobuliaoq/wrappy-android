package com.wrappy.android.contact;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Resource;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.server.account.body.response.VCardInfoResponse;
import com.wrappy.android.xmpp.XMPPRepository;

import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jxmpp.jid.Jid;

import java.util.List;

public class ContactViewModel extends ViewModel {

    XMPPRepository mXMPPRepository;
    AccountRepository mAccountRepository;

    public ContactViewModel(XMPPRepository xmppRepository, AccountRepository accountRepository) {
        mXMPPRepository = xmppRepository;
        mAccountRepository = accountRepository;

    }

    public LiveData<Resource<String>> searchUser(String userJid) {
        return mXMPPRepository.searchContact(userJid);
    }

    public LiveData<Resource<Contact>> getContact(String userJid) {
        return mXMPPRepository.getContact(userJid);
    }

    public void addContact(String userJid) {
        mXMPPRepository.sendRequest(userJid);
    }

    public LiveData<Resource<Boolean>> changeContactName(String userJid, String name) {
        return mXMPPRepository.changeContactName(userJid, name);
    }

    public void removeContact(String userJid) {
        mXMPPRepository.removeContact(userJid);
    }

    public void blockContact(String userJid, String name) {
        mXMPPRepository.blockContact(userJid, name);
    }

    public void unblockContact(List<Block> userJids) {
        mXMPPRepository.unblockContact(userJids);
    }

    public String getContactFileUrl(FileBody.Type fileTypeAvatar, String userName) {
        return mAccountRepository.getContactFileUrl(fileTypeAvatar, userName);
    }

    public LiveData<Resource<VCardInfoResponse>> getVCard(String userJid) {
        return mAccountRepository.getVCardInfo(userJid);
    }

}
