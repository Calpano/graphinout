package com.graphinout.reader.graphml;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class SchemaInfo implements LSInput {

    final String schemaContent;
    private final String baseUri;
    private final String publicId;
    private final String systemId;

    public SchemaInfo(String schemaContent, String baseUri, String publicId, String systemId) {
        this.schemaContent = schemaContent;
        this.baseUri = baseUri;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    @Override
    public String getBaseURI() {
        return baseUri;
    }

    @Override
    public InputStream getByteStream() {
        return null;
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    @Override
    public Reader getCharacterStream() {
        return new StringReader(schemaContent);
    }

    @Override
    public String getEncoding() {
        return StandardCharsets.UTF_8.toString();
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public String getStringData() {
        return schemaContent;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public void setBaseURI(String baseURI) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEncoding(String encoding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPublicId(String publicId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStringData(String stringData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSystemId(String systemId) {
        throw new UnsupportedOperationException();
    }

}
