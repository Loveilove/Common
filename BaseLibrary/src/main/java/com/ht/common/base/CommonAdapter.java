package com.ht.common.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.LayoutRes;
import butterknife.ButterKnife;

/**
 * 万能通用Adapter
 *
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

	protected Context mContext;
	protected List<T> mDatas;
	protected int mResourceId;
	private int mDropDownResource;
	private int mTextViewResourceId;

	private final LayoutInflater mInflater;


	public CommonAdapter(Context context, List<T> data,int resourceId) {
		super();
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mDatas = data;
		this.mResourceId = this.mDropDownResource = mTextViewResourceId = resourceId;
	}

	/**
	 * 返回数据集。
	 * @return
	 */
	public List<T> getData(){
		return mDatas;
	}

	/**
	 * 设置数据集并刷新列表。
	 * @param data
	 */
	public void setData(List<T> data){
		mDatas = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mDatas==null?0:mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public T getEntity(int position){
		return (T) mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent);
	}

	@SuppressWarnings("unchecked")
	private View createViewFromResource(int position, View convertView,
										ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			int resourceId = getLayoutId(mDatas.get(position));

			viewHolder = new ViewHolder(mContext,parent,resourceId);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if(mDatas.size() > 0){
			ButterKnife.bind(this,viewHolder.convertView);
			bindView(position,viewHolder);
		}
		return viewHolder.convertView;
	}

	/**
	 * <p>Sets the layout resource to create the drop down views.</p>
	 *
	 * @param resource the layout resource defining the drop down views
	 * @see #getDropDownView(int, View, ViewGroup)
	 */
	public void setDropDownViewResource(@LayoutRes int resource,int textViewResourceId) {
		this.mDropDownResource = resource;
		this.mTextViewResourceId = textViewResourceId;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createDropDownViewFromResource(mInflater, position, convertView, parent, mDropDownResource);
	}

	/**
	 * @param position
	 * @return
	 */
	protected String getDropDownText(int position){
		return mDatas.get(position).toString();
	}

	private View createDropDownViewFromResource(LayoutInflater inflater, int position, View convertView,
										ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		if(view instanceof TextView){
			TextView text = (TextView)view;
			T item = mDatas.get(position);
			if (item instanceof CharSequence) {
				text.setText((CharSequence)item);
			} else {
				String dropText = getDropDownText(position);
				if(dropText != null){
					text.setText(dropText);
				}else{
					text.setText("");
				}
			}
		}
		return view;
	}

	/**
	 * 绑定View
	 * @param position
	 * @param viewHolder
	 */
	abstract protected void bindView(int position, ViewHolder viewHolder);


	private int getLayoutId(T entity){
		return mResourceId;
	}

	private int getLayoutType(T entity){
		return 0;
	}

	/**
	 * 通用ViewHolder
	 * ViewHolder
	 * @author Wkkyo
	 * @date 2015-10-28
	 * @version 1.0.0
	 */
	protected final class ViewHolder {

		private View convertView;

		private int resourceType = -1;

		private SparseArray<View> views;

		public ViewHolder(Context context,ViewGroup parent,int layoutId) {
			this.views = new SparseArray<View>();
			this.convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
			this.convertView.setTag(this);
		}

		public ViewHolder(Context context,ViewGroup parent,int layoutId,int resourceType) {
			this.views = new SparseArray<View>();
			this.convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
			this.convertView.setTag(this);
			this.resourceType = resourceType;
		}

		public <K extends View> K getView(int viewId){
			View view = views.get(viewId);
			if(view == null){
				view = convertView.findViewById(viewId);
				views.put(viewId, view);
			}
			return (K) view;
		}

		public void setText(int viewId,String value){
			TextView textView = getView(viewId);
			textView.setText(value);
		}

		public View getRootView(){
			return this.convertView;
		}

	}
}

