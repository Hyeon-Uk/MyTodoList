package com.hyeonuk.todo.integ.util;

public class StringUtils {
    public static boolean isNull(String str){
        return str==null;
    }

    public static boolean isBlank(String str){
        return isNull(str) || "".equals(str.trim());
    }
}
