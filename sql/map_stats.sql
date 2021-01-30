DROP TABLE IF EXISTS `map_stats`;
CREATE TABLE `map_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `times_played` int(11) DEFAULT NULL,
  `max_game_duration` decimal(20,0) DEFAULT NULL,
  `max_online_players` int(11) DEFAULT NULL,
  `last_game_date` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
