package com.fps.charging.adapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingProfileRequest {

  private String idTag;
  private ChargingProfileDetail chargingProfile;
}
