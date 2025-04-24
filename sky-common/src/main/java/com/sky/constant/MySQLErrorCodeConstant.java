package com.sky.constant;

/**
 * MySQL错误码常量类
 */
public class MySQLErrorCodeConstant {

    // 唯一约束冲突错误码
    public static final int DUPLICATE_ENTRY = 1062;

    // 外键约束失败错误码
    public static final int FOREIGN_KEY_CONSTRAINT_FAILS = 1452;

    // 数据过长错误码
    public static final int DATA_TOO_LONG = 1406;

    // 表不存在错误码
    public static final int TABLE_NOT_EXIST = 1146;

    // 列不存在错误码
    public static final int COLUMN_NOT_EXIST = 1054;

    // 语法错误码
    public static final int SYNTAX_ERROR = 1064;

    // 连接错误码
    public static final int CONNECTION_ERROR = 2003;

    // 访问被拒绝错误码
    public static final int ACCESS_DENIED = 1045;

    // 数据库不存在错误码
    public static final int DATABASE_NOT_EXIST = 1049;

    // 锁等待超时错误码
    public static final int LOCK_WAIT_TIMEOUT = 1205;
    public static final int NONNULL_VIOLATION = 1048;
}