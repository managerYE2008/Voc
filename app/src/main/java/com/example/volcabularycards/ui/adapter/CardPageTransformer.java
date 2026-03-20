package com.example.volcabularycards.ui.adapter;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.ui.fragment.ReviewCardFragment;

public class CardPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.6f;
    private static final float MAX_ROTATION = 6f;
    private static final float CENTER_ELEVATION = 28f;
    private static final float SHADOW_THRESHOLD = 0.01f;
    
    private ViewPager2 viewPager;

    public CardPageTransformer(ViewPager2 viewPager) {
        this.viewPager = viewPager;
        if (viewPager.getChildAt(0) instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            recyclerView.setClipToPadding(false);
            recyclerView.setClipChildren(false);
        }
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        notifyFragmentOfPositionChange(page, position);

        View cardView = page.findViewById(R.id.review_card_view);
        int pageWidth = page.getWidth();

        float scaleFactor = Math.max(0.5f, MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)));

        float alpha = Math.max(0.1f, MIN_ALPHA + (1 - MIN_ALPHA) * (1 - Math.abs(position*position)));

        cardView.setAlpha(alpha);
        page.setScaleX(scaleFactor);
        page.setScaleY(scaleFactor);
        float translationX;
        if(Math.abs(position)<1) translationX = -position * Math.abs(position) * pageWidth * 0.75f;
        else translationX = -position * pageWidth * 0.75f;
        page.setTranslationX(translationX);
        page.setTranslationZ((1-Math.abs(position)) * 50);

        // 添加旋转变换，让卡片向两侧滑动时产生旋转效果
        float rotationY = -position * MAX_ROTATION;
        page.setRotationY(rotationY);

        // 只有中心位置的卡片才可以点击
        // 当 position 接近 0 时（通常在 -0.1 到 0.1 之间），启用点击
        boolean isCenterPage = Math.abs(position) < 0.01f;
        if (cardView != null) {
            cardView.setClickable(isCenterPage);
            cardView.setEnabled(isCenterPage);
        }
    }
    
    private void notifyFragmentOfPositionChange(View page, float position) {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView == null) return;
        
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(
            viewPager.getCurrentItem());
        
        if (holder != null && holder.itemView == page) {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (adapter instanceof ReviewCardFragmentAdapter) {
                    ReviewCardFragment fragment = (ReviewCardFragment) 
                        ((ReviewCardFragmentAdapter) adapter).getFragment(adapterPosition);
                    if (fragment != null) {
                        if (Math.abs(position) < 0.1f) {
                            fragment.switchFromBack();
                        } else if (Math.abs(position) > 0.4f) {
                            fragment.resetToBack();
                        }
                    }
                }
            }
        }
    }
}

