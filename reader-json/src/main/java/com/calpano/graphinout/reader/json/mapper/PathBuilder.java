package com.calpano.graphinout.reader.json.mapper;

import java.util.Set;

public interface PathBuilder {
    String findAll();

    String findAllId();

    String findById(String id);

    String findById(Integer id);

    String findByLabel(String label);

    Set<String> findLink(String id);

    Set<String> findLink(Integer id);


}
