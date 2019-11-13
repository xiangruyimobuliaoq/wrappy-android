package com.wrappy.android.di;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import com.bumptech.glide.util.LruCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wrappy.android.BuildConfig;
import com.wrappy.android.WrappyApp;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.AppLifecycleListener;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.common.utils.Base64ImageFileAdapter;
import com.wrappy.android.common.utils.CryptLib;
import com.wrappy.android.db.AppDatabase;
import com.wrappy.android.otr.OtrManager;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.AuthorizationService;
import com.wrappy.android.server.account.AccountRepository;
import com.wrappy.android.server.account.AccountService;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.ContactManager;
import com.wrappy.android.xmpp.XMPPManager;
import com.wrappy.android.xmpp.XMPPRepository;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ViewModelProviderModule.class, RetrofitProviderModule.class})
public class AppProviderModule {
    private WrappyApp mAppInstance;

    public AppProviderModule(WrappyApp app) {
        mAppInstance = app;
    }

    @Provides
    @AppScope
    WrappyApp provideApp() {
        return mAppInstance;
    }

    @Provides
    @AppScope
    AppExecutors provideAppExecutors() {
        return new AppExecutors();
    }

    @Provides
    @AppScope
    LruCache<String, String> providePresignedCache() {
        return new LruCache<>(250);
    }

    @Provides
    @AppScope
    AppDatabase provideDatabase(WrappyApp app) {
        return Room.databaseBuilder(app, AppDatabase.class, "wrappy.db")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @AppScope
    SharedPreferences provideSharedPreferences() {
        // TODO if logout does not work on update:
        return mAppInstance.getSharedPreferences("wrappy_prefs", Context.MODE_PRIVATE);
        //return PreferenceManager.getDefaultSharedPreferences(mAppInstance);
    }

    @Provides
    @AppScope
    AuthRepository provideAuthRepository(AppDatabase database, AppExecutors appExecutors, AuthorizationService authService, SharedPreferences sharedPreferences) {
        return new AuthRepository(database, appExecutors, authService, sharedPreferences);
    }

    @Provides
    @AppScope
    AccountRepository provideAccountRepository(AccountService accountService, AuthRepository authRepository, AppDatabase appDatabase, AppExecutors appExecutors, SharedPreferences sharedPreferences) {
        return new AccountRepository(accountService, authRepository, appDatabase, appExecutors, sharedPreferences);
    }

    @Provides
    @AppScope
    Gson provideGson() {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Base64ImageFile.class, new Base64ImageFileAdapter(mAppInstance).nullSafe());
        if (BuildConfig.DEBUG) {
            gson.setPrettyPrinting();
        }
        return gson.create();
    }

    @Provides
    @AppScope
    XMPPManager provideXMPPManager() {
        return new XMPPManager();
    }

    @Provides
    @AppScope
    OtrManager provideOtrManager(XMPPManager xmppManager) {
        return new OtrManager(xmppManager);
    }

    @Provides
    @AppScope
    ContactManager provideContactManager(AppDatabase appDatabase, XMPPManager xmppManager, AppExecutors appExecutors, AuthRepository authRepository, CryptLib cryptLib) {
        return new ContactManager(appDatabase, xmppManager, appExecutors, authRepository, cryptLib);
    }

    @Provides
    @AppScope
    ChatManager provideChatManager(AppDatabase appDatabase, XMPPManager xmppManager, AppExecutors appExecutors, SharedPreferences sharedPreferences, OtrManager otrManager, CryptLib cryptLib) {
        return new ChatManager(appDatabase, appExecutors, xmppManager, sharedPreferences, otrManager, cryptLib);
    }

    @Provides
    @AppScope
    XMPPRepository provideXMPPRepository(AppExecutors appExecutors, XMPPManager xmppManager, ContactManager contactManager, ChatManager chatManager, AuthRepository authRepository) {
        return new XMPPRepository(appExecutors, xmppManager, contactManager, chatManager, authRepository);
    }

    @Provides
    @AppScope
    AppLifecycleListener provideAppLifecycleListener(XMPPRepository xmppRepository, AppExecutors appExecutors, AppDatabase appDatabase) {
        return new AppLifecycleListener(xmppRepository, appExecutors, appDatabase);
    }

    @Provides
    @AppScope
    CryptLib provideCryptLib() {
        try {
            return new CryptLib();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
