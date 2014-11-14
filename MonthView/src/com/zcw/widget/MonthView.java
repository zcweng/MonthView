package com.zcw.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MonthView extends ViewGroup{
	static final Calendar calendar = Calendar.getInstance(Locale.getDefault());
	
	int thisYear = 1970;
	int thisMonth = 0;
	long time;
	
	/** 视图回收列表*/
	protected List<View> scrapCellViews = new ArrayList<View>(7);
	
	/** 每个视图的宽度*/
	protected int cellWidth;
	/** 每个视图的高度*/
	protected int cellHeight;
	
	/** 元素间距*/
	protected final int cellPadding = 0;
	/** 元素列*/
	protected final int cellColums = 7;
	protected final int cellRows = 6;
	
	protected MonthAdapter adapter;
	
	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}
	
	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}
	public MonthView(Context context) {
		super(context);
		setup(context);
	}
	
	private void setup(Context context) {
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		cellWidth = (int)((widthSize - getPaddingLeft() - getPaddingRight() - cellPadding  * (cellColums - 1)) / cellColums );
		cellHeight = cellWidth;
		
		widthSize = getPaddingLeft() + getPaddingRight() + cellColums * cellWidth + cellPadding  * (cellColums - 1);
		if(heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST){
			int maxHeightSize = getPaddingBottom() + getPaddingTop() + cellPadding * (cellRows - 1) + cellRows * cellHeight;
			heightSize = Math.min(heightSize, maxHeightSize);
		}
		setMeasuredDimension(widthSize, heightSize);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if(adapter == null){
			scrapAllViews();
			return;
		}
		
		if(changed){
			scrapAllViews();
		}
		layoutChildren();
	}
	
	protected void layoutChildren() {
		int index = 0;
		int left ;
		int top = getPaddingTop();
		calendar.setTimeInMillis(time);
		
		for (int row = 0; row < cellRows; row++) {
			left = getPaddingLeft();
			for (int col = 0; col < cellColums; col++) {
				makeAndAddCellView(left, top, index);
				index++;
				left += (cellWidth + cellPadding);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			top += (cellHeight + cellPadding);
		}
		
		postInvalidate();
	}
	
	/**
	 * @param left
	 * @param top
	 * @param index
	 */
	protected void makeAndAddCellView(int left, int top, int index) {
		final boolean recycled = !scrapCellViews.isEmpty();
		final View child = recycled ? (scrapCellViews.remove(0)) : (adapter.createCellView(this, index));
		adapter.bindCellView(this, child, index, calendar);
		
		LayoutParams lp = child.getLayoutParams();
		if(lp == null){
			lp = new LayoutParams(cellWidth, cellHeight);
		}
		
		if(recycled){
			attachViewToParent(child, index, lp);
		}else{
			addViewInLayout(child, index, lp, true);
		}
		
		final boolean needToMeasure = !recycled || child.isLayoutRequested();
		
		if (needToMeasure) {
		    final int childWidthSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY), 0, lp.width);
		    final int childHeightSpec = getChildMeasureSpec(
		            MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY), 0, lp.height);
		    child.measure(childWidthSpec, childHeightSpec);
		} else {
		    cleanupLayoutState(child);
		}
		
		if (needToMeasure) {
			child.layout(left, top,left + cellWidth, top + cellHeight);
		} else {
			child.offsetLeftAndRight(left - child.getLeft());
			child.offsetTopAndBottom(top - child.getTop());
		}
	}
	
	private void scrapView(View child){
		scrapCellViews.add(child);
		detachViewFromParent(child);
	}
	
	private void scrapAllViews(){
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			scrapCellViews.add(child);
        }
		detachAllViewsFromParent();
	}
	
	public MonthAdapter getAdapter() {
		return adapter;
	}
	
	public void setAdapter(MonthAdapter adapter) {
		this.adapter = adapter;
		requestLayout();
	}
	
	public void setTime(final long time){
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		
		thisYear = calendar.get(Calendar.YEAR);
		thisMonth = calendar.get(Calendar.MONTH);
		
		final int firstDayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK);
		final int between = firstDayOfWeekInMonth - Calendar.MONDAY;
		calendar.set(Calendar.DAY_OF_MONTH, -between);
		this.time = calendar.getTimeInMillis();
		
		requestLayout();
	}
	
	public void refresh() {
		final int childCount = getChildCount();
		calendar.setTimeInMillis(time);
		
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			adapter.bindCellView(this, child, i, calendar);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	public long getTime() {
		return time;
	}

	public int getThisYear() {
		return thisYear;
	}

	public int getThisMonth() {
		return thisMonth;
	}
}
