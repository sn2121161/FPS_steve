package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ocpp.cs._2015._10.ChargePointErrorCode;
import ocpp.cs._2015._10.ChargePointStatus;
import org.joda.time.DateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"connectorId", "status", "errorCode", "info", "timestamp", "vendorId",
    "vendorErrorCode"})
public class StatusNotificationRequest {

  @JsonInclude(Include.ALWAYS)
  protected int connectorId;

  @JsonInclude(Include.ALWAYS)
  protected String status;

  @JsonInclude(Include.ALWAYS)
  protected String errorCode;

  @JsonInclude(Include.ALWAYS)
  protected String info;

  @JsonInclude(Include.ALWAYS)
  @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
  protected DateTime timestamp;

  @JsonInclude(Include.ALWAYS)
  protected String vendorId;

  @JsonInclude(Include.ALWAYS)
  protected String vendorErrorCode;
}
