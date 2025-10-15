package com.graphinout.foundation.json;

import com.graphinout.foundation.xml.XML;

public class JSON {

    public enum XmlSpace {
        preserve("preserve"), ignore("ignore"), auto("default");

        public final String jsonStringValue;

        XmlSpace(String jsonStringValue) {
            this.jsonStringValue = jsonStringValue;
        }

        public XML.XmlSpace toXml_XmlSpace() {
            return switch (this) {
                case preserve -> XML.XmlSpace.preserve;
                // XML has no 'ignore' option
                case ignore, auto -> XML.XmlSpace.default_;
            };
        }
    }

}
