MonthView
=========
用于Android开发中自定义的月历视图控件，使用简单，可以很方便自定义各种漂亮的风格。
适配器模式设计，性能优良。


Sample
========
<img src="https://github.com/zcweng/MonthView/blob/master/MonthView/device-2014-11-15-004408.png" width="300" height="500"/>

Usage
=========
    XML中布局：
    
    <com.zcw.widget.MonthView
        android:id="@+id/monthView1"
        android:padding="20dp"
        android:background="@drawable/dialog_full_holo_light"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" >
    </com.zcw.widget.MonthView>
    
    java代码：
    
    monthView.setAdapter(new MonthAdapter() {
			@Override
			public View createCellView(ViewGroup viewGroup, int position) {
				TextView textView  = new TextView(getContext());
				textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				textView.setGravity(Gravity.CENTER);
				return textView;
			}
			@Override
			public void bindCellView(ViewGroup viewGroup, View child, int position, Calendar calendar) {
				TextView textView  = (TextView) child;
				textView.setText(""+calendar.get(Calendar.DAY_OF_MONTH));
			}
		});
