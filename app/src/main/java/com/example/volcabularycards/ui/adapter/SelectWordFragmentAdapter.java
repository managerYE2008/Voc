package com.example.volcabularycards.ui.adapter;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.fragment.SelectWordFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectWordFragmentAdapter extends FragmentStateAdapter {

    private static final String TAG = "SelectWordFragmentAdapter";
    
    private List<LiveData<Word>> wordList = new ArrayList<>();
    private Map<Integer, SelectWordFragment> fragmentMap = new HashMap<>();

    public SelectWordFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        Log.d(TAG, "Adapter created");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "Creating fragment for position: " + position);
        SelectWordFragment fragment = SelectWordFragment.newInstance(position);
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void submitList(List<LiveData<Word>> words) {
        wordList = words;
        fragmentMap.clear();
        notifyDataSetChanged();
        Log.d(TAG, "List submitted with " + wordList.size() + " items");
    }

    public LiveData<Word> getWord(int position) {
        if (position >= 0 && position < wordList.size()) {
            return wordList.get(position);
        }
        return null;
    }
    

    

    
    public SelectWordFragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}
