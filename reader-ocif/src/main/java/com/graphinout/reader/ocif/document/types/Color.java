package com.graphinout.reader.ocif.document.types;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.regex.Pattern;

public class Color extends OcifType {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    final String value;

    public Color(String value) {
        if (!HEX_COLOR_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid hex color format. Expected #RGB, #RGBA, #RRGGBB, or #RRGGBBAA.");
        }
        this.value = value;
    }

    public static Color of(IJsonValue jsonValue) throws IllegalStateException {
        return new Color(jsonValue.asString());
    }

    @Override
    IJsonValue toJson() {
        return IJsonFactory.INSTANCE.createString(value);
    }

}
