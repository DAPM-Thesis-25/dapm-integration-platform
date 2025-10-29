package com.dapm.security_service.controllers.events;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

@RestController
public class HospitalEventController {

    private static final Random RANDOM = new Random();
    private static final List<String> DEPARTMENTS =
            Arrays.asList("Emergency", "Cardiology", "Neurology", "Oncology", "Pediatrics");
    private static final List<String> ACTIVITIES =
            Arrays.asList("ADMISSION", "TRIAGE", "DIAGNOSIS", "TREATMENT", "DISCHARGE");

    @GetMapping(value = "/hospital/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> generateEvent());
    }

    private Map<String, Object> generateEvent() {
        String caseId = "PAT-" + (1000 + RANDOM.nextInt(9000));
        String department = DEPARTMENTS.get(RANDOM.nextInt(DEPARTMENTS.size()));
        String activity = ACTIVITIES.get(RANDOM.nextInt(ACTIVITIES.size()));
        String doctor = "Dr." + (char) ('A' + RANDOM.nextInt(26));
        int severity = RANDOM.nextInt(5) + 1;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("patientId", caseId);
        map.put("activity", activity);
        map.put("timestamp", System.currentTimeMillis());
        map.put("department", department);
        map.put("doctor", doctor);
        map.put("severity", severity);
        return map;
    }
}
