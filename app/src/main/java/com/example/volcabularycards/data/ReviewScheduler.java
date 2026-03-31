package com.example.volcabularycards.data;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.volcabularycards.data.repository.WordRepository;

import java.util.ArrayList;
import java.util.List;


//Sₙ = Sₙ₋₁ × [1 + k × D × e^(-b × |t - t_opt|/t_opt)]

/**
 * Sₙ：第 n 次复习后的记忆强度
 * Sₙ₋₁：第 n-1 次复习后的记忆强度（初始值 S₀ 由首次学习效果决定）
 * k：学习效率系数（通常 0.3-0.5，反映个体学习能力）
 * D：复习强度（取值范围 [0, 1]，反映学习时长、专注度、测试难度等综合因素）
 * b：间隔敏感系数（通常 2.0-3.0，反映对时间间隔的敏感度）
 * t：实际复习间隔时间
 * t_opt：最优复习间隔时间（根据目标记忆时长计算：t_opt ≈ 0.15 × T，其中 T 为希望保持记忆的总时长）
 */
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
    private static final double k=0.4;
    private static final double b=4.0;



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
            if (b.Retrievability > a.Retrievability) return 1;
            if (b.Retrievability < a.Retrievability) return -1;
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
            if (b.Retrievability > a.Retrievability) return 1;
            if (b.Retrievability < a.Retrievability) return -1;
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
        Stability=calculateNextStability(word1,timeNow,difficulty);
        word1.setMasteryLevel(setMasterLevel(Stability));
        word1.setLastReviewTime(timeNow);
        repository.update(word1);
        Log.d("ReviewScheduler", "WordReviewed: " + word1.getText() + " " + word1.getMasteryLevel());



    }
    //Sₙ = Sₙ₋₁ × [1 + k × D × e^(-b × t/t_opt)]

    private static long calculateNextStability(Word word, long timeNow, float difficulty){
        long Stability;
        Stability=calculateStability(word.getMasteryLevel());
        double timePercentage=(double)(timeNow-word.getLastReviewTime()-calculateTOpt(word))/calculateTOpt(word);
        //timePercentage is the percentage of time to the Optimal Review Time since last reviewed
        //b/(timePercentage+1) is to make the exponential function return a smaller value when now is before the Optimal Review Time
        //                      and to make the exponential function return a larger value when now is after the Optimal Review Time but still has a limited value of b
        long result=(long)(Stability*(1+k*difficulty*Math.exp(b/(timePercentage+1))));
        return result;

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

    private static long calculateTOpt(Word word) {
        long TOpt;
        TOpt = -(long)(Math.log(0.9)*calculateStability(word.getMasteryLevel()));

        return TOpt;
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





        public WordInfo(Word word,long timeNow) {
            this.word = word;
            Retrievability=calculateRetrievability(word,timeNow);

        }


    }

    //
}
