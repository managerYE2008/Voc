package com.example.volcabularycards.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.volcabularycards.R;
import com.example.volcabularycards.ui.fragment.SelectWordFragment;

public class SelectWordPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.6f;
    private static final float MAX_ROTATION = 6f;
    private static final float CENTER_ELEVATION = 28f;
    private static final float SHADOW_THRESHOLD = 0.01f;
    
    private ViewPager2 viewPager;

    public SelectWordPageTransformer(ViewPager2 viewPager) {
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

        View cardView = page.findViewById(R.id.select_word_view);
        View frontCard = page.findViewById(R.id.select_word_card_front);
        View backCard = page.findViewById(R.id.card_back);
        
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

        float rotationY = -position * MAX_ROTATION;
        page.setRotationY(rotationY);

        updateCardFaceVisibility(frontCard, backCard, position);

        boolean isCenterPage = Math.abs(position) < 0.01f;
        if (cardView != null) {
            cardView.setClickable(isCenterPage);
            cardView.setEnabled(isCenterPage);
        }
    }

    private void updateCardFaceVisibility(View frontCard, View backCard, float position) {
        if (frontCard == null || backCard == null) return;
        
        float absPosition = Math.abs(position);
        
        if (absPosition < 0.1f) {
            frontCard.setVisibility(View.VISIBLE);
            frontCard.setAlpha(1f);
            backCard.setVisibility(View.GONE);
            backCard.setAlpha(1f);
        } else if (absPosition > 0.5f) {
            frontCard.setVisibility(View.GONE);
            frontCard.setAlpha(1f);
            backCard.setVisibility(View.VISIBLE);
            backCard.setAlpha(1f);
        } else {
            float rangePosition = (absPosition - 0.1f) / 0.4f;
            float frontAlpha = 1f - rangePosition;
            float backAlpha = rangePosition;
            
            frontCard.setVisibility(View.VISIBLE);
            frontCard.setAlpha(frontAlpha);
            backCard.setVisibility(View.VISIBLE);
            backCard.setAlpha(backAlpha);
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
                if (adapter instanceof SelectWordFragmentAdapter) {
                    SelectWordFragment fragment = (SelectWordFragment) 
                        ((SelectWordFragmentAdapter) adapter).getFragment(adapterPosition);
                    if (fragment != null) {
                        if (Math.abs(position) < 0.1f) {
                            resetToBack(fragment);
                        } else if (Math.abs(position) > 0.4f) {
                            
                        }
                    }
                }
            }
        }
    }
    
    private void resetToBack(SelectWordFragment fragment) {
        
    }
}
