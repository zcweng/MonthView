MonthView
=========



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
