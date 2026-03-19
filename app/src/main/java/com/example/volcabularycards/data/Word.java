package com.example.volcabularycards.data;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "word")
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name="meaning")
    private String meaning;

    @ColumnInfo(name="last_review_time",index=true)
    private long lastReviewTime;

    @ColumnInfo(name="mastery_level",index=true,defaultValue = "0")
    private int masteryLevel;

    public Word(String text, String meaning) {
        this.text = text;
        this.meaning = meaning;
        lastReviewTime = System.currentTimeMillis();
    }
    
    @Ignore
    public Word(){
        this.lastReviewTime=System.currentTimeMillis();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public long getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(long lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public int getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = masteryLevel;
    }
}
