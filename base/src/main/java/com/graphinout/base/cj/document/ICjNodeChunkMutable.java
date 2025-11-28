package com.graphinout.base.cj.document;

/**
 * The part of a CJ node which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjNodeChunkMutable extends //
        ICjNodeChunk, // extends the read-only version
        ICjChunkMutable, // but is generally mutable
        ICjHasIdMutable, // mutable id
        ICjHasDataMutable, // mutable data
        ICjHasLabelMutable, // mutable label
        ICjHasPortsMutable // mutable ports
{

}


