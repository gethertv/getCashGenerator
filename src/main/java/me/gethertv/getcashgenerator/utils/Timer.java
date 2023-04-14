package me.gethertv.getcashgenerator.utils;

import java.text.DecimalFormat;

public class Timer {

    private static DecimalFormat formatter = new DecimalFormat("00");
    public static String getTime(Long time)
    {

        long sec = time - System.currentTimeMillis();
        int seconds = (int) sec/1000;
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        String timer = formatter.format(p2) + ":"+ formatter.format(p3) + ":" + formatter.format(p1);
        return timer;
    }

    public static String getTimeBySec(int seconds)
    {

        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        String timer = p2 + ":"+ formatter.format(p3) + ":" + formatter.format(p1);
        return timer;
    }

}
