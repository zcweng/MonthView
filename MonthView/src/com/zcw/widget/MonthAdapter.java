package com.zcw.widget;

import java.util.Calendar;

import android.view.View;
import android.view.ViewGroup;

public abstract class MonthAdapter {

	public abstract View createCellView(ViewGroup viewGroup, int position);
	
	public abstract void bindCellView(ViewGroup viewGroup, View child, int position, Calendar calendar);
}
