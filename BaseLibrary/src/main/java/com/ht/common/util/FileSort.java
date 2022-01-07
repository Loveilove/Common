package com.ht.common.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 自定义文件字典排序
 * @date 2016年10月10日
 */

public class FileSort {

	public static File[] sortFile(File[] files) {
		List<File> listfile = Arrays.asList(files);
		Collections.sort(listfile, new CustomComparator());   //按照指定的规则进行一个排序
		File[] array = (File[]) listfile.toArray(new File[listfile.size()]); 
		return array;


	}

	private static class CustomComparator implements Comparator<File>{

		@Override
		public int compare(File pFile1, File pFile2) {
			/**
			 * 1.先比较文件夹 （文件夹在文件的顺序之上）
			 * 2.以A-Z的字典排序
			 * 3.比较文件夹和文件
			 * 4.比较文件和文件夹
			 */
			if (pFile1.isDirectory() && pFile2.isDirectory()) {
				return pFile1.getName().compareToIgnoreCase(pFile2.getName());
			} else {
				if (pFile1.isDirectory() && pFile2.isFile()) {
					return -1;
				} else if (pFile1.isFile() && pFile2.isDirectory()) {
					return 1;
				} else {
					return pFile1.getName().compareToIgnoreCase(pFile2.getName());
				}
			}
		}
	}
}

