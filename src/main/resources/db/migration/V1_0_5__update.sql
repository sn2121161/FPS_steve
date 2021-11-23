ALTER TABLE `ocpp_tag`
    ADD COLUMN `connector_id` INT(11);

ALTER TABLE `ocpp_tag`
    ADD COLUMN `charge_box_id` varchar(255);