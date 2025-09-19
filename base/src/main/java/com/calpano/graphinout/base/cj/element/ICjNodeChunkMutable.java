package com.calpano.graphinout.base.cj.element;

/**
 * The part of a CJ node which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjNodeChunkMutable extends ICjNodeChunk, ICjHasIdMutable, ICjHasDataMutable, ICjHasLabelMutable, ICjHasPortsMutable {


}


