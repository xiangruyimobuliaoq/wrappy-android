package com.wrappy.android.common.ui;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.request.SecurityQuestionAnswerBody;

public abstract class AbstractSecurityQuestionFragment extends SubFragment implements View.OnClickListener, View.OnTouchListener, AdapterView.OnItemSelectedListener, TextWatcher {
    private Button mButtonNext;

    private AppCompatSpinner[] mAppCompatSpinnerQuestion;
    private EditText[] mEditTextAnswer;

    private AbstractSecurityQuestionFragment.QuestionsAdapter mSpinnerAdapter;

    private List<SecurityQuestionAnswerBody> mQuestionAnswerList = new ArrayList<>();
    private List<SecurityQuestionAnswerBody> mQuestionDataList;

    private int mCurrentSpinner = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_reg_question_page, container, false);

        mAppCompatSpinnerQuestion = new AppCompatSpinner[3];
        mAppCompatSpinnerQuestion[0] = view.findViewById(R.id.reg_question_spinner_question1);
        mAppCompatSpinnerQuestion[1] = view.findViewById(R.id.reg_question_spinner_question2);
        mAppCompatSpinnerQuestion[2] = view.findViewById(R.id.reg_question_spinner_question3);

        mEditTextAnswer = new EditText[3];
        mEditTextAnswer[0] = view.findViewById(R.id.reg_question_edittext_answer1);
        mEditTextAnswer[1] = view.findViewById(R.id.reg_question_edittext_answer2);
        mEditTextAnswer[2] = view.findViewById(R.id.reg_question_edittext_answer3);

        // get the current EditText disabled color as hint color to match
        int hintColor = mEditTextAnswer[0]
                .getTextColors()
                .getColorForState(new int[]{-android.R.attr.state_enabled}, 0);

        for (EditText editText : mEditTextAnswer) {
            editText.addTextChangedListener(this);
            editText.setHintTextColor(hintColor);
            InputUtils.disableWhitespaceInput(editText);
        }

        mButtonNext = view.findViewById(R.id.reg_question_button_next);
        String buttonText = getNextButtonText();
        mButtonNext.setText(TextUtils.isEmpty(buttonText) ?
                getString(R.string.next) : buttonText);
        mButtonNext.setOnClickListener(this);

        enableViews(false,
                mAppCompatSpinnerQuestion[1],
                mEditTextAnswer[1],
                mAppCompatSpinnerQuestion[2],
                mEditTextAnswer[2],
                mButtonNext);
        return view;
    }

    protected abstract List<SecurityQuestionAnswerBody> getSelectedQuestions();

    protected abstract List<SecurityQuestionAnswerBody> getQuestions();

    protected abstract void onSubmitQuestionAnswers(List<SecurityQuestionAnswerBody> questionAnswerList);

    protected abstract void onActivityCreatedInternal();

    protected abstract String getNextButtonText();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onActivityCreatedInternal();

        mQuestionDataList = getQuestions();
        List<SecurityQuestionAnswerBody> selectedQuestions = getSelectedQuestions();

        List<String> uiQuestionList = new ArrayList<>();
        uiQuestionList.add(0, getString(R.string.choose_question));
        for (SecurityQuestionAnswerBody qaBody : mQuestionDataList) {
            uiQuestionList.add(qaBody.question);
        }

        mSpinnerAdapter = new AbstractSecurityQuestionFragment.QuestionsAdapter(
                getContext(),
                R.layout.reg_question_list_item,
                uiQuestionList);

        for (AppCompatSpinner spinner : mAppCompatSpinnerQuestion) {
            spinner.setAdapter(mSpinnerAdapter);
            spinner.setOnTouchListener(this);
            spinner.setOnItemSelectedListener(this);
        }

        int questionSize = Math.min(3, selectedQuestions.size());
        for (int x = 0; x < questionSize; x++) {
            SecurityQuestionAnswerBody selected = selectedQuestions.get(x);
            mAppCompatSpinnerQuestion[x].setSelection(
                    mQuestionDataList.indexOf(selected) + 1);
            mEditTextAnswer[x].setText(selected.answer);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_question_button_next:
                if (checkQACompleted()) {
                    registerQuestionAndAnswers();
                    onSubmitQuestionAnswers(mQuestionAnswerList);
                } else {
                    showAlertDialog(getString(R.string.dialog_security_question_error_message), null);
                }
                break;
        }
    }

    private void registerQuestionAndAnswers() {
        mQuestionAnswerList.clear();
        for (int x = 0; x < mAppCompatSpinnerQuestion.length; x++) {
            addQAToList(mAppCompatSpinnerQuestion[x], mEditTextAnswer[x]);
        }
    }

    private void addQAToList(AppCompatSpinner spinner, TextView answerEditText) {
        if (spinner.getSelectedItemPosition() > 0) {
            SecurityQuestionAnswerBody question = mQuestionDataList.get(spinner.getSelectedItemPosition() - 1);
            mQuestionAnswerList.add(new SecurityQuestionAnswerBody(question.code,
                    question.question,
                    answerEditText.getText().toString()));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            switch (v.getId()) {
                case R.id.reg_question_spinner_question1:
                    mCurrentSpinner = 0;
                    break;
                case R.id.reg_question_spinner_question2:
                    mCurrentSpinner = 1;
                    break;
                case R.id.reg_question_spinner_question3:
                    mCurrentSpinner = 2;
                    break;
            }
            v.performClick();
        }
        return false;
    }

    private void enableViews(boolean enable, @Nullable View... nextViews) {
        if (nextViews != null) {
            for (View v : nextViews) {
                if (v.isEnabled() != enable) {
                    InputUtils.enableView(v, enable);
                }
            }
        }
    }

    private boolean checkQACompleted() {
        boolean isComplete = mAppCompatSpinnerQuestion[0].getSelectedItemPosition() > 0
                && mEditTextAnswer[0].length() > 0;

        if (mAppCompatSpinnerQuestion[1].getSelectedItemPosition() > 0) {
            isComplete &= mEditTextAnswer[1].length() > 0;
        } else {
            isComplete &= mEditTextAnswer[1].length() == 0;
        }

        if (mAppCompatSpinnerQuestion[2].getSelectedItemPosition() > 0) {
            isComplete &= mEditTextAnswer[2].length() > 0;
        } else {
            isComplete &= mEditTextAnswer[2].length() == 0;
        }
        return isComplete;
    }

    private void onSpinnerItemSelected(int position, int spinnerId, EditText editText) {
        mSpinnerAdapter.selectItem(position, spinnerId);
        if (position == 0) {
            editText.setText("");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.reg_question_spinner_question1:
                onSpinnerItemSelected(position, 0, mEditTextAnswer[0]);
                enableViews(position > 0 && mEditTextAnswer[0].length() > 0,
                        mAppCompatSpinnerQuestion[1],
                        mEditTextAnswer[1],
                        mButtonNext);
                break;
            case R.id.reg_question_spinner_question2:
                onSpinnerItemSelected(position, 1, mEditTextAnswer[1]);
                enableViews(position > 0 && mEditTextAnswer[1].length() > 0,
                        mAppCompatSpinnerQuestion[2],
                        mEditTextAnswer[2]);
                break;
            case R.id.reg_question_spinner_question3:
                onSpinnerItemSelected(position, 2, mEditTextAnswer[2]);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        enableViews(mAppCompatSpinnerQuestion[0].getSelectedItemPosition() > 0
                        && mEditTextAnswer[0].length() > 0,
                mAppCompatSpinnerQuestion[1],
                mEditTextAnswer[1],
                mButtonNext);
        enableViews(mAppCompatSpinnerQuestion[1].getSelectedItemPosition() > 0
                        && mEditTextAnswer[1].length() > 0,
                mAppCompatSpinnerQuestion[2],
                mEditTextAnswer[2]);

        if (mEditTextAnswer[0].length() <= 0) {
            clearQuestion(mAppCompatSpinnerQuestion[1], mEditTextAnswer[1]);
        }

        if (mEditTextAnswer[1].length() <= 0) {
            clearQuestion(mAppCompatSpinnerQuestion[2], mEditTextAnswer[2]);
        }
    }

    private void clearQuestion(AppCompatSpinner spinner, EditText editText) {
        if (spinner.getSelectedItemPosition() == 0 && editText.length() > 0) {
            editText.setText("");
        } else {
            spinner.setSelection(0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public class QuestionsAdapter extends ArrayAdapter<String> {
        private int[] selected = new int[3];
        List<String> values;

        public QuestionsAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            values = objects;
        }

        public void selectItem(int position, int spinnerId) {
            selected[spinnerId] = position;
        }

        @Override
        public boolean isEnabled(int position) {
            for (int i = 0; i < selected.length; i++) {
                if (selected[i] == position && selected[i] > 0) {
                    return i == mCurrentSpinner;
                }
            }
            return true;
        }

//        private void toggleCheckIcon(TextView tv, boolean selected) {
//            if (selected) {
//                tv.setCompoundDrawablePadding((int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP,
//                        10,
//                        getResources().getDisplayMetrics()));
//                tv.setCompoundDrawablesWithIntrinsicBounds(null,
//                        null,
//                        getResources().getDrawable(R.drawable.ic_check_pink, null),
//                        null);
//            } else {
//                tv.setCompoundDrawablePadding((int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP,
//                        0,
//                        getResources().getDisplayMetrics()));
//                tv.setCompoundDrawablesWithIntrinsicBounds(null,
//                        null,
//                        null,
//                        null);
//            }
//        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = (TextView) super.getDropDownView(position, null, parent);
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(values.get(position));
            tv.setEnabled(true);
            //toggleCheckIcon(tv, false);
            tv.setBackgroundColor(getResources().getColor(R.color.white));
            if (position > 0) {
                for (int i = 0; i < selected.length; i++) {
                    if (selected[i] == position) {
                        if (i != mCurrentSpinner) {
                            //toggleCheckIcon(tv, true);
                            tv.setEnabled(false);
                            tv.setBackgroundColor(getResources().getColor(R.color.questionSelected));
                        }
//                        } else {
//
//                        }
                    }
                }
            }
            return tv;
        }
    }
}
