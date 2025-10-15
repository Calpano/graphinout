package com.graphinout.base.cj.element;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.stream.ICjWriter;

/**
 * The part of a CJ node which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjNodeChunk extends ICjHasId, ICjHasData, ICjHasLabel, ICjHasPorts {


    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.nodeStart();
        // streaming order: id, label, data, ports (graphs)
        cjWriter.maybe(id(), cjWriter::id);
        fireLabelMaybe(cjWriter);
        cjWriter.list(ports().toList(), CjType.ArrayOfPorts, ICjPort::fire);
        fireDataMaybe(cjWriter);
    }


}
