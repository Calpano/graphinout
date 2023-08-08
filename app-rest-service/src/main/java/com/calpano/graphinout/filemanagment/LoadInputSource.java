package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.input.InputSource;

import javax.annotation.Nonnull;
import java.io.IOException;

@FunctionalInterface
public interface LoadInputSource {

    IOResource<InputSource> load(@Nonnull String sessionID,Type type) throws IOException;
    default  IOResource<InputSource> load(@Nonnull String sessionID) throws IOException{
        return  load(sessionID,Type.INPUT);
    }

    default  IOResource<InputSource> load(@Nonnull String sessionID,String type) throws IOException{
        return  load(sessionID,Type.getType(type));
    }
}
