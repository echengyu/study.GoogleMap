package tw.com.maxkit.simple.android.testgooglemap.adapter;

import java.util.List;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BasicAdapter extends BaseAdapter {
	protected Context ctx;
	protected List coll;
	protected ViewGroup layout;
	
	public BasicAdapter(Context ctx, List attList){
		this.ctx =ctx;
		this.coll = attList;
	}
	
	@Override
	public int getCount() {
		return coll.size();
	}

	@Override
	public Object getItem(int position) {
		return coll.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public List getColl() {
		return coll;
	}

	public void setColl(List coll) {
		this.coll = coll;
	}
	
	public void clear() {
		this.coll.clear();
	}
	
	abstract void setItemHeight(double itemHeight);
	
}
