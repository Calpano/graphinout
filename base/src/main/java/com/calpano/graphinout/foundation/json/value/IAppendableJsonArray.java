package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonException;

import java.util.function.Consumer;

/** Mutable */
public interface IAppendableJsonArray extends IJsonArray {

    void add(IJsonValue jsonValue);

}
