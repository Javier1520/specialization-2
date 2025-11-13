package com.epam.gym.storage;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper component to map CSV file IDs to actual database IDs during data loading.
 */
@Component
public class DataIDMapper {
    private final Map<String, Map<Long, Long>> idMaps = new HashMap<>();

    public void addMapping(String type, Long csvId, Long dbId) {
        idMaps.computeIfAbsent(type, k -> new HashMap<>()).put(csvId, dbId);
    }

    public Long getMappedId(String type, Long csvId) {
        return idMaps.getOrDefault(type, Map.of())
                .getOrDefault(csvId, csvId);
    }
}