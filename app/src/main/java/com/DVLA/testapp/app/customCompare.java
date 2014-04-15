package com.DVLA.testapp.app;

import org.opencv.core.Rect;

import java.util.Comparator;

/**
 * Created by breezed on 15/04/14.
 */
public class customCompare implements Comparator<Rect> {
    @Override
    public int compare(Rect o1, Rect o2) {
        Integer o1Area = o1.width * o1.height;
        Integer o2Area = o2.width * o2.height;
        return o2Area.compareTo(o1Area);
    }
}
