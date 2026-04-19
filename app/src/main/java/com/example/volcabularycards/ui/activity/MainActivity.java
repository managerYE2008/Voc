package com.example.volcabularycards.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.ReviewScheduler;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.data.WordDatabase;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnImport;
    private Button btnSelectWords;
    private FloatingActionButton btnSearchWord;
    private Button btnQuiz;
    private WordViewModel wordViewModel;
    private SeekBar seekBar;
    private int quizWordsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate called");



        btnImport = findViewById(R.id.btn_import_excel);
        btnImport.setOnClickListener(v -> {
            // 跳转到 Excel 导入界面
            startImportActivity();
        });

        btnSelectWords = findViewById(R.id.btn_select_words);
        btnSelectWords.setOnClickListener(v -> {
            // 跳转到选择单词界面
            startSelectWordsActivity();
        });

        btnSearchWord = findViewById(R.id.btn_search_word);
        btnSearchWord.setOnClickListener(v -> {
            // 跳转到搜索单词界面
            startSearchWordActivity();
        });

        btnQuiz = findViewById(R.id.btn_quiz);
        btnQuiz.setOnClickListener(v -> {
            startQuizActivity();
        });
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(50);
        seekBar.setProgress(10);
        
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        Thread getOptimalQuizAmount = new Thread(() -> {
            List<Word> words=wordViewModel.getReviewWords();
            ReviewScheduler.init(wordViewModel);
            ReviewScheduler.getReviewWords(words);
            quizWordsCount = ReviewScheduler.getOptimalReviewAmount();
            try {
                Thread.sleep(500);
                int count = wordViewModel.getReviewTotalCount();
                Log.d("MainActivity", "Total words count: " + count);
                runOnUiThread(() -> {
                    if (count > 0) {
                        seekBar.setMax(count);
                        Log.d("MainActivity", "Total words count: " + count);
                        seekBar.setProgress(quizWordsCount);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        getOptimalQuizAmount.start();


        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quizWordsCount = progress;
                Log.d("MainActivity", "SeekBar progress changed: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("MainActivity", "SeekBar touch started");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int progress = seekBar.getProgress();
                if(progress>0){
                    Toast.makeText(MainActivity.this, "测验单词数量: " + progress, Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "SeekBar progress changed: " + progress);
                    btnQuiz.setText("开始测验");
                }
                else{
                    Toast.makeText(MainActivity.this, "就是看看", Toast.LENGTH_SHORT).show();
                    btnQuiz.setText("就是看看");
                }

            }
        });

        WordDatabase wordDatabase = WordDatabase.getInstance(this);
        if(wordDatabase.isFirstTime(this)){
            addSampleWords();
            WordDatabase.setIsFirstTime(this);
        }
        

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume called");
        Thread getOptimalQuizAmount = new Thread(() -> {
            List<Word> words=wordViewModel.getReviewWords();
            ReviewScheduler.init(wordViewModel);
            ReviewScheduler.getReviewWords(words);
            quizWordsCount = ReviewScheduler.getOptimalReviewAmount();
            try {
                Thread.sleep(500);
                int count = wordViewModel.getReviewTotalCount();
                Log.d("MainActivity", "Total words count: " + count);
                runOnUiThread(() -> {
                    if (count > 0) {
                        seekBar.setMax(count);
                        Log.d("MainActivity", "Total words count: " + count);
                        seekBar.setProgress(quizWordsCount);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        getOptimalQuizAmount.start();
    }

    private void addSampleWords() {
        Log.d("MainActivity", "Adding sample words...");
        List<Word> sampleWords = new ArrayList<>();

        // 为第一个词 Voc 添加图片
        Word word0 = new Word();
        word0.setText("Voc");
        word0.setMeaning("全世界最好的应用");
        word0.setAnnotation("不是 woc 是 Voc");
        word0.setLearning(true);
        word0.setMasteryLevel(9999.0f);

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
        word3.setMasteryLevel(1.0f);
        sampleWords.add(word3);

        Word word4 = new Word();
        word4.setText("computer");
        word4.setMeaning("电脑");
        word4.setMasteryLevel(1.0f);
        sampleWords.add(word4);

        Word word5 = new Word();
        word5.setText("language");
        word5.setMeaning("语言");
        word5.setMasteryLevel(201.0f);
        sampleWords.add(word5);

        Word word6 = new Word();
        word6.setText("book");
        word6.setMeaning("书");
        word6.setMasteryLevel(201.0f);
        sampleWords.add(word6);

        Word word7 = new Word();
        word7.setText("phone");
        word7.setMeaning("手机");
        word7.setMasteryLevel(701.0f);
        sampleWords.add(word7);

        Word word8 = new Word();
        word8.setText("car");
        word8.setMeaning("汽车");
        word8.setMasteryLevel(701.0f);
        sampleWords.add(word8);

        Word word9 = new Word();
        word9.setText("house");
        word9.setMeaning("房子");
        word9.setMasteryLevel(2501.0f);
        sampleWords.add(word9);

        Word word10 = new Word();
        word10.setText("tree");
        word10.setMeaning("树");
        word10.setMasteryLevel(2501.0f);
        sampleWords.add(word10);



        wordViewModel.insertAll(sampleWords);
        Log.d("MainActivity", "Sample words inserted, count: " + sampleWords.size());
    }

    private String copyImageFromDrawableToInternalStorage(int resourceId) {
        String fileName = "sample_image_" + resourceId + ".jpg";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try {
            // 获取 Drawable 并转换为 BitmapDrawable
            android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(this, resourceId);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();

                // 将 Bitmap 保存到文件
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, fos);
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
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, fos);
                }
            }

            return file.getAbsolutePath();

        } catch (Exception e) {
            Log.e("MainActivity", "Failed to copy image from drawable", e);
            return "";
        }
    }

    private void startCardActivity() {
        Intent intent=new Intent(this, CardReviewActivity.class);
        startActivity( intent);
    }

    private void startImportActivity() {
        Intent intent = new Intent(this, ExcelWordAddActivity.class);
        startActivity(intent);
    }
    
    private void startSelectWordsActivity() {
        Intent intent = new Intent(this, SelectWordsActivity.class);
        startActivity(intent);
    }

    private void startSearchWordActivity() {
        Intent intent = new Intent(this, SearchWordActivity.class);
        startActivity(intent);
    }
    private void startQuizActivity() {

        if(quizWordsCount>0){
            Intent intent = new Intent(this, QuizActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("quiz_words_count", quizWordsCount);
            intent.putExtras(bundle);

            startActivity(intent);

        }
        else{
            Intent intent = new Intent(this, CardReviewActivity.class);
            startActivity(intent);
        }

    }
}