package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjDocumentMetaElement;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;

public interface ICjDocumentMetaMutable extends ICjDocumentMeta {

    static ICjDocumentMetaMutable of(@NonNull IJsonValue jsonValue) {
        CjDocumentMetaElement meta = new CjDocumentMetaElement();
        IJsonObject o = jsonValue.asObject();
        o.getMaybe(CjConstants.CONNECTED_JSON__CANONICAL, v -> meta.canonical(v.asBoolean()));
        o.getMaybe(CjConstants.CONNECTED_JSON__VERSION_DATE, v -> meta.versionDate(v.asString()));
        o.getMaybe(CjConstants.CONNECTED_JSON__VERSION_NUMBER, v -> meta.versionNumber(v.asString()));
        return meta;
    }

    ICjDocumentMetaMutable canonical(Boolean canonical);

    ICjDocumentMetaMutable versionNumber(String versionNumber);

    ICjDocumentMetaMutable versionDate(String versionDate);

}
