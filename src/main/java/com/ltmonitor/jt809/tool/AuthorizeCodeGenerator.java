package com.ltmonitor.jt809.tool;

import java.util.Random;

public class AuthorizeCodeGenerator {

    static  char getRandom(char startChar, int range) {     //输入起始字符startChar，输入范围range
        Random random = new Random();
        return (char) (random.nextInt(range) + (int) startChar);
    }

    public static String create()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (i < 20) {
                sb.append(getRandom('a', 26));
            }
            else if (i > 40) {
                sb.append(getRandom('A', 26));
            } else {
                sb.append(getRandom('0', 10));
            }
        }
        return sb.toString();
    }
}
