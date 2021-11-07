package com.fps.charging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    registerModule(mapper);
  }

  public static <T> T toObject(String jsonString, Class<T> clazz) {
    T jsonObject;
    if (jsonString == null) {
      jsonObject = null;
    } else {
      try {
        jsonObject = mapper.readValue(jsonString, clazz);
      } catch (Exception e) {
        jsonObject = null;
      }
    }

    return jsonObject;
  }

  public static <T> String toJson(T object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void registerModule(ObjectMapper mapper) {
    mapper.registerModule(new JsonOrgModule());
    mapper.registerModule(new JodaModule());
    // mapper.registerModule(new JavaTimeModule());
  }
}
