package com.dapm.security_service.repositories;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PetriNetRepository {
    private static PetriNetRepository INSTANCE;

    // normal fields
    private final Map<Integer, String> svgStore = new ConcurrentHashMap<>();
    private String lock = "m";
    private String petri = "mn";

    // --- constructor ensures Spring sets the instance ---
    public PetriNetRepository() {
        INSTANCE = this;
    }

    // --- static accessor used in PetriNetSink ---
    public static PetriNetRepository getInstance() {
        return INSTANCE;
    }

    // --- existing methods ---
    public void save(int instanceId, String svg) {
        svgStore.put(instanceId, svg);
    }

    public Optional<String> get(int instanceId) {
        return Optional.ofNullable(svgStore.get(instanceId));
    }

    public void remove(int instanceId) {
        svgStore.remove(instanceId);
    }

    public String getLock() {
        return lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }

    public String getPetri() {
        return petri;
    }

    public void setPetri(String petri) {
        this.petri = petri;
    }
}
