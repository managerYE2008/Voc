package com.example.volcabularycards.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class ReviewCardFragment extends Fragment {

    private static final String TAG = "CardReviewActivity_fragment";

    private CardView cardView;
    private TextView wordTextView;
    private TextView meaningTextView;
    
    // 0: back, 1: front, 2: annotation
    private int cardState = 0;
    private boolean isAnimating = false;
    
    private ConstraintLayout FrontCard;
    private ConstraintLayout BackCard;
    private ConstraintLayout AnnotationCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_review_card,container,false);
        cardView=view.findViewById(R.id.review_card_view);

        Log.d(TAG, "onCreateView called");

        View cardFront = view.findViewById(R.id.card_front);
        wordTextView=cardFront.findViewById(R.id.word);
        meaningTextView=cardFront.findViewById(R.id.meaning);
        
        FrontCard = view.findViewById(R.id.card_front);
        BackCard = view.findViewById(R.id.card_back);
        AnnotationCard = view.findViewById(R.id.card_annonation);

        BackCard.setVisibility(View.VISIBLE);
        AnnotationCard.setVisibility(View.GONE);
        FrontCard.setVisibility(View.GONE);
        cardState = 0;
        
        if(getArguments()!=null){
            String wordText = getArguments().getString("word");
            String wordMeaning = getArguments().getString("meaning");
            
            if (wordTextView != null && wordText != null) {
                wordTextView.setText(wordText);
            }
            if (meaningTextView != null && wordMeaning != null) {
                meaningTextView.setText(wordMeaning);
            }
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AnnotationCard.getVisibility() == View.VISIBLE){
                    AnnotationCard.setVisibility(View.GONE);
                    FrontCard.setVisibility(View.VISIBLE);
                }else{
                    FrontCard.setVisibility(View.GONE);
                    AnnotationCard.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "onClick called, cardState:"+cardState);
            }
        });
        return view;
    }

    public static ReviewCardFragment newInstance(Word word) {
        Log.d(TAG, "newInstance called");
        ReviewCardFragment fragment = new ReviewCardFragment();
        Bundle args = new Bundle();
        args.putString("word",word.getText());
        args.putString("meaning",word.getMeaning());
        fragment.setArguments(args);
        return fragment;
    }


    public void resetToBack() {
        if (getView() != null && !isAnimating) {
            FrontCard.setVisibility(View.GONE);
            FrontCard.setAlpha(1f);
            FrontCard.setTranslationY(0f);
            
            if (AnnotationCard != null) {
                AnnotationCard.setVisibility(View.GONE);
                AnnotationCard.setAlpha(1f);
                AnnotationCard.setTranslationY(0f);
            }
            
            BackCard.setVisibility(View.VISIBLE);
            BackCard.setAlpha(1f);
            BackCard.setTranslationY(0f);
            
            cardState = 0;
        }
    }
    
    public void switchFromBack(){
        if (getView() != null && !isAnimating && cardState == 0) {
            BackCard.setVisibility(View.GONE);
            FrontCard.setVisibility(View.VISIBLE);
            if (AnnotationCard != null) {
                AnnotationCard.setVisibility(View.GONE);
            }
            cardState = 1;
        }
    }
}
