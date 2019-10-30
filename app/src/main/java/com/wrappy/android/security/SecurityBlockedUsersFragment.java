package com.wrappy.android.security;

import android.support.v7.app.AlertDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.Block;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;

import org.jxmpp.jid.Jid;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SecurityBlockedUsersFragment extends SubFragment implements View.OnClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private MenuItem mMenuItemUnblock;

    RecyclerView mRecyclerViewBlockList;
    RecyclerView mRecyclerViewUnblockList;

    SearchView mSearchView;

    private TextView mTextViewNone;

    SecurityViewModel mSecurityViewModel;

    BlockListAdapter mBlockListAdapter;
    SelectedListAdapter mSelectedListAdapter;

    private List<Block> mBlockList;
    private List<Block> mSelectedList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_security_block_page, container, false);

        setHasOptionsMenu(true);

        mSearchView = view.findViewById(R.id.sec_block_search);

        mTextViewNone = view.findViewById(R.id.sec_block_textview_none);

        mRecyclerViewBlockList = view.findViewById(R.id.sec_block_recyclerview_block_list);
        mRecyclerViewUnblockList = view.findViewById(R.id.sec_block_recyclerview_unblock_list);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mSecurityViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(SecurityViewModel.class);

        setToolbarTitle(getString(R.string.security_page_title_block_list));
        showToolbarBackButton(true);

        mBlockList = new ArrayList<>();
        mSelectedList = new ArrayList<>();

        //mBlockList = mSecurityViewModel.getBlockListasJid();
        mBlockListAdapter = new BlockListAdapter(mBlockList, this);
        mSelectedListAdapter = new SelectedListAdapter(this);

        mSecurityViewModel.getBlockList().observe(this, result -> {
            mBlockList = result;
            if(mBlockList.isEmpty()) {
                mTextViewNone.setVisibility(View.VISIBLE);
            } else {
                mTextViewNone.setVisibility(View.GONE);
            }
            mBlockListAdapter.setBlockList(mBlockList);
            //mBlockListAdapter.notifyDataSetChanged();
        });



        mRecyclerViewBlockList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewBlockList.setAdapter(mBlockListAdapter);

        mRecyclerViewUnblockList.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
        mRecyclerViewUnblockList.setAdapter(mSelectedListAdapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("") || newText.equals(null)) {
                    mBlockListAdapter.setBlockList(mBlockList);
                } else {
                    mSecurityViewModel.getBlockListQuery(newText + "%").observe(getParentFragment(), result -> {
                        mBlockListAdapter.setBlockList(result);
                        if(result.isEmpty()) {
                            mTextViewNone.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNone.setVisibility(View.GONE);
                        }
                    });
                    Log.d("SEARCH_BLOCK", newText);
                }
                return false;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_security_block_done, menu);
        mMenuItemUnblock = menu.findItem(R.id.sec_block_done);
        if(mSelectedList.isEmpty()) {
            mMenuItemUnblock.setEnabled(false);
        } else {
            mMenuItemUnblock.setEnabled(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showUnblockConfirmDialog(DialogInterface.OnClickListener confirmClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false)
                .setTitle(R.string.unblock_user)
                .setMessage(R.string.security_dialog_title_unblock)
                .setPositiveButton(R.string.dialog_ok, confirmClickListener)
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sec_block_done: /** UNBLOCK DONE **/
                if (mSelectedList.size() > 0) {
                    showUnblockConfirmDialog((dialog, which) -> {
                        mSecurityViewModel.unblockContact(mSelectedList);
                        mSelectedList.clear();
                        //mBlockList = mSecurityViewModel.getBlockListasJid();
                        mBlockListAdapter.setBlockList(mBlockList);
                        showAlertDialog(getString(R.string.security_dialog_msg_unblock_success), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getParentFragment().getChildFragmentManager().popBackStackImmediate(null, 0);
                                getParentFragment().getFragmentManager().popBackStackImmediate();
                                //((BaseFragment) getParentFragment()).onBackPressed();
                            }
                        });
                        //mBlockListAdapter.notifyDataSetChanged();
                        if (mBlockList.isEmpty()) {
                            mTextViewNone.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNone.setVisibility(View.GONE);
                        }
                        if (mSelectedList.isEmpty()) {
                            mMenuItemUnblock.setEnabled(false);
                        } else {
                            mMenuItemUnblock.setEnabled(true);
                        }
                    });
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.security_block_list_container: /** SELECT FROM BLOCK **/

                if (!mSelectedList.contains(v.getTag())) {
                    mSelectedList.add((Block) v.getTag());

                } else if (mSelectedList.contains(v.getTag())) {
                    mSelectedList.remove((Block) v.getTag());

                }
                mBlockListAdapter.notifyDataSetChanged();
                mSelectedListAdapter.setSelectedList(mSelectedList);
                mSelectedListAdapter.notifyDataSetChanged();
                if (mSelectedList.isEmpty()) {
                    mRecyclerViewUnblockList.setVisibility(View.GONE);
                    mMenuItemUnblock.setEnabled(false);
                } else if (!mSelectedList.isEmpty()) {
                    mRecyclerViewUnblockList.setVisibility(View.VISIBLE);
                    mMenuItemUnblock.setEnabled(true);
                }
                break;

            case R.id.security_unblock_list_imagebutton_remove:
                mSelectedList.remove(v.getTag());
                mSelectedListAdapter.setSelectedList(mSelectedList);
                mSelectedListAdapter.notifyDataSetChanged();
                mBlockListAdapter.notifyDataSetChanged();
                if (mSelectedList.isEmpty()) {
                    mRecyclerViewUnblockList.setVisibility(View.GONE);
                    mMenuItemUnblock.setEnabled(false);
                } else if (!mSelectedList.isEmpty()) {
                    mRecyclerViewUnblockList.setVisibility(View.VISIBLE);
                    mMenuItemUnblock.setEnabled(true);
                }

                break;

        }
    }

    public class BlockListAdapter extends RecyclerView.Adapter<BlockListAdapter.BlockViewHolder> {

        List<Block> mBlockList;
        View.OnClickListener mOnClickListener;

        public BlockListAdapter(List<Block> blockList, View.OnClickListener onClickListener) {
            mBlockList = blockList;
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public BlockListAdapter.BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.security_block_list_item, parent, false);
            return new BlockViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BlockListAdapter.BlockViewHolder holder, int position) {
            holder.bindTo(mBlockList.get(position));
        }

        @Override
        public int getItemCount() {
            return mBlockList.size();
        }

        public void setBlockList(List<Block> blockList) {
            mBlockList = blockList;
            notifyDataSetChanged();
        }

        public class BlockViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout relativeLayoutContainer;

            ImageView imageViewCheckBox;
            ImageView imageViewAvatar;

            TextView textViewUserId;

            public BlockViewHolder(View itemView) {
                super(itemView);
                relativeLayoutContainer = itemView.findViewById(R.id.security_block_list_container);
                textViewUserId = itemView.findViewById(R.id.security_block_list_user_id);
                imageViewAvatar = itemView.findViewById(R.id.security_block_list_avatar);
                imageViewCheckBox = itemView.findViewById(R.id.security_block_list_check_box);

            }

            void bindTo(Block userJid) {
                InputUtils.loadAvatarImage(
                        getContext(),
                        imageViewAvatar,
                        mSecurityViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(userJid.getBlockId())));
                relativeLayoutContainer.setOnClickListener(mOnClickListener);
                relativeLayoutContainer.setTag(userJid);
                textViewUserId.setText(userJid.getBlockName());
                if (mSelectedList.contains(userJid)) {
                    relativeLayoutContainer.setBackgroundColor(getResources().getColor(R.color.selected_contact));
                    imageViewCheckBox.setImageResource(R.drawable.checkbox_selected);
                } else if (!mSelectedList.contains(userJid)) {
                    relativeLayoutContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
                    imageViewCheckBox.setImageResource(R.drawable.checkbox);
                }
            }
        }
    }

    public class SelectedListAdapter extends RecyclerView.Adapter<SecurityBlockedUsersFragment.SelectedListAdapter.SelectedViewHolder> {

        List<Block> mSelectedList;
        View.OnClickListener mOnClickListener;

        public SelectedListAdapter(View.OnClickListener onClickListener) {
            mSelectedList = new ArrayList<>();
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public SecurityBlockedUsersFragment.SelectedListAdapter.SelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.security_unblock_list_item, parent, false);
            return new SecurityBlockedUsersFragment.SelectedListAdapter.SelectedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SecurityBlockedUsersFragment.SelectedListAdapter.SelectedViewHolder holder, int position) {
            holder.bindTo(mSelectedList.get(position));
        }

        @Override
        public int getItemCount() {
            return mSelectedList.size();
        }

        public void setSelectedList(List<Block> blockList) {
            mSelectedList = blockList;
        }

        public class SelectedViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewAvatar;
            ImageView imageViewClose;

            TextView textViewName;

            public SelectedViewHolder(View itemView) {
                super(itemView);

                imageViewAvatar = itemView.findViewById(R.id.security_unblock_list_imageview_avatar);
                imageViewClose = itemView.findViewById(R.id.security_unblock_list_imagebutton_remove);
                imageViewClose.setOnClickListener(mOnClickListener);

                textViewName = itemView.findViewById(R.id.security_unblock_list_textview_name);

            }

            void bindTo(Block userJid) {
                InputUtils.loadAvatarImage(
                        getContext(),
                        imageViewAvatar,
                        mSecurityViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(userJid.getBlockId())));

                imageViewClose.setOnClickListener(mOnClickListener);
                imageViewClose.setTag(userJid);

                textViewName.setText(ContactManager.getUserName(userJid.getBlockId()));

            }
        }
    }

}
