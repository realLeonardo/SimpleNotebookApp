package com.leeeshuang.myfirstapp.service;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class StringAxisValueFormatter extends ValueFormatter {

    //区域值
    private List<String> mStrs;

    /**
     * 对字符串类型的坐标轴标记进行格式化
     * @param strs
     */
    public StringAxisValueFormatter(List<String> strs){
        this.mStrs = strs;
    }

    @Override
    public String getFormattedValue(float v) {
        int index = Math.round(v);

        if (index < 0 || index >= mStrs.size() || index != (int)v)
            return "";

        return mStrs.get(index);
    }


}
