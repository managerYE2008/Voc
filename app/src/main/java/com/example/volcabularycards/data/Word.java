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
    private float masteryLevel;
    //1~10000 means the word is in the process of learning
    //10000 means the word is learned
    //0 means the word is not learned
    //this value is used to calculate the S value in the Ebbinghaus Forgetting Curve


    @ColumnInfo(name="is_learning",defaultValue = "0")
    boolean isLearning;
    
    @ColumnInfo(name="annotation")
    private String annotation;

    @ColumnInfo(name="image_path")
    private String imagePath;

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

    public float getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(float masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public boolean isLearning() {
        return isLearning;
    }

    public void setLearning(boolean learning) {
        if(learning&&masteryLevel==0){
            masteryLevel=1;
            lastReviewTime=System.currentTimeMillis();
        }
        isLearning = learning;

    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Word word = (Word) obj;
        
        if (id != word.id) return false;
        if (lastReviewTime != word.lastReviewTime) return false;
        if (Float.compare(word.masteryLevel, masteryLevel) != 0) return false;
        if (isLearning != word.isLearning) return false;
        
        if (text != null ? !text.equals(word.text) : word.text != null) return false;
        if (meaning != null ? !meaning.equals(word.meaning) : word.meaning != null) return false;
        if (annotation != null ? !annotation.equals(word.annotation) : word.annotation != null) return false;
        return imagePath != null ? imagePath.equals(word.imagePath) : word.imagePath == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (meaning != null ? meaning.hashCode() : 0);
        result = 31 * result + (int) (lastReviewTime ^ (lastReviewTime >>> 32));
        result = 31 * result + Float.floatToIntBits(masteryLevel);
        result = 31 * result + (isLearning ? 1 : 0);
        result = 31 * result + (annotation != null ? annotation.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        return result;
    }
}
