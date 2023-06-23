package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Data
 class Link {

    private String refer;
    private String idTarget;
    private String linkLabel;
    private String target;
    private String id;
    private String label;


    public String path(String... parent) {
        String result = "";
        if(idTarget!=null)
            result = idTarget;
        else if(target!=null)
            result = target;
        result  = Arrays.stream(result.split("\\.")).collect(Collectors.joining("']['" ,"['", "']"));
        return  String.join("",parent)+result;

    }


}
