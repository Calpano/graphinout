package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.Ocifs.factory;

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
public class PageNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String TYPE_NAME = "@ocif/node/page";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/page-node.json";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String LABEL = "label";
    /** page order; first page typically 1 */
    private int pageNumber;
    /** optional label shown in UI */
    private String label;

    public PageNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        PageNodeExtension ext = new PageNodeExtension();
        obj.getMaybeAs(OCIF.Common.PAGE_NUMBER, IJsonValue::asNumber, n -> ext.setPageNumber(n.intValue()));
        obj.getIfString(OCIF.Common.LABEL, ext::setLabel);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.PAGE_NUMBER, OCIF.Common.LABEL);
    }

    public String label() {return label;}

    public int pageNumber() {return pageNumber;}

    public PageNodeExtension setLabel(String label) {
        this.label = label;
        return this;
    }

    public PageNodeExtension setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public Map<String, Object> toMap() {
        return JaJson.createMap()//
                .putNonNull(TYPE, typeUri())//
                .putNonNull(PAGE_NUMBER, pageNumber()) //
                .putMaybe(LABEL, label())//
                .build();
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        o.add(OCIF.Common.PAGE_NUMBER, pageNumber);
        ifPresentAccept(label, v -> o.setString(OCIF.Common.LABEL, v));
        return o;
    }

}
