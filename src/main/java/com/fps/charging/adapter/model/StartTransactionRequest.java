package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"connectorId", "idTag", "timestamp", "meterStart", "reservationId"})
public class StartTransactionRequest {

  @JsonInclude(Include.ALWAYS)
  private Integer connectorId;

  @JsonInclude(Include.ALWAYS)
  private String idTag;

  @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
  private DateTime timestamp;

  @JsonInclude(Include.ALWAYS)
  private Integer meterStart;

  @JsonInclude(Include.ALWAYS)
  private Integer reservationId;
}
