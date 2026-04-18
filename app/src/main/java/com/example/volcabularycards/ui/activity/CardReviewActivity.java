package com.example.volcabularycards.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.ReviewScheduler;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.CardPageTransformer;
import com.example.volcabularycards.ui.adapter.ReviewCardFragmentAdapter;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CardReviewActivity extends AppCompatActivity {

    private static final String TAG = "CardReviewActivity";

    // 移除 static 修饰符，让生命周期跟随 Activity
    private WordViewModel wordViewModel;

    private ReviewCardFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private TextView tvEmptyMessage;
    private int lastPosition = 0;

    private Button btnQuit;
    private Button btnEdit;
    private ViewPager2.OnPageChangeCallback viewPagerCallback;
    private boolean isInitialDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // 初始化 ViewModel
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 初始化 ReviewScheduler
        ReviewScheduler.init(wordViewModel);

        // 绑定 View
        viewPager = findViewById(R.id.viewPager);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        // 初始化 Adapter
        adapter = new ReviewCardFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        // 配置 ViewPager
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(new CardPageTransformer(viewPager));

        // 初始化按钮
        btnQuit = findViewById(R.id.btn_quit);
        btnEdit = findViewById(R.id.btn_edit);

        // Quit 按钮
        btnQuit.setOnClickListener(v -> finish());

        // Edit 按钮：实时获取当前单词
        btnEdit.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            // 直接从 Adapter 获取当前位置的 LiveData
            LiveData<Word> currentWordLiveData = adapter.getWord(position);

            if (currentWordLiveData != null && currentWordLiveData.getValue() != null) {
                Word currentWord = currentWordLiveData.getValue();
                handleEditWord(currentWord);
            } else {
                Log.e(TAG, "Current word is null at position: " + position);
            }
        });

        // 【核心修复】只在这里注册一次 Observer
        // 这样无论数据怎么变，Observer 只有一个，不会内存泄漏
        wordViewModel.getReviewWordsLive().observe(this, words->{
            if(isInitialDataLoaded){
                return;
            }
            words= ReviewScheduler.getReviewWords(words);
            updateUI(words);
            
            if (words != null && !words.isEmpty()) {
                LiveData<Word> firstWord = adapter.getWord(0);
                if (firstWord != null && firstWord.getValue() != null) {
                    ReviewScheduler.WordReviewed(firstWord, 0.001f);
                    Log.d(TAG, "Reviewed first word: " + firstWord.getValue().getText());
                }
            }
            
            isInitialDataLoaded = true;
            wordViewModel.getReviewWordsLive().removeObservers(this);

        });

        // 注册 Pager 回调
        viewPagerCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                lastPosition = position;
                Log.d(TAG, "Page changed to position: " + position);
                ReviewScheduler.WordReviewed(adapter.getWord(position),0.001f);
            }
        };
        viewPager.registerOnPageChangeCallback(viewPagerCallback);
    }

    /**
     * 纯粹的 UI 更新逻辑，不包含 observe
     */
    private void updateUI(List<Word> words) {
        if (words != null && !words.isEmpty()) {
            Log.d(TAG, "Updating UI with " + words.size() + " words.");
            tvEmptyMessage.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);

            // 【性能优化】不再循环调用 getWordById 查库
            // 直接将已有的 Word 对象包装成 LiveData，避免 N+1 查询问题
            List<LiveData<Word>> liveDataList = new ArrayList<>();
            for (Word word : words) {
                liveDataList.add(new MutableLiveData<>(word));
            }

            adapter.submitList(liveDataList);
        } else {
            Log.d(TAG, "No words found, showing empty view.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        }
    }

    // 【修复】移除 onResume 中的 updateUI
    // LiveData 会自动感知生命周期，数据变化会自动回调，不需要手动刷新
    @Override
    protected void onResume() {
        super.onResume();
        // 不需要做任何事
    }

    // 【修复】移除 onActivityResult 中的 updateUI
    // 因为编辑完数据后，ViewModel 的数据库变了，LiveData 会自动触发 onActive -> updateUI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 不需要做任何事
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPagerCallback != null) {
            viewPager.unregisterOnPageChangeCallback(viewPagerCallback);
        }
    }

    private void handleEditWord(Word word) {
        String currentImagePath = word.getImagePath();

        // 检查图片有效性
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            File imageFile = new File(currentImagePath);
            if (!imageFile.exists()) {
                Log.w(TAG, "Image file missing: " + currentImagePath);
                currentImagePath = null;
            }
        }

        Intent intent = new Intent(this, EditWordActivity.class);
        intent.putExtra("word_id", word.getId());
        intent.putExtra("is_adding_word", false);
        startActivity(intent);
    }

    // 移除 addSampleWords 和 copyImage... 方法，这些应该在 MainActivity 处理
    public ReviewCardFragmentAdapter getAdapter() {
        return adapter;
    }
}