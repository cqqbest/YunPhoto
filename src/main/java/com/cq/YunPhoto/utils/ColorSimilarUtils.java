package com.cq.YunPhoto.utils;

import java.awt.*;

/**
 * 颜色相似度计算工具类
 */

public class ColorSimilarUtils {


    /**
     * 计算两个颜色之间的相似度
     * @param color1
     * @param color2
     * @return
     */
    private static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        double maxDistance = Math.sqrt(3 * 255 * 255);

        return 1 - (distance / maxDistance);
    }

    /**
     * 根据16进制颜色计算相似度
     */
    public static double calculateSimilarity(String color1, String color2) {
        Color c1 = Color.decode(color1);
        Color c2 = Color.decode(color2);
        return calculateSimilarity(c1, c2);
    }

}
