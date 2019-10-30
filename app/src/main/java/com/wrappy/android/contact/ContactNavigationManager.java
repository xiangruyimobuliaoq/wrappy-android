package com.wrappy.android.contact;

import android.support.v4.app.FragmentManager;

import com.wrappy.android.R;
import com.wrappy.android.common.AbstractNavigationManager;

public class ContactNavigationManager extends AbstractNavigationManager {

    public ContactNavigationManager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getContainerId() {
        return R.id.frag_base_container;
    }

    public void showContactAddFriend(String userJid) {
        showPage(ContactAddFragment.create(userJid), false);
    }

    public void showContactEditFriend(String userJid) {
        showPage(ContactEditFragment.create(userJid), false);
    }

}
