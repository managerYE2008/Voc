package com.example.volcabularycards.ui.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.fragment.ReviewCardFragment;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewCardFragmentAdapter extends FragmentStateAdapter {
    private final List<Word> words=new ArrayList<>();
    private static final String TAG = "CardReviewActivity_Adapter";
    private Map<Integer, ReviewCardFragment> fragmentMap = new HashMap<>();

    public ReviewCardFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

        Log.d(TAG, "Constructor called,wordsCount:"+words.size());
    }

    public ReviewCardFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Word> words) {
        super(fragmentManager, lifecycle);
        this.words.addAll(words);
        Log.d(TAG, "Constructor called,wordsCount:"+words.size());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        Log.d(TAG, "createFragment called,position:"+position);
        Word word=words.get(position);
        ReviewCardFragment fragment = ReviewCardFragment.newInstance(word);
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount called, returning: " + words.size());
        return words.size();
    }

    public void addWord(Word word) {
        words.add(word);
        notifyItemInserted(words.size() - 1);
    }
    
    public void removeWord(Word word) {
        int position = words.indexOf(word);
        if (position != -1) {
            words.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * 批量更新单词列表
     */
    public void submitList(List<Word> newWords) {
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
    
    public ReviewCardFragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}
