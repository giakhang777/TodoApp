-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: todoapp
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `label`
--

DROP TABLE IF EXISTS `label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `label` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm6qinh9q8j4ecafw3d32mdvra` (`title`,`user_id`),
  KEY `FKoxgv2i3tt8m6y1pyfmutblhf2` (`user_id`),
  CONSTRAINT `FKoxgv2i3tt8m6y1pyfmutblhf2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `label`
--

LOCK TABLES `label` WRITE;
/*!40000 ALTER TABLE `label` DISABLE KEYS */;
INSERT INTO `label` VALUES (10,'demo',1),(9,'LTDD',1),(13,'LTDD',8),(2,'read',1),(5,'write',1);
/*!40000 ALTER TABLE `label` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo06v2e9kuapcugnyhttqa1vpt` (`user_id`),
  CONSTRAINT `FKo06v2e9kuapcugnyhttqa1vpt` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (13,'2025-05-12 18:52:13.166866','2025-05-12 18:52:13.167866','#FF0000','Project 7',2),(14,'2025-05-12 19:20:31.223496','2025-05-12 19:20:31.223496','#FFAA00','Project 14',2),(23,'2025-05-15 18:06:34.858941','2025-05-15 18:06:34.858941','#00FFFF','LapTrinhDiDong',1),(28,'2025-05-15 19:16:19.008363','2025-05-15 19:16:19.008363','#00FFFF','LapTrinhDiDong',8),(33,'2025-05-16 07:55:09.760663','2025-05-16 07:55:09.760663','#00FFFF','LapTrinhDiDong',9);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sub_task`
--

DROP TABLE IF EXISTS `sub_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sub_task` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `completed` bit(1) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `task_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sub_task`
--

LOCK TABLES `sub_task` WRITE;
/*!40000 ALTER TABLE `sub_task` DISABLE KEYS */;
INSERT INTO `sub_task` VALUES (17,'2025-05-14 16:06:51.988395','2025-05-14 16:07:58.023217',_binary '','Review final project 1',50),(18,'2025-05-14 16:06:55.541341','2025-05-15 00:20:03.696176',_binary '','Review final project 2',50),(19,'2025-05-14 16:06:58.496889','2025-05-15 00:35:33.355196',_binary '','Review final project 3',50),(20,'2025-05-14 16:07:01.609223','2025-05-15 00:38:59.822127',_binary '','Review final project 4',50),(41,'2025-05-15 19:19:17.436868','2025-05-16 07:14:51.592339',_binary '','sub task 1',71),(42,'2025-05-15 19:19:26.516452','2025-05-16 07:14:51.598417',_binary '','sub task 2',71),(44,'2025-05-16 07:57:21.596851','2025-05-16 07:57:43.598063',_binary '','sub task 1',76),(45,'2025-05-16 07:57:30.260541','2025-05-16 07:57:30.260541',_binary '\0','sub task 2',76);
/*!40000 ALTER TABLE `sub_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed` bit(1) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `priority` varchar(255) DEFAULT NULL,
  `reminder` bit(1) DEFAULT NULL,
  `reminder_time` datetime(6) DEFAULT NULL,
  `project_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `label_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk8qrwowg31kx7hp93sru1pdqa` (`project_id`),
  KEY `FKcvxhsvaa4b0eqvoknwdjoqb8e` (`label_id`),
  KEY `FK2hsytmxysatfvt0p1992cw449` (`user_id`),
  CONSTRAINT `FK2hsytmxysatfvt0p1992cw449` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKcvxhsvaa4b0eqvoknwdjoqb8e` FOREIGN KEY (`label_id`) REFERENCES `label` (`id`),
  CONSTRAINT `FKk8qrwowg31kx7hp93sru1pdqa` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (23,_binary '','Build UI','Complete the final report for the project','2025-05-13','High',_binary '','2025-05-09 09:00:00.000000',14,'2025-05-12 19:21:09.387678','2025-05-14 15:05:30.071859',2,2),(24,_binary '\0','Build UI 2','Complete the final report for the project','2025-05-14','High',_binary '','2025-05-09 09:00:00.000000',14,'2025-05-12 20:47:32.635780','2025-05-12 20:47:32.635780',2,2),(44,_binary '\0','cccccc','ccccccc','2025-05-16','Low',_binary '\0',NULL,NULL,'2025-05-14 00:17:32.540767','2025-05-14 00:17:32.540767',NULL,1),(50,_binary '\0','SubTask','TestSubTask','2025-05-20','Low',_binary '','2025-05-20 16:06:00.000000',NULL,'2025-05-14 16:06:24.707382','2025-05-14 16:06:24.707382',NULL,1),(53,_binary '\0','aaaaaa','dddddddd','2025-05-14','Low',_binary '\0',NULL,NULL,'2025-05-14 18:19:28.351464','2025-05-15 00:40:19.455665',NULL,1),(54,_binary '','aaaaa','ddddddd','2025-05-14','Low',_binary '\0',NULL,NULL,'2025-05-14 18:26:56.555054','2025-05-14 18:41:40.222157',NULL,1),(55,_binary '\0','bbbbbbbb','bbbbbbbb','2025-05-14','Low',_binary '\0',NULL,NULL,'2025-05-14 18:30:50.891124','2025-05-14 18:30:50.891124',NULL,1),(56,_binary '\0','ccccccccc','ddddddddd','2025-05-14','Low',_binary '\0',NULL,NULL,'2025-05-14 18:31:07.826329','2025-05-14 18:31:27.618167',NULL,1),(57,_binary '\0','zzzzzz',NULL,'2025-05-14','Low',_binary '\0',NULL,NULL,'2025-05-14 18:36:23.470444','2025-05-14 18:36:23.470444',NULL,1),(63,_binary '\0','BaoCao','Viet bao cao mon LTDD','2025-05-15','High',_binary '','2025-05-15 18:30:00.000000',23,'2025-05-15 18:07:10.610613','2025-05-15 18:07:50.983806',9,1),(71,_binary '','Task 1','them task co project va label','2025-05-15','High',_binary '','2025-05-15 19:20:00.000000',28,'2025-05-15 19:17:58.990632','2025-05-16 07:14:51.592339',13,8),(73,_binary '','task 3','them task khong reminder','2025-05-15','Low',_binary '\0',NULL,NULL,'2025-05-15 19:18:58.791869','2025-05-16 07:14:52.058814',NULL,8),(74,_binary '','task 1','demo','2025-05-15','High',_binary '','2025-05-15 21:32:00.000000',28,'2025-05-15 21:32:27.680478','2025-05-16 07:14:52.630208',13,8),(75,_binary '','task 2','demo','2025-05-14','Medium',_binary '','2025-05-15 21:32:00.000000',28,'2025-05-15 21:32:43.506893','2025-05-16 07:14:50.497993',13,8),(76,_binary '\0','Task 1t','them task vao project','2025-05-16','High',_binary '','2025-05-16 07:57:00.000000',33,'2025-05-16 07:56:14.816427','2025-05-16 07:57:10.433168',NULL,9);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlqjrcobrh9jc8wpcar64q1bfh` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,'nguyengiakhang116@gmail.com','MALE',_binary '','$2a$10$kcOzhVfk0fzW4JXDdZx5KuOWoIeu6CEQA.zqUkGbozm17efjREKwq','khang'),(2,NULL,'votranminhtrisuper123@gmail.com','MALE',_binary '','$2a$10$398/AA5vIcxKkoaFshxge.aHCH.OGpw6e3ZWSlJNJXiVJV/mUVHpm','tri'),(8,'https://res.cloudinary.com/dxhniukul/image/upload/v1747355197/user_avatar_1747355193149.jpg','22110347@student.hcmute.edu.vn','MALE',_binary '','$2a$10$hu7222a5dg/EOrQKuYq28uDAsG0BIgkbCoal8q86qqW9lUKkjS.4u','giakhang'),(9,NULL,'nvh11103@gmail.com','MALE',_binary '','$2a$10$EuPg3fBbmSmD6WPjqE.YauxRkcd9tI1tRLfY5UYWOloo/IQWLKjTC','hung');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-18 14:28:17
