package com.example.volcabularycards.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.ReviewScheduler;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.CardPageTransformer;
import com.example.volcabularycards.ui.adapter.QuizFragmentAdapter;
import com.example.volcabularycards.ui.adapter.QuizPageTransformer;
import com.example.volcabularycards.ui.fragment.quizeFragments.QuizBaseFragment;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private WordViewModel wordViewModel;
    private QuizFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private TextView tvEmptyMessage;
    private int lastPosition = 0;
    private ViewPager2.OnPageChangeCallback viewPagerCallback;
    private boolean isInitialDataLoaded = false;
    private int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        
        ReviewScheduler.init(wordViewModel);

        viewPager = findViewById(R.id.quiz_viewPager);
        tvEmptyMessage = findViewById(R.id.quiz_tv_empty_message);

        adapter = new QuizFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(new QuizPageTransformer(viewPager));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            amount = bundle.getInt("quiz_words_count");
            Log.d(TAG, "Amount: " + amount);

        }

        wordViewModel.getReviewWordsLive().observe(this, words -> {
            if (isInitialDataLoaded) {
                return;
            }
            
            Log.d(TAG, "ReviewWordsLive updated with " + (words != null ? words.size() : 0) + " words.");

            List<Word> reviewWords = ReviewScheduler.getReviewWords(words,amount);
            Log.d(TAG, "ReviewScheduler returned " + (reviewWords != null ? reviewWords.size() : 0) + " words");
            updateUI(reviewWords);
            
            isInitialDataLoaded = true;
            wordViewModel.getReviewWordsLive().removeObservers(this);
            Log.d(TAG, "Observer removed, initial data loaded");
        });

        viewPagerCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                lastPosition = position;
                QuizBaseFragment fragment = adapter.getFragment(position);
                fragment.showFront();
                if(position>0)adapter.getFragment(position-1).showBack();
                if(position<adapter.getItemCount()-1)adapter.getFragment(position+1).showBack();
                Log.d(TAG, "Page changed to position: " + position);
            }
        };
        viewPager.registerOnPageChangeCallback(viewPagerCallback);
    }

    private void updateUI(List<Word> words) {
        Log.d(TAG, "updateUI called with " + (words != null ? words.size() : 0) + " words.");
        if (words != null && !words.isEmpty()) {
            Log.d(TAG, "Updating UI with " + words.size() + " words.");
            tvEmptyMessage.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);

            List<LiveData<Word>> liveDataList = new ArrayList<>();
            for (Word word : words) {
                MutableLiveData<Word> liveWord = new MutableLiveData<>(word);
                liveDataList.add(liveWord);
                //Log.d(TAG, "Created LiveData for word: " + word.getText());
            }

            Log.d(TAG, "Calling adapter.submitList with " + liveDataList.size() + " items");
            adapter.submitList(liveDataList);
        } else {
            Log.d(TAG, "No words found, showing empty view.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPagerCallback != null) {
            viewPager.unregisterOnPageChangeCallback(viewPagerCallback);
        }
    }

    public QuizFragmentAdapter getAdapter() {
        return adapter;
    }
}
