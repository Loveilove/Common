package com.ht.common.util;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 数字处理工具类
 */
public class NumberUtils {


    /**
     * 将数字格式化输出
     *
     * @param value     需要格式化的值
     * @param precision 精度(小数点后的位数)
     * @return
     */
    public static String format(Number value, Integer precision) {
        Double number = value.doubleValue();
        precision = (precision == null || precision < 0) ? 2 : precision;
        BigDecimal bigDecimal = new BigDecimal(number + "");
        return bigDecimal.setScale(precision, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 将char型数据转成字节数组
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(char value) {
        byte[] bt = new byte[2];
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte) (value >>> (i * 8));
        }
        return bt;
    }

    /**
     * 将short型数据转成字节数组
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(short value) {
        byte[] bt = new byte[2];
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte) (value >>> (i * 8));
        }
        return bt;
    }

    /**
     * 将int型数据转成字节数组
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(int value) {
        byte[] bt = new byte[4];
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte) (value >>> (i * 8));
        }
        return bt;
    }

    /**
     * 将long型数据转成字节数组
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(long value) {
        byte[] bt = new byte[8];
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte) (value >>> (i * 8));
        }
        return bt;
    }

    /**
     * 将short型数据插入到指定索引的字节数组中
     *
     * @param index  索引
     * @param values 字节数组
     * @param value  需要插入的值
     */
    public static void insert(int index, byte[] values, short value) {
        byte[] bt = NumberUtils.toBytes(value);
        System.arraycopy(bt, 0, values, index, 2);
    }

    /**
     * 将int型数据插入到指定索引的字节数组中
     *
     * @param index  索引
     * @param values 字节数组
     * @param value  需要插入的值
     */
    public static void insert(int index, byte[] values, int value) {
        byte[] bt = NumberUtils.toBytes(value);
        System.arraycopy(bt, 0, values, index, 4);
    }

    /**
     * 将long型数据插入到指定索引的字节数组中
     *
     * @param index  索引
     * @param values 字节数组
     * @param value  需要插入的值
     */
    public static void insert(int index, byte[] values, long value) {
        byte[] bt = NumberUtils.toBytes(value);
        System.arraycopy(bt, 0, values, index, 8);
    }

    /**
     * 将字节转换成整型
     *
     * @param value 字节类型值
     * @return
     */
    public static int byteToInt(byte value) {
        if (value < 0) {
            return value + 256;
        }
        return value;
    }


    /**
     * 数据保留小数位数格式转换
     *
     * @param num
     * @param maximumFractionDigits 保留的小数位数
     * @return
     */
    public static String numberFormat(double num, int maximumFractionDigits) {
        StringBuffer formatStr = new StringBuffer("0.");
        for (int i = 0; i < maximumFractionDigits; i++) {
            formatStr.append("0");
        }
        String pattern = formatStr.toString();
        if (maximumFractionDigits == 0) {
            pattern = "0";
        }
        DecimalFormat g = new DecimalFormat(pattern);
        g.setGroupingUsed(false);
        return g.format(num);
    }

    /**
     * 将度转换成度分秒
     *
     * @param num
     * @param maximumFractionDigits 保留最大小数位数
     * @return
     */
    public static String convertToSexagesimal(double num, int maximumFractionDigits) {
        int du = (int) Math.floor(Math.abs(num)); // 获取整数部分
        double temp = getdPoint(Math.abs(num)) * 60;
        int fen = (int) Math.floor(temp); // 获取整数部分
        double miao = getdPoint(temp) * 60;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(maximumFractionDigits);
        String s = nf.format(miao);
//		114°39′9.944901466369629″
        if (num < 0) {
            return "-" + du + "°" + fen + "′" + s + "″";
        } else {
            return du + "°" + fen + "′" + s + "″";
        }
//		if (num < 0){
//			return  "-"+du + "." + fen + "" + compass_s;
//		}else{
//			return du + "." + fen + "" + compass_s;
//		}
    }

    // 获取小数部分
    private static double getdPoint(double num) {
        double d = num;
        int fInt = (int) d;
        BigDecimal b1 = new BigDecimal(Double.toString(d));
        BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
        double dPoint = b1.subtract(b2).floatValue();
        return dPoint;
    }

    /**
     * @param dmsStr 114°39′35.43919″ 转换成 度 114.6598442481748545
     * @return
     */
    public static double DMSToDegree(String dmsStr) {
        String du = dmsStr.substring(0, dmsStr.indexOf("."));
        String fen = dmsStr.substring(dmsStr.indexOf(".") + 1, dmsStr.indexOf(".") + 3);
        String miao = dmsStr.substring(dmsStr.indexOf("28") + 2, dmsStr.length());
        double d = Double.parseDouble(du);
        double f = Double.parseDouble(fen);
        double m = Double.parseDouble(miao);
        return Math.abs(d) + (Math.abs(f) / 60 + Math.abs(m) / 3600);
    }

    /**
     * 将度转换为度分秒数字串
     *
     * @param num                   // 114.526,5 --> 114.313360008
     * @param maximumFractionDigits 保留的小数位
     * @return
     */
    public static double DegreeToDMSNumber(double num, int maximumFractionDigits) {
        int du = (int) Math.floor(Math.abs(num)); // 获取整数部分
        double temp = getdPoint(Math.abs(num)) * 60;
        int fen = (int) Math.floor(temp); // 获取整数部分
        double miao = getdPoint(temp) * 60;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(maximumFractionDigits);
        String s = nf.format(miao).replace(".", "");
//		114°39′9.944901466369629″
//		if (num < 0){
//			return  "-"+du + "°" + fen + "′" + compass_s + "″";
//		}else{
//			return du + "°" + fen + "′" + compass_s + "″";
//		}
        if (num < 0) {
            return Double.parseDouble("-" + du + "." + fen + "" + s);
        } else {
            return Double.parseDouble(du + "." + fen + "" + s);
        }
    }

    public static String percent(float num, float total) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format((float) num / (float) total * 100);
    }


}

