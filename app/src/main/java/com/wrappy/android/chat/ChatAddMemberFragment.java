package com.wrappy.android.chat;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;

import org.jxmpp.jid.Jid;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.wrappy.android.chat.ChatFragment.KEY_MEMBER;

public class ChatAddMemberFragment extends SubFragment implements View.OnClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ChatNavigationManager mChatNavigationManager;

    ChatViewModel mChatViewModel;

    private MenuItem mMenuItemAdd;

    private AddListAdapter mAddListAdapter;

    private SelectedListAdapter mSelectedListAdapter;

    private RecyclerView mRecyclerViewContactList;
    private RecyclerView mRecyclerViewSelectedList;

    private SearchView mSearchView;

    private List<Contact> mContactList;
    private List<Contact> mSelectedList;

    public static ChatAddMemberFragment create(ArrayList<Contact> members) {
        ChatAddMemberFragment chatAddMemberFragment = new ChatAddMemberFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_MEMBER, members);
        chatAddMemberFragment.setArguments(args);
        return chatAddMemberFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_add_member_page, container, false);

        setHasOptionsMenu(true);

        mRecyclerViewContactList = view.findViewById(R.id.chat_add_contact_list);
        mRecyclerViewSelectedList = view.findViewById(R.id.chat_add_adding_list);


        mSearchView = view.findViewById(R.id.chat_add_search);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getInjector().inject(this);

        showToolbarBackButton(true);
        setToolbarTitle("");

        mChatViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChatViewModel.class);

        List<Contact> memberList = (ArrayList<Contact>) getArguments().getSerializable(KEY_MEMBER);

        mAddListAdapter = new AddListAdapter(this);

        mSelectedListAdapter = new SelectedListAdapter(this);

        mChatViewModel.getContacts().observe(this, result -> {
            init(result, memberList);
            mAddListAdapter.notifyDataSetChanged();
        });

        mSelectedList = new ArrayList<>();

        mRecyclerViewContactList.setAdapter(mAddListAdapter);
        mRecyclerViewContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerViewSelectedList.setAdapter(mSelectedListAdapter);
        mRecyclerViewSelectedList.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));

    }

    private void init(List<Contact> contactList, List<Contact> memberList) {
        mContactList = contactList;
        mContactList.removeAll(memberList);
        mAddListAdapter.setAddList(mContactList);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mAddListAdapter.setAddList(mContactList);
                    mAddListAdapter.notifyDataSetChanged();
                } else {
                    List<Contact> queryContacts = new ArrayList<>();
                    for (Contact contact : mContactList) {
                        if (contact.queryContact(newText)) {
                            queryContacts.add(contact);
                        }
                    }
                    mAddListAdapter.setAddList(queryContacts);
                    mAddListAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_security_block_done, menu);
        mMenuItemAdd = menu.findItem(R.id.sec_block_done).setTitle(R.string.add);
        if(mSelectedList.isEmpty()) {
            mMenuItemAdd.setEnabled(false);
        } else {
            mMenuItemAdd.setEnabled(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sec_block_done: /** UNBLOCK DONE **/
                if (mSelectedList.size() > 0) {
                    for (Contact contact : mSelectedList) {
                        mChatViewModel.addMember(contact.getContactId());
                    }
                }
                getActivity().onBackPressed();
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
                    mSelectedList.add((Contact) v.getTag());

                } else if (mSelectedList.contains(v.getTag())) {
                    mSelectedList.remove((Contact) v.getTag());

                }
                mSelectedListAdapter.setSelectedList(mSelectedList);
                mSelectedListAdapter.notifyDataSetChanged();
                mAddListAdapter.notifyDataSetChanged();
                if (mSelectedList.isEmpty()) {
                    mRecyclerViewSelectedList.setVisibility(View.GONE);
                    mMenuItemAdd.setEnabled(false);
                } else if (!mSelectedList.isEmpty()) {
                    mRecyclerViewSelectedList.setVisibility(View.VISIBLE);
                    mMenuItemAdd.setEnabled(true);
                }

                break;

            case R.id.selected_member_imageview_close:
                mSelectedList.remove(v.getTag());
                mSelectedListAdapter.setSelectedList(mSelectedList);
                mSelectedListAdapter.notifyDataSetChanged();
                mAddListAdapter.notifyDataSetChanged();
                if (mSelectedList.isEmpty()) {
                    mRecyclerViewSelectedList.setVisibility(View.GONE);
                    mMenuItemAdd.setEnabled(false);
                } else if (!mSelectedList.isEmpty()) {
                    mRecyclerViewSelectedList.setVisibility(View.VISIBLE);
                    mMenuItemAdd.setEnabled(true);
                }

                break;

        }
    }

    public class AddListAdapter extends RecyclerView.Adapter<ChatAddMemberFragment.AddListAdapter.AddViewHolder> {

        List<Contact> mBlockList;
        View.OnClickListener mOnClickListener;

        public AddListAdapter(View.OnClickListener onClickListener) {
            mBlockList = new ArrayList<>();
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ChatAddMemberFragment.AddListAdapter.AddViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.security_block_list_item, parent, false);
            return new ChatAddMemberFragment.AddListAdapter.AddViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatAddMemberFragment.AddListAdapter.AddViewHolder holder, int position) {
            holder.bindTo(mBlockList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mBlockList.size();
        }

        public void setAddList(List<Contact> blockList) {
            mBlockList = blockList;
        }

        public class AddViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout relativeLayoutContainer;

            ImageView imageViewCheckBox;
            ImageView imageViewAvatar;
            ImageView imageViewStatus;

            TextView textViewUserId;
            TextView textViewStatus;

            public AddViewHolder(View itemView) {
                super(itemView);
                relativeLayoutContainer = itemView.findViewById(R.id.security_block_list_container);
                textViewUserId = itemView.findViewById(R.id.security_block_list_user_id);
                imageViewCheckBox = itemView.findViewById(R.id.security_block_list_check_box);
                imageViewAvatar = itemView.findViewById(R.id.security_block_list_avatar);
                imageViewStatus = itemView.findViewById(R.id.security_block_list_imageview_status);
                textViewStatus = itemView.findViewById(R.id.security_block_list_status);

            }

            void bindTo(Contact userJid, int position) {
                relativeLayoutContainer.setOnClickListener(mOnClickListener);
                relativeLayoutContainer.setTag(userJid);
                textViewUserId.setText(userJid.getContactName());
                textViewStatus.setText(userJid.getContactPresence());
                InputUtils.loadAvatarImage(
                        getContext(),
                        imageViewAvatar,
                        mChatViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(userJid.getContactId())));
                if (userJid.getContactPresence().equals("available")) {
                    imageViewStatus.setImageDrawable(getContext().getDrawable(R.drawable.status_active));
                    textViewStatus.setText(getString(R.string.status_online));
                } else if (userJid.getContactPresence().equals("unavailable")) {
                    imageViewStatus.setImageDrawable(getContext().getDrawable(R.drawable.status_disable));
                    textViewStatus.setText(getString(R.string.status_offline));
                }
                if (mSelectedList.contains(userJid)) {
                    relativeLayoutContainer.setBackgroundColor(getResources().getColor(R.color.selected_contact));
                    imageViewCheckBox.setImageResource(R.drawable.ic_check_box_black_24dp);
                } else if (!mSelectedList.contains(userJid)) {
                    relativeLayoutContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
                    imageViewCheckBox.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
                }
            }
        }
    }

    public class SelectedListAdapter extends RecyclerView.Adapter<ChatAddMemberFragment.SelectedListAdapter.SelectedViewHolder> {

        List<Contact> mSelectedList;
        View.OnClickListener mOnClickListener;

        public SelectedListAdapter(View.OnClickListener onClickListener) {
            mSelectedList = new ArrayList<>();
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ChatAddMemberFragment.SelectedListAdapter.SelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.selected_contact_list_item, parent, false);
            return new ChatAddMemberFragment.SelectedListAdapter.SelectedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatAddMemberFragment.SelectedListAdapter.SelectedViewHolder holder, int position) {
            holder.bindTo(mSelectedList.get(position));
        }

        @Override
        public int getItemCount() {
            return mSelectedList.size();
        }

        public void setSelectedList(List<Contact> blockList) {
            mSelectedList = blockList;
        }

        public class SelectedViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewAvatar;
            ImageView imageViewClose;

            TextView textViewName;

            public SelectedViewHolder(View itemView) {
                super(itemView);

                imageViewAvatar = itemView.findViewById(R.id.selected_member_imageview_avatar);
                imageViewClose = itemView.findViewById(R.id.selected_member_imageview_close);

                textViewName = itemView.findViewById(R.id.selected_member_textview_name);

            }

            void bindTo(Contact userJid) {
                InputUtils.loadAvatarImage(
                        getContext(),
                        imageViewAvatar,
                        mChatViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(userJid.getContactId())));

                imageViewClose.setOnClickListener(mOnClickListener);
                imageViewClose.setTag(userJid);

                textViewName.setText(userJid.getContactName());

            }
        }
    }

}
