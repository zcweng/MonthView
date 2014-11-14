package com.zcw.widget;

import android.view.View;
import android.view.ViewGroup;

public abstract class CycleAdapter {

	public abstract View createView(ViewGroup viewGroup, int position);

	public abstract void bindView(View child, int position);

	public int getInitPosition() {
		return 0;
	}

	public void onPositionChanged(int currentPosition) { }

}
