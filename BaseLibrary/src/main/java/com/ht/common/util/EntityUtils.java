package com.ht.common.util;

import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.suke.widget.SwitchButton;

import java.lang.reflect.Field;

/**
 * 实体类工具类
 * EntityUtils
 * @author wkkyo
 * @date 2020/5/27
 */
public class EntityUtils {

    /**
     * 界面自动赋值实体类
     * @param k
     * @param contentView
     * @param <K>
     * @return
     */
    public static <K> K viewToEntity(K k, View contentView) {
        Field[] fields = k.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            View view = contentView.findViewWithTag(fieldName);
            if (view != null) {
                String formValue = getValueByView(view);
                if(formValue != null){
                    field.setAccessible(true);
                    try {
                        field.set(k, formValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return k;
    }

    /**
     * 返回值控件中对应的值。
     * @return
     */
    private static String getValueByView(View valueView) {
        if (valueView == null) {
            return null;
        }
        if (valueView != null) {
            String data = "";
            if (valueView instanceof EditText) {
                data = ((EditText) valueView).getText().toString().trim();
            } else if (valueView instanceof TextView) {
                data = ((TextView) valueView).getText().toString().trim();
            } else if (valueView instanceof Spinner) {
                Spinner sp = (Spinner) valueView;
                Adapter adapter = sp.getAdapter();
                data = sp.getSelectedItem().toString();
                if (data.equals("无") || data.equals("请选择")) {
                    data = "";
                }
            }
            return data;
        }
        return null;
    }

    /**
     * 实体类自动填充界面
     * @param k
     * @param contentView
     * @param <K>
     */
    public static <K> void entityToView(K k, View contentView) {
        Field[] fields = k.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            View view = contentView.findViewWithTag(fieldName);
            if (view != null) {
                field.setAccessible(true);
                try {
                    String dataValue = field.get(k).toString();
                    if (view instanceof TextView) {
                        ((TextView) view).setText(dataValue);
                    }else if (view instanceof EditText) {
                        ((EditText) view).setText(dataValue);
                    }else if (view instanceof Spinner) {
                        Spinner sp = (Spinner) view;
                        Adapter adapter = sp.getAdapter();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).equals(dataValue)) {
                                sp.setSelection(i);
                                break;
                            }
                        }
                    }else if (view instanceof SwitchButton) {
                        //TODO 还没实现
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
