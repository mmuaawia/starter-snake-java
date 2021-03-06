package io.battlesnake.starter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnakeApplicationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private SnakeApplication.Handler handler;

    @BeforeEach
    void setUp() {
        handler = new SnakeApplication.Handler();
    }

    @Test
    void pingTest() throws IOException {
        Map<String, String> response = handler.ping();
        assertEquals("{}", response.toString());
    }

    @Ignore
    void startTest() throws IOException {
        JsonNode startRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.start(startRequest);
        assertEquals("#ff00ff", response.get("color"));
    }

    @Ignore
    void moveTest() throws IOException {
        JsonNode moveRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.move(moveRequest);
        assertEquals("right", response.get("move"));
    }

    @Test //@Ignore
    void endTest() throws IOException {
        JsonNode endRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(endRequest);
        assertEquals(0, response.size());
    }
}