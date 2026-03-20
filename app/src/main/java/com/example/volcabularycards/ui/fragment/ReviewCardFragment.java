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
    
    // 0: back, 1: front
    private int cardState = 0;
    
    private ConstraintLayout FrontCard;
    private ConstraintLayout BackCard;
    
    private ConstraintLayout AnnotationsLayout;
    private ConstraintLayout TextLayout;
    
    private boolean isAnimating = false;
    private static final int ANIMATION_DURATION = 250;

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
        
        // 从 include 布局的根节点获取子视图
        AnnotationsLayout = FrontCard.findViewById(R.id.Annotations);
        TextLayout = FrontCard.findViewById(R.id.TextView);
        
        Log.d(TAG, "AnnotationsLayout: " + (AnnotationsLayout != null ? "not null" : "null"));
        Log.d(TAG, "TextLayout: " + (TextLayout != null ? "not null" : "null"));

        BackCard.setVisibility(View.VISIBLE);
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
                Log.d(TAG, "onClick called, cardState:"+cardState);
                if (cardState == 1) {
                    toggleAnnotationWithAnimation();
                } else if (cardState == 0) {
                    switchFromBack();
                }
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

            cardState = 1;
        }
    }
    
    private void toggleAnnotationWithAnimation() {
        Log.d(TAG, "toggleAnnotationWithAnimation called");
        if (isAnimating) return;
        isAnimating = true;
        
        final boolean showAnnotation = AnnotationsLayout.getVisibility() == View.GONE;
        
        if (showAnnotation) {
            // 从 TextView 切换到 Annotations
            TextLayout.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.d(TAG, "TextLayout animation ended");

                        TextLayout.setVisibility(View.GONE);
                        TextLayout.setAlpha(1f);
                        
                        AnnotationsLayout.setVisibility(View.VISIBLE);
                        AnnotationsLayout.setAlpha(0f);
                        AnnotationsLayout.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isAnimating = false;
                                }
                            });
                    }
                });
        } else {
            // 从 Annotations 切换到 TextView
            AnnotationsLayout.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        AnnotationsLayout.setVisibility(View.GONE);
                        AnnotationsLayout.setAlpha(1f);
                        
                        TextLayout.setVisibility(View.VISIBLE);
                        TextLayout.setAlpha(0f);
                        TextLayout.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isAnimating = false;
                                }
                            });
                    }
                });
        }
    }
}
