package utils.music;

import android.view.View;
import utils.view.pull.Mode;
import utils.view.pull.State;


/**
 * 下拉刷新侦听
 *
 * Created by Administrator on 2014/11/19.
 */
public interface OnPullEventListener<V extends View> {

	public void onPullEvent(State state, Mode direction);
}
