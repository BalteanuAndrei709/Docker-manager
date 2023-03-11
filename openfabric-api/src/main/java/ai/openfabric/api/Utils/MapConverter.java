package ai.openfabric.api.Utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.util.Map;

public class MapConverter implements AttributeConverter<Map<Integer, Integer>, JsonNode> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode convertToDatabaseColumn(Map<Integer, Integer> attribute) {
        return mapper.valueToTree(attribute);
    }

    @Override
    public Map<Integer, Integer> convertToEntityAttribute(JsonNode dbData) {
        try {
            JavaType type = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, Integer.class);
            return mapper.treeToValue(dbData, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert JSON to map", e);
        }
    }
}