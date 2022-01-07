package com.ht.common.util;

import android.util.Log;

/**
 * 日志工具
 */
public final class HTLog {
	
	private static final String DEBUG_TAG = "HTAPI";

	private static final boolean debug = true;

	public static void d(String msg){
		if(debug){
			Log.d(DEBUG_TAG, msg);
		}
	}

	public static void e(String msg){
		if(debug){
			Log.e(DEBUG_TAG, msg);
		}
	}
}

