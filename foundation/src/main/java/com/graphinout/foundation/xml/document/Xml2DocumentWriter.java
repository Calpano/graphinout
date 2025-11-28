package com.graphinout.foundation.xml.document;

import com.graphinout.foundation.util.PowerStackOnClasses;
import com.graphinout.foundation.xml.factory.BaseXmlHandler;
import com.graphinout.foundation.xml.CharactersKind;
import com.graphinout.foundation.xml.IXmlName;
import com.graphinout.foundation.xml.sax.Sax2XmlWriter;
import com.graphinout.foundation.xml.util.XmlTool;
import com.graphinout.foundation.xml.writer.XmlWriter;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class assumes that an XML parser (like Java SAX2) parsed XML and sends events via {@link Sax2XmlWriter} or an
 * equivalent mechanism to this {@link XmlWriter} implementation. IN particular, XML decoding of the character inputs
 * has already happened.
 */
public class Xml2DocumentWriter extends BaseXmlHandler implements XmlWriter {

    PowerStackOnClasses<IXmlNode> stack = PowerStackOnClasses.create();
    @Nullable XmlDocument resultDoc;
    private @Nullable Consumer<XmlDocument> onDone;

    public Xml2DocumentWriter(@Nullable Consumer<XmlDocument> onDone) {
        this.onDone = onDone;
    }

    public Xml2DocumentWriter() {
        this(null);
    }

    /** read into doc */
    public static XmlDocument parseToDoc(String xml) throws Exception {
        Xml2DocumentWriter xmlWriter2XmlDocument = new Xml2DocumentWriter();
        XmlTool.parseAndWriteXml(xml, xmlWriter2XmlDocument);
        return xmlWriter2XmlDocument.resultDoc();
    }

    /**
     * @param xmlFragment has no xmlDeclaration
     */
    public static XmlContent parseToXmlContent(String xmlFragment) throws Exception {
        XmlDocument doc = parseToDoc("<wrapperRoot>" + xmlFragment + "</wrapperRoot>");
        XmlElement wrapperRoot = doc.rootElement();
        XmlContent content = wrapperRoot;
        return content;
    }

    public void characters(String characters, CharactersKind kind) {
        if (characters.isEmpty()) return;
        assert kind != CharactersKind.IgnorableWhitespace || characters.trim().isEmpty();
        stack.peek(XmlText.class).addSection(characters, kind);
    }

    public void charactersEnd() {
        stack.pop(XmlText.class);
    }

    public void charactersStart() {
        XmlElement parent = stack.peek(XmlElement.class);
        XmlText text = stack.push(new XmlText());
        parent.addChild(text);
    }

    @Override
    public void documentEnd() {
        this.resultDoc = stack.pop(XmlDocument.class);
    }

    @Override
    public void documentStart() {
        stack.push(new XmlDocument());
    }

    @Override
    public void elementEnd(String uri, String localName, String qName) {
        stack.pop(XmlElement.class);
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) {
        IXmlNode parent = stack.peek();
        IXmlName xmlName = IXmlName.of(uri, localName, qName);
        if (parent instanceof XmlDocument doc) {
            XmlElement child = stack.push(new XmlElement(null, xmlName, attributes));
            doc.setRoot(child);
        } else if (parent instanceof XmlElement parentElement) {
            XmlElement child = stack.push(new XmlElement(parentElement, xmlName, attributes));
            parentElement.addChild(child);
        } else {
            throw new IllegalStateException("Unexpected parent '" + parent + "'");
        }
    }

    @Override
    public void lineBreak() {
    }

    /**
     * When the {@link Xml2DocumentWriter} gets sent a raw XML snippet, that snippet is stored in the DOM unparsed.
     *
     * @param rawXml may contain line breaks, processing instructions and any other syntax constructs. MUST be a valid
     *               XML fragment to get valid XML.
     */
    @Override
    public void raw(String rawXml) {
        stack.peek(XmlElement.class).addChild(new XmlRaw(rawXml));
    }

    public void reset() {
        this.resultDoc = null;
        this.stack.reset();
    }

    @Nullable
    public XmlDocument resultDoc() {
        return resultDoc;
    }


}
