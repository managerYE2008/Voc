package com.example.volcabularycards.ui.fragment.quizeFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.ReviewScheduler;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

public class QuizBaseFragment extends Fragment {
    protected float difficulty = 0;
    protected WordViewModel wordViewModel;
    protected ReviewScheduler reviewScheduler;
    protected LiveData<Word> word;
    protected FrameLayout cardFlipper;
    protected View quizCardFront;
    protected View quizCardBack;

    protected Button btnSubmit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_quiz, container, false);
        cardFlipper = view.findViewById(R.id.cardFlipper);
        quizCardBack = view.findViewById(R.id.card_back);
        return view;
    }

    protected void OnQuized(float difficulty){
        Log.d("QuizBaseFragment", "OnQuized: difficulty="+difficulty);

        reviewScheduler.WordReviewed(word, difficulty);
    }

    public void setWord(LiveData<Word> word){
        this.word = word;
    }

    protected void setCardFront(int layoutResId){
        if (cardFlipper == null) return;

        if (quizCardFront != null) {
            cardFlipper.removeView(quizCardFront);
        }

        LayoutInflater inflater = getLayoutInflater();
        quizCardFront = inflater.inflate(layoutResId, cardFlipper, false);
        quizCardFront.setVisibility(View.VISIBLE);
        Log.d("QuizBaseFragment", "setCardFront: "+getResources().getResourceEntryName(layoutResId));

        cardFlipper.addView(quizCardFront);
    }
    
    public void showFront() {
        if (quizCardBack != null) quizCardBack.setVisibility(View.GONE);
        if (quizCardFront != null) quizCardFront.setVisibility(View.VISIBLE);
        //Log.d("QuizBaseFragment", "showFront() called");
    }
    
    public void showBack() {
        if (quizCardBack != null) quizCardBack.setVisibility(View.VISIBLE);
        if (quizCardFront != null) quizCardFront.setVisibility(View.GONE);
        //Log.d("QuizBaseFragment", "showBack() called");
    }
    
    public View getQuizCardFront() {
        return quizCardFront;
    }
    
    public View getQuizCardBack() {
        return quizCardBack;
    }
}
