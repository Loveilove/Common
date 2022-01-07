package com.ht.common.util;

/**
 * 快速双击检测工具
 */
public class DoubleUtils {

    private static long lastClickTime;
    private final static long TIME = 600;

    /**
     * 是否连续快速点击。
     * 两个功能切换的间隔不能少于600毫秒，临时解决，还要想个更好的方案。
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
