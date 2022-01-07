package com.ht.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;


/**
 * SD卡相关工具类
 * 
 */
public class SDCardUtils {

    private SDCardUtils() {
        throw new UnsupportedOperationException("");
    }

    /**
     * 判断SD卡是否可用
     *
     * @return true : 可用<br>false : 不可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SD卡路径，结尾包含"/"，如果找不到SD卡，则返回根目录。
     * <p>一般是/storage/emulated/0/</p>
     *
     * @return SD卡路径
     */
    public static String getSDCardPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取根目录
		} else {
			return "";
		}
		return sdDir.getPath() + File.separator;
//      return Environment.getExternalStorageDirectory().getPath()
    }

    /**
     * 获取SD卡Data路径
     *
     * @return Data路径
     */
    public static String getDataPath() {
        return Environment.getDataDirectory().getPath();

    }

    /**
     * 获取系统存储路径
     *
     * @return
     */
    public static String getRootDirectoryPath()
    {
        return Environment.getRootDirectory().getAbsolutePath();
    }

    /**
     * 计算SD卡的剩余空间
     *
     * @param unit <ul>
     *             <li>{@link ConstUtils.MemoryUnit#BYTE}: 字节</li>
     *             <li>{@link ConstUtils.MemoryUnit#KB}  : 千字节</li>
     *             <li>{@link ConstUtils.MemoryUnit#MB}  : 兆</li>
     *             <li>{@link ConstUtils.MemoryUnit#GB}  : GB</li>
     *             </ul>
     * @return 返回-1，说明SD卡不可用，否则返回SD卡剩余空间
     */
    @SuppressLint("NewApi")
	public static double getFreeSpace(ConstUtils.MemoryUnit unit) {
        if (isSDCardEnable()) {
            try {
                StatFs stat = new StatFs(getSDCardPath());
                long blockSize, availableBlocks;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    availableBlocks = stat.getAvailableBlocksLong();
                    blockSize = stat.getBlockSizeLong();
                } else {
                    availableBlocks = stat.getAvailableBlocks();
                    blockSize = stat.getBlockSize();
                }
                return FileUtils.byte2Size(availableBlocks * blockSize, unit);
            } catch (Exception e) {
                e.printStackTrace();
                return -1.0;
            }
        } else {
            return -1.0;
        }
    }

    /**
     * 返回可用的SD卡根目录，支持扩展卡。
     * @param context
     * @return
     */
    public final static String[] getSDCardPaths(Context context){
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        // 获取sdcard的路径：外置和内置
        String[] paths = {};
        try {
            paths = (String[]) sm.getClass().getMethod("getVolumePaths", new Class[0]).invoke(sm, new Object[]{});
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return paths;
    }
}