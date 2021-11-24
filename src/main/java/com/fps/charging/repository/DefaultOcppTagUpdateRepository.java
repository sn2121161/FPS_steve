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
package com.fps.charging.repository;

import static jooq.steve.db.tables.OcppTag.OCPP_TAG;

import de.rwth.idsg.steve.SteveException;
import jooq.steve.db.tables.OcppTag;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.08.2014
 */
@Slf4j
@Repository
public class DefaultOcppTagUpdateRepository implements OcppTagUpdateRepository {

  private final DSLContext ctx;

  @Autowired
  public DefaultOcppTagUpdateRepository(DSLContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void updateOcppTagWithChargingProfile(String ocppTag, Integer chargingProfileId) {
    try {
      ctx.update(OCPP_TAG)
          .set(OCPP_TAG.CHARGING_PROFILE_ID, chargingProfileId)
          .where(OCPP_TAG.ID_TAG.equal(ocppTag))
          .execute();
    } catch (DataAccessException e) {
      throw new SteveException(
          "Execution of updateOcppTagWithChargingProfile for idTag '%s' FAILED.", ocppTag, e);
    }
  }

  @Override
  public void updateOcppTagWithChargingBoxId(String ocppTag, String chargingBoxId) {
    try {
      ctx.update(OCPP_TAG)
          .set(OCPP_TAG.CHARGE_BOX_ID, chargingBoxId)
          .where(OCPP_TAG.ID_TAG.equal(ocppTag))
          .execute();
    } catch (DataAccessException e) {
      throw new SteveException(
          "Execution of updateOcppTagWithChargingBoxId for idTag '%s' FAILED.", ocppTag, e);
    }
  }

  @Override
  public OcppTagRecord findByOcppTag(String ocppTag) {
    try {
      return ctx.selectFrom(OCPP_TAG)
          .where(OCPP_TAG.ID_TAG.equal(ocppTag))
          .fetchOne();
    } catch (DataAccessException e) {
      throw new SteveException(
          "Execution of findByOcppTag for idTag '%s' FAILED.", ocppTag, e);
    }
  }

}
