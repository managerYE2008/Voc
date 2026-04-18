package com.example.volcabularycards.ui.fragment.quizeFragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.QuizActivity;
import com.example.volcabularycards.ui.adapter.QuizFragmentAdapter;

import java.util.Random;

public class SpellPart extends QuizBaseFragment{
    private static final String TAG = "SpellPart";
    
    private LinearLayout wordSpellContainer;
    private TextView wordSpellMeaning;
    private Button btnSubmit;
    
    private String fullWord;
    private int hiddenStartIndex;
    private int hiddenLength;
    private boolean isCompleted = false;
    private String userAnswer = "";
    private TextView[] charViews;
    private boolean isCorrect = false;

    //difficulty=0.3
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);
        setCardFront(R.layout.spell_part);

        if (quizCardFront == null) {
            Log.e(TAG, "quizCardFront is null!");
            return view;
        }
        
        wordSpellContainer = quizCardFront.findViewById(R.id.word_spell_container);
        wordSpellMeaning = quizCardFront.findViewById(R.id.word_spell_meaning);
        btnSubmit = quizCardFront.findViewById(R.id.btn_submit);
        difficulty = 0.3f;
        if(getArguments() != null){
            int position = getArguments().getInt("position");
            if(getActivity() != null){
                QuizActivity activity = (QuizActivity) getActivity();
                QuizFragmentAdapter adapter = activity.getAdapter();
                
                if(adapter != null){
                    word = adapter.getWord(position);
                    if (word != null) {
                        word.observe(getViewLifecycleOwner(), wordValue -> {
                            if(wordValue != null){
                                updateWordUI(wordValue);
                            }
                        });
                    }
                }
            }
        }
        
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> handleSubmit());
        }
        
        return view;
    }
    
    private void updateWordUI(Word wordValue) {
        if (wordSpellContainer == null || wordSpellMeaning == null) {
            return;
        }
        
        fullWord = wordValue.getText();
        String meaning = wordValue.getMeaning();
        
        wordSpellMeaning.setText(meaning);
        
        wordSpellContainer.removeAllViews();
        
        calculateHiddenSection(fullWord);
        
        createWordDisplay();
        
        isCompleted = false;
        userAnswer = "";
    }
    
    private void calculateHiddenSection(String word) {
        if (word == null || word.length() < 3) {
            hiddenStartIndex = 0;
            hiddenLength = word != null ? word.length() : 0;
            return;
        }
        
        int totalLength = word.length();
        int hiddenCount = (int)(Math.max(1.0f, totalLength / 3.0f)+0.7f);
        
        Random random = new Random();
        hiddenStartIndex = random.nextInt(totalLength - hiddenCount + 1);
        hiddenLength = hiddenCount;
        
        Log.d(TAG, "Word: " + word + ", Hidden start: " + hiddenStartIndex + ", Length: " + hiddenLength);
    }
    
    private void createWordDisplay() {
        if (fullWord == null || fullWord.isEmpty()) {
            return;
        }
        
        wordSpellContainer.removeAllViews();
        charViews = new TextView[fullWord.length()];
        
        for (int i = 0; i < fullWord.length(); i++) {
            char c = fullWord.charAt(i);
            
            TextView charView = new TextView(getContext());
            charView.setText(String.valueOf(c));
            charView.setTextSize(34);
            charView.setTypeface(null, android.graphics.Typeface.BOLD);
            charView.setPadding(4, 0, 4, 0);
            
            if (i >= hiddenStartIndex && i < hiddenStartIndex + hiddenLength && !isCompleted) {
                charView.setText("_");
                charView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(2, 0, 2, 0);
                charView.setLayoutParams(params);
                
                charView.setClickable(true);
                charView.setFocusable(true);

                charView.setOnClickListener(v -> showInputDialog());
            } else if (isCompleted && i >= hiddenStartIndex && i < hiddenStartIndex + hiddenLength) {
                charView.setText(userAnswer.charAt(i - hiddenStartIndex) + "");
                charView.setTextColor(getResources().getColor(android.R.color.black, null));
            } else {
                charView.setTextColor(getResources().getColor(android.R.color.black, null));
            }
            
            charViews[i] = charView;
            wordSpellContainer.addView(charView);
        }
    }
    
    private void showInputDialog() {
        if(isSubmitted){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("填写缺失的字母");
        
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("请输入 " + hiddenLength + " 个字母");
        input.setMaxLines(1);
        
        if (!userAnswer.isEmpty()) {
            input.setText(userAnswer);
        }
        
        builder.setView(input);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String answer = input.getText().toString().trim();
            
            if (answer.isEmpty()) {
                Toast.makeText(getContext(), "请输入答案", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (answer.length() != hiddenLength) {
                Toast.makeText(getContext(), 
                    "需要输入 " + hiddenLength + " 个字母，当前输入了 " + answer.length() + " 个", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            
            userAnswer = answer.toLowerCase();
            String correctAnswer = fullWord.substring(hiddenStartIndex, hiddenStartIndex + hiddenLength).toLowerCase();
            
            isCompleted = true;
            
            updateWordDisplayAfterInput(userAnswer.equals(correctAnswer));
            
            if (userAnswer.equals(correctAnswer)) {
                isCorrect = true;
            } else {
                isCorrect = false;
            }
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void updateWordDisplayAfterInput(boolean isCorrect) {
        wordSpellContainer.removeAllViews();
        
        for (int i = 0; i < fullWord.length(); i++) {
            char c = fullWord.charAt(i);
            
            TextView charView = charViews[i];
            charView.setText(String.valueOf(c));
            
            if (i >= hiddenStartIndex && i < hiddenStartIndex + hiddenLength) {
                charView.setText(userAnswer.charAt(i - hiddenStartIndex) + "");
                charView.setTextColor(getResources().getColor(android.R.color.holo_purple, null));
            } else {
                charView.setTextColor(getResources().getColor(android.R.color.black, null));
            }
            
            wordSpellContainer.addView(charView);
        }
        
        Log.d(TAG, "User input completed for word: " + fullWord + ", Correct: " + isCorrect);
    }
    
    private void handleSubmit() {
        if (!isCompleted) {
            Toast.makeText(getContext(), "请先填写缺失的字母", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Submit clicked but not completed yet");
            return;
        }
        if(!isCompleted){
            Toast.makeText(getContext(), "请先填写缺失的字母", Toast.LENGTH_SHORT).show();
            return;
        }
        wordSpellContainer.removeAllViews();
        for (int i = 0; i < fullWord.length(); i++) {
            char c = fullWord.charAt(i);

            TextView charView = charViews[i];
            charView.setText(String.valueOf(c));

            if (i >= hiddenStartIndex && i < hiddenStartIndex + hiddenLength) {
                charView.setText(userAnswer.charAt(i - hiddenStartIndex) + "");
                if (isCorrect) {
                    charView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                } else {
                    charView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                }
            } else {
                charView.setTextColor(getResources().getColor(android.R.color.black, null));
            }

            wordSpellContainer.addView(charView);
        }
        if(isCorrect){
            Toast.makeText(getContext(), "✓ 正确！", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getContext(), "✗ 正确答案: " + fullWord, Toast.LENGTH_LONG).show();
        }
        OnQuized(isCorrect? difficulty : 0.01f);
    }
}
