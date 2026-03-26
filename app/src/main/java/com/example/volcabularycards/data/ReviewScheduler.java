package com.example.volcabularycards.data;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.volcabularycards.data.repository.WordRepository;

import java.util.ArrayList;
import java.util.List;

public class ReviewScheduler {
    private static WordRepository repository;

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;
    private static final long MONTH_IN_MILLIS = 30 * DAY_IN_MILLIS;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS;
    private static final long SECOND_IN_MILLIS = 1000;
    private static final double RetrievabilityFinal=0.9;
    private static final long remainingTime=50*DAY_IN_MILLIS;
    private static final long StabilityFinal=(long)(-(remainingTime)/Math.log(RetrievabilityFinal));
    private static final double k=0.5;



    public ReviewScheduler(WordRepository repository) {
        ReviewScheduler.repository = repository;
    }

    public static List<Word> getReviewWords() {
        long currentTime = System.currentTimeMillis();
        List<Word> AllWords=repository.getReviewWords();
        List<WordInfo> wordInfos=new ArrayList<>();
        for(Word word:AllWords){
            WordInfo wordInfo=new WordInfo(word,currentTime);
            wordInfos.add(wordInfo);
        }
        List<Word> reviewWords=new ArrayList<>();
        wordInfos.sort((a, b) -> {
            if (b.importance > a.importance) return 1;
            if (b.importance < a.importance) return -1;
            return 0;
        });
        for(WordInfo wordInfo:wordInfos){
            reviewWords.add(wordInfo.word);
        }
        return reviewWords;


    }
    public static List<Word> getReviewWords(int amount){
        long currentTime = System.currentTimeMillis();
        List<Word> AllWords=repository.getReviewWords();
        List<WordInfo> wordInfos=new ArrayList<>();
        for(Word word:AllWords){
            WordInfo wordInfo=new WordInfo(word,currentTime);
            wordInfos.add(wordInfo);
        }
        List<Word> reviewWords=new ArrayList<>();
        wordInfos.sort((a, b) -> {
            if (b.importance > a.importance) return 1;
            if (b.importance < a.importance) return -1;
            return 0;
        });
        for(int i=0;i<amount&&i<wordInfos.size();i++){
            reviewWords.add(wordInfos.get(i).word);
        }
        return reviewWords;
    }

    public static void WordReviewed(LiveData<Word> word,float difficulty) {
        Word word1=word.getValue();
        long timeNow = System.currentTimeMillis();
        WordInfo wordInfo=new WordInfo(word1,timeNow);
        long Stability=calculateStability(word1.getMasteryLevel());
        Stability=(long)((double)Stability*(1+k/difficulty));
        word1.setMasteryLevel(setMasterLevel(Stability));
        word1.setLastReviewTime(timeNow);
        repository.update(word1);
        Log.d("ReviewScheduler", "WordReviewed: " + word1.getText() + " " + word1.getMasteryLevel());



    }

    //R=exp(-t/S)
    //S_n+1 = S_n * (1+k/D)
    //k=0.5


    //S0=1
    //S_final=-50days/ln(R_final)

    //R_final>0.9           t=50days

    //S=sqrt(masteryLevel)/100*StabilityFinal

    private static long calculateStability(int masteryLevel) {
        long Stability;
        Stability =(long)(Math.sqrt(masteryLevel)/100*StabilityFinal+1);
        return Stability;

    }

    private static float calculateRetrievability(Word word, long timeNow) {
        float Retrievability;
        Retrievability = (float)Math.exp(-(timeNow-word.getLastReviewTime())/calculateStability(word.getMasteryLevel()));

        return Retrievability;
    }


    public static int setMasterLevel(long Stability){
        int MasteryLevel;
        MasteryLevel=(int)(Math.pow((double)Stability*100/StabilityFinal,2));
        return MasteryLevel;

    }
    private static class WordInfo{
        public  Word word;
        public float Retrievability;


        public double importance;

        public WordInfo(Word word,long timeNow) {
            this.word = word;
            Retrievability=calculateRetrievability(word,timeNow);
            float RetrievabilityNext=calculateRetrievability(word,timeNow+DAY_IN_MILLIS);
            importance=Retrievability-RetrievabilityNext;
        }

    }

    //
}
