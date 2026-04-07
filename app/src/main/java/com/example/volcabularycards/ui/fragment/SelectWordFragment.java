package com.example.volcabularycards.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.SelectWordsActivity;
import com.example.volcabularycards.ui.adapter.SelectWordFragmentAdapter;

import org.jspecify.annotations.NonNull;

public class SelectWordFragment extends Fragment {

    private static final String TAG = "SelectWordFragment";

    private LiveData<Word> word;
    private CardView cardView;
    private TextView wordTextView;
    private TextView meaningTextView;
    private ImageButton btnIsLearning;
    private boolean hasUpdatedMasteryLevel = false;

    public static SelectWordFragment newInstance(int position) {
        Log.d(TAG, "newInstance called");
        SelectWordFragment fragment = new SelectWordFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreateView(inflater,container,savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_select_word,container,false);
        cardView=view.findViewById(R.id.select_word_view);

        Log.d(TAG, "onCreateView called");

        View cardFront = view.findViewById(R.id.select_word_card_front);
        wordTextView=cardFront.findViewById(R.id.select_word_word);
        meaningTextView=cardFront.findViewById(R.id.select_word_meaning);
        btnIsLearning = cardFront.findViewById(R.id.btn_is_learning);
        
        if(getArguments()!=null){
            int position = getArguments().getInt("position");
            
            // 通过 Activity 获取 Adapter，然后获取 Word 的 LiveData
            if (getActivity() != null) {
                SelectWordsActivity activity = (SelectWordsActivity) getActivity();
                SelectWordFragmentAdapter adapter = activity.getAdapter();
                
                Log.d(TAG, "Adapter retrieved from activity");
                if (adapter != null) {
                    LiveData<Word> wordLiveData = adapter.getWord(position);
                    Log.d(TAG, "Word LiveData retrieved from adapter");
                    if (wordLiveData != null) {
                        word = wordLiveData;
                        wordLiveData.observe(getViewLifecycleOwner(), word -> {
                            if (word != null) {
                                updateWordUI();
                                updateLearningButton();
                                
                                // 如果是第一个 Fragment 且 masteryLevel 为 0，自动更新
                                if (position == 0 && !hasUpdatedMasteryLevel && word.getMasteryLevel() == 0) {
                                    updateMasteryLevelIfNeeded();
                                }
                            }
                        });
                    }

                }
            }
            
            String wordText = getArguments().getString("word");
            String wordMeaning = getArguments().getString("meaning");
            
            if (wordTextView != null && wordText != null) {
                wordTextView.setText(wordText);
            }
            if (meaningTextView != null && wordMeaning != null) {
                meaningTextView.setText(wordMeaning);
            }
        }

        // 设置学习按钮点击事件
        if (btnIsLearning != null) {
            btnIsLearning.setOnClickListener(v -> {
                if (word != null && word.getValue() != null) {
                    Word currentWord = word.getValue();
                    boolean newIsLearning = !currentWord.isLearning();
                    currentWord.setLearning(newIsLearning);
                    SelectWordsActivity activity = (SelectWordsActivity) getActivity();
                    
                    activity.getWordViewModel().update(currentWord);

                    
                    Log.d(TAG, "Learning status toggled for word: " + currentWord.getText() + ", new status: " + newIsLearning);
                }
            });
        }



        return view;
    }
    
    private void updateWordUI() {
        Word nonLiveWord=word.getValue();
        if (wordTextView != null && nonLiveWord.getText() != null) {
            wordTextView.setText(nonLiveWord.getText());
        }
        if (meaningTextView != null && nonLiveWord.getMeaning() != null) {
            meaningTextView.setText(nonLiveWord.getMeaning());
        }
    }
    
    private void updateLearningButton() {

        Word nonLiveWord=word.getValue();
        if (btnIsLearning != null) {
            if (nonLiveWord.isLearning()) {
                btnIsLearning.setColorFilter(0xFFFFD700); // 金色
            } else {
                btnIsLearning.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            }
        }
    }

    /**
     * 当 Fragment 被选中时调用（页面滑动到中间位置）
     */
    public void onPageSelected() {
        Log.d(TAG, "onPageSelected called for position " + getArguments().getInt("position", -1));
        updateMasteryLevelIfNeeded();
    }
    
    /**
     * 更新 masteryLevel（如果需要）
     */
    public void updateMasteryLevelIfNeeded() {
        if (word != null && word.getValue() != null && !hasUpdatedMasteryLevel) {
            Word currentWord = word.getValue();
            if (currentWord.getMasteryLevel() == 0) {
                Word updatedWord = new Word(currentWord.getText(), currentWord.getMeaning());
                updatedWord.setId(currentWord.getId());
                updatedWord.setLearning(currentWord.isLearning());
                updatedWord.setMasteryLevel(1);
                updatedWord.setLastReviewTime(currentWord.getLastReviewTime());
                updatedWord.setAnnotation(currentWord.getAnnotation());
                updatedWord.setImagePath(currentWord.getImagePath());
                SelectWordsActivity activity = (SelectWordsActivity) getActivity();
                activity.getWordViewModel().update(updatedWord);
                hasUpdatedMasteryLevel = true;
                Log.d(TAG, "Updated masteryLevel for word: " + currentWord.getText());
            }
        }
    }
    
    /**
     * 专门为第一个 Fragment 更新 masteryLevel
     */
    private void updateMasteryLevelForFirstFragment() {
        if (word != null && word.getValue() != null) {
            Word currentWord = word.getValue();
            if (currentWord.getMasteryLevel() == 0) {
                Word updatedWord = new Word(currentWord.getText(), currentWord.getMeaning());
                updatedWord.setId(currentWord.getId());
                updatedWord.setLearning(currentWord.isLearning());
                updatedWord.setMasteryLevel(1);
                updatedWord.setLastReviewTime(currentWord.getLastReviewTime());
                updatedWord.setAnnotation(currentWord.getAnnotation());
                updatedWord.setImagePath(currentWord.getImagePath());
                SelectWordsActivity activity = (SelectWordsActivity) getActivity();
                activity.getWordViewModel().update(updatedWord);
                hasUpdatedMasteryLevel = true;
                Log.d(TAG, "Auto-updated masteryLevel for first fragment: " + currentWord.getText());
            }
        }
    }
}
