package com.dapm.security_service.ingest_anonymization.utils;

import communication.message.impl.Trace;
import communication.message.impl.event.Event;

import java.util.List;

public class JXESUtil {
    public static String toJXES(Event event) {
        Trace trace = new Trace(List.of(event));
        return JXESConverter.convertTraceToJXES(trace);
    }
}
