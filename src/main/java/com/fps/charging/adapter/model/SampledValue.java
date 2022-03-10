package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.Location;
import ocpp.cs._2015._10.Measurand;
import ocpp.cs._2015._10.Phase;
import ocpp.cs._2015._10.ReadingContext;
import ocpp.cs._2015._10.UnitOfMeasure;
import ocpp.cs._2015._10.ValueFormat;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"value", "context", "format", "measurand", "phase", "location", "unit"})
public class SampledValue {

  @JsonInclude(Include.ALWAYS)
  private String value;

  @JsonInclude(Include.ALWAYS)
  private String context;

  @JsonInclude(Include.ALWAYS)
  private ValueFormat format;
  @JsonInclude(Include.ALWAYS)
  private Measurand measurand;
  @JsonInclude(Include.ALWAYS)
  private Phase phase;
  @JsonInclude(Include.ALWAYS)
  private Location location;
  @JsonInclude(Include.ALWAYS)
  private UnitOfMeasure unit;
}
