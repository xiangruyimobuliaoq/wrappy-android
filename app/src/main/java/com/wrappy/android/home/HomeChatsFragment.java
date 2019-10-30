package com.wrappy.android.home;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.tubb.smrv.SwipeHorizontalMenuLayout;
import com.tubb.smrv.SwipeMenuRecyclerView;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.chat.AuthorViewObject;
import com.wrappy.android.common.chat.DialogViewObject;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.common.utils.NotificationID;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.Contact;
import com.wrappy.android.server.account.body.request.FileBody;
import com.wrappy.android.xmpp.ContactManager;
import com.wrappy.android.xmpp.XMPPRepository;
import com.wrappy.android.xmpp.muc.RoomMUCExtend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class HomeChatsFragment extends SubFragment implements View.OnClickListener {

    SwipeMenuRecyclerView mDialogsList;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    NavigationManager mNavigationManager;

    HomeViewModel mHomeViewModel;

    ChatListAdapter mChatListAdapter;

    List<Chat> mChatList = Collections.emptyList();;

    List<Chat> mGroupChatList = Collections.emptyList();;

    List<Contact> mContactList;

    private SearchView mSearchView;

    private ConstraintLayout mContainerWelcome;

    private TabLayout mChatTabLayout;

    private TextView mTextViewNoUser;

    private LiveData<Resource<XMPPRepository.ConnectionStatus>> mConnectionStatus;

    private LiveData<List<Chat>> mQueryOnetoOne;
    private LiveData<List<Chat>> mQueryGroup;

    private boolean isOnetoOne = true;

    private int mCurrentPage = 0;

    private Bundle savedState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home_chats_page, container, false);
        mDialogsList = view.findViewById(R.id.chat_dialogs_list);
        mContainerWelcome = view.findViewById(R.id.chat_container_welcome);
        mSearchView = view.findViewById(R.id.chat_searchview);
        mChatTabLayout = view.findViewById(R.id.chat_tablayout);
        mTextViewNoUser = view.findViewById(R.id.chat_no_user);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        mHomeViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(HomeViewModel.class);
        mConnectionStatus = mHomeViewModel.getConnectionStatus();

        mDialogsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mContactList = new ArrayList<>();



        mChatTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0:
                        mSearchView.setQuery("",false);
                        mChatListAdapter.setChatList(mChatList);
                        isOnetoOne = true;
                        mCurrentPage = tab.getPosition();
                        Log.d("CURRENTPAGE", String.valueOf(mCurrentPage));
                        if(mChatList.isEmpty()) {
                            mContainerWelcome.setVisibility(View.VISIBLE);
                        } else {
                            mContainerWelcome.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        mSearchView.setQuery("",false);
                        mChatListAdapter.setChatList(mGroupChatList);
                        isOnetoOne = false;
                        mCurrentPage = tab.getPosition();
                        Log.d("CURRENTPAGE", String.valueOf(mCurrentPage));
                        if(mGroupChatList.isEmpty()) {
                            mContainerWelcome.setVisibility(View.VISIBLE);
                        } else {
                            mContainerWelcome.setVisibility(View.GONE);
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(isOnetoOne) {
            mChatTabLayout.getTabAt(0).select();
            //mChatTabLayout.setScrollPosition(0,0f, true);
            if(mChatList.isEmpty()) {
                mContainerWelcome.setVisibility(View.VISIBLE);
            } else {
                mContainerWelcome.setVisibility(View.GONE);
            }
            Log.d("CURRENTPAGE", String.valueOf(mCurrentPage));
            mCurrentPage = 0;
        } else {
            mChatTabLayout.getTabAt(1).select();
            if(mGroupChatList.isEmpty()) {
                mContainerWelcome.setVisibility(View.VISIBLE);
            } else {
                mContainerWelcome.setVisibility(View.GONE);
            }
            //mChatTabLayout.setScrollPosition(1, 0f, true);
            mCurrentPage = 1;
        }





        mChatListAdapter = new ChatListAdapter(isOnetoOne ? mChatList : mGroupChatList, this);
        mChatListAdapter.setHasStableIds(true);

        mDialogsList.setItemAnimator(null);
        mDialogsList.setAdapter(mChatListAdapter);
        mHomeViewModel.getUserJid().observe(getViewLifecycleOwner(), result1 -> {
            if(!result1.isEmpty()) {
                /**mHomeViewModel.getChatList(result1).observe(this, result -> {
                    mChatListAdapter.setChatList(result);
                    mChatList = result;
                    if (result.isEmpty()) {
                        mContainerWelcome.setVisibility(View.VISIBLE);
                    } else {
                        mContainerWelcome.setVisibility(View.GONE);
                    }
                    if (!result1.isEmpty()) {
                        mChatListAdapter.notifyDataSetChanged();
                    }
                });**/
                mHomeViewModel.getOnetoOneChatList(result1).observe(getViewLifecycleOwner(), result -> {
                    mChatList = result;
                    if(isOnetoOne) {
                        if(result.isEmpty()) {
                            mContainerWelcome.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNoUser.setVisibility(View.GONE);
                            mContainerWelcome.setVisibility(View.GONE);
                        }
                        mChatListAdapter.setChatList(mChatList);

                    }
                });
                mHomeViewModel.getGroupChatList(result1).observe(getViewLifecycleOwner(), result -> {
                    mGroupChatList = result;
                    if(!isOnetoOne) {
                        if(result.isEmpty()) {
                            mContainerWelcome.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNoUser.setVisibility(View.GONE);
                            mContainerWelcome.setVisibility(View.GONE);
                        }
                        mChatListAdapter.setChatList(mGroupChatList);
                    }
                });
            }
        });

        mHomeViewModel.getContacts().observe(getViewLifecycleOwner(), result -> {
            mContactList = result;
            mChatListAdapter.notifyDataSetChanged();
        });

        /**mHomeViewModel.getMUCRooms().observe(this, result -> {
            Log.d("GET_MUC_ROOMS", "Observe");
            switch(result.status) {
                case SUCCESS:
                    Log.d("GET_MUC_ROOMS", "Success");
                    String message = "";
                    for(RoomMUCExtend room: result.data) {
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

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("") || newText.equals(null) || newText.isEmpty()) {
                    mTextViewNoUser.setVisibility(View.GONE);
                    if(isOnetoOne) {
                        mChatListAdapter.setChatList(mChatList);
                    } else {
                        mChatListAdapter.setChatList(mGroupChatList);
                    }
                    if(mQueryOnetoOne!=null && mQueryOnetoOne.hasActiveObservers()) {
                        mQueryOnetoOne.removeObservers(getParentFragment());
                    }
                    if(mQueryGroup != null && mQueryGroup.hasActiveObservers()) {
                        mQueryGroup.removeObservers(getParentFragment());
                    }
                    return false;
                } else {
                    mHomeViewModel.getUserJid().observe(getParentFragment(), result1 -> {
                        queryChats(newText, result1);
                    });
                    return true;
                }

            }
        });


    }

    private void queryChats(String newText, String userJid){
        if(isOnetoOne) {
            mQueryOnetoOne = mHomeViewModel.getQueryOnetoOneChatList(newText + "%", userJid);
            mQueryOnetoOne.observe(getViewLifecycleOwner(), new Observer<List<Chat>>() {
                @Override
                public void onChanged(@Nullable List<Chat> result) {
                    mQueryOnetoOne.removeObserver(this);
                    if (result.isEmpty()) {
                        // TODO: No such user
                        mTextViewNoUser.setVisibility(View.VISIBLE);
                        mChatListAdapter.setChatList(result);
                    } else {
                        mChatListAdapter.setChatList(result);
                        mTextViewNoUser.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            mQueryGroup = mHomeViewModel.getQueryGroupChatList(newText + "%", userJid);
            mQueryGroup.observe(getViewLifecycleOwner(), new Observer<List<Chat>>() {
                @Override
                public void onChanged(@Nullable List<Chat> result) {
                    mQueryGroup.removeObserver(this);
                    if (result.isEmpty()) {
                        mTextViewNoUser.setVisibility(View.VISIBLE);
                        mChatListAdapter.setChatList(result);
                    } else {
                        mChatListAdapter.setChatList(result);
                        mTextViewNoUser.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sml_home_chat_dialog:
                Chat chat = (Chat) v.getTag();
                mNavigationManager.showChatPage(chat.getChatId(), chat.getChatName(), chat.getChatType());
                break;

            case R.id.home_chat_dialog_menu_delete:

                break;

        }
    }

    public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

        List<Chat> mChatList;
        View.OnClickListener mOnClickListener;

        public ChatListAdapter(List<Chat> chatList, View.OnClickListener onClickListener) {
            mChatList = chatList;
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ChatListAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.home_chat_dialog_list_item, parent, false);
            return new ChatViewHolder(view, mOnClickListener);
        }

        public void setChatList(List<Chat> chatList) {
            mChatList = chatList;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListAdapter.ChatViewHolder holder, int position) {
            holder.bindTo(mChatList.get(position));
        }

        @Override
        public int getItemCount() {
            return mChatList.size();
        }

        @Override
        public long getItemId(int position) {
            long itemId;
            Chat chat = mChatList.get(position);
            itemId = (chat.getChatId() + chat.getChatLastMessage()).hashCode()
                    + chat.getChatUnreadCount();
            if (chat.getChatLastDate() != null) {
                itemId += chat.getChatLastDate().getTime();
            }
            return itemId;
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {

            View.OnClickListener mOnClickListener;

            CardView cardViewUnreadContainer;

            ImageButton imageButtonDelete;

            ImageView imageViewAvatar;
            ImageView imageViewStatus;

            SwipeHorizontalMenuLayout smlHomeChatDialog;

            TextView textViewChatName;
            TextView textViewLastMessage;
            TextView textViewUnreadCount;

            public ChatViewHolder(View itemView, View.OnClickListener onClickListener) {
                super(itemView);

                mOnClickListener = onClickListener;

                cardViewUnreadContainer = itemView.findViewById(R.id.home_chat_dialog_badge_container);

                imageButtonDelete = itemView.findViewById(R.id.home_chat_dialog_menu_delete);

                imageViewAvatar = itemView.findViewById(R.id.home_chat_dialog_avatar);
                imageViewStatus = itemView.findViewById(R.id.home_chat_dialog_status);

                smlHomeChatDialog = itemView.findViewById(R.id.sml_home_chat_dialog);
                smlHomeChatDialog.setOnClickListener(mOnClickListener);

                textViewChatName = itemView.findViewById(R.id.home_chat_dialog_name);
                textViewLastMessage = itemView.findViewById(R.id.home_chat_dialog_last);
                textViewUnreadCount = itemView.findViewById(R.id.home_chat_dialog_badge_count);

                imageButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mConnectionStatus.getValue().data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                            Chat chat1 = (Chat) v.getTag();
                            NotificationManagerCompat.from(getContext().getApplicationContext()).cancel(NotificationID.getGroupID(chat1.getChatId()));
                            mHomeViewModel.deleteMessages(chat1.getChatId(), chat1.getChatType());
                            smlHomeChatDialog.smoothCloseMenu(0);
                        } else {
                            showAlertDialog(getString(R.string.no_internet_connection),null);
                        }
                    }
                });
            }

            void bindTo(Chat chat) {
                imageButtonDelete.setTag(chat);
                textViewChatName.setText(chat.getChatName());
                String lastmessage;
                if (chat.getChatLastMessage().equals("image")) {
                    lastmessage = "[" + getString(R.string.preview_photo) + "]";
                } else if (chat.getChatLastMessage().equals("voice")) {
                    lastmessage = "[" + getString(R.string.preview_voice) + "]";
                } else if (chat.getChatLastMessage().equals("location")) {
                    lastmessage = "[" + getString(R.string.preview_location) + "]";
                } else if (chat.getChatLastMessage().equals("stamp")) {
                    lastmessage = "[" + getString(R.string.preview_stamp) + "]";
                } else {
                    lastmessage = chat.getChatLastMessage();
                }
                textViewLastMessage.setText(lastmessage);
                smlHomeChatDialog.setTag(chat);

                if(chat.getChatUnreadCount()>0) {
                    textViewUnreadCount.setText(String.valueOf(chat.getChatUnreadCount()));
                    cardViewUnreadContainer.setVisibility(View.VISIBLE);
                    textViewLastMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    textViewUnreadCount.setText("");
                    cardViewUnreadContainer.setVisibility(View.GONE);
                    textViewLastMessage.setTextColor(getResources().getColor(R.color.last_message_preview));
                }

                InputUtils.loadAvatarImage(getContext(),
                        imageViewAvatar,
                        mHomeViewModel.getContactFileUrl(
                                FileBody.Type.FILE_TYPE_AVATAR,
                                ContactManager.getUserName(chat.getChatId()),
                                chat.getChatType().equals("groupchat")));

                imageViewStatus.setImageResource(R.drawable.status_disable);
                for(Contact contact : mContactList) {
                    if (contact.isEqualToChat(chat) && contact.getContactPresence().equals("available")) {
                        imageViewStatus.setImageResource(R.drawable.status_active);
                        break;
                    }
                }

            }

        }

    }

}
