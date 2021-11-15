package com.fps.charging.adapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingProfileDetail {

  private Integer stackLevel;
  private String chargingProfilePurpose;
  private String chargingProfileKind;
  private String recurrencyKind;
  private ChargingScheduleDetail chargingSchedule;
}
