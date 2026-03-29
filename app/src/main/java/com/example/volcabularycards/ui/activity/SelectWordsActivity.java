package com.example.volcabularycards.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.CardPageTransformer;
import com.example.volcabularycards.ui.adapter.SelectWordFragmentAdapter;
import com.example.volcabularycards.ui.adapter.SelectWordPageTransformer;
import com.example.volcabularycards.ui.fragment.SelectWordFragment;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SelectWordsActivity extends AppCompatActivity {

    private static final String TAG = "SelectWordsActivity";
    private static WordViewModel wordViewModel;

    private SelectWordFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private TextView tvEmptyMessage;
    private boolean hasInitializedList = false;

    private Button btnQuit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_words);

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        viewPager = findViewById(R.id.viewPager);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        adapter = new SelectWordFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        // 设置预加载页面数量，让两侧卡片可见
        viewPager.setOffscreenPageLimit(2);

        viewPager.setPageTransformer(new SelectWordPageTransformer(viewPager));

        // 初始化按钮
        btnQuit = findViewById(R.id.btn_quit_word_select);
        btnQuit.setOnClickListener(view -> finish());

        Log.d(TAG, "ViewPager initialized, adapter created");
        
        // 使用 Observer 监听数据变化
        wordViewModel.getNotLearnedWordsLive().observe(this, words -> {
            int count = words != null ? words.size() : 0;
            Log.d(TAG, "Not learned words count: " + count);
            
            if (count > 0) {
                // 只在第一次初始化列表
                if (!hasInitializedList) {
                    List<LiveData<Word>> wordLiveList = new ArrayList<>();
                    for (Word word : words) {
                        wordLiveList.add(wordViewModel.getWordById(word.getId()));
                    }
                    
                    adapter.submitList(wordLiveList);
                    Log.d(TAG, "Adapter updated with " + wordLiveList.size() + " words");
                    
                    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            Log.d(TAG, "Page changed to position: " + position);
                            
                            // 通知当前选中的 Fragment
                            SelectWordFragment currentFragment = adapter.getFragment(position);
                            if (currentFragment != null) {
                                currentFragment.onPageSelected();
                            }
                        }
                    });
                    
                    // 使用延迟确保 Fragment 已经创建

                    
                    hasInitializedList = true;
                }
                
                tvEmptyMessage.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
            } else {
                int totalCount = wordViewModel.getTotalCount();
                Log.d(TAG, "No not-learned words found. Total count: " + totalCount);
                
                if (totalCount == 0) {
                    Log.d(TAG, "Database is empty, adding sample words...");
                    addSampleWords();
                } else {
                    Log.d(TAG, "Database has words but no not-learned words. Showing empty view.");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * 重新加载初始单词列表（用于添加样本词后）
     */
    private void addSampleWords() {
        Log.d(TAG, "Adding sample words...");
        List<Word> sampleWords = new ArrayList<>();

        // 为第一个词 Voc 添加图片
        Word word0 = new Word();
        word0.setText("Voc");
        word0.setMeaning("全世界最好的应用");
        word0.setAnnotation("不是woc是Voc");
        word0.setLearning( true);

        // 复制 drawable 资源到内部存储并获取路径
        String imagePath = copyImageFromDrawableToInternalStorage(R.drawable.the_creator);
        word0.setImagePath(imagePath);

        sampleWords.add(word0);

        // 后续单词保持不变...
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
    private String copyImageFromDrawableToInternalStorage(int resourceId) {
        String fileName = "sample_image_" + resourceId + ".png";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try {
            // 获取 Drawable 并转换为 BitmapDrawable
            android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(this, resourceId);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();

                // 将 Bitmap 保存到文件
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                }
            } else {
                // 如果不是 BitmapDrawable（如 VectorDrawable），使用 Canvas 绘制到 bitmap
                int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 100;
                int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 100;
                android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                // 将 Bitmap 保存到文件
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                }
            }

            return file.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Failed to copy image from drawable", e);
            return "";
        }
    }

    
    public SelectWordFragmentAdapter getAdapter() {
        return adapter;
    }
    

    
    public static WordViewModel getWordViewModel() {
        return wordViewModel;
    }
}
