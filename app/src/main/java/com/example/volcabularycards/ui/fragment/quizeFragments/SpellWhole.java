package com.example.volcabularycards.ui.fragment.quizeFragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.QuizActivity;
import com.example.volcabularycards.ui.adapter.QuizFragmentAdapter;

public class SpellWhole extends QuizBaseFragment{
    private static final String TAG = "SpellWhole";
    
    private EditText wordSpellInput;
    private TextView wordSpellMeaning;
    private Button btnSubmit;
    
    private String fullWord;
    private boolean isAnswered = false;
    private boolean isCorrect = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setCardFront(R.layout.spell_whole);
        
        if (quizCardFront == null) {
            Log.e(TAG, "quizCardFront is null!");
            return view;
        }
        
        wordSpellInput = quizCardFront.findViewById(R.id.word_spell_input);
        wordSpellMeaning = quizCardFront.findViewById(R.id.word_spell_meaning);
        wordSpellInput.setTextColor(getResources().getColor(android.R.color.holo_blue_light, null));
        btnSubmit = quizCardFront.findViewById(R.id.btn_submit);
        difficulty = 0.4f;
        
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
        if (wordSpellMeaning == null) {
            return;
        }
        
        fullWord = wordValue.getText();
        String meaning = wordValue.getMeaning();
        
        wordSpellMeaning.setText(meaning);
        
        if (wordSpellInput != null) {
            wordSpellInput.setText("");
            wordSpellInput.setEnabled(true);
        }

    }
    
    private void handleSubmit() {
        if (!isAnswered) {
            if (wordSpellInput == null || fullWord == null) {
                return;
            }
            
            String userInput = wordSpellInput.getText().toString().trim();
            
            if (userInput.isEmpty()) {
                Toast.makeText(getContext(), "请输入单词", Toast.LENGTH_SHORT).show();
                return;
            }
            
            isAnswered = true;
            isCorrect = userInput.equalsIgnoreCase(fullWord);
            
            wordSpellInput.setEnabled(false);
            
            if (isCorrect) {
                wordSpellInput.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                Toast.makeText(getContext(), "✓ 正确！", Toast.LENGTH_SHORT).show();
            } else {
                wordSpellInput.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                wordSpellInput.setText(fullWord);
                Toast.makeText(getContext(), "✗ 正确答案: " + fullWord, Toast.LENGTH_LONG).show();
            }
            
            OnQuized(isCorrect ? difficulty : 0.01f);
        }
    }
}
