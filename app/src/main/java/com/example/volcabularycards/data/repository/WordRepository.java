package com.example.volcabularycards.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.data.WordDatabase;
import com.example.volcabularycards.data.dao.WordDao;
import java.util.List;

/**
 * Repository 层 - 处理数据操作和业务逻辑
 * 作为数据源（数据库、网络等）和 ViewModel 之间的中介
 */
public class WordRepository {

    private static final String TAG = "CardReviewActivity_repository";
    private final WordDao wordDao;
    private final LiveData<List<Word>> allWordsLive;
    private final MutableLiveData<Integer> totalCountLive;
    
    public WordRepository(Application application) {
        WordDatabase database = WordDatabase.getInstance(application);
        this.wordDao = database.wordDao();
        this.allWordsLive = wordDao.getAllWordsLive();
        this.totalCountLive = new MutableLiveData<>();
        
        Log.d(TAG, "Repository initialized");
        
        this.allWordsLive.observeForever(words -> {
            int size = words != null ? words.size() : 0;
           /*
            Log.d(TAG, "allWordsLive changed, size: " + size);
            if (words != null) {
                for (int i = 0; i < words.size(); i++) {
                    Log.d(TAG, "Word " + i + ": " + words.get(i).getText());
                }
            }
            */

            totalCountLive.postValue(size);
        });
    }
    
    // ==================== Insert Operations ====================
    
    /**
     * 插入单个单词
     * @param word 要插入的单词对象
     */
    public void insert(Word word) {
        wordDao.insert(word);
    }
    
    /**
     * 批量插入单词
     * @param words 要插入的单词列表
     */
    public void insertAll(List<Word> words) {
        wordDao.insertAll(words);
    }
    
    // ==================== Update Operations ====================
    
    /**
     * 更新单词信息
     * @param word 要更新的单词对象
     */
    public void update(Word word) {
        wordDao.update(word);
    }
    
    // ==================== Delete Operations ====================
    
    /**
     * 删除单个单词
     * @param word 要删除的单词对象
     */
    public void delete(Word word) {
        wordDao.delete(word);
    }
    
    /**
     * 删除所有单词
     */
    public void deleteAll() {
        wordDao.deleteAll();
    }
    
    // ==================== Query Operations (LiveData) ====================
    
    /**
     * 获取所有单词（实时观察）
     * @return 可观察的单词列表
     */
    public LiveData<List<Word>> getAllWords() {
        return allWordsLive;
    }
    
    /**
     * 根据 ID 获取单词（实时观察）
     * @param id 单词 ID
     * @return 可观察的单词对象
     */
    public LiveData<Word> getWordById(int id) {
        return wordDao.getWordByIdLive(id);
    }
    
    /**
     * 获取需要复习的单词（实时观察）
     * @param timestamp 时间戳
     * @return 可观察的单词列表
     */
    public LiveData<List<Word>> getWordsDueForReview(long timestamp) {
        return wordDao.getWordsDueForReviewLive(timestamp);
    }
    
    /**
     * 根据掌握程度获取单词（实时观察）
     * @param masteryLevel 掌握程度等级
     * @return 可观察的单词列表
     */
    public LiveData<List<Word>> getWordsByMasteryLevel(int masteryLevel) {
        return wordDao.getWordsByMasteryLevelLive(masteryLevel);
    }

    public LiveData<List<Word>> searchWords(String query) {
        return wordDao.search(query);
    }
    
    public LiveData<Integer> getTotalCountLive() {
        return totalCountLive;
    }
    
    // ==================== Synchronous Query Operations ====================
    
    /**
     * 获取词汇总数
     * @return 单词总数
     */
    public int getTotalCount() {
        return wordDao.getTotalCount();
    }
    
    // ==================== 复杂逻辑预留方法 ====================
    // 你可以在这里添加更复杂的业务逻辑方法
    
    /**
     * 示例：智能复习算法
     * 提示：这个方法需要你自己在下面实现具体逻辑
     */
    // public List<Word> getSmartReviewWords() {
    //     // TODO: 实现你的复杂逻辑
    //     return null;
    // }
    
    /**
     * 示例：高级搜索功能
     * 提示：这个方法需要你自己在下面实现具体逻辑
     */
    // public List<Word> advancedSearch(String query) {
    //     // TODO: 实现你的搜索逻辑
    //     return null;
    // }
}
