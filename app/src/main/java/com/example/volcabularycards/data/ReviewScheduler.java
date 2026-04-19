package com.example.volcabularycards.data;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
    private static WordViewModel wordViewModel;

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;
    private static final long MONTH_IN_MILLIS = 30 * DAY_IN_MILLIS;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS;
    private static final long SECOND_IN_MILLIS = 1000;
    private static final double RetrievabilityFinal=0.8;
    private static final long remainingTime = 7 * DAY_IN_MILLIS;//20 days after review still has 80% left in memory
    private static final long StabilityFinal=(long)(-(remainingTime)/Math.log(RetrievabilityFinal));
    private static final double k=0.8;
    private static final double b_early=1.0;
    private static final double b_late=0.01;
    private static int optimalReviewAmount;
    private static final double optimalVarForCmp=0.4;



    public static void init(WordViewModel viewModel) {
        ReviewScheduler.wordViewModel = viewModel;
    }

    public static List<Word> getReviewWords(List<Word> allWords) {


        
        if (allWords == null || allWords.isEmpty()) {
            Log.d("ReviewScheduler", "No words available for review");
            return new ArrayList<>();
        }
        
        Log.d("ReviewScheduler", "Total words to evaluate: " + allWords.size());
        long currentTime = System.currentTimeMillis();
        List<WordInfo> wordInfos=new ArrayList<>();
        for(Word word:allWords){
            WordInfo wordInfo=new WordInfo(word,currentTime);
            wordInfos.add(wordInfo);
            /*Log.d("ReviewScheduler_Debug", String.format(
                "Word: %s | Mastery: %d | LastReview: %d | TimeDiff: %d hours | Stability: %d | VarForCmp: %.4f",
                word.getText(),
                word.getMasteryLevel(),
                word.getLastReviewTime(),
                (currentTime - word.getLastReviewTime()) / (60 * 60 * 1000),
                calculateStability(word.getMasteryLevel()),
                wordInfo.VarForCmp
            ));

             */
        }
        List<Word> reviewWords=new ArrayList<>();
        wordInfos.sort((a, b) -> {
            if (a.VarForCmp < b.VarForCmp) return -1;
            if (a.VarForCmp > b.VarForCmp) return 1;
            return 0;
        });
        optimalReviewAmount=0;
        for(WordInfo wordInfo:wordInfos){
            reviewWords.add(wordInfo.word);
            if(wordInfo.VarForCmp<optimalVarForCmp){
                optimalReviewAmount++;
            }
        }
        Log.d("ReviewScheduler", "Final review list size: " + reviewWords.size());
        return reviewWords;


    }
    public static List<Word> getReviewWords(List<Word> allWords,int amount){

        
        if (allWords == null || allWords.isEmpty()) {
            return new ArrayList<>();
        }
        
        long currentTime = System.currentTimeMillis();
        List<WordInfo> wordInfos=new ArrayList<>();
        for(Word word:allWords){
            WordInfo wordInfo=new WordInfo(word,currentTime);
            wordInfos.add(wordInfo);
        }
        List<Word> reviewWords=new ArrayList<>();
        wordInfos.sort((a, b) -> {
            if (a.VarForCmp < b.VarForCmp) return -1;
            if (a.VarForCmp > b.VarForCmp) return 1;
            return 0;
        });
        optimalReviewAmount=0;
        for(int i=0;i<amount&&i<wordInfos.size()||wordInfos.get(i).VarForCmp<optimalVarForCmp;i++){
            reviewWords.add(wordInfos.get(i).word);
            if(wordInfos.get(i).VarForCmp<optimalVarForCmp){
                optimalReviewAmount++;
            }

        }
        return reviewWords;
    }
    public static int getOptimalReviewAmount(){

        return optimalReviewAmount;
    }




    public static void WordReviewed(LiveData<Word> word, float difficulty) {
        if (wordViewModel == null) {
            Log.e("ReviewScheduler", "WordViewModel not initialized");
            return;
        }
        
        Word word1=word.getValue();
        if (word1 == null) {
            Log.e("ReviewScheduler", "Word is null");
            return;
        }
        
        long timeNow = System.currentTimeMillis();
        long oldStability = calculateStability(word1.getMasteryLevel());
        float oldMastery = word1.getMasteryLevel();
        long Stability;
        Stability=calculateNextStability(word1,timeNow,difficulty);
        word1.setMasteryLevel(setMasterLevel(Stability));
        word1.setLastReviewTime(timeNow);
        Log.d("ReviewScheduler_Update", String.format(
            "Word: %s | Old Mastery: %.2f -> New: %.2f | Old Stability: %d -> New: %d | Difficulty: %.2f",
            word1.getText(),
            oldMastery,
            word1.getMasteryLevel(),
            oldStability,
            Stability,
            difficulty
        ));
        wordViewModel.update(word1);
        Log.d("ReviewScheduler", "WordReviewed: " + word1.getText() + " " + word1.getMasteryLevel());



    }
    //Sₙ = Sₙ₋₁ × [1 + k × D × e^(-b × t/t_opt)]

    private static long calculateNextStability(Word word, long timeNow, float difficulty){
        long Stability;
        Stability=calculateStability(word.getMasteryLevel());
        long timePast=timeNow-word.getLastReviewTime();
        double timePercentage=(double)(timePast)/calculateTOpt(word);
        Log.d("ReviewScheduler_Calc", String.format(
            "Word: %s | Stability: %d | TimePast: %d hrs | TOpt: %d hrs | TimePercentage: %.2f",
            word.getText(),
            Stability,
            timePast / (60 * 60 * 1000),
            calculateTOpt(word) / (60 * 60 * 1000),
            timePercentage
        ));
        double x=b_early*((-1.0f/(timePercentage))+1);
        if(x>0) x=-(double)b_late*(timePercentage);
        Log.d("ReviewScheduler_Calc", String.format(
            "Exponent x: %.4f | Difficulty: %.2f | Result: %d",
            x, difficulty, (long)(Stability*(1+k*difficulty*Math.exp(x)))
        ));
        long result=(long)(Stability*(1+k*difficulty*Math.exp(x)));
        return result;

    }
    public static List<Word> getDistractors(Word word,int  amount) {
        if (wordViewModel == null) {
            Log.e("ReviewScheduler", "WordViewModel not initialized");
            return new ArrayList<>();
        }

        List<Word> similarWords = wordViewModel.getRandomReviewWords(amount);
        
        if (similarWords == null || similarWords.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (similarWords.contains(word)) {
            similarWords.remove(word);
            Log.d("ReviewScheduler", "Removed word from similar words list");
        } else if (similarWords.size() > amount) {
            similarWords = similarWords.subList(0, amount);
        }
        else if(similarWords.size()<amount){
            similarWords.addAll(wordViewModel.getRandomReviewWords(amount-similarWords.size()));
        }
        Log.d("ReviewScheduler", "Final similar words list size: " + similarWords.size());
        
        return similarWords;
    }
    //R=exp(-t/S)
    //S_n+1 = S_n * (1+k/D)
    //k=0.5


    //S0=1
    //S_final=-50days/ln(R_final)

    //R_final>0.9           t=50days

    //S=sqrt(masteryLevel)/100*StabilityFinal

    private static long calculateStability(float masteryLevel) {
        long Stability;
        Stability =(long)(Math.sqrt(masteryLevel)/100*StabilityFinal+1);
        return Stability;

    }

    private static long calculateTOpt(Word word) {
        long TOpt;
        TOpt = -(long)(Math.log(RetrievabilityFinal)*calculateStability(word.getMasteryLevel()));

        return TOpt;
    }


    private static float calculateRetrievability(Word word, long timeNow) {
        float Retrievability;
        Retrievability = (float)Math.exp(-(double)(timeNow-word.getLastReviewTime())/calculateStability(word.getMasteryLevel()));

        return Retrievability;
    }


    public static float setMasterLevel(long Stability){
        float MasteryLevel;
        MasteryLevel=(float)Math.pow((double)Stability*100/StabilityFinal,2);
        return MasteryLevel;

    }
    private static class WordInfo{
        public  Word word;
        //public float Retrievability;


        public double VarForCmp;
        private static double priorityAdjustmentFactor=0.5;





        public WordInfo(Word word,long timeNow) {
            this.word = word;
            //Retrievability=calculateRetrievability(word,timeNow);
            //VarForCmp=(double)(timeNow-word.getLastReviewTime())/calculateStability(word.getMasteryLevel());//与Retrievability成负相关

            double timePercentage=(double)(timeNow-word.getLastReviewTime())/calculateTOpt(word);
            VarForCmp=((double)-1.0f/timePercentage)+1;
            if(VarForCmp<0){
                VarForCmp=-VarForCmp;
            }else{
                VarForCmp*=priorityAdjustmentFactor;
            }
        }


    }

    //
}
