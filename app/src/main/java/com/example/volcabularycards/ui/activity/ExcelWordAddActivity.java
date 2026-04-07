package com.example.volcabularycards.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.data.WordDatabase;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;
import com.google.android.material.card.MaterialCardView;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: Optimize this shit
 * this shit is to big for me to handle without a century of time
 */
public class ExcelWordAddActivity extends AppCompatActivity {

    private static final String TAG = "ExcelWordAddActivity";
    private static final int MAX_PREVIEW_COUNT = 10;
    private static final int BATCH_SIZE = 50;

    private Button btnSelectFile;
    private Button btnImport;
    private TextView tvFileName;
    private TextView tvPreview;
    private Spinner spinnerWordColumn;
    private Spinner spinnerMeaningColumn;
    private EditText etStartRow;
    private EditText etEndRow;
    private MaterialCardView cardPreview;

    private Uri fileUri;
    private int totalColumns = 0;
    private int totalRows = 0;
    private List<String> previewData = new ArrayList<>();

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private WordViewModel wordViewModel;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleFileSelection
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel_word_add);

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        initViews();
        setupListeners();
    }

    private void initViews() {
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnImport = findViewById(R.id.btn_import);
        tvFileName = findViewById(R.id.tv_file_name);
        tvPreview = findViewById(R.id.tv_preview);
        spinnerWordColumn = findViewById(R.id.spinner_word_column);
        spinnerMeaningColumn = findViewById(R.id.spinner_meaning_column);
        etStartRow = findViewById(R.id.et_start_row);
        etEndRow = findViewById(R.id.et_end_row);
        cardPreview = findViewById(R.id.card_preview);
    }

    private void setupListeners() {
        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnImport.setOnClickListener(v -> startImport());

        spinnerWordColumn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fileUri != null) regeneratePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMeaningColumn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fileUri != null) regeneratePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextWatcher rowTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (fileUri != null) regeneratePreview();
            }
        };

        etStartRow.addTextChangedListener(rowTextWatcher);
        etEndRow.addTextChangedListener(rowTextWatcher);
    }

    private void openFilePicker() {
        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private void handleFileSelection(Uri uri) {
        if (uri != null) {
            fileUri = uri;
            tvFileName.setText(getFileName(uri));
            analyzeExcelFile(uri);
        }
    }

    private String getFileName(Uri uri) {
        return uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "未知文件";
    }

    private void analyzeExcelFile(Uri uri) {
        executor.execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                
                totalRows = sheet.getLastRowNum() + 1;
                Row firstRow = sheet.getRow(0);
                totalColumns = firstRow != null ? firstRow.getLastCellNum() : 0;
                
                List<String[]> previewRows = readFirstNRows(sheet, MAX_PREVIEW_COUNT);
                workbook.close();
                
                mainHandler.post(() -> {
                    setupColumnSpinners();
                    updateRowHints();
                    showPreviewFromData(previewRows);
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> 
                    Toast.makeText(this, "文件解析失败：" + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private List<String[]> readFirstNRows(Sheet sheet, int n) {
        List<String[]> result = new ArrayList<>();
        int rowCount = 0;
        
        for (Row row : sheet) {
            if (rowCount >= n) break;
            
            List<String> rowData = new ArrayList<>();
            for (Cell cell : row) {
                rowData.add(getCellValue(cell));
            }
            
            if (!rowData.isEmpty()) {
                result.add(rowData.toArray(new String[0]));
                rowCount++;
            }
        }
        
        return result;
    }

    private void setupColumnSpinners() {
        String[] columns = new String[totalColumns];
        for (int i = 0; i < totalColumns; i++) {
            columns[i] = convertIndexToColumnLetter(i) + " 列";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, columns);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerWordColumn.setAdapter(adapter);
        spinnerMeaningColumn.setAdapter(adapter);

        if (totalColumns >= 2) {
            spinnerWordColumn.setSelection(0);
            spinnerMeaningColumn.setSelection(1);
        }
    }

    private void updateRowHints() {
        etStartRow.setHint("1 (跳过表头)");
        etEndRow.setHint(String.valueOf(totalRows));
        if (totalRows > 1) etStartRow.setText("1");
    }

    private void showPreviewFromData(List<String[]> previewRows) {
        previewData.clear();
        int wordCol = spinnerWordColumn.getSelectedItemPosition();
        int meaningCol = spinnerMeaningColumn.getSelectedItemPosition();
        
        for (int i = 0; i < previewRows.size(); i++) {
            String[] row = previewRows.get(i);
            if (wordCol < row.length && meaningCol < row.length) {
                String word = row[wordCol];
                String meaning = row[meaningCol];
                if (!word.isEmpty() && !meaning.isEmpty()) {
                    previewData.add((i + 1) + ". " + truncateString(word, 30) + " - " + truncateString(meaning, 30));
                }
            }
        }
        
        showPreview();
    }

    private void showPreview() {
        if (!previewData.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String line : previewData) sb.append(line).append("\n");
            tvPreview.setText(sb.toString());
            cardPreview.setVisibility(android.view.View.VISIBLE);
            btnImport.setEnabled(true);
        } else {
            Toast.makeText(this, "未找到有效数据", Toast.LENGTH_SHORT).show();
            btnImport.setEnabled(false);
        }
    }

    private void regeneratePreview() {
        executor.execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                List<String[]> previewRows = readFirstNRows(sheet, MAX_PREVIEW_COUNT);
                workbook.close();
                
                mainHandler.post(() -> showPreviewFromData(previewRows));
            } catch (Exception e) {
                // 忽略错误
            }
        });
    }

    private void startImport() {
        if (fileUri == null) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("ExcelWordsAddActivity", "btn pressed");

        int wordCol = spinnerWordColumn.getSelectedItemPosition();
        int meaningCol = spinnerMeaningColumn.getSelectedItemPosition();
        int startRow, endRow;

        try {
            String startStr = etStartRow.getText().toString();
            startRow = startStr.isEmpty() ? 1 : Integer.parseInt(startStr);
            
            String endStr = etEndRow.getText().toString();
            endRow = endStr.isEmpty() ? -1 : Integer.parseInt(endStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的行号", Toast.LENGTH_SHORT).show();
            return;
        }
        importWordsFromExcel(wordCol, meaningCol, startRow, endRow);
    }

    private void importWordsFromExcel(int wordCol, int meaningCol, int startRow, int endRow) {
        btnImport.setEnabled(false);

        executor.execute(() -> {
            int successCount = 0;
            int skipCount = 0;
            List<Word> batchToInsert = new ArrayList<>(BATCH_SIZE);
            int currentRow = 0;
            Log.d("ExcelWordsAddActivity", "Importing words");

            try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                Log.d("ExcelWordsAddActivity", "Importing from sheet: " + sheet.getSheetName());
                int lastRow = endRow == -1 ? sheet.getLastRowNum() + 1 : Math.min(endRow, sheet.getLastRowNum() + 1);
                Log.d("ExcelWordsAddActivity", "Total rows: " + lastRow);

                for (int i = startRow - 1; i < lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell wordCell = row.getCell(wordCol);
                        Cell meaningCell = row.getCell(meaningCol);
                        
                        String wordText = getCellValue(wordCell);
                        String meaning = getCellValue(meaningCell);

                        if (!wordText.isEmpty() && !meaning.isEmpty()) {
                            Word word = new Word(wordText, meaning);
                            batchToInsert.add(word);
                            successCount++;
                            
                            if (batchToInsert.size() >= BATCH_SIZE) {
                                insertWordsInBatch(batchToInsert);
                                batchToInsert.clear();
                            }
                        } else {
                            skipCount++;
                        }
                    } else {
                        skipCount++;
                    }

                    currentRow++;
                    int finalCurrent = currentRow;
                    int finalSuccess = successCount;
                    

                }

                if (!batchToInsert.isEmpty()) {
                    insertWordsInBatch(batchToInsert);
                }

                int finalSuccess = successCount;
                int finalSkip = skipCount;
                mainHandler.post(() -> {
                    btnImport.setEnabled(true);
                    
                    String message = "导入完成！\n成功：" + finalSuccess + " 条\n跳过：" + finalSkip + " 条";
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    finish();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnImport.setEnabled(true);
                    Toast.makeText(this, "导入失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    long timestamp=0;
    private void insertWordsInBatch(List<Word> words) {
        wordViewModel.insertAll(new ArrayList<>(words));

        Log.d("ExcelWordsAddActivity","导入："+words.size()+"条，耗时："+(System.currentTimeMillis()-timestamp)+"ms");
        timestamp= System.currentTimeMillis();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    return value == (int) value ? String.valueOf((int) value) : String.valueOf(value);
                }
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            default:
                return "";
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    private String convertIndexToColumnLetter(int index) {
        StringBuilder column = new StringBuilder();
        while (index >= 0) {
            column.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return column.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
