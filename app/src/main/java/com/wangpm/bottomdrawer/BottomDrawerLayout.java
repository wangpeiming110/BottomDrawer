package com.wangpm.bottomdrawer;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;


/**
 * Created by wpm on 2016/7/8.
 */
public class BottomDrawerLayout extends ViewGroup {

    private static final String TAG = "BottomDrawerLayout";

    private View mDrawerView;
    private View mBottomView;

    private View mRotateView;

    private ViewDragHelper mDragHelper;
    private float mInitialX;
    private float mInitialY;
    private int mTouchSlop;
    private int mCurTop=-1;
    private int mBottomHeight;
    private int mDrawerHeight;
    private int mParentHeight;
    private float mDragOffset = 1;
    private boolean isUnderBottomView = false;
    private boolean isUnderDrawerView = false;
    private OnDrawerStatusChanged onDrawerStatusChanged;


    public BottomDrawerLayout(Context context) {
        super(context);
        init(context);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragerCallBack());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }



    public void setOnDrawerStatusChanged(OnDrawerStatusChanged onDrawerStatusChanged) {
        this.onDrawerStatusChanged = onDrawerStatusChanged;
    }

    public void switchDrawer() {
        if(mDragOffset<1){
            minimize();
        }else{
            maximize();
        }
    }

    private class DragerCallBack extends  ViewDragHelper.Callback{

        //从底部到顶部的顺序遍历子view
        @Override
        public int getOrderedChildIndex(int index) {
            int childCount = BottomDrawerLayout.this.getChildCount();
            int newIndex = childCount - index -1;

            return  newIndex;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            return child == mDrawerView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
//            Log.d(TAG, "clampViewPositionHorizontal " + left + "," + dx);
//            final int leftBound  = getPaddingLeft();
//            final int rightBound = getWidth() - mBottomView.getWidth() - leftBound;
//            //坐标系三种情况
//            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
//
//            return newLeft;

            return super.clampViewPositionHorizontal(child, left, dx);
        }

        //要想上下拖动必须重写此方法
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
//            Log.d(TAG, "clampViewPositionVertical " + top + "," + dy);

            final int topBound = getHeight() - mDrawerView.getMeasuredHeight() - mBottomView.getHeight();
            final int bottomBound = getHeight()  - mBottomView.getHeight();
            final int newTop = Math.min(Math.max(top, topBound), bottomBound);

            return newTop;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

//            Log.i(TAG, "onViewReleased:" + "xvel:" + xvel + ",yvel:" + yvel);
            //yvel Fling产生的值，yvel > 0 则是快速往下Fling || yvel < 0 则是快速往上Fling

            int top = mParentHeight - mDrawerHeight - mBottomHeight;
            if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)/* 后面这个小括号里判断处理拖动之后停下来但是未松手的情况 */) {
                top += mDrawerHeight;
            }
            mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();//important 不加，就不会刷新View的位置
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mCurTop = top;
            mDragOffset = ((float) top -(mParentHeight - mDrawerHeight - mBottomHeight))/ mDrawerHeight;
//            Log.d(TAG, "onViewPositionChanged: mDragOffset:" + mDragOffset);

            //旋转与透明跟随效果
            mDrawerView.setAlpha(1-mDragOffset);
            mRotateView.setRotation((1-mDragOffset)*180);
            requestLayout();

            if (onDrawerStatusChanged != null) {
                onDrawerStatusChanged.onChanged(mParentHeight,top);
            }


//            if(onDrawerStatusChanged !=null){
//                if(mDragOffset == 0 || mDragOffset == 1){
//                    onDrawerStatusChanged.onChanged(mParentHeight,top);
//                }
//            }
        }
    }

    public interface OnDrawerStatusChanged{
        void onChanged(int parentHeight, int drawerTop);
    }

    public void maximize()
    {
        smoothSlideTo(0.0f);
    }

    public void minimize()
    {
        smoothSlideTo(1.0f);
    }

    private boolean smoothSlideTo(float slideOffset) {
        final int topBound = mParentHeight - mDrawerHeight - mBottomHeight;
        int y = (int) (topBound + slideOffset * mDrawerHeight);

        if(mDragHelper.smoothSlideViewTo(mDrawerView, mDrawerView.getLeft(), y))
        {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        int act = MotionEventCompat.getActionMasked(event);
//        final int action = event.getAction();

        switch (act) {
            //由于很多情况不能拦截事件，这种时候系统不会调用onTouchEvent()
            // 手动把事件传递给mDragHelper.processTouchEvent
            case MotionEvent.ACTION_DOWN:

                mInitialX = event.getX();
                mInitialY = event.getY();
                //Feed the down event to the detector so it has
                // context when/if dragging begins
//                mDetector.onTouchEvent(event);
                mDragHelper.processTouchEvent(event);
                isUnderBottomView = mDragHelper.isViewUnder(mBottomView, (int)mInitialX, (int)mInitialY);
                isUnderDrawerView = mDragHelper.isViewUnder(mDrawerView, (int)mInitialX, (int)mInitialY);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mDragHelper.processTouchEvent(event) ;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mDragHelper.processTouchEvent(event) ;
                break;
            case MotionEvent.ACTION_MOVE:

                final float x = event.getX();
                final float y = event.getY();
                final int yDiff = (int) Math.abs(y - mInitialY);
                final int xDiff = (int) Math.abs(x - mInitialX);
                //Verify that either difference is enough to be a drag
                if ((yDiff > mTouchSlop || xDiff > mTouchSlop) && (isUnderBottomView || isUnderDrawerView) ){
                    //Start capturing events
                    return true;
                }
                break;
        }

        //父类是viewgroup，返回的false
        return super.onInterceptTouchEvent(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mDragHelper.processTouchEvent(event);
        //down事件返回false，让其底部的平行层级的view能够接收到点击事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return false;
            case MotionEvent.ACTION_UP:
                return false;
            //只有当手指达到拖动阈值时this才确定消耗此系列事件
            //若未达到阈值也返回true，则与其平行的view不会收到click事件
            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();
                final int yDiff = (int) Math.abs(y - mInitialY);
                final int xDiff = (int) Math.abs(x - mInitialX);
                //Verify that either difference is enough to be a drag
                if ((yDiff > mTouchSlop || xDiff > mTouchSlop) && (isUnderBottomView || isUnderDrawerView) ){
                    //Start capturing events
                    return true;
                }

                break;
        }
        return false;
    }



    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBottomView = findViewById(R.id.layout_bottom_bar);
        mDrawerView = findViewById(R.id.layout_price_detail);

        mRotateView = findViewById(R.id.img_spread_out);

        mBottomView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                maximize();
            }
        });

        mDrawerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                minimize();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mParentHeight = this.getHeight();
        mBottomHeight = mBottomView.getMeasuredHeight();

        mDrawerHeight = mDrawerView.getMeasuredHeight();
//        Log.d(TAG, "onLayout: drawHeight:"+drawHeight);


        mBottomView.layout(l,mParentHeight - mBottomHeight,r,b);
        if(mCurTop == -1){
            mCurTop = mParentHeight - mBottomHeight;
        }

        mDrawerView.layout(l,mCurTop,r,mCurTop + mDrawerHeight);

    }



    @Override
    public void computeScroll() {
        if(mDragHelper.continueSettling(true))
        {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
