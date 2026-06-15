package com.graphinout.base.cj.document;

/**
 * Mutable variant of {@link ICjPort} used while constructing a node or port.
 */
public interface ICjPortMutable extends ICjPort, ICjHasIdMutable<ICjPortMutable>, ICjHasPortsMutable, ICjHasLabelMutable, ICjHasDataMutable {}
