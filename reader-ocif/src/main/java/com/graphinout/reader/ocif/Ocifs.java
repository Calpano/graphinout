package com.graphinout.reader.ocif;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;

public class Ocifs {

    public static IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

}
