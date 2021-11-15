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
//    form.setValidFrom(DateTimeUtils.toLocalDateTime(profile.getValidFrom()));
//    form.setValidTo(DateTimeUtils.toLocalDateTime(profile.getValidTo()));
    form.setDurationInSeconds(chargingProfile.getChargingSchedule().getDuration());
    form.setStartSchedule( new LocalDateTime(chargingProfile.getChargingSchedule().getStartSchedule()));
    form.setChargingRateUnit(ChargingRateUnitType.fromValue(chargingProfile.getChargingSchedule().getChargingRateUnit()));
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
