package com.example.volcabularycards.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.volcabularycards.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            // 跳转到单词卡复习界面
            startCardActivity();
        });

        Button btnImport = findViewById(R.id.btn_import_excel);
        btnImport.setOnClickListener(v -> {
            // 跳转到 Excel 导入界面
            startImportActivity();
        });
    }

    private void startCardActivity() {
        Intent intent=new Intent(this, CardReviewActivity.class);
        startActivity( intent);
    }

    private void startImportActivity() {
        Intent intent = new Intent(this, ExcelWordAddActivity.class);
        startActivity(intent);
    }
}