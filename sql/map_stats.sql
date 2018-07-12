DROP TABLE IF EXISTS `map_stats`;

CREATE TABLE `map_stats` 
(
	`id` INT(11) NOT NULL auto_increment,
	`name` VARCHAR(255),
	`times_played` INT(11),
	`max_game_duration` DECIMAL(20),
	`max_online_players` INT(11),
	`last_game_date` DECIMAL(20),
	PRIMARY KEY (`id`)
);