package com.calpano.graphinout.base.graphml;

import java.util.LinkedHashMap;
import java.util.Map;

public interface XMLValue {

    @Deprecated
    public String startTag() ;

    /**
     * The full map of all attributes, built-in and 'extra attributes'
     */
    Map<String, String> getAttributes() ;

    @Deprecated
    public String valueTag() ;

    @Deprecated
    public String endTag() ;

    @Deprecated
    default String fullTag(){
        StringBuilder builder =  new StringBuilder();
        builder.append(startTag());
        builder.append(valueTag());
        builder.append(endTag());
        return builder.toString();
    }
}
