package com.example.volcabularycards.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.CardPageTransformer;
import com.example.volcabularycards.ui.adapter.ReviewCardFragmentAdapter;
import com.example.volcabularycards.ui.fragment.ReviewCardFragment;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;

public class CardReviewActivity extends AppCompatActivity {

    private static final String TAG = "CardReviewActivity";

    private static WordViewModel wordViewModel;
    private ReviewCardFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private int lastPosition = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        
        viewPager = findViewById(R.id.viewPager);
        
        adapter = new ReviewCardFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        
        // 设置预加载页面数量，让两侧卡片可见
        viewPager.setOffscreenPageLimit(2);
        
        // 设置页面变换器，实现卡片效果
        viewPager.setPageTransformer(new CardPageTransformer(viewPager));
        

        
        Log.d(TAG, "ViewPager initialized,adapter created");

        // 使用 LiveData 观察总数


        // 直接观察所有单词的变化来更新 UI

        
        // 直接观察所有单词的变化来更新 UI
        wordViewModel.getAllWords().observe(this, words -> {

            int count = words != null ? words.size() : 0;
            Log.d(TAG, "Total count: " + count);
            if (count>0) {
                Log.d(TAG, "Words received, count: " + words.size());
                Log.d(TAG, "Updating adapter with " + words.size() + " words");
                adapter.submitList(new ArrayList<>(words));
            } else{
                addSampleWords();

                Log.d(TAG, "No words to display");
            }
        });
    }
    

    
    private void addSampleWords() {
        Log.d(TAG, "Adding sample words...");
        List<Word> sampleWords = new ArrayList<>();
        Word word1 = new Word();
        word1.setText("apple");
        word1.setMeaning("苹果");
        sampleWords.add(word1);
        
        Word word2 = new Word();
        word2.setText("banana");
        word2.setMeaning("香蕉");
        sampleWords.add(word2);
        
        Word word3 = new Word();
        word3.setText("orange");
        word3.setMeaning("橙子");
        sampleWords.add(word3);
        
        Word word4 = new Word();
        word4.setText("computer");
        word4.setMeaning("电脑");
        sampleWords.add(word4);
        
        Word word5 = new Word();
        word5.setText("language");
        word5.setMeaning("语言");
        sampleWords.add(word5);

        Word word6 = new Word();
        word6.setText("book");
        word6.setMeaning("书");
        sampleWords.add(word6);

        Word word7 = new Word();
        word7.setText("phone");
        word7.setMeaning("手机");
        sampleWords.add(word7);

        Word word8 = new Word();
        word8.setText("car");
        word8.setMeaning("汽车");
        sampleWords.add(word8);

        Word word9 = new Word();
        word9.setText("house");
        word9.setMeaning("房子");
        sampleWords.add(word9);

        Word word10 = new Word();
        word10.setText("tree");
        word10.setMeaning("树");
        sampleWords.add(word10);
        
        wordViewModel.insertAll(sampleWords);
        Log.d(TAG, "Sample words inserted, count: " + sampleWords.size());
    }
}
