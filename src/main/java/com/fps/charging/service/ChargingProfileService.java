package com.fps.charging.service;

import com.fps.charging.adapter.model.ChargingProfileRequest;

public interface ChargingProfileService {

  void processChargingProfileMessage(ChargingProfileRequest chargingProfileRequest);

}
