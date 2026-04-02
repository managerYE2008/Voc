package com.example.volcabularycards.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.CardReviewActivity;
import com.example.volcabularycards.ui.adapter.ReviewCardFragmentAdapter;

import org.jspecify.annotations.NonNull;

public class ReviewCardFragment extends Fragment {

    private static final String TAG = "CardReviewActivity_fragment";


    private LiveData<Word> word;
    private CardView cardView;
    private TextView wordTextView;
    private TextView meaningTextView;
    private TextView annotationTextView;
    private ImageView annotationImageView;
    
    // 0: back, 1: front
    private int cardState = 0;
    
    private ConstraintLayout FrontCard;
    private ConstraintLayout BackCard;
    
    private ConstraintLayout AnnotationsLayout;
    private ConstraintLayout TextLayout;
    
    private boolean isAnimating = false;
    private static final int ANIMATION_DURATION = 250;

    public static ReviewCardFragment newInstance(int position) {
        Log.d(TAG, "newInstance called");
        ReviewCardFragment fragment = new ReviewCardFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String imagePath = getArguments().getString("image_path");
            if (imagePath != null && !imagePath.isEmpty()) {
                setAnnotationImage(imagePath);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_review_card,container,false);
        cardView=view.findViewById(R.id.edit_word_view);

        Log.d(TAG, "onCreateView called");

        View cardFront = view.findViewById(R.id.card_front);
        wordTextView=cardFront.findViewById(R.id.word);
        meaningTextView=cardFront.findViewById(R.id.meaning);
        
        FrontCard = view.findViewById(R.id.card_front);
        BackCard = view.findViewById(R.id.card_back);
        
        // 从 include 布局的根节点获取子视图
        AnnotationsLayout = FrontCard.findViewById(R.id.Annotations);
        TextLayout = FrontCard.findViewById(R.id.TextView);
        
        annotationTextView = AnnotationsLayout.findViewById(R.id.annotation_text);
        annotationImageView = AnnotationsLayout.findViewById(R.id.annotation_image);
        
        Log.d(TAG, "AnnotationsLayout: " + (AnnotationsLayout != null ? "not null" : "null"));
        Log.d(TAG, "TextLayout: " + (TextLayout != null ? "not null" : "null"));

        BackCard.setVisibility(View.VISIBLE);
        FrontCard.setVisibility(View.GONE);
        cardState = 0;
        
        if(getArguments()!=null){
            int position = getArguments().getInt("position");
            
            // 通过 Activity 获取 Adapter，然后获取 Word 的 LiveData
            if (getActivity() != null) {
                CardReviewActivity activity = (CardReviewActivity) getActivity();
                ReviewCardFragmentAdapter adapter = activity.getAdapter();
                if (adapter != null) {
                    LiveData<Word> wordLiveData = adapter.getWord(position);
                    if (wordLiveData != null) {
                        wordLiveData.observe(getViewLifecycleOwner(), word -> {
                            if (word != null) {
                                updateWordUI(word);
                            }
                        });
                    }
                }
            }
            
            String wordText = getArguments().getString("word");
            String wordMeaning = getArguments().getString("meaning");
            String annotationText = getArguments().getString("annotation");
            String imagePath = getArguments().getString("image_path");
            
            if (wordTextView != null && wordText != null) {
                wordTextView.setText(wordText);
            }
            if (meaningTextView != null && wordMeaning != null) {
                meaningTextView.setText(wordMeaning);
            }
            if (annotationTextView != null && annotationText != null) {
                annotationTextView.setText(annotationText);
            }
            if (imagePath != null && !imagePath.isEmpty()) {
                setAnnotationImage(imagePath);
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
    
    private void updateWordUI(Word word) {
        if (wordTextView != null && word.getText() != null) {
            wordTextView.setText(word.getText());
        }
        if (meaningTextView != null && word.getMeaning() != null) {
            meaningTextView.setText(word.getMeaning());
        }
        if (annotationTextView != null && word.getAnnotation() != null) {
            annotationTextView.setText(word.getAnnotation());
        }
        else if(word.getAnnotation()==null){
            annotationTextView.setText("sth random");
        }
        if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
            setAnnotationImage(word.getImagePath());
        }
    }
    
    public void setAnnotationImage(int resourceId) {
        if (annotationImageView != null) {
            annotationImageView.setImageResource(resourceId);
        }
    }
    
    public void setAnnotationImage(String imagePath) {
        if (annotationImageView != null && imagePath != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    annotationImageView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load image from path: " + imagePath, e);
            }
        }
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
