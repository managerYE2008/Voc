package com.example.volcabularycards.ui.fragment.quizeFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.ReviewScheduler;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.QuizActivity;
import com.example.volcabularycards.ui.adapter.QuizFragmentAdapter;

import java.util.List;
import java.util.Random;

public class SelectEn extends QuizBaseFragment {
    private static final String TAG = "SelectEn";
    private TextView textView;
    private Button btnA, btnB, btnC, btnD;
    private int CorrectAnswer;
    private int selectedAnswer = -1;
    private boolean isSubmitted = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView: ");

        setCardFront(R.layout.select_en);
        difficulty=0.2f;

        if (quizCardFront == null) {
            Log.e(TAG, "quizCardFront is null!");
            return view;
        }

        textView = quizCardFront.findViewById(R.id.word_spell_meaning);
        btnSubmit=quizCardFront.findViewById(R.id.btn_submit);
        btnA=quizCardFront.findViewById(R.id.btn_selection1);
        btnB=quizCardFront.findViewById(R.id.btn_selection2);
        btnC=quizCardFront.findViewById(R.id.btn_selection3);
        btnD=quizCardFront.findViewById(R.id.btn_selection4);




        Log.d(TAG, "Views found - textView: " + (textView != null) +
                ", btnSubmit: " + (btnSubmit != null) +
                ", btnA: " + (btnA != null));

        if (btnA != null) btnA.setOnClickListener(v -> onSelected(0));
        if (btnB != null) btnB.setOnClickListener(v -> onSelected(1));
        if (btnC != null) btnC.setOnClickListener(v -> onSelected(2));
        if (btnD != null) btnD.setOnClickListener(v -> onSelected(3));


