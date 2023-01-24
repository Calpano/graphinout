package com.calpano.graphinout.base;

import java.util.LinkedHashMap;

public interface XMLValue {

     String getName();
     String startTag();
     LinkedHashMap<String,String>
     String valueTag();

     String endTag();

     default String fullTag() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startTag());
        stringBuilder.append(valueTag());
        stringBuilder.append(endTag());
        return stringBuilder.toString();
    }

}
