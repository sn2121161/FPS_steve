package com.fps.charging.adapter.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSchedulePeriodDetail {

  private Integer startPeriod;
  private BigDecimal limit;
  private Integer numberPhases;
}
