package com.example.volcabularycards.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

public class EditWordActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String imagePath = null; // 保存选中的图片路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_word);

        EditText editTextWord = findViewById(R.id.edit_word_text);
        EditText editTextMeaning = findViewById(R.id.edit_word_meaning);
        EditText editTextAnnotation = findViewById(R.id.edit_word_annotation);
        ImageView imageView = findViewById(R.id.edit_word_image);

        // 添加按钮引用
        Button btnQuit = findViewById(R.id.btn_quit_edit);
        Button btnConfirm = findViewById(R.id.btn_confirm_edit);

        // 从 Intent 中获取数据
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String wordText = bundle.getString("word_text");
            String wordMeaning = bundle.getString("word_meaning");
            String wordAnnotation = bundle.getString("word_annotation");
            imagePath = bundle.getString("image_path"); // 保留原路径
            
            android.util.Log.d("EditWordActivity", "Loaded word: id=" + bundle.getInt("word_id") + 
                              ", text=" + wordText + 
                              ", image_path=" + imagePath);

            if (wordText != null) editTextWord.setText(wordText);
            if (wordMeaning != null) editTextMeaning.setText(wordMeaning);
            if (wordAnnotation != null) editTextAnnotation.setText(wordAnnotation);


        }

        // 设置点击事件：点击 ImageView 打开图库
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // 延迟加载图片，确保视图已经初始化
        imageView.post(() -> {
            loadImageView(imageView, imagePath);
            android.util.Log.d("EditWordActivity", "Image loaded, path=" + imagePath);
        });

        // 设置 Quit 按钮点击事件：返回上一个 Activity
        btnQuit.setOnClickListener(v -> {
            finish();
        });

        // 设置 Confirm 按钮点击事件：保存更改并返回
        btnConfirm.setOnClickListener(v -> {
            // 获取编辑后的文本
            String newText = editTextWord.getText().toString().trim();
            String newMeaning = editTextMeaning.getText().toString().trim();
            String newAnnotation = editTextAnnotation.getText().toString().trim();

            Word currentWord=null;
            if (bundle != null) {
                int wordId = bundle.getInt("word_id");
                currentWord = new Word(newText, newMeaning);
                currentWord.setId(wordId);
            }
            // 更新Word对象
            if (currentWord != null && !newText.isEmpty()) {
                currentWord.setText(newText);
                currentWord.setMeaning(newMeaning);
                currentWord.setAnnotation(newAnnotation);
                currentWord.setImagePath(imagePath); // 更新图片路径

                // 通过ViewModelProvider获取ViewModel实例，确保正确性
                WordViewModel wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
                wordViewModel.update(currentWord);
            }

            // 返回上一个Activity
            setResult(RESULT_OK); // 添加结果码，通知父Activity数据已更改
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            // 将图片复制到应用私有目录
            String savedImagePath = copyImageToAppPrivateDir(selectedImage);
            imagePath = savedImagePath;
            
            if (savedImagePath != null) {
                ImageView imageView = findViewById(R.id.edit_word_image);
                loadImageView(imageView, savedImagePath);
                android.util.Log.d("EditWordActivity", "Image copied to: " + savedImagePath);
            } else {
                android.util.Log.e("EditWordActivity", "Failed to copy image");
            }
        }
    }
    
    private String copyImageToAppPrivateDir(Uri uri) {
        try {
            // 创建文件名
            String fileName = "word_image_" + System.currentTimeMillis() + ".jpg";
            java.io.File destFile = new java.io.File(getExternalFilesDir(null), fileName);
            
            // 复制文件
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                android.util.Log.e("EditWordActivity", "Failed to open input stream for URI: " + uri);
                return null;
            }
            
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            return destFile.getAbsolutePath();
            
        } catch (Exception e) {
            android.util.Log.e("EditWordActivity", "Failed to copy image", e);
            return null;
        }
    }
    
    private void loadImageView(ImageView imageView, String path) {
        if (path != null && !path.isEmpty()) {
            java.io.File file = new java.io.File(path);
            android.util.Log.d("EditWordActivity", "Checking image file: " + path + 
                              ", exists=" + file.exists() + 
                              ", readable=" + file.canRead());
            
            if (file.exists() && file.canRead()) {
                android.graphics.Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    android.util.Log.d("EditWordActivity", "Image loaded successfully: " + 
                                      bitmap.getWidth() + "x" + bitmap.getHeight());
                    imageView.setImageBitmap(bitmap);
                    return;
                } else {
                    android.util.Log.e("EditWordActivity", "Failed to decode bitmap");
                }
            } else {
                android.util.Log.e("EditWordActivity", "Image file not found or not readable: " + path);
            }
        } else {
            android.util.Log.d("EditWordActivity", "Image path is null or empty");
        }
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    private String getRealPathFromURI(Uri uri) {
        // 改进的路径获取方法
        String result = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    result = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                // 如果仍然失败，返回 URI 字符串
                result = uri.toString();
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.toString();
        }
        return result;
    }
}
