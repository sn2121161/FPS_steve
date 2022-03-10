package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.Reason;
import org.joda.time.DateTime;


@Data
@Builder
@JsonPropertyOrder({"transactionId", "idTag", "timestamp", "meterStop", "reason", "transactionData"})
public class StopTransactionRequest {
  private Integer transactionId;
  private String idTag;
  @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
  private DateTime timestamp;
  private Integer meterStop;
  private String reason;
  private List<MeterValue> transactionData;
}
