/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.xml.XMlValueMapper;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author rbaba
 */
public class SGMLValueMapper implements XMlValueMapper {

    private final static Map<String, String> MAP_STORAGE = new TreeMap<>();

    @Override
    public String mapper(String value) {
        if (MAP_STORAGE.containsKey(value)) {
            return MAP_STORAGE.get(value);
        }
        return value;
    }

}
