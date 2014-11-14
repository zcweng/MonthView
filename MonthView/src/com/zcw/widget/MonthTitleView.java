package com.zcw.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MonthTitleView extends ViewGroup{

	/** */
	protected List<View> scrapTitleViews = new ArrayList<View>(7);
	/** */
	protected int titleWidth;
	/** */
	protected int titleHeight;
	protected final int titlePadding = 0;

	protected MonthTitleAdapter adapter;
	
	public MonthTitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 
	}

	public MonthTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 
	}

	public MonthTitleView(Context context) {
		super(context);
		// 
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		titleWidth = (int)((widthSize - getPaddingLeft() - getPaddingRight() - titlePadding  * 6) / 7 );
		
		if(getChildCount() > 0){
			final View titleView = getChildAt(0);
			titleHeight = titleView.getMeasuredHeight();
		}else if(! scrapTitleViews.isEmpty()){
			final View titleView = scrapTitleViews.get(0);
			titleHeight = titleView.getMeasuredHeight();
		}else if(adapter != null){
			final View titleView = adapter.createTitleView(this);
			LayoutParams lp = titleView.getLayoutParams();
			if(lp == null){
				lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
			addViewInLayout(titleView, 0, lp, true);
			final int childWidthSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(titleWidth, MeasureSpec.AT_MOST), 0, lp.width);
		    final int childHeightSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED), 0, lp.height);
		    titleView.measure(childWidthSpec, childHeightSpec);
		    
		    titleHeight = titleView.getMeasuredHeight();
		    
		    detachViewFromParent(titleView);
		    scrapTitleViews.add(titleView);
		}
		setMeasuredDimension(widthSize, titleHeight + getPaddingBottom() + getPaddingTop());
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if(adapter == null){
			scrapAllViews();
			return;
		}
		
		if(changed){
			
		}
		scrapAllViews();
		layoutChildren();
	}
	
	private void layoutChildren() {
		int left = getPaddingLeft();
		int top = getPaddingTop();
		
		for (int i = 0; i < 7; i++) {
			makeAndAddCellView(left, top, i);
			left += titlePadding + titleWidth;
		}
	}

	private void scrapAllViews(){
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			scrapTitleViews.add(child);
        }
		detachAllViewsFromParent();
	}
	protected void makeAndAddCellView(int left, int top, int index) {
		final boolean recycled = !scrapTitleViews.isEmpty();
		final View child = recycled ? (scrapTitleViews.remove(0)) : (adapter.createTitleView(this));
		adapter.bindTitleView(child, index);
		
		LayoutParams lp = child.getLayoutParams();
		if(lp == null){
			lp = new LayoutParams(titleWidth, titleHeight);
		}
		
		if(recycled){
			attachViewToParent(child, index, lp);
		}else{
			addViewInLayout(child, index, lp, true);
		}
		
		final boolean needToMeasure = !recycled || child.isLayoutRequested();
		
		if (needToMeasure) {
		    final int childWidthSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(titleWidth, MeasureSpec.EXACTLY), 0, lp.width);
		    final int childHeightSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(titleHeight, MeasureSpec.EXACTLY), 0, lp.height);
		    child.measure(childWidthSpec, childHeightSpec);
		} else {
		    cleanupLayoutState(child);
		}
		
		if (needToMeasure) {
			child.layout(left, top,left + titleWidth, top + titleHeight);
		} else {
			child.offsetLeftAndRight(left - child.getLeft());
			child.offsetTopAndBottom(top - child.getTop());
		}
	}

	public void setAdapter(MonthTitleAdapter adapter) {
		this.adapter = adapter;
		requestLayout();
	}
	
	
}
