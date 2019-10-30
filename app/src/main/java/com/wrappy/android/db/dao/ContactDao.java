package com.wrappy.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.wrappy.android.db.entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Contact contact);

    @Query("DELETE from contact_table WHERE contact_id=:contact_id")
    void delete(String contact_id);

    @Query("SELECT * from contact_table WHERE contact_isBlocked=0 ORDER BY contact_Type DESC, contact_name ASC")
    LiveData<List<Contact>> getAllContacts();

    @Query("UPDATE contact_table SET contact_name=:userName WHERE contact_id=:userId")
    void setContactName(String userId, String userName);

    @Query("SELECT contact_presence FROM contact_table WHERE contact_id=:userId")
    LiveData<String> getContactStatus(String userId);

    @Query("SELECT * FROM contact_table WHERE contact_Type=0 ORDER BY contact_name ASC")
    LiveData<List<Contact>> getContacts();

    @Query("SELECT * from contact_table WHERE contact_isBlocked=0 AND (contact_name LIKE :query OR contact_id LIKE :query) AND contact_Type=0 ORDER BY contact_name ASC")
    LiveData<List<Contact>> getQueryContacts(String query);

    @Query("SELECT * from contact_table WHERE contact_id=:contact_id")
    Contact getContact(String contact_id);

    @Query("SELECT * FROM contact_table WHERE contact_id=:contact_id")
    LiveData<Contact> getContactUpdate(String contact_id);

    @Query("UPDATE contact_table SET contact_isBlocked=1 WHERE contact_id=:contact_id")
    void blockContact(String contact_id);

    @Query("UPDATE contact_table SET contact_isBlocked=0 WHERE contact_id=:contact_id")
    void unblockContact(String contact_id);

    @Query("UPDATE contact_table SET contact_presence=:contact_status WHERE contact_id=:contact_id")
    void setContactStatus(String contact_id, String contact_status);

    @Query("SELECT COUNT(*) from contact_table WHERE contact_Type=0")
    int getContactCount();

    @Query("DELETE from contact_table")
    void deleteAll();


}
