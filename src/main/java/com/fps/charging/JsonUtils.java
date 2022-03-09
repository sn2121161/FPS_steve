/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2021 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fps.charging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import de.rwth.idsg.steve.ocpp.ws.JsonObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {

  private static ObjectMapper mapper = JsonObjectMapper.INSTANCE.getMapper();

  static {
    registerModule(mapper);
  }

  public static <T> T toObject(String jsonString, Class<T> clazz) {
    T jsonObject;
    if (jsonString == null) {
      jsonObject = null;
    } else {
      try {
        registerModule(mapper);

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
//    mapper.registerModule(new JavaTimeModule());
  }
}
