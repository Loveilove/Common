package com.ht.orm;

import android.os.Environment;

import java.io.File;

/**
 * 数据库配置
 * @author Wkkyo
 *
 */
@Deprecated
public final class DBConfig {

	/**
	 * 数据库文件完整路径，默认SD卡根目录<br/>
	 * 即：sdcard/db，建议根据实际需要进行修改
	 */
	private static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"db";
	
	/**
	 * 数据库文件名，默认为data.db
	 */
	private static String DB_NAME = "data.db";
	
	 /** 数据库版本号. */
	 private static int DB_VERSION = 1;

	
}
