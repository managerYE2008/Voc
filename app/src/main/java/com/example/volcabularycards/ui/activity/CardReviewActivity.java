package com.example.volcabularycards.ui.activity;

import android.os.Environment;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.CardPageTransformer;
import com.example.volcabularycards.ui.adapter.ReviewCardFragmentAdapter;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;

public class CardReviewActivity extends AppCompatActivity {

    private static final String TAG = "CardReviewActivity";

    private static WordViewModel wordViewModel;
    private ReviewCardFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private TextView tvEmptyMessage;
    private int lastPosition = 0;
    private Word currentWord;

    private Button btnQuit;
    private Button btnEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        
        viewPager = findViewById(R.id.viewPager);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        
        adapter = new ReviewCardFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        
        // 设置预加载页面数量，让两侧卡片可见
        viewPager.setOffscreenPageLimit(2);
        
        // 设置页面变换器，实现卡片效果
        viewPager.setPageTransformer(new CardPageTransformer(viewPager));

        // 初始化按钮
        btnQuit = findViewById(R.id.btn_quit);
        btnEdit = findViewById(R.id.btn_edit);

        // 设置 Quit 按钮点击事件：返回 MainActivity
        btnQuit.setOnClickListener(v -> {
            finish();
        });

        // 设置 Edit 按钮点击事件：跳转到 EditWordActivity
        btnEdit.setOnClickListener(v -> {
            if (currentWord != null) {
                String currentImagePath = currentWord.getImagePath();
                
                // 检查图片路径是否有效
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    java.io.File imageFile = new java.io.File(currentImagePath);
                    if (!imageFile.exists()) {
                        android.util.Log.w(TAG, "Image file does not exist: " + currentImagePath + 
                                          ", clearing path for word: " + currentWord.getText());
                        currentImagePath = null; // 清除无效路径
                    }
                }
                
                Log.d(TAG, "Editing word: id=" + currentWord.getId() + 
                      ", text=" + currentWord.getText() + 
                      ", annotation=" + currentWord.getAnnotation() +
                      ", image_path=" + currentImagePath);
                
                Intent intent = new Intent(this, EditWordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("word_id", currentWord.getId());
                bundle.putString("word_text", currentWord.getText());
                bundle.putString("word_meaning", currentWord.getMeaning());
                bundle.putString("word_annotation", currentWord.getAnnotation());
                bundle.putString("image_path", currentImagePath);
                bundle.putBoolean("is_adding_word", false);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                Log.e(TAG, "currentWord is null when clicking Edit button!");
            }
        });

        Log.d(TAG, "ViewPager initialized,adapter created");
        


        // 观察所有单词的变化来更新 UI
        wordViewModel.getReviewWords().observe(this, words -> {
            int count = words != null ? words.size() : 0;
            Log.d(TAG, "All words count: " + count);
            
            if (count > 0) {
                // 隐藏空状态提示
                tvEmptyMessage.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                
                // 将 List<Word> 转换为 List<LiveData<Word>>
                List<LiveData<Word>> wordLiveList = new ArrayList<>();
                for (Word word : words) {
                    wordLiveList.add(wordViewModel.getWordById(word.getId()));
                }
                
                adapter.submitList(wordLiveList);
                Log.d(TAG, "Adapter updated with " + wordLiveList.size() + " words");
                
                // 注册页面变化监听
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        lastPosition = position;
                        Log.d(TAG, "Page changed to position: " + position);
                        
                        // 获取并观察当前位置的单词
                        if (position < wordLiveList.size()) {
                            wordLiveList.get(position).observe(CardReviewActivity.this, word -> {
                                currentWord = word;
                                Log.d(TAG, "Current word at position " + position + ": " + (word != null ? word.getText() : "null"));
                            });
                        }
                    }
                });
                
                // 初始化当前显示的单词（使用 getCurrentItem 获取实际显示的位置）
                int currentPosition = viewPager.getCurrentItem();
                if (currentPosition < wordLiveList.size()) {
                    wordLiveList.get(currentPosition).observe(this, word -> {
                        currentWord = word;
                        Log.d(TAG, "Initial word at position " + currentPosition + ": " + (word != null ? word.getText() : "null"));
                    });
                }
            } else{
                // 使用 LiveData 观察总单词数，避免在主线程访问数据库
                wordViewModel.getTotalCountLive().observe(CardReviewActivity.this, totalCount -> {
                    if (totalCount == 0 || totalCount == null) {
                        Log.d(TAG, "No words found, adding sample words...");
                        addSampleWords();
                    } else {
                        Log.d(TAG, "No words found, but total count is " + totalCount);
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当 Activity 恢复时，强制刷新数据

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 当从 EditWordActivity 返回且数据已更改时，刷新数据

        }
    }
    
    private void addSampleWords() {
        Log.d(TAG, "Adding sample words...");
        List<Word> sampleWords = new ArrayList<>();

        // 为第一个词 Voc 添加图片
        Word word0 = new Word();
        word0.setText("Voc");
        word0.setMeaning("全世界最好的应用");
        word0.setAnnotation("不是 woc 是 Voc");
        word0.setLearning(true);

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

    /**
     * 将 drawable 资源复制到应用私有目录，并返回文件路径
     */
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
    
    public ReviewCardFragmentAdapter getAdapter() {
        return adapter;
    }
    
    public Word getCurrentWord() {
        return currentWord;
    }
    
    public static WordViewModel getWordViewModel() {
        return wordViewModel;
    }
}
