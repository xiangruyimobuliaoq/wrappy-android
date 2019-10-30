package com.wrappy.android.chat;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static com.wrappy.android.chat.ChatFragment.KEY_JID;

/**
 * Created by Dan Chua on 2019-06-14
 */
public class ChatTranslateFragment extends SubFragment implements OnCheckedChangeListener {

    private static final String KEY_SHOW_AUTO_TRANSLATE = "chat_show_auto_translate";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ChatViewModel mChatViewModel;

    private SwitchCompat mSwitchCompatAuto;
    private RecyclerView mRecyclerViewLanguageList;

    private LanguageListAdapter mLanguageListAdapter;

    private List<String> mLanguageList;
    private List<String> mLanguageCodeList;
    private String mSelectedLanguage;

    private String mChatJid;
    private boolean mAutoTranslateFlg;

    public static Fragment create(String chatJid, boolean showAutoTranslate) {
        Bundle args = new Bundle();
        args.putString(KEY_JID, chatJid);
        args.putBoolean(KEY_SHOW_AUTO_TRANSLATE, showAutoTranslate);
        ChatTranslateFragment chatTranslateFragment = new ChatTranslateFragment();
        chatTranslateFragment.setArguments(args);
        return chatTranslateFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_translate, container, false);

        mSwitchCompatAuto = view.findViewById(R.id.chat_translate_switchcompat_auto);
        mRecyclerViewLanguageList = view.findViewById(R.id.chat_translate_recyclerview_language);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        setToolbarTitle(getString(R.string.translation));

        if (!getArguments().getBoolean(KEY_SHOW_AUTO_TRANSLATE, true)) {
            getView().findViewById(R.id.border2).setVisibility(View.GONE);
            getView().findViewById(R.id.chat_translate_auto_container).setVisibility(View.GONE);
        }

        mChatViewModel = ViewModelProviders
                .of(getParentFragment(), mViewModelFactory)
                .get(ChatViewModel.class);

        mLanguageListAdapter = new LanguageListAdapter(getContext(), mLanguageList);

        mRecyclerViewLanguageList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewLanguageList.setAdapter(mLanguageListAdapter);

        mLanguageCodeList = Arrays.asList(getResources().getStringArray(R.array.chat_iso_language_list));

        mLanguageList = Arrays.asList(getResources().getStringArray(R.array.chat_language_list));
        mLanguageListAdapter.setLanguageList(mLanguageList);

        mChatJid = getArguments().getString(KEY_JID);
        mChatViewModel.getChat(mChatJid).observe(this, chat -> {
            showLoadingDialog(chat);
            switch (chat.status) {
                case SUCCESS:
                    mSelectedLanguage = getLanguageFromCode(chat.data.getChatLanguage());
                    mAutoTranslateFlg = chat.data.isChatAutoTranslate();
                    setSwitchChecked(mAutoTranslateFlg, true);
                    mLanguageListAdapter.notifyDataSetChanged();
                    break;
            }
        });
    }

    private void setSwitchChecked(boolean isChecked) {
        setSwitchChecked(isChecked, false);
    }

    private void setSwitchChecked(boolean isChecked, boolean skipAnimation) {
        mSwitchCompatAuto.setOnCheckedChangeListener(null);
        mSwitchCompatAuto.setChecked(isChecked);
        if (skipAnimation) {
            mSwitchCompatAuto.jumpDrawablesToCurrentState();
        }
        mSwitchCompatAuto.setOnCheckedChangeListener(this);
    }

    private void setAutoTranslateFlag(boolean autoTranslate) {
        mChatViewModel.setChatAutoTranslate(mChatJid, autoTranslate).observe(this, this::setSwitchChecked);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setAutoTranslateFlag(isChecked);
    }

    private String getLanguageFromCode(String code) {
        return mLanguageList.get(mLanguageCodeList.indexOf(code));
    }

    private String getCodeFromLanguage(String language) {
        return mLanguageCodeList.get(mLanguageList.indexOf(language));
    }

    public class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.ViewHolder> {

        private Context mContext;
        private List<String> mLanguageList;

        public LanguageListAdapter(Context context, List<String> languageList) {
            this.mContext = context;
            this.mLanguageList = languageList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.translate_language_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(mLanguageList.get(position));
        }

        @Override
        public int getItemCount() {
            return mLanguageList.size();
        }

        public void setLanguageList(List<String> languageList) {
            this.mLanguageList = languageList;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewLanguageName;
            ImageView imageViewCheck;

            public ViewHolder(View itemView) {
                super(itemView);
                textViewLanguageName = itemView.findViewById(R.id.translate_language_textview_name);
                imageViewCheck = itemView.findViewById(R.id.translate_language_imageview_check);
                itemView.setOnClickListener((v) -> {
                    String selected = getCodeFromLanguage(v.getTag().toString());
                    mChatViewModel.setChatLanguage(mChatJid, selected)
                            .observe(ChatTranslateFragment.this, (language) -> {
                                mSelectedLanguage = getLanguageFromCode(selected);
                                mLanguageListAdapter.notifyDataSetChanged();
                            });
                });
            }

            public void bindTo(String language) {
                itemView.setTag(language);
                textViewLanguageName.setText(language);
                if (language.equals(mSelectedLanguage)) {
                    imageViewCheck.setVisibility(View.VISIBLE);
                } else {
                    imageViewCheck.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

}
