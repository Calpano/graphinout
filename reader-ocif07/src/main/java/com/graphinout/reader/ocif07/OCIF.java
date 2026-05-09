package com.graphinout.reader.ocif07;

/**
 * OCIF constant strings used across parsing and mapping. This centralizes JSON property names to avoid magic strings
 * and helps keep parser and writers consistent with the spec.
 */
public final class OCIF {

    public static class OcifSchema {

        public static final String V0_6 = "https://canvasprotocol.org/ocif/v0.6";
        public static final String V0_7_0 = "https://canvasprotocol.org/ocif/v0.7.0";

    }

    public static final class Type {

        // v0.7.0 type names (short form, no namespace segment)
        public static final String OCIF_EDGE = "@ocif/edge";
        public static final String OCIF_GROUP = "@ocif/group";
        public static final String OCIF_HYPEREDGE = "@ocif/hyperedge";
        public static final String OCIF_INHERIT = "@ocif/inherit";

        // v0.6 legacy type names kept for backward-compatible reading
        /** @deprecated use {@link #OCIF_EDGE} */
        @Deprecated public static final String OCIF_REL_EDGE = "@ocif/rel/edge";
        /** @deprecated use {@link #OCIF_GROUP} */
        @Deprecated public static final String OCIF_REL_GROUP = "@ocif/rel/group";
        /** @deprecated replaced by node.parent + {@link #OCIF_INHERIT} */
        @Deprecated public static final String OCIF_REL_PARENT_CHILD = "@ocif/rel/parent-child";
        /** @deprecated use {@link #OCIF_HYPEREDGE} */
        @Deprecated public static final String OCIF_REL_HYPEREDGE = "@ocif/rel/hyperedge";

    }

    /** Root-level properties of an OCIF document. */
    public static final class Root {

        public static final String OCIF_SCHEMA_URI = "ocif";
        public static final String NODES = "nodes";
        public static final String RELATIONS = "relations";
        public static final String RESOURCES = "resources";
        public static final String SCHEMAS = "schemas";
        /** TODO is this OCIF ? */
        public static final String EXTRA = "extra";
        /** TODO is this OCIF ? */
        public static final String FLAGS = "flags";
        /** TODO is this OCIF ? */
        public static final String HAS_RELATIONS_PROPERTY = "hasRelationsProperty";

        public static final String ROOT_NODE = "rootNode";

    }

    public static final class Node {

        public static final String DATA = "data";
        public static final String ID = "id";
        public static final String PARENT = "parent";
        public static final String DELETE_WITH_PARENT = "deleteWithParent";
        public static final String POSITION = "position";
        public static final String RELATION = "relation";
        public static final String RESOURCE = "resource";
        public static final String RESOURCE_FIT = "resourceFit";
        public static final String ROTATION = "rotation";
        public static final String ROTATION_AXIS = "rotationAxis";
        public static final String SCALE = "scale";
        public static final String SIZE = "size";

    }

    /** Common property names used across elements and extensions. */
    public static final class Common {

        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String COMMENT = "comment";
        public static final String DATA = "data";
        public static final String NODE = "node";
        public static final String REL = "rel";
        public static final String WEIGHT = "weight";
        public static final String DIRECTION = "direction";
        public static final String PARENT = "parent";
        public static final String CHILD = "child";
        public static final String INHERIT = "inherit";
        public static final String START = "start";
        public static final String END = "end";
        /** TODO is this OCIF ? */
        public static final String FROM = "from";
        /** TODO is this OCIF ? */
        public static final String TO = "to";
        public static final String DIRECTED = "directed";
        public static final String MEMBERS = "members";
        public static final String CASCADE_DELETE = "cascadeDelete";
        /** TODO is this OCIF ? */
        public static final String PORT = "port";
        public static final String PORTS = "ports";
        public static final String START_MARKER = "startMarker";
        public static final String END_MARKER = "endMarker";
        public static final String STROKE_WIDTH = "strokeWidth";
        public static final String STROKE_COLOR = "strokeColor";
        public static final String FILL_COLOR = "fillColor";
        public static final String PATH = "path";
        public static final String SCALE = "scale";
        public static final String ROTATION_AXIS = "rotationAxis";
        public static final String OFFSET = "offset";
        public static final String TOP_LEFT_ANCHOR = "topLeftAnchor";
        public static final String BOTTOM_RIGHT_ANCHOR = "bottomRightAnchor";
        public static final String TOP_LEFT_OFFSET = "topLeftOffset";
        public static final String BOTTOM_RIGHT_OFFSET = "bottomRightOffset";
        public static final String FONT_SIZE_PX = "fontSizePx";
        public static final String FONT_FAMILY = "fontFamily";
        public static final String COLOR = "color";
        public static final String ALIGN = "align";
        public static final String BOLD = "bold";
        public static final String ITALIC = "italic";
        public static final String THEMES = "themes";
        public static final String SELECT_THEME = "selectTheme";
        /** @deprecated v0.6 used kebab-case; replaced by {@link #SELECT_THEME} */
        @Deprecated public static final String SELECT_THEME_LEGACY = "select-theme";
        public static final String PAGE_NUMBER = "pageNumber";
        public static final String LABEL = "label";
        public static final String ENDPOINTS = "endpoints";

    }

    /** Resource representation property names. */
    public static final class Resource {

        public static final String REPRESENTATIONS = "representations";
        public static final String LOCATION = "location";
        public static final String MIME_TYPE = "mimeType";
        public static final String CONTENT = "content";

    }

    /** Schema declaration property names. */
    public static final class Schema {

        public static final String URI = "uri";
        public static final String SCHEMA = "schema";
        public static final String LOCATION = "location";
        public static final String NAME = "name";

    }

    public static class Relation {

        public static final String NODE = "node";

    }

    public static final String V_0_6 = "v0.6";
    /** document-level for schema uri */
    public static final String _OCIF_ = "ocif";

    private OCIF() {}

}
