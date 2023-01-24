package com.calpano.graphinout.base;

import java.util.LinkedHashMap;

public interface XMLValue {

     default String getName(){
         return this.getClass().getName().toLowerCase().replaceFirst("gio","");
     }
     String startTag();
     LinkedHashMap<String,String> getAttributes();
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
