package com.graphinout.base.gio;

import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author rbaba
 * @implNote Declaration Nodes and Edges  and Hyperedges are nested within a graph element. A node is declared with a
 * node element and an edge with an edge element and a Hyperedge with a  Hyperedge element.
 * <p>
 * <b>In the output we avoid the edge and change them all to Hyperedge.</b>
 * @see GioEdge {@link GioEdge}
 */
public class GioGraph extends GioElementWithDescription  implements GioElementWithId{

    public static class GioGraphBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable XmlFragmentString description;
        private boolean edgedefaultDirected = false;
        private @Nullable String id;

        public GioGraph build() {
            return new GioGraph(customAttributes, description, id, edgedefaultDirected);
        }

        public GioGraphBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioGraphBuilder description(@Nullable XmlFragmentString description) {
            this.description = description;
            return this;
        }

        public GioGraphBuilder edgedefaultDirected(boolean edgedefaultDirected) {
            this.edgedefaultDirected = edgedefaultDirected;
            return this;
        }

        public GioGraphBuilder id(@Nullable String id) {
            this.id = id;
            return this;
        }

    }

    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>edgedefault</b>
     */
    private boolean edgedefaultDirected = false;
    /**
     * This is an attribute that can be empty or null.
     * </p>
     * The name of this attribute in graph is <b>id</b>
     */
    private @Nullable String id;


    public GioGraph() {
        super();
    }

    public GioGraph(@Nullable String id, boolean edgedefaultDirected) {
        super();
        this.edgedefaultDirected = edgedefaultDirected;
        this.id = id;
    }

    public GioGraph(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description,
                    @Nullable String id, boolean edgedefaultDirected) {
        super(customAttributes, description);
        this.edgedefaultDirected = edgedefaultDirected;
        this.id = id;
    }


    public static GioGraphBuilder builder() {
        return new GioGraphBuilder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioGraph gioGraph = (GioGraph) o;
        return edgedefaultDirected == gioGraph.edgedefaultDirected &&
                Objects.equals(id, gioGraph.id);
    }

    public @Nullable String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgedefaultDirected, id);
    }


    public boolean isEdgedefaultDirected() {
        return edgedefaultDirected;
    }

    public void setEdgedefaultDirected(boolean edgedefaultDirected) {
        this.edgedefaultDirected = edgedefaultDirected;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GioGraph{" +
                "edgedefaultDirected=" + edgedefaultDirected +
                ", id='" + id + '\'' +
                ", description='" + getDescription() + '\'' +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
