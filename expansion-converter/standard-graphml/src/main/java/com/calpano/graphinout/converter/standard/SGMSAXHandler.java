/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.graph.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author rbaba
 */
@Data
@Slf4j
public class SGMSAXHandler extends DefaultHandler {
    private Stack<String> elementsNames = new Stack<>();

    private GioGraphML gioGraphML = new GioGraphML();

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        super.endElement(uri, localName, qName); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("graphml")) {
            startElementGraphML(uri, localName, qName, attributes);

        } else if (qName.equalsIgnoreCase("key")) {
            startElementKey(uri, localName, qName, attributes);
        } else if (qName.equalsIgnoreCase("graph")) {
            startElementGraph(uri, localName, qName, attributes);
        }
        else if (qName.equalsIgnoreCase("node")) {
            startElementNode(uri, localName, qName, attributes);
        }
        elementsNames.add(qName);
        super.startElement(uri, localName, qName, attributes); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }


    private void startElementGraphML(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String id = attributes.getValue("id");
        gioGraphML.setId(id);

    }
    private void startElementKey(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        String name = attributes.getValue("attr.name");
        String type = attributes.getValue("attr.type");
        String id = attributes.getValue("id");
        String defaultValue = attributes.getValue("default");
        GioKey key = new GioKey(name, type, id, defaultValue);
        if (gioGraphML.getKeys() == null)
            gioGraphML.setKeys(new ArrayList<>());
        gioGraphML.getKeys().add(key);

    }

    private void startElementGraph(String uri, String localName, String qName, Attributes attributes) throws SAXException {

         String edgedefault=attributes.getValue("edgedefault");
         String id=attributes.getValue("id");
         if(gioGraphML.getGraphs()==null)
             gioGraphML.setGraphs(new ArrayList<>());
         gioGraphML.getGraphs().add(gioGraphML.getGraphs().size(),new GioGraph(id,edgedefault));

    }
    private void startElementNode(String uri, String localName, String qName, Attributes attributes) throws SAXException {


        String id=attributes.getValue("id");
        if(gioGraphML.getGraphs().get(gioGraphML.getGraphs().size()-1)==null
       ) {
      //TODO error
        }
        if(gioGraphML.getGraphs().get(gioGraphML.getGraphs().size()-1).getNodes()==null)
            gioGraphML.getGraphs().get(gioGraphML.getGraphs().size()-1).setNodes(new ArrayList<>());


    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }
}
