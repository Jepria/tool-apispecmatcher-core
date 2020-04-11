package org.jepria.tools.apispecmatcher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
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


  public static Map<String, Object> buildSchema(Type type) {
    if (type instanceof Class) {
      return buildSchema((Class<?>) type);
    } else if (type instanceof ParameterizedType) {
      return buildSchema((ParameterizedType) type);
    } else {
      throw new IllegalArgumentException("Unsupported type of [" + type + "]: "
              + (type == null ? "" : type.getClass().getCanonicalName()));
    }
  }

  public static Map<String, Object> buildSchema(ParameterizedType type) {
    // for the parameterized type A<B,C>: type.getRawType() is A; type.getActualTypeArguments() is [B, C].

    // TODO build schema for the parameterized type; fallback below:
    return buildSchema((Class<?>)type.getRawType());
  }

  /**
   *
   * @param type simple non-parameterized Class
   * @return
   */
  public static Map<String, Object> buildSchema(Class<?> type) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

    JsonSchema schema;
    try {
      schema = schemaGen.generateSchema(type);
    } catch (JsonMappingException e) {
      throw new RuntimeException(e);
    }

    // no way to convert JsonSchema to the Map directly, but through the string only.
    // see sources for com.fasterxml.jackson.module.jsonSchema.SchemaTestBase#writeAndMap

    String s;
    try {
      s = mapper.writeValueAsString(schema);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    Map<String,Object> map;
    try {
      map = (Map<String,Object>) mapper.readValue(s, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return map;
  }

}
