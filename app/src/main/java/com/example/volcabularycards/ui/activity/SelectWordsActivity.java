package com.example.volcabularycards.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.data.Word;
import com.example.volcabularycards.ui.adapter.SelectWordFragmentAdapter;
import com.example.volcabularycards.ui.adapter.SelectWordPageTransformer;
import com.example.volcabularycards.ui.fragment.SelectWordFragment;
import com.example.volcabularycards.ui.viewmodel.WordViewModel;

import java.util.ArrayList;
import java.util.List;

public class SelectWordsActivity extends AppCompatActivity {

    private static final String TAG = "SelectWordsActivity";
    private WordViewModel wordViewModel; // 移除 static

    private SelectWordFragmentAdapter adapter;
    private ViewPager2 viewPager;
    private TextView tvEmptyMessage;

    // 用于记录是否已经初始化过 Adapter，防止重复提交
    private boolean hasInitializedList = false;

    private Button btnQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_words);

        // 初始化 ViewModel
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 绑定 View
        viewPager = findViewById(R.id.viewPager);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        // 初始化 Adapter
        adapter = new SelectWordFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        // 配置 ViewPager
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(new SelectWordPageTransformer(viewPager));

        // 初始化按钮

        Log.d(TAG, "ViewPager initialized, adapter created");

        // 【核心修复】只使用一个 Observer 监听 "未学习单词" 的变化
        // 无论数据是增加、删除还是修改，这里都会自动回调
        wordViewModel.getNotLearnedWordsLive().observe(this, words -> {
            int count = words != null ? words.size() : 0;
            Log.d(TAG, "Not learned words count: " + count);

            if (count > 0) {
                // 有数据，显示列表
                tvEmptyMessage.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                // 只在第一次加载时初始化 Adapter 数据
                // 如果后续数据变化（比如用户选了词），ViewModel 会更新列表，但 Adapter 不需要重新构建 LiveData 列表
                // 除非你需要动态改变列表结构，否则这里其实不需要重新遍历
                if (!hasInitializedList) {
                    List<LiveData<Word>> wordLiveList = new ArrayList<>();
                    for (Word word : words) {
                        // 复用 ViewModel 的单条查询，或者直接包装 Word 对象（参考 CardReviewActivity 的优化）
                        // 这里保留你原有的 getWordById 逻辑，假设你有特殊用途
                        wordLiveList.add(wordViewModel.getWordById(word.getId()));
                    }

                    adapter.submitList(wordLiveList);
                    Log.d(TAG, "Adapter updated with " + wordLiveList.size() + " words");

                    // 注册 Pager 回调
                    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            Log.d(TAG, "Page changed to position: " + position);

                            // 通知当前选中的 Fragment
                            // 注意：确保 adapter.getFragment(position) 在你的 Adapter 中是安全实现的
                            SelectWordFragment currentFragment = adapter.getFragment(position);
                            if (currentFragment != null) {
                                currentFragment.onPageSelected();
                            }
                        }
                    });

                    hasInitializedList = true;
                }
            } else {
                // 列表为空
                // 因为你删除了 addSampleWords，这里只需要显示空视图即可
                // 不需要再查 TotalCount，因为 "未学习" 为空就是最终状态
                Log.d(TAG, "No not-learned words found.");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
            }
        });
    }

    // 【删除】onResume 不需要做任何事，LiveData 会自动刷新

    // 【删除】onActivityResult 不需要做任何事，LiveData 会自动刷新

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源（如果需要）
    }

    public SelectWordFragmentAdapter getAdapter() {
        return adapter;
    }

    public WordViewModel getWordViewModel() {
        return wordViewModel;
    }
}