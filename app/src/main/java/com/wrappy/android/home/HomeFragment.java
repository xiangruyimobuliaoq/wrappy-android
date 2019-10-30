package com.wrappy.android.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.common.ui.LockableViewPager;

public class HomeFragment extends BaseFragment {
    public static final String KEY_EXTRA_SHOW_WELCOME = "showWelcome";

    private LockableViewPager mViewPagerHome;
    private BottomNavigationView mBottomNav;
    private AlertDialog mWelcomeDialog;

    private Toolbar mToolbar;

    private int mCurrentPage = 1;

    public static HomeFragment create(boolean showWelcome) {
        HomeFragment f = new HomeFragment();
        Bundle b = new Bundle();
        b.putBoolean(KEY_EXTRA_SHOW_WELCOME, showWelcome);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home_page, container, false);

        mViewPagerHome = view.findViewById(R.id.home_viewpager_home);
        mBottomNav = view.findViewById(R.id.home_bottomnav_view);

        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*String name = getFragmentManager().getBackStackEntryAt(getBackStackCount()-1).getName();*/

        setToolbar(mToolbar);
        switchToolbarHeader(mCurrentPage);

        mViewPagerHome.setSwipeLocked(true);

        SparseIntArray menuItems = new SparseIntArray();
        menuItems.append(0, R.id.nav_button_contacts);
        menuItems.append(1, R.id.nav_button_chat);
        menuItems.append(2, R.id.nav_button_profile);

        mViewPagerHome.setOffscreenPageLimit(2);
        mViewPagerHome.setAdapter(new HomePagerAdapter(getChildFragmentManager(), menuItems.size()));
        mViewPagerHome.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                mBottomNav.setSelectedItemId(menuItems.get(mCurrentPage));
                switchSelectedIcon(mBottomNav.getMenu(), position);
                switchToolbarHeader(mCurrentPage);
            }
        });
        mBottomNav.setOnNavigationItemSelectedListener(item -> {
            mCurrentPage = menuItems.indexOfValue(item.getItemId());
            mViewPagerHome.setCurrentItem(mCurrentPage, false);
            return true;
        });
        mViewPagerHome.setCurrentItem(mCurrentPage, false);

        if (getArguments().getBoolean(KEY_EXTRA_SHOW_WELCOME)) {
            getArguments().putBoolean(KEY_EXTRA_SHOW_WELCOME, false);
            mWelcomeDialog = createWelcomeToWrappyDialog();
            mWelcomeDialog.show();
        }
    }

    @Override
    public void onDestroyView() {
        if (mWelcomeDialog != null) {
            mWelcomeDialog.dismiss();
            mWelcomeDialog = null;
        }
        super.onDestroyView();
    }

    private void switchToolbarHeader(int tabPosition) {
        if (tabPosition != 2) {
            setToolbarLogo(R.drawable.text_1);
        } else {
            setToolbarTitle(getString(R.string.my_page));
        }
    }

    private void switchSelectedIcon(Menu menu, int tabPosition) {

        switch(tabPosition) {
            case 0:
                menu.getItem(1).setIcon(R.drawable.chat1);
                menu.getItem(2).setIcon(R.drawable.my_page1);
                break;
            case 1:
                menu.getItem(2).setIcon(R.drawable.my_page1);
                menu.getItem(tabPosition).setIcon(R.drawable.chat_selected1);
                break;
            case 2:
                menu.getItem(1).setIcon(R.drawable.chat1);
                menu.getItem(tabPosition).setIcon(R.drawable.my_page_selected1);
                break;
        }
    }

    private AlertDialog createWelcomeToWrappyDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.misc_dialog_welcome_wrappy, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.button).setOnClickListener(v -> {
            dialog.dismiss();
        });
        return dialog;
    }

    public class HomePagerAdapter extends FragmentStatePagerAdapter {

        int mNumOfTabs;

        public HomePagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            mNumOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeContactsFragment();

                case 1:
                    return new HomeChatsFragment();

                case 2:
                    return new HomeProfileFragment();

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

}
