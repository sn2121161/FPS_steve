ALTER TABLE `ocpp_tag`
    ADD COLUMN `charging_profile_id` INT(11);


ALTER TABLE `ocpp_tag`
    ADD CONSTRAINT `FK_ocpp_tag_charging_profilepk`
        FOREIGN KEY (`charging_profile_id`) REFERENCES `charging_profile` (`charging_profile_pk`) ON DELETE SET NULL ON UPDATE NO ACTION;