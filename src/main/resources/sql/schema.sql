CREATE TABLE IF NOT EXISTS `bilibili_user`(
    `id` IDENTITY NOT NULL AUTO_INCREMENT,
    `dedeuserid` VARCHAR NOT NULL ,
    `username` VARCHAR NOT NULL ,
    `coins` DOUBLE DEFAULT 0,
    `current_exp` INT NOT NULL ,
    `next_exp` INT NOT NULL ,
    `is_login` BOOLEAN DEFAULT FALSE,
    `upgrade_days` INT NULL ,
    `level` INT DEFAULT 1,
    `medals` VARCHAR NULL ,
    `vip_status` BOOLEAN DEFAULT FALSE,
    `vip_type` TINYINT DEFAULT 0,
    primary key (`id`)
);

CREATE TABLE IF NOT EXISTS `task_config` (
    `id` IDENTITY NOT NULL AUTO_INCREMENT,
    `dedeuserid` VARCHAR NOT NULL ,
    `sessdata` VARCHAR NOT NULL ,
    `bili_jct` VARCHAR NOT NULL ,
    `donate_coins` INT DEFAULT 0,
    `reserve_coins` INT DEFAULT 50,
    `auto_charge` BOOLEAN DEFAULT FALSE,
    `donate_gift` BOOLEAN DEFAULT FALSE,
    `donate_gift_target` VARCHAR NULL ,
    `auto_charge_target` VARCHAR NULL ,
    `device_platform` VARCHAR DEFAULT 'ios',
    `donate_coin_strategy` INT DEFAULT 0,
    `user_agent` VARCHAR DEFAULT 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36',
    `skip_task` BOOLEAN DEFAULT FALSE,
    `tg_bot_token` VARCHAR NULL ,
    `tg_bot_chat_id` VARCHAR NULL ,
    `sc_key` VARCHAR NULL ,
    `corp_id` VARCHAR NULL ,
    `corp_secret` VARCHAR NULL ,
    `agent_id` VARCHAR NULL ,
    `media_id` VARCHAR NULL ,
    `bili_push` BOOLEAN DEFAULT FALSE,
    `follow_developer` BOOLEAN DEFAULT FALSE,
    primary key (`id`)
)