        if(getArguments()!=null){
            int position = getArguments().getInt("position");
            if(getActivity()!=null){

                QuizActivity activity = (QuizActivity) getActivity();
                QuizFragmentAdapter adapter = activity.getAdapter();

                if(adapter!=null){
                    word = adapter.getWord(position);
                    Log.d(TAG, "Word LiveData obtained for position: " + position + ", LiveData is null: " + (word == null));
                    if (word != null) {
                        Log.d(TAG, "Word current value: " + (word.getValue() != null ? word.getValue().getText() : "null"));
                        word.observe(getViewLifecycleOwner(), wordValue -> {
                            if(wordValue!=null){
                                Log.d(TAG, "Word received in observer: " + wordValue.getText() + ", mastery: " + wordValue.getMasteryLevel());
                                updateWordUI();

                                new Thread(() -> {
                                    List<Word> distractors = ReviewScheduler.getDistractors(wordValue, 3);
                                    requireActivity().runOnUiThread(() -> {
                                        if (distractors != null && distractors.size() >= 3) {
                                            setWords(distractors);
                                        } else {
                                            Log.w(TAG, "Not enough distractors: " + (distractors != null ? distractors.size() : 0));
                                        }
                                    });
                                }).start();
                            } else {
                                Log.e(TAG, "Word is null in observer");
                            }
                        });
                    } else {
                        Log.e(TAG, "Word LiveData is null for position: " + position);
                    }
                }
            }
        }
        showBack();
        Log.d(TAG, "showFront() called");

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                handleSubmit();
            });
        }





        return view;
    }

    private void handleSubmit() {
        Log.d(TAG, "handleSubmit called, isSubmitted: " + isSubmitted + ", selectedAnswer: " + selectedAnswer + ", CorrectAnswer: " + CorrectAnswer);

        if (isSubmitted) {
            Log.d(TAG, "Already submitted, returning");
            return;
        }

        if (selectedAnswer == -1) {
            Log.w(TAG, "No answer selected!");
            Toast.makeText(getContext(), "请选择一个答案", Toast.LENGTH_SHORT);

            return;
        }

        isSubmitted = true;
        boolean isCorrect = (selectedAnswer == CorrectAnswer);
        Log.d(TAG, "isCorrect: " + isCorrect);

        resetAllButtons();
        Log.d(TAG, "Buttons reset to normal");

        if (isCorrect) {
            Log.d(TAG, "Setting correct answer button " + CorrectAnswer + " to green");
            setButtonState(CorrectAnswer, R.drawable.btn_option_correct);
        } else {
            Log.d(TAG, "Setting wrong answer button " + selectedAnswer + " to red");
            Log.d(TAG, "Setting correct answer button " + CorrectAnswer + " to green");
            setButtonState(selectedAnswer, R.drawable.btn_option_wrong);
            setButtonState(CorrectAnswer, R.drawable.btn_option_correct);
        }

        Log.d(TAG, "Calling OnQuized with difficulty: " + (isCorrect ? difficulty : difficulty / 3));
        OnQuized(isCorrect ? difficulty : difficulty / 3);
    }

    private void onSelected(int index){
        if (isSubmitted) {
            return;
        }
        selectedAnswer=index;
        setButtonBackground(index);
    }

    private void setButtonBackground(int selectedIndex){
        resetAllButtons();

        switch (selectedIndex){
            case 0:
                btnA.setBackgroundResource(R.drawable.btn_option_selected);
                break;
            case 1:
                btnB.setBackgroundResource(R.drawable.btn_option_selected);
                break;
            case 2:
                btnC.setBackgroundResource(R.drawable.btn_option_selected);
                break;
            case 3:
                btnD.setBackgroundResource(R.drawable.btn_option_selected);
                break;
        }
    }

    private void resetAllButtons(){
        btnA.setBackgroundResource(R.drawable.btn_option_normal);
        btnB.setBackgroundResource(R.drawable.btn_option_normal);
        btnC.setBackgroundResource(R.drawable.btn_option_normal);
        btnD.setBackgroundResource(R.drawable.btn_option_normal);
    }

    private void setButtonState(int buttonIndex, int drawableResId){
        switch (buttonIndex){
            case 0:
                btnA.setBackgroundResource(drawableResId);
                break;
            case 1:
                btnB.setBackgroundResource(drawableResId);
                break;
            case 2:
                btnC.setBackgroundResource(drawableResId);
                break;
            case 3:
                btnD.setBackgroundResource(drawableResId);
                break;
        }
    }

    private void setWords(List<Word> distractors){
        if (distractors == null || distractors.size() < 3 || word.getValue() == null) {
            Log.e(TAG, "Invalid distractors or word data");
            return;
        }

        isSubmitted = false;
        Random random = new Random();
        int index = random.nextInt(4);
        switch (index){
            case 0:
                btnA.setText(word.getValue().getText());
                btnB.setText(distractors.get(0).getText());
                btnC.setText(distractors.get(1).getText());
                btnD.setText(distractors.get(2).getText());
                CorrectAnswer=0;
                break;
            case 1:
                btnA.setText(distractors.get(0).getText());
                btnB.setText(word.getValue().getText());
                btnC.setText(distractors.get(1).getText());
                btnD.setText(distractors.get(2).getText());
                CorrectAnswer=1;
                break;

            case 2:
                btnA.setText(distractors.get(0).getText());
                btnB.setText(distractors.get(1).getText());
                btnC.setText(word.getValue().getText());
                btnD.setText(distractors.get(2).getText());
                CorrectAnswer=2;
                break;

            case 3:
                btnA.setText(distractors.get(0).getText());
                btnB.setText(distractors.get(1).getText());
                btnC.setText(distractors.get(2).getText());
                btnD.setText(word.getValue().getText());
                CorrectAnswer=3;
                break;


        }
        resetAllButtons();
    }

    private void updateWordUI(){
        Log.d(TAG, "updateWordUI called");

        if (textView != null && word != null && word.getValue() != null) {
            String meaningText = word.getValue().getMeaning();
            float masteryLevel = word.getValue().getMasteryLevel();
            Log.d(TAG, "Setting meaning: '" + meaningText + "', mastery: " + masteryLevel);
            textView.setText(meaningText);



            Log.d(TAG, "UI updated successfully");
        } else {
            Log.e(TAG, "Cannot update UI - textView: " + (textView != null) +
                    ", word: " + (word != null) +
                    ", word.getValue(): " + (word != null ? word.getValue() != null : "N/A"));
        }

    }



}