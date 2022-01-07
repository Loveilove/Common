package com.ht.common.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;


import com.ht.common.okgo.BaseLoader;
import com.ht.common.util.HTLog;
import com.ht.common.util.T;

import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;

public abstract class BaseFragment extends SupportFragment {

    private boolean createdView = false;

    private boolean mDestroyView = false;

    protected BaseActivity mActivity;

    protected BaseLoader baseLoader;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (BaseActivity) context;
        this.baseLoader = getBaseLoader();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setClickable(false);
        createdView = true;
    }

    @Override
    public void onStart() {
        HTLog.d(getClass().getName()+" onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        HTLog.d(getClass().getName()+" onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        HTLog.d(getClass().getName()+" onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        HTLog.d(getClass().getName()+" onStop");
    }

    /**
     * Fragment销毁时回调，请不要覆盖此方法，建议覆盖onDestroyUI()
     */
    @Override
    public void onDestroyView() {
        HTLog.d(getClass().getName()+" onDestroyView");
        mDestroyView = true;
        super.onDestroyView();
        if(createdView){
            onDestroyUI();
        }
    }

    @Override
    public void onDestroy() {
        HTLog.d(getClass().getName()+" onDestroy");
        if(baseLoader != null){
            baseLoader.cancel();
        }
        super.onDestroy();

    }

    @Override
    public void onDetach() {
        HTLog.d(getClass().getName()+" onDetach");
        super.onDetach();
        this.createdView = false;
        this.mActivity = null;

    }

    /**
     * 覆盖当前Fragment
     * @param supportFragment
     */
    public void replace(ISupportFragment supportFragment){
        HTLog.d(getClass().getName()+" replace");
        startWithPop(supportFragment);
    }

    public void popAll(){
        popTo(this.getClass(),true);
    }

    @Override
    public void pop() {
        super.pop();
        if(createdView){
            onDestroyUI();
        }
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     *
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    @Override
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        super.popTo(targetFragmentClass,includeTargetFragment);
        if(createdView){
            onDestroyUI();
        }
    }

    /**
     * 主动销毁Fragment，框架会自动调用，不需要手动调用
     */
    public void destroyFragment(){
        if(createdView){
            onDestroyUI();
        }
    }

    /**
     * 视图销毁时回调
     */
    public void onDestroyUI(){}

    @Override
    public boolean onBackPressedSupport() {
        if(createdView){
            onDestroyUI();
        }
        return super.onBackPressedSupport();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
//        HTLog.d("fragment onActivityResult");
        hideSoftInput();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        HTLog.d("fragment onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            onPause();
        }else{
            onResume();
        }
    }

    //    @Override
//    public void onDestroyView() {
//        // TODO Auto-generated method stub
//        super.onDestroyView();
//        KKLog.d(getClass().getName()+" onDestroyView");
//    }

    /**
     * 获取服务请求接口
     * @param <K>
     * @return
     */
    protected <K extends BaseLoader> K getBaseLoader() {
        return null;
    }

    /**
     * 是否销毁了视图，释放了内存
     * @return
     */
    public boolean isDestroyView() {
        return mDestroyView;
    }

    protected int getPixelsFromDp(int size,Context context){
        DisplayMetrics metrics =new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public void showShort(String message)
{
    T.showShort(getActivity(),message);
}
    public void showLong(String message)
    {
        T.showLong(getActivity(),message);
    }
}
