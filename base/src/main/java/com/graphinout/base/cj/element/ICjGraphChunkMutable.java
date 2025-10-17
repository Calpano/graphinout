package com.graphinout.base.cj.element;

/**
 * The part of a graph which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjGraphChunkMutable extends ICjChunkMutable, ICjGraphChunk, ICjHasIdMutable, ICjHasDataMutable, ICjHasLabelMutable {

}
