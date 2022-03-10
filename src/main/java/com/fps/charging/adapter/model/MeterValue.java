package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.rwth.idsg.ocpp.jaxb.JodaDateTimeConverter;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"timestamp", "sampledValue"})
public class MeterValue {

  @XmlJavaTypeAdapter(JodaDateTimeConverter.class)
  protected DateTime timestamp;
  protected List<SampledValue> sampledValue;
}
