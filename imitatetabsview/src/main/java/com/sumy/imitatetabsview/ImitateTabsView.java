package com.sumy.imitatetabsview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sumy on 2015/8/4 0004.
 */
public class ImitateTabsView extends HorizontalScrollView {

    public interface OnTabItemClickListener {
        public void onItemClick(int position);
    }

    private OnTabItemClickListener listener;

    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor
    };

    private static final float DEFAULT_INDICATOR_CORNER_RADIUS = 0f;

    private LinearLayout tabsContainer;
    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private List<String> tabTitles;
    private int tabCount;

    private int currentPosition = 0;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int drawIndicatorHeight;
    private int indicatorHeight = 8;
    private int dividerWidth = 1;
    private float indicatorCornerRadius;
    private int dividerPadding = 12;
    private int tabPadding = 24;

    private int tabTextSize = 12;
    private int tabTextColor = 0xFF666666;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.BOLD;

    private Locale locale;

    private int lastScrollX = 0;

    private RectF indicatorRectF = new RectF();

    private int animDuration = 200;
    private boolean inAnimation = false;
    private int underlineLeft = 0;
    private int underlineRight = 0;

    public ImitateTabsView(Context context) {
        this(context, null);
    }

    public ImitateTabsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImitateTabsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final float density = getResources().getDisplayMetrics().density;
        float indicatorCornerRadius = DEFAULT_INDICATOR_CORNER_RADIUS * density;

        tabTitles = new ArrayList<>();

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        drawIndicatorHeight = indicatorHeight;
        dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);
        this.indicatorCornerRadius = indicatorCornerRadius;

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(1, tabTextColor);

        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
    }

    public void setCurrentSelectTab(int position) {
        scrollToChild(currentPosition, position);
    }

    public void setOnTabItemClickListener(OnTabItemClickListener listener) {
        this.listener = listener;
    }


    public void addTab(String title) {
        tabTitles.add(title);
        notifyDataSetChanged();
    }

    public void addAllTabs(Collection<? extends String> titles) {
        tabTitles.addAll(titles);
        notifyDataSetChanged();
    }

    public void addAllTabs(String[] titles) {
        for (String title : titles) {
            tabTitles.add(title);
        }
        notifyDataSetChanged();
    }

    public void removeTab(int position) {
        tabTitles.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        tabTitles.clear();
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        tabCount = tabTitles.size();

        for (int i = 0; i < tabCount; i++) {
            addTextTab(i, tabTitles.get(i));
        }

        updateTabStyles();
    }

    private void addTextTab(final int position, String title) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
                scrollToChild(currentPosition, position);
            }
        });

        tab.setPadding(tabPadding, 0, tabPadding, 0);
        tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
    }


    private void updateTabStyles() {

        for (int i = 0; i < tabCount; i++) {

            View v = tabsContainer.getChildAt(i);

            //v.setBackgroundResource(tabBackgroundResId);

            if (v instanceof TextView) {

                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                tab.setTypeface(tabTypeface, tabTypefaceStyle);
                tab.setTextColor(tabTextColor);

                // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                // pre-ICS-build
                if (textAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString().toUpperCase(locale));
                    }
                }
            }
        }
    }

    private void scrollToChild(int fromPosition, int toPosition) {
        if (tabCount == 0) {
            return;
        }

        View lastTab = tabsContainer.getChildAt(fromPosition);

        View currentTab = tabsContainer.getChildAt(toPosition);

        inAnimation = true;

        ValueAnimator leftanim = ValueAnimator.ofInt(lastTab.getLeft(), currentTab.getLeft());
        leftanim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                underlineLeft = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        leftanim.setDuration(animDuration);
        if (fromPosition < toPosition) {
            leftanim.addListener(new AnimationListener());
            leftanim.setStartDelay((int) (animDuration * 0.5));
        }
        leftanim.start();

        ValueAnimator rightanim = ValueAnimator.ofInt(lastTab.getRight(), currentTab.getRight());
        rightanim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                underlineRight = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        rightanim.setDuration(animDuration);
        if (fromPosition > toPosition) {
            rightanim.addListener(new AnimationListener());
            rightanim.setStartDelay((int) (animDuration * 0.5));
        }
        rightanim.start();

        ValueAnimator underlineAnim = ValueAnimator.ofInt(indicatorHeight, (int) (indicatorHeight * 0.5), indicatorHeight);
        underlineAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawIndicatorHeight = (int) animation.getAnimatedValue();
            }
        });
        underlineAnim.setDuration(animDuration);
        underlineAnim.start();

        int newScrollX = currentTab.getLeft();

        if (newScrollX != lastScrollX) {
            ValueAnimator scrollAnim = ValueAnimator.ofInt(getScrollX(), newScrollX);
            scrollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scrollTo((int) animation.getAnimatedValue(), 0);
                }
            });
            scrollAnim.setDuration(animDuration);
            scrollAnim.start();
            lastScrollX = newScrollX;
        }

        currentPosition = toPosition;

        invalidate();
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount == 0) {
            return;
        }

        final int height = getHeight();

        rectPaint.setColor(indicatorColor);

        View currentTab = tabsContainer.getChildAt(currentPosition);
        if (!inAnimation) {
            underlineLeft = currentTab.getLeft();
            underlineRight = currentTab.getRight();
        }

        indicatorRectF.set(underlineLeft, height - drawIndicatorHeight, underlineRight, height);
        if (indicatorCornerRadius > 0f) {
            canvas.drawRoundRect(indicatorRectF, indicatorCornerRadius, indicatorCornerRadius, rectPaint);
        } else {
            canvas.drawRect(indicatorRectF, rectPaint);
        }

        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
        }
    }

    class AnimationListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            inAnimation = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

    }
}
