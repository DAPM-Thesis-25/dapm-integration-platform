package com.dapm.security_service.repositories;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PetriNetRepository {
    private static PetriNetRepository INSTANCE;

    private final Map<Integer, String> svgStore = new ConcurrentHashMap<>();

    public PetriNetRepository() {
        INSTANCE = this;
    }

    public static PetriNetRepository getInstance() {
        return INSTANCE;
    }

    public void save(int instanceId, String svg) {
        svgStore.put(instanceId, svg);
    }

    public Optional<String> get(int instanceId) {
        return Optional.ofNullable(svgStore.get(instanceId));
    }

    public void remove(int instanceId) {
        svgStore.remove(instanceId);
    }
}

