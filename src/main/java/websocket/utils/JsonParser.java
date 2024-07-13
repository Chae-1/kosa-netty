package websocket.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonParser {
    private static final ObjectMapper objectMapper = initMapper();

    private static ObjectMapper initMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public static String objectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T stringToOjbect(String json, Class<T> convertClass) {
        try {
            return objectMapper.readValue(json, convertClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
