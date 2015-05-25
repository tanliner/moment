package utils.view.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import utils.view.animation.InOutAnimation;

public class HideImageButton extends ImageButton {

	private Animation animation;

	public HideImageButton(Context context) {
		super(context);
	}

	public HideImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HideImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		if ((this.animation instanceof InOutAnimation)) {
			setVisibility(((InOutAnimation) this.animation).direction ==
					InOutAnimation.Direction.IN ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	protected void onAnimationStart() {
		super.onAnimationStart();
		if ((this.animation instanceof InOutAnimation)) {
			setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void startAnimation(Animation animation) {
		super.startAnimation(animation);
		this.animation = animation;
		getRootView().postInvalidate();
	}
}