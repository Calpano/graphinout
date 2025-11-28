package com.graphinout.reader.gml;

/**
 * Many more keys are defined in <a href="https://docs.yworks.com/yfiles/doc/developers-guide/gml.html">yWorks GML dialect</a>
 */
public class Gml {

    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    public static final String GRAPH = "graph";
    public static final String NODE = "node";
    public static final String EDGE = "edge";

    /** boolean. EXTENSION by yWorks */
    public static final String HIERARCHIC = "hierarchic";

    /** boolean (0,1). graph attribute. */
    public static final String DIRECTED = "directed";

    /** int Defines an identification number for an object. This is usually used to represent pointers. */
    public static final String ID = "id";
    /** string    Defines a label attached to an object. */
    public static final String LABEL = "label";
    /** string    Defines a comment embedded in a GML file. Comments are ignored by the application. */
    public static final String COMMENT = "comment";
    /**
     * string  Shows which application created this file and should therefore only be used once per file at the top
     * level.
     */
    public static final String CREATOR = "Creator";
    /** record (x,y,z,w,h,d)     Describes graphics which are used to draw a particular object.Within graphics, the */
    public static final String GRAPHICS = "graphics";

}
