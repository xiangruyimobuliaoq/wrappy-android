package com.wrappy.android.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.wrappy.android.R;
import com.wrappy.android.chat.gallery.ChatImageGalleryFragment;
import com.wrappy.android.common.AbstractNavigationManager;
import com.wrappy.android.common.chat.MessageViewObject;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.xmpp.aws.AWSCertificate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatNavigationManager extends AbstractNavigationManager {
    public ChatNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showChatMessagePage(String chatJid, String chatName, String chatType) {
        showPage(ChatMessageFragment.create(chatJid, chatName, chatType), false);
    }

    public void showChatSettingsPage(String chatJid) {
        showPage(ChatSettingsFragment.create(chatJid), true);
    }

    public void showChatGroupDetailsPage(String chatJid) {
        showPage(ChatGroupDetailsFragment.create(chatJid), true);
    }

    public void showChatAddMembersPage(ArrayList<Contact> memberList) {
        showPage(ChatAddMemberFragment.create(memberList), true);
    }

    public void showChatTranslatePage(String chatJid, boolean showAutoTranslate) {
        showPage(ChatTranslateFragment.create(chatJid, showAutoTranslate), true);
    }

    public void showImageGallery(String chatJid,
                                 String chatType,
                                 Date messageCreatedAt,
                                 AWSCertificate awsCertificate) {
        showPopupPage(ChatImageGalleryFragment.create(chatJid,
                                                    chatType,
                                                    messageCreatedAt,
                                                    awsCertificate),
                true);
    }

    private void showPopupPage(Fragment fragment, boolean withBack) {
        if (withBack) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.frag_popup_container, fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.frag_popup_container, fragment)
                    .commitAllowingStateLoss();
        }
    }
}
