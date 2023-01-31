package com.calpano.graphinout.base.graphml;

import java.util.LinkedHashMap;

public interface XMLValue {

    public String startTag() ;

    public LinkedHashMap<String, String> getAttributes() ;

    public String valueTag() ;

    public String endTag() ;

    default String fullTag(){
        StringBuilder builder =  new StringBuilder();
        builder.append(startTag());
        builder.append(valueTag());
        builder.append(endTag());
        return builder.toString();
    }
}
