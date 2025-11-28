package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Page Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Page Node Extension"
 * Name: @ocif/node/page
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/page-node.json
 * <p>
 * Properties:
 * - pageNumber (number)
 * - label (string)
 * </p>
 */
public class OcifPageNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/page";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/page-node.json";

    private Double pageNumber; // page order; first page typically 1
    private String label;      // optional label shown in UI
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public Double getPageNumber() { return pageNumber; }
    public OcifPageNodeExt setPageNumber(Double pageNumber) { this.pageNumber = pageNumber; return this; }

    public String getLabel() { return label; }
    public OcifPageNodeExt setLabel(String label) { this.label = label; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifPageNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}
