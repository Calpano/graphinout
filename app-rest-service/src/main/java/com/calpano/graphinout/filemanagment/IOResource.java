package com.calpano.graphinout.filemanagment;

import lombok.Data;

@Data
public  class IOResource<T> {

    private String type;
    private T resource;

    public IOResource(T resource) {
        this.resource = resource;
    }
    public IOResource() {
    }

    public IOResource(T resource,String type) {
        this.type = type;
        this.resource = resource;
    }
}
