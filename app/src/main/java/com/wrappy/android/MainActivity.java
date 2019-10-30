package com.wrappy.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.wrappy.android.common.AppExecutors;
import com.wrappy.android.common.Backable;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.ResourceRequestScreen;
import com.wrappy.android.di.ActivityInjector;
import com.wrappy.android.di.ActivityProviderModule;
import com.wrappy.android.misc.reset.ResetPasswordFragment;
import com.wrappy.android.server.AuthRepository.LoginStatus;
import com.wrappy.android.xmpp.XMPPRepository;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements ResourceRequestScreen {
    public static final String ACTION_RESET_DONE = "com.wrappy.android.intent.action.RESET_DONE";

    private ActivityInjector mActivityInjector;
    @Inject
    NavigationManager mNavigationManager;

    @Inject
    AppExecutors mAppExecutors;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    MainActivityViewModel mViewModel;

    private ProgressDialog mLoadingDialog;
    private int mLoadingCount;

    private boolean mFirstPageShown;
    private boolean mResetDone;

    private LoginStatus mLoginStatus;

    private LiveData<Resource<XMPPRepository.ConnectionStatus>> mConnectionStatus;

    private Observer<LoginStatus> loginStatusObserver =  loginStatus -> {
        mLoginStatus = loginStatus;
        mAppExecutors.mainThread().execute(() -> {
            // in case there are other observers observing the login status
            // have the following execute a little later by post()
            if (LoginStatus.IN == loginStatus) {
                Log.d("Check", "IN");
                if (!mFirstPageShown) {
                    mFirstPageShown = true;
                    mNavigationManager.showHomePage(false);
                    mViewModel.loginXMPP();
                    mConnectionStatus.observe(this, result -> {
                        checkRedirect();
                        showLoadingDialog(result);
                        Log.d("Result", result.data.toString());
                        switch(result.status) {

                            case SUCCESS:
                                //checkRedirect();
                                break;
                            case LOADING:
                                break;
                            case CLIENT_ERROR:
                                if(result.data==XMPPRepository.ConnectionStatus.NOCONNECTION) {
                                    showAlertDialog(getString(R.string.no_internet_connection),null);
                                }
                                break;
                            case SERVER_ERROR:
                                //showAlertDialog(result.message, null);
                                mViewModel.logoutXMPP();
                                mNavigationManager.showWelcomePage();
                                break;

                        }
                    });
                }
            } else {
                mFirstPageShown = true;
                mLoadingCount = 0;
                if (mLoadingDialog != null) {
                    mLoadingDialog.cancel();
                    mLoadingDialog = null;
                }
                mViewModel.logoutXMPP();
                Log.d("Check", "OUT");
                if (mResetDone || mViewModel.isForgotLogout()) {
                    mResetDone = false;
                    mViewModel.setForgotLogout(false);
                    mNavigationManager.showLoginPage();
                } else {
                    mNavigationManager.showWelcomePage();
                }
                if(LoginStatus.CONFLICT == loginStatus) {
                    showAlertDialog(getString(R.string.you_have_been_logged_out),null);
                }
            }
        });
    };

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getInjector().inject(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainActivityViewModel.class);
        mConnectionStatus = mViewModel.getConnectionStatus();

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(connectivityManager.getActiveNetworkInfo()!=null) {
                    if(mConnectionStatus.getValue()==null || mConnectionStatus.getValue().data== XMPPRepository.ConnectionStatus.NOCONNECTION) {
                        if(LoginStatus.IN == mLoginStatus) {
                            mViewModel.loginXMPP();
                        }
                    }
                }
            }
        };

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_VIEW)
                && getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            handleResetUri(uri);
        } else {
            mViewModel.getLoginStatus().observe(this, loginStatusObserver);
        }
        mViewModel.checkLogin();
        /**mViewModel.getMUCRooms().observe(this, result1 -> {
            Log.d("GET_MUC_ROOMS", "Observe");
            switch(result1.status) {
                case SUCCESS:
                    Log.d("GET_MUC_ROOMS", "Success");
                    String message = "";
                    for(RoomMUCExtend room: result1.data) {
                        message += room.getRoomID() + " - " + room.getRoomName() + " - " + room.getRoomStatus() + "\n";
                    }
                    showAlertDialog(message, null);
                    break;
                case LOADING:
                    break;
                case CLIENT_ERROR:
                    break;
                case SERVER_ERROR:
                    break;
            }
        });**/

        this.registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Messages";
            String description = "Wrappy chat messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WRAPPY_MESSAGE_CHANNEL", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        checkGoogleApiAvailability();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_VIEW) &&
                intent.getData() != null) {
            Uri uri = intent.getData();
            handleResetUri(uri);
        } else if (intent.getAction().equals(ACTION_RESET_DONE)) {
            mResetDone = true;
            mViewModel.getLoginStatus().observe(this, loginStatusObserver);
        }
        setIntent(intent);
        checkRedirect();
    }

    private void checkRedirect() {
        if (getIntent() == null ||
                !getIntent().hasExtra("JID") ||
                mLoginStatus == LoginStatus.OUT) {
            return;
        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
            Bundle redirectData = getIntent().getExtras();
            if (mNavigationManager.getFragmentManager().getBackStackEntryCount() > 0) {
                mNavigationManager.clearBackstack();
                mNavigationManager.showHomePage(false);
            }
            mNavigationManager.showChatPage(
                    redirectData.getString("JID"),
                    redirectData.getString("name"),
                    "user".equals(redirectData.getString("category")) ? "chat" : "groupchat");
            getIntent().removeExtra("JID");
            getIntent().removeExtra("name");
            getIntent().removeExtra("category");
        } else {
            Intent intent = new Intent(getIntent());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**if(mLoginStatus == LoginStatus.IN) {
            if(!mViewModel.isConnectionAuth()) {
                mViewModel.loginXMPP();
                mViewModel.getConnectionStatus().observe(this, result -> {
                    //showLoadingDialog(result);
                    switch (result.status) {

                        case SUCCESS:

                            break;
                        case LOADING:
                            break;
                        case CLIENT_ERROR:
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(result.message);
                            break;
                        case SERVER_ERROR:
                            break;
                    }
                });
            }
        }**/
    }

    public ActivityInjector getInjector() {
        if (mActivityInjector == null) {
            mActivityInjector = ((WrappyApp) getApplication()).getInjector()
                    .plusActivityInjector(new ActivityProviderModule(this));
        }
        return mActivityInjector;
    }

    @Override
    public void showLoadingDialog(Resource resource) {
        switch (resource.status) {
            case LOADING:
                mLoadingCount++;
                if (mLoadingDialog != null) {
                    mLoadingDialog.show();
                } else {
                    mLoadingDialog = ProgressDialog.show(this, null, getString(R.string.dialog_loading), true);
                }
                break;
            default:
                mLoadingCount--;
                if (mLoadingDialog != null && mLoadingCount <= 0) {
                    mLoadingDialog.cancel();
                    mLoadingDialog = null;
                }
                break;
        }
    }

    @Override
    public void showAlertDialog(String message, @Nullable DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false);
        if (clickListener == null) {
            clickListener = (dialog, id) -> dialog.dismiss();
        }
        builder.setPositiveButton(R.string.dialog_ok, clickListener);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof Backable) {
            FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            int backStackCount = childFragmentManager.getBackStackEntryCount();

            boolean consumed = false;
            if (childFragmentManager.getFragments().size() > 0) {
                Fragment subFragment = childFragmentManager.getFragments().get(0);
                consumed = subFragment instanceof Backable &&
                        ((Backable) subFragment).onBackPressed();
            }

            if (!consumed) {
                consumed = ((Backable) fragment).onBackPressed();
            }

            if (!consumed) {
                if (backStackCount > 0) {
                    childFragmentManager.popBackStack();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void handleResetUri(@NonNull Uri uri) {
        if (!uri.getLastPathSegment().startsWith("reset")) {
            return;
        }

        mViewModel.getLoginStatus().removeObserver(loginStatusObserver);
        mFirstPageShown = false;
        mLoginStatus = LoginStatus.OUT;

        String mode = uri.getLastPathSegment().equals("resetPattern") ?
                ResetPasswordFragment.MODE_PATTERN : ResetPasswordFragment.MODE_TEXT;
        mNavigationManager.showResetPasswordPage(mode, uri.getQueryParameter("secretKey"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        mViewModel.disconnectXMPP();
        super.onDestroy();
    }

    private void checkGoogleApiAvailability() {
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
                .addOnSuccessListener(result -> mViewModel.initFirebase());
    }

    @RequiresPermission("android.permission.WRITE_SETTINGS")
    public void setSettingsAutomaticDateTimeIfNeeded() {
        String timeSettings = android.provider.Settings.Global.getString(
                this.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME);
        if (timeSettings.contentEquals("0")) {
            android.provider.Settings.Global.putString(
                    this.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME, "1");
        }
    }


}
