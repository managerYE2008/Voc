package com.example.volcabularycards.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.data.repository.WordRepository;

public class WordViewModel extends AndroidViewModel {
    private final WordRepository repository;
    private final LiveData<List<Word>> allWords;
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>();
    private  ExecutorService executorService;

    private boolean isEmpty;

    private static final String TAG = "CardReviewActivity_viewmodel";
    
    public WordViewModel(@NonNull Application application) {
        super(application);

        Log.d(TAG, "WordViewModel initialized");
        executorService = Executors.newSingleThreadExecutor();
        repository = new WordRepository(application);
        allWords = repository.getAllWords();

        
        // 观察所有单词变化，自动更新总数
        allWords.observeForever(words -> {
            int count = words != null ? words.size() : 0;
            totalCount.postValue(count);
            Log.d(TAG, "Total count updated: " + count);
        });
        
        // 初始化时获取一次总数
        executorService.execute(() -> {
            int count = repository.getTotalCount();
            totalCount.postValue(count);
            Log.d(TAG, "Initial total count: " + count);
        });

    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        //executorService.shutdown();
        executorService.shutdown();
    }
    
    // ==================== 数据插入 ====================
    
    public void insert(Word word) {
        executorService.execute(() -> {
            repository.insert(word);
        });
    }
    
    public void insertAll(List<Word> words) {
        executorService.execute(() -> {
            repository.insertAll(words);
        });
    }
    
    // ==================== 数据更新 ====================
    
    public void update(Word word) {
        executorService.execute(() -> {
            repository.update(word);
        });
    }
    
    // ==================== 数据删除 ====================
    
    public void delete(Word word) {
        executorService.execute(() -> {
            repository.delete(word);
        });
    }
    
    public void deleteAll() {
        executorService.execute(() -> {
            repository.deleteAll();
        });
    }
    
    // ==================== 数据查询 ====================
    
    public LiveData<List<Word>> getAllWords() {
        return allWords;
    }
    
    public LiveData<Word> getWordById(int id) {
        return repository.getWordById(id);
    }


    
    public LiveData<List<Word>> getWordsDueForReview(long timestamp) {
        return repository.getWordsDueForReview(timestamp);
    }
    
    public LiveData<List<Word>> getWordsByMasteryLevel(int masteryLevel) {
        return repository.getWordsByMasteryLevel(masteryLevel);
    }
    
    public LiveData<List<Word>> search(String keyword) {
        return repository.searchWords(keyword);
    }
    
    public int getTotalCount() {

        executorService.execute(() -> {
            int count = repository.getTotalCount();
            totalCount.postValue(count);
            Log.d(TAG, "getTotalCount: " + count);
        });
        Log.d(TAG, "returning getTotalCount: " + totalCount.getValue());
        if(totalCount.getValue()==null) return 0;
        else return totalCount.getValue();

    }
    
    public LiveData<Integer> getTotalCountLive() {
        return totalCount;
    }
    public boolean isEmpty() {
        executorService.execute(() -> {
            isEmpty = repository.isEmpty();
            Log.d(TAG, "isEmpty: " + isEmpty);
        });
        return isEmpty;
    }



    public List<LiveData<Word>> getAllWordsListLive(){
        return repository.getAllWordsListLive();
    }

    public LiveData<List<Word>> getNotLearningWords(){
        return repository.getReviewWordsLive();
    }

    public LiveData<List<Word>> getReviewWords(){
        return repository.getReviewWordsLive();
    }

    public LiveData<List<Word>> getNotLearningWordsLive(){
        return repository.getNotLearningWordsLive();
    }

    public LiveData<List<Word>> getNotLearnedWordsLive(){
        return repository.getNotLearnedWordsLive();
    }
}
