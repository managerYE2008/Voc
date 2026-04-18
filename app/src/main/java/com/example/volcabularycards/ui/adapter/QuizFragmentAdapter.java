package com.example.volcabularycards.ui.adapter;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.activity.QuizActivity;
import com.example.volcabularycards.ui.fragment.quizeFragments.QuizBaseFragment;
import com.example.volcabularycards.ui.fragment.quizeFragments.SelectCh;
import com.example.volcabularycards.ui.fragment.quizeFragments.SelectEn;
import com.example.volcabularycards.ui.fragment.quizeFragments.SpellPart;
import com.example.volcabularycards.ui.fragment.quizeFragments.SpellWhole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragmentAdapter extends FragmentStateAdapter {
    private static final String TAG = "QuizFragmentAdapter";
    private final List<LiveData<Word>> words = new ArrayList<>();
    private Map<Integer, QuizBaseFragment> fragmentMap = new HashMap<>();
    
    private static final int SelectChMasteryLevel = 200;
    private static final int SelectEnMasteryLevel = 700;
    private static final int SpellPartMasteryLevel = 2500;

    
    
    public QuizFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        Log.d(TAG, "Constructor called");
    }
    
    public QuizFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<LiveData<Word>> words) {
        super(fragmentManager, lifecycle);
        this.words.addAll(words);
        Log.d(TAG, "Constructor called, wordsCount: " + words.size());
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "createFragment called, position: " + position);
        Word word = words.get(position).getValue();
        
        if (word == null) {
            Log.e(TAG, "Word at position " + position + " is null");
            return new SelectCh();
        }
        
        QuizBaseFragment quizFragment;
        float masteryLevel = word.getMasteryLevel();
        
        if (masteryLevel < SelectChMasteryLevel) {
            quizFragment = new SelectCh();
        } else if (masteryLevel < SelectEnMasteryLevel) {
            quizFragment = new SelectEn();
        } else if (masteryLevel < SpellPartMasteryLevel) {
            quizFragment = new SpellPart();
        } else {
            quizFragment = new SpellWhole();
        }
        
        quizFragment.setWord(words.get(position));
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        quizFragment.setArguments(bundle);
        fragmentMap.put(position, quizFragment);
        
        Log.d(TAG, "Created fragment: " + quizFragment.getClass().getSimpleName() + 
                " for word: " + word.getText() + 
                " (mastery: " + masteryLevel + ")");
        
        return quizFragment;
    }
    
    @Override
    public int getItemCount() {
        return words.size();
    }
    
    public LiveData<Word> getWord(int position) {
        if (position >= 0 && position < words.size()) {
            return words.get(position);
        }
        return null;
    }
    
    public void submitList(List<LiveData<Word>> newWords) {
        Log.d(TAG, "submitList called, newWords size: " + (newWords != null ? newWords.size() : "null"));
        words.clear();
        fragmentMap.clear();
        if (newWords != null) {
            words.addAll(newWords);
            Log.d(TAG, "After addAll, words size: " + words.size());
        }
        notifyDataSetChanged();
        Log.d(TAG, "After notifyDataSetChanged");
    }
    
    public QuizBaseFragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}
