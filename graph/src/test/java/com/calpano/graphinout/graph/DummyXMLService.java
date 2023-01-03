/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.graph;

import com.calpano.graphinout.xml.XMLConverter;
import com.calpano.graphinout.xml.XMLFileValidator;
import com.calpano.graphinout.xml.XMLService;
import com.calpano.graphinout.xml.XMLValidator;
import com.calpano.graphinout.xml.XMlValueMapper;
import com.calpano.graphinout.xml.XSD;
import java.util.List;

/**
 *
 * @author rbaba
 */
public class DummyXMLService implements XMLService<GioGraphML> {

    @Override
    public String getId() {
        return "DummyXMLService";
    }

    @Override
    public XSD getXsd() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public XMlValueMapper getXMlValueMapper() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public XMLFileValidator getXMLFileValidator() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<XMLValidator> getXMLValidators() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public XMLConverter<GioGraphML> getConverter() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
