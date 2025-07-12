package com.sky.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

public class TakeawayOrderGenerator {

    // 雪花算法实例 (机器ID自动获取)
    private static final Snowflake snowflake;
    
    static {
        // 1. 自动获取机器ID (根据本机IP生成)
        long machineId = NetUtil.getLocalhostStr().hashCode() & 31;
        snowflake = IdUtil.getSnowflake(machineId, 1);
    }

    /**
     * 生成外卖订单号 (20位)
     * 格式：WM{时间戳yyMMddHHmmss}{机器ID}{序列号}{校验位}
     */
    public static String generate() {
        // 1. 生成雪花ID
        long snowflakeId = snowflake.nextId();
        String snowflakeStr = String.valueOf(snowflakeId);
        
        // 2. 提取核心部分 (雪花ID后14位包含时间+机器+序列)
        String coreId = snowflakeStr.substring(Math.max(0, snowflakeStr.length() - 14));
        
        // 3. 生成2位校验码 (Luhn算法增强安全性)
        String verifyCode = calculateLuhnCheckDigit(coreId);
        
        // 4. 组合完整订单号
        return "WM" + coreId + verifyCode;
    }

    /**
     * Luhn算法生成校验位 (防篡改)
     */
    private static String calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) digit = digit % 10 + digit / 10;
            }
            sum += digit;
            alternate = !alternate;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return String.format("%01d", checkDigit);
    }
    

}