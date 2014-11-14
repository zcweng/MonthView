package com.zcw.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

public class CyclePager extends ViewGroup {
	protected CycleAdapter adapter;
	
	/** 视图回收列表*/
	protected List<View> scrapViews = new ArrayList<View>();
	/** 元素区域的第一个元素的position*/
	protected int firstPosition = 0;
	protected int currentPosition = -1;
	/** 元素的区域*/
	protected Rect childrenBounds = new Rect();
	/** 显示区域*/
	protected Rect screenBounds = new Rect();
	protected ViewFlinger viewFlinger;
	
	int cellWidth;
	int cellHeight;
	final int cellPadding = 60;
	int cellWidthWithPadding;
	
	public CyclePager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}

	public CyclePager(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}

	public CyclePager(Context context) {
		super(context);
		setup(context);
	}
	
	private void setup(Context context) {
		viewFlinger = new ViewFlinger(context);
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		if(adapter != null){
			final boolean existed = getChildCount() > 0;
			if(existed){
				setMeasuredDimension(cellWidth, cellHeight);
				return;
			}
			
			final boolean recycled = ! scrapViews.isEmpty();
			final View child = recycled ? (scrapViews.get(0)) : (adapter.createView(this, 0));
			LayoutParams lp = child.getLayoutParams();
			if(lp == null){
				lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			}
			if(recycled){
				attachViewToParent(child, 0, lp);
			}else{
				addViewInLayout(child, 0, lp, true);
			}
			
			final boolean needToMeasure = !recycled || child.isLayoutRequested();
			if (needToMeasure) {
			    final int childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
			    final int childHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
			    child.measure(childWidthSpec, childHeightSpec);
			}
			
			cellWidth = child.getMeasuredWidth();
			cellHeight = child.getMeasuredHeight();
			
			cellWidthWithPadding = cellWidth + cellPadding;
			
			widthSize = cellWidth;
			heightSize = cellHeight;
			
			detachViewFromParent(child);
			if(! recycled){
				scrapViews.add(child);
			}
			setMeasuredDimension(widthSize, heightSize);
		}else{
			setMeasuredDimension(widthSize, (int) (widthSize * (6f / 7)));
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if(changed){
			reset();
		}
		if(adapter != null){
			layoutChildren();
		}
	}

	private void reset() {
		scrollTo(0, 0);
		scrapAllViews();
		childrenBounds.set(0, 0, 0, 0);
		screenBounds.set(0, 0, getWidth(), getHeight());
		if(adapter != null){
			firstPosition = adapter.getInitPosition();
		}else{
			firstPosition = 0;
		}
	}
	
	private void layoutChildren() {
		final int leftSpace = screenBounds.left - childrenBounds.left;
		final int width = cellWidthWithPadding;
		if(leftSpace > width){
			//remove left
			final View child = getChildAt(0);
			scrapViews.add(child);
			detachViewFromParent(child);
			childrenBounds.left += width;
			firstPosition ++;
		}
		
		final int rightSpace = screenBounds.right - childrenBounds.right;
		if(rightSpace < -width){
			//remove right
			final View child = getChildAt(getChildCount() - 1);
			scrapViews.add(child);
			detachViewFromParent(child);
			childrenBounds.right -= width;
		}
		
		if(leftSpace < 0){
			//add left
			final int left = (childrenBounds.left -= width);
			final int postion = (-- firstPosition);
			makeAndAddView(left, 0, postion, 0);
		}
		
		if(rightSpace > 0){
			//add right
			final int childCount = getChildCount();
			final int index = childCount;
			final int left = childrenBounds.right;
			final int postion = childCount + firstPosition;
			makeAndAddView(left, 0, postion, index);
			childrenBounds.right += width;
		}
	}

	/**
	 * @param left
	 * @param top
	 * @param position
	 * @param index
	 */
	protected void makeAndAddView(int left, int top, int position, int index) {
		final boolean recycled = !scrapViews.isEmpty();
		final View child = recycled ? (scrapViews.remove(0)) : (adapter.createView(this, position));
		
		adapter.bindView(child, position);
		
		LayoutParams lp = child.getLayoutParams();
		if(lp == null){
			lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		
		if(recycled){
			attachViewToParent(child, index, lp);
		}else{
			addViewInLayout(child, index, lp, true);
		}
		
		final boolean needToMeasure = !recycled || child.isLayoutRequested();
		final int width = cellWidth;
		final int height = cellHeight;
		
		if (needToMeasure) {
			final int childWidthSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), 0, lp.width);
			final int childHeightSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST), 0, lp.height);
		    child.measure(childWidthSpec, childHeightSpec);
		} else {
		    cleanupLayoutState(child);
		}
		
		if (needToMeasure) {
			child.layout(left, top, left + width, top + height);
		} else {
			child.offsetLeftAndRight(left - child.getLeft());
			child.offsetTopAndBottom(top - child.getTop());
		}
		
	}
	
	private void scrapAllViews(){
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			scrapViews.add(child);
        }
		detachAllViewsFromParent();
	}

	public void setAdapter(CycleAdapter adapter) {
		this.adapter = adapter;
		if(adapter != this.adapter){
			reset();
		}
		requestLayout();
	}
	

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }
        if(adapter == null){
        	return false;
        }
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }
                final boolean canScrollHorizontally = true;
                boolean touchSloped = false;
                if(canScrollHorizontally){
                	final int x = (int) ev.getX(pointerIndex);
                    final int xDiff = Math.abs(x - mLastMotionX);
                    if (xDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionX = x;
                        touchSloped = true;
                    }
                }
                
                if(touchSloped){
                	initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                mLastMotionX = x;
                
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mIsBeingDragged = false;
                viewFlinger.stop();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return mIsBeingDragged;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(adapter == null){
        	return false;
        }
    	
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if ((mIsBeingDragged = false)) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                
                // Remember where the motion event started
                mLastMotionX = (int) ev.getX();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                final boolean canScrollHorizontally = true;//layouter.canScrollHorizontally();
                
                final int x = (int) ev.getX(activePointerIndex);
                int deltaX = mLastMotionX - x;
                
                if (canScrollHorizontally) {
                	if(!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop){
                		final ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        mIsBeingDragged = true;
                        if (deltaX > 0) {
                            deltaX -= mTouchSlop;
                        } else {
                            deltaX += mTouchSlop;
                        }
                	}
                }else{
                	deltaX = 0;
                }
                
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                	scrollBy(deltaX, 0);
                	screenBounds.offset(deltaX, 0);
    	    		if(childrenBounds.left > screenBounds.left 
    	    				|| childrenBounds.right < screenBounds.right){
//    	    			layoutChildren();
    	    			requestLayout();
    	        	}
    	    		
                    mLastMotionX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                    final int scrollX = getScrollX();
                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    	viewFlinger.fling(scrollX, -initialVelocity, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    }else{
                    	fixRightPosition();
                    }
                    
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }else{
                	fixRightPosition();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                	fixRightPosition();
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                break;
        }
        return true;
    }

	private void fixRightPosition() {
		final int scrollX = getScrollX();
		int dx = scrollX % (cellWidthWithPadding);
		if(Math.abs(dx) < (cellWidthWithPadding) * 0.5){
			dx = -dx;
		}else if(dx > 0){
			dx = (cellWidthWithPadding) - dx;
		}
		else{
			dx = - dx - (cellWidthWithPadding);
		}
		viewFlinger.scroll(scrollX, dx);
	}
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
    
    private void endDrag() {
        mIsBeingDragged = false;
        recycleVelocityTracker();
    }
    
    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private int mLastMotionX;
    private boolean mIsBeingDragged = false;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    
    
    /**
     * @author ThinkPad
     *
     */
    public class ViewFlinger implements Runnable {
    	Scroller scroller;
    	
    	public ViewFlinger(Context context){
    		scroller = new Scroller(context);
    	}
    	
    	void fling(int startX, int velocityX, int minX, int maxX){
    		stop();
    		scroller.fling(startX, 0, velocityX, 0, minX, maxX, 0, 0);
    		int finalX = scroller.getFinalX();
    		final int remain = finalX % (cellWidthWithPadding);
    		if(Math.abs(remain) < cellWidthWithPadding * 0.5){
    			finalX = finalX - remain;
    		}else if(remain > 0){
    			finalX = finalX - remain + cellWidthWithPadding;
    		}else{
    			finalX = finalX - remain - cellWidthWithPadding;
    		}
    		
    		scroller.setFinalX(finalX);
    		
    		post(this);
    	}
    	
    	public void scroll(int startX, int dx) {
    		stop();
    		scroller.startScroll(startX, 0, dx, 0, 800);
    		post(this);
		}
    	
    	void stop(){
    		if(! scroller.isFinished()){
    			scroller.abortAnimation();
    		}
    	}
		
    	@Override
		public void run() {
			if (scroller.computeScrollOffset()) {
                final int x = scroller.getCurrX();
                final int scrollX = getScrollX();
                scrollTo(x, 0);
                screenBounds.offset(x - scrollX, 0);
	    		if(childrenBounds.left > screenBounds.left 
	    				|| childrenBounds.right < screenBounds.right){
	    			requestLayout();
	        	}else{
	        		postInvalidate();
	        	}
	    		post(this);
			}else{}
		}
    }


	@Override
	public void computeScroll() {
		final int scrollX = getScrollX();
		final int screenCenterX = (int) (scrollX + cellWidth * (scrollX > 0 ? 0.5f : -0.5f));
		
		final int curPosition = (int) (1.0f * screenCenterX / cellWidthWithPadding);
		if(currentPosition != curPosition){
			currentPosition = curPosition;
			if(adapter != null)
				adapter.onPositionChanged(currentPosition);
		}
		super.computeScroll();
	}
    
    
}
