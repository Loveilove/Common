package com.ht.common.base;

import android.view.View;


import com.ht.common.okgo.BaseLoader;
import com.ht.common.util.DPreference;
import com.ht.common.util.T;

import butterknife.ButterKnife;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity {

    protected DPreference dPreference;

    protected BaseLoader baseLoader;

    @Override
    protected void onDestroy() {
        // 在组件销毁的时候调用队列的按照sign取消的方法即可取消。
        if(baseLoader != null){
            baseLoader.cancel();
        }
        super.onDestroy();
    }


    /**
     * 描述：用指定资源ID表示的View填充主界面.可根据需要实现.
     */
    protected void onBindView() {

    }
    /**
     * 描述：初始化数据。建议在此方法内填充视图的数据。<br/>
     * <b>注意：此方法在onBindView后执行。</b>
     */
    protected void onInitData() {

    }

    /**
     * 覆盖当前最顶层Fragemnt
     * @param supportFragment
     */
    public void replace(ISupportFragment supportFragment){
        replaceFragment(supportFragment,false);
    }

    /**
     * 描述：用指定的View填充主界面.
     * @param contentView  指定的View
     */
    @Override
    public void setContentView(View contentView) {
        super.setContentView(contentView);
        //注解控件
        ButterKnife.bind(this);
        //注册特殊监听
        onBindView();
        //初始化数据。
        onInitData();
    }

    /**
     * 描述：用指定资源ID表示的View填充主界面.
     * @param resId  指定的View的资源ID
     */
    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);
        //注解控件
        ButterKnife.bind(this);
        //注册特殊监听
        onBindView();
        //初始化数据。
        onInitData();
    }


    protected void showShortMessage(String message){
        T.showShort(this,message);
    }

    protected void showLongMessage(String message){
        T.showLong(this,message);
    }

    /**
     * 获取服务请求接口
     * @param <K>
     * @return
     */
    protected <K extends BaseLoader> K getBaseLoader() {
        return null;
    }




}

