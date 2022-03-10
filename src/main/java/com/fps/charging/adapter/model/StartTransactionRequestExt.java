package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ocpp.cs._2015._10.StartTransactionRequest;

@JsonSerialize
public class StartTransactionRequestExt extends StartTransactionRequest {

  @Override
  @JsonProperty("reservationId")
  @JsonInclude(Include.ALWAYS)
  public void setReservationId(Integer value) {
    super.setReservationId(value);
  }

  @Override
  public Integer getReservationId() {
    return super.getReservationId();
  }
}
