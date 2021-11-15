package com.fps.charging.adapter.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingScheduleDetail {

  private Integer duration;
  private String startSchedule;
  private String chargingRateUnit;
  private BigDecimal minChargingRate;
  private List<ChargingSchedulePeriodDetail> chargingSchedulePeriod;
}
