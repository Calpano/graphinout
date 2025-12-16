package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Page Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Page Node Extension" Name: @ocif/node/page TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/page-node.json
 * <p>
 * Properties:
 * <li>pageNumber (number)
 * <li>label (string)
 * </p>
 */
public class PageNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/page";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/page-node.json";
    /** page order; first page typically 1 */
    private Double pageNumber;
    /** optional label shown in UI */
    private String label;
    private IJsonObject extras;

    public PageNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        PageNodeExtension ext = new PageNodeExtension();
        obj.getMaybeAs(OCIF.Common.PAGE_NUMBER, IJsonValue::asNumber, n -> ext.setPageNumber(n.doubleValue()));
        obj.getIfString(OCIF.Common.LABEL, ext::setLabel);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.PAGE_NUMBER, OCIF.Common.LABEL);
    }

    public IJsonObject extras() {return extras;}

    public String label() {return label;}

    public Double pageNumber() {return pageNumber;}

    public PageNodeExtension setExtras(IJsonObject extras) {
        this.extras = extras;
        return this;
    }

    public PageNodeExtension setLabel(String label) {
        this.label = label;
        return this;
    }

    public PageNodeExtension setPageNumber(Double pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

}
