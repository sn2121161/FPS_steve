/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2021 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fps.charging.adapter.mapper;

import com.fps.charging.adapter.model.ChargingProfileDetail;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import com.fps.charging.adapter.model.ChargingSchedulePeriodDetail;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm.SchedulePeriod;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ocpp.cp._2015._10.ChargingProfileKindType;
import ocpp.cp._2015._10.ChargingProfilePurposeType;
import ocpp.cp._2015._10.ChargingRateUnitType;
import ocpp.cp._2015._10.RecurrencyKindType;
import org.joda.time.LocalDateTime;

public class ChargingProfileMapper {

  // THis method converts FPS charging profile message to SteVe format
  public static ChargingProfileForm toChargingProfile(
      ChargingProfileRequest chargingProfileRequest) {

    ChargingProfileForm form = new ChargingProfileForm();

    ChargingProfileDetail chargingProfile = chargingProfileRequest.getChargingProfile();
    form.setDescription("description");
    form.setNote("note");
    form.setStackLevel(chargingProfile.getStackLevel());
    form.setChargingProfilePurpose(
        ChargingProfilePurposeType.fromValue(chargingProfile.getChargingProfilePurpose()));
    form.setChargingProfileKind(
        ChargingProfileKindType.fromValue(chargingProfile.getChargingProfileKind()));
    form.setRecurrencyKind(chargingProfile.getRecurrencyKind() == null ? null
        : RecurrencyKindType.fromValue(chargingProfile.getRecurrencyKind()));
    form.setDurationInSeconds(chargingProfile.getChargingSchedule().getDuration());
    form.setStartSchedule(
        new LocalDateTime(chargingProfile.getChargingSchedule().getStartSchedule()));
    form.setChargingRateUnit(ChargingRateUnitType.fromValue(
        chargingProfile.getChargingSchedule().getChargingRateUnit()));
    form.setMinChargingRate(chargingProfile.getChargingSchedule().getMinChargingRate());

    Map<String, SchedulePeriod> periodMap = new LinkedHashMap<>();
    List<ChargingSchedulePeriodDetail> chargingSchedulePeriod = chargingProfile.getChargingSchedule()
        .getChargingSchedulePeriod();
    for (ChargingSchedulePeriodDetail rec : chargingSchedulePeriod) {
      ChargingProfileForm.SchedulePeriod p = new ChargingProfileForm.SchedulePeriod();
      p.setStartPeriodInSeconds(rec.getStartPeriod());
      p.setPowerLimit(rec.getLimit());
      p.setNumberPhases(rec.getNumberPhases());
      periodMap.put(UUID.randomUUID().toString(), p);
    }
    form.setSchedulePeriodMap(periodMap);

    return form;
  }

}
