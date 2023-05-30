package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

@Data
 class Link {

    private String refer;
    private String idTarget;
    private String linkLabel;
    private String target;
    private String id;
    private String label;


    public String path(String... parent) {
        if(idTarget!=null)
            return String.join(".",parent) +"."+idTarget;
        else if(target!=null)
            return String.join(".",parent) +"."+target;
        else return String.join(".",parent);
    }


}
