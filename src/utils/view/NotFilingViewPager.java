package utils.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * 使page不能水平滑动，故复写父类scrollTo方法。
 * <p/>
 * Created by Administrator on 2014/11/15.
 */
public class NotFilingViewPager extends ViewPager {

	// 决定该View要不要有滑动
	private boolean horizonScrollable = true;

	public NotFilingViewPager(Context context) {

		super(context);
	}

	public NotFilingViewPager(Context context, AttributeSet attrs) {

		super(context, attrs);
	}

	public void enableHorizonScroll(boolean isCanScroll) {

		this.horizonScrollable = isCanScroll;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (horizonScrollable) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (horizonScrollable) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}
}
