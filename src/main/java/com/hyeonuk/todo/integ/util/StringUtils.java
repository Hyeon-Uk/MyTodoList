package com.hyeonuk.todo.integ.util;

import java.util.Random;

public class StringUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static boolean isNull(String str){
        return str==null;
    }

    public static boolean isBlank(String str){
        return isNull(str) || "".equals(str.trim());
    }

    public static String randomSting(int length){
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for(int i=0;i<length;i++){
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}
