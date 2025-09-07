package com.calpano.graphinout.foundation.json.value;

import java.util.function.Consumer;

/** Mutable */
public interface IAppendableJsonObject extends IJsonObject {

    default IAppendableJsonObject addProperty(String key, String value) {
       return addProperty(key, factory().createString(value));
    }

    IAppendableJsonObject addProperty(String key, IJsonValue jsonValue);

    default IAppendableJsonObject object(String key, Consumer<IAppendableJsonObject> nestedObject) {
        IAppendableJsonObject nested = factory().createObjectAppendable();
        // let consumer populate the nested object
        nestedObject.accept(nested);
        // add it
        return addProperty(key, nested);
    }

}
