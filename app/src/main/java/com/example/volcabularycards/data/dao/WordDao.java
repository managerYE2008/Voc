package com.example.volcabularycards.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.volcabularycards.data.Word;

import java.util.List;

@Dao
public interface WordDao {
    
    @Insert
    void insert(Word word);

    @Insert
    void insertAll(List<Word> words);
    
    @Update
    void update(Word word);
    
    @Delete
    void delete(Word word);
    
    @Query("SELECT * FROM word")
    LiveData<List<Word>> getAllWordsLive();


    
    @Query("SELECT * FROM word WHERE last_review_time < :time Order by last_review_time ASC")
    LiveData<List<Word>> getWordsDueForReviewLive(long time);
    
    @Query("SELECT * FROM word WHERE mastery_level = :masteryLevel ORDER by mastery_level")
    LiveData<List<Word>> getWordsByMasteryLevelLive(int masteryLevel);
    
    @Query("SELECT * FROM word WHERE id = :id")
    LiveData<Word> getWordByIdLive(int id);
    
    @Query("DELETE FROM word")
    void deleteAll();

    @Query("SELECT COUNT(*)")
    int getTotalCount();


    @Query("Select * from word where text like '%' || :keyword || '%'")
    LiveData<List<Word>> search(String keyword);

    @Query("Select * from word where mastery_level>0 and mastery_level<1000 Order by last_review_time ASC")
    LiveData<List<Word>> getReviewWordsLive();

    @Query("Select * from word where mastery_level>0 and mastery_level<1000 Order by last_review_time ASC")
    List<Word> getReviewWords();

    
}
