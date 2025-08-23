package templates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import org.springframework.web.reactive.function.client.WebClient;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.WebSource;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class EventSource extends WebSource<Event> {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://stream.wikimedia.org/v2/stream/recentchange")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventSource(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected Flux<Event> process() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .handle((incomingEvent, sink) -> {
                    try {
                        JsonNode root = objectMapper.readTree(incomingEvent);
                        JsonNode titleNode = root.get("user");
                        JsonNode typeNode = root.get("type");
                        JsonNode timestampNode = root.get("timestamp");
                        JsonNode metaNode = root.get("meta");
                        JsonNode domain = metaNode.get("domain");
                        if (titleNode != null && typeNode != null && timestampNode != null && domain != null) {
                            String title = titleNode.asText();
                            String type = typeNode.asText();
                            String timestamp = timestampNode.asText();
                            Set<Attribute<?>> attributes = new HashSet<>();
                            Attribute<String> language = new Attribute<>("domain", domain.asText());
                            attributes.add(language);
                            sink.next(new Event(title, type, timestamp, attributes));
                        }
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }
}
