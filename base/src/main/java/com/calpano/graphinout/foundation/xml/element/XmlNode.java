package com.calpano.graphinout.foundation.xml.element;

import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.io.IOException;

public abstract class XmlNode {

    abstract void fire(XmlWriter writer) throws IOException;

}
