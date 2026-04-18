package com.example.volcabularycards.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchWordActivity extends AppCompatActivity {

    private EditText editSearchWord;
    private Button btnAddWord;
    private RecyclerView recyclerSearchResults;
    private WordViewModel wordViewModel;
    private SearchAdapter searchAdapter;
    private List<Word> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_word);

        editSearchWord = findViewById(R.id.edit_search_word);
        btnAddWord = findViewById(R.id.btn_add_word);
        recyclerSearchResults = findViewById(R.id.recycler_search_results);

        Log.d("SearchWordActivity", "Views initialized");

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        setupRecyclerView();
        setupSearchListener();
        setupAddButton();
        
        Log.d("SearchWordActivity", "Setup completed");
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchAdapter(searchResults);
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchAdapter);
    }

    private void setupSearchListener() {
        editSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                } else {
                    searchWords(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchWords(String keyword) {
        wordViewModel.search(keyword).observe(this, words -> {
            searchResults.clear();
            if (words != null) {
                searchResults.addAll(words);
                searchAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupAddButton() {
        Log.d("SearchWordActivity", "Setting up add button");
        if (btnAddWord == null) {
            Log.e("SearchWordActivity", "btnAddWord is null!");
            return;
        }
        
        btnAddWord.setOnClickListener(v -> {
            String wordText = editSearchWord.getText().toString().trim();
            Log.d("SearchWordActivity", "Add button clicked, word: " + wordText);
            
            if (!wordText.isEmpty()) {
                Log.d("SearchWordActivity", "Adding word: " + wordText);
                addWord(wordText);
            } else {
                Log.d("SearchWordActivity", "Word text is empty");
                Toast.makeText(this, "请输入单词", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addWord(String wordText) {
        Log.d("SearchWordActivity", "addWord method called with: " + wordText);
        
        Intent intent = new Intent(this, EditWordActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("word_text", wordText);
        bundle.putBoolean("is_adding_word", true);
        intent.putExtras(bundle);
        
        Log.d("SearchWordActivity", "Starting EditWordActivity");
        startActivity(intent);

        finish();
    }

    static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private List<Word> words;

        public SearchAdapter(List<Word> words) {
            this.words = words;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Word word = words.get(position);
            holder.textWordText.setText(word.getText());
            holder.textWordMeaning.setText(word.getMeaning());
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), EditWordActivity.class);
                intent.putExtra("word_id", word.getId());
                intent.putExtra("word_text", word.getText());
                intent.putExtra("word_meaning", word.getMeaning());
                intent.putExtra("word_annotation", word.getAnnotation());
                intent.putExtra("image_path", word.getImagePath());
                intent.putExtra("is_adding_word", false);
                intent.putExtra("is_learning", true);
                holder.itemView.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textWordText;
            TextView textWordMeaning;

            ViewHolder(View itemView) {
                super(itemView);
                textWordText = itemView.findViewById(R.id.text_word_text);
                textWordMeaning = itemView.findViewById(R.id.text_word_meaning);
            }
        }
    }
}
