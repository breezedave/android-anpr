package com.DVLA.testapp.app;

import java.util.Comparator;

/**
 * Created by breezed on 15/04/14.
 */
public class boxLetterComapre implements Comparator<histCountBox> {
    @Override
    public int compare(histCountBox h1, histCountBox h2) {
        Integer vol1 = h1.volume;
        Integer vol2 = h2.volume;
        return vol2.compareTo(vol1);
    }
}
