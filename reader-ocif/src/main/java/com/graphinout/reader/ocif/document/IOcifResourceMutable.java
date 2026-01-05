package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.resource.IOcifResourceExtension;

import java.util.Set;

public interface IOcifResourceMutable extends IOcifResource, IOcifEntityMutable {

    /** Add a typed extension for the resource's data array. */
    IOcifResourceMutable addExtension(IOcifResourceExtension extension);

    IOcifResourceMutable addRepresentation(IOcifRepresentation rep);

    @Override
    default Set<String> definedKeys() {
        return IOcifResource.super.definedKeys();
    }

}
