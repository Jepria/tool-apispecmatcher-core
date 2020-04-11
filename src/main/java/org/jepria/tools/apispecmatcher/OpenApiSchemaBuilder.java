package org.jepria.tools.apispecmatcher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class OpenApiSchemaBuilder {

  public static Map<String, Object> buildSchema(String className) {
    Class<?> type;
    try {
      type = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return buildSchema(type);
  }

  /**
   *
   * @param type simple non-parameterized {@link java.lang.Class} or complex {@link java.lang.reflect.ParameterizedType} to build schema for
   * @return
   */
  public static Map<String, Object> buildSchema(Type type) {
    // JavaType in contrast to Class allows to build schemas for ParameterizedTypes using jackson
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);

    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

    JsonSchema schema;
    try {
      schema = schemaGen.generateSchema(javaType);
    } catch (JsonMappingException e) {
      throw new RuntimeException(e);
    }

    return schemaToMap(schema);
  }

  public static Map<String, Object> schemaToMap(JsonSchema schema) {
    // no way to convert JsonSchema to the Map directly, but through the string only.
    // see sources for com.fasterxml.jackson.module.jsonSchema.SchemaTestBase#writeAndMap

    if (schema == null) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper();

    String s;
    try {
      s = mapper.writeValueAsString(schema);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    Map<String, Object> map;
    try {
      map = (Map<String, Object>) mapper.readValue(s, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return map;
  }
}
