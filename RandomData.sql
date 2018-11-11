-- phpMyAdmin SQL Dump
-- version 4.6.6deb5
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Nov 11, 2018 at 09:59 AM
-- Server version: 5.7.23-0ubuntu0.18.04.1
-- PHP Version: 7.2.10-0ubuntu0.18.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `RandomData`
--

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_EXECUTION`
--

CREATE TABLE `BATCH_JOB_EXECUTION` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `JOB_INSTANCE_ID` bigint(20) NOT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `STATUS` varchar(10) DEFAULT NULL,
  `EXIT_CODE` varchar(2500) DEFAULT NULL,
  `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
  `LAST_UPDATED` datetime DEFAULT NULL,
  `JOB_CONFIGURATION_LOCATION` varchar(2500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_EXECUTION_CONTEXT`
--

CREATE TABLE `BATCH_JOB_EXECUTION_CONTEXT` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `SHORT_CONTEXT` varchar(2500) NOT NULL,
  `SERIALIZED_CONTEXT` text
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_EXECUTION_PARAMS`
--

CREATE TABLE `BATCH_JOB_EXECUTION_PARAMS` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `TYPE_CD` varchar(6) NOT NULL,
  `KEY_NAME` varchar(100) NOT NULL,
  `STRING_VAL` varchar(250) DEFAULT NULL,
  `DATE_VAL` datetime DEFAULT NULL,
  `LONG_VAL` bigint(20) DEFAULT NULL,
  `DOUBLE_VAL` double DEFAULT NULL,
  `IDENTIFYING` char(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_EXECUTION_SEQ`
--

CREATE TABLE `BATCH_JOB_EXECUTION_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_INSTANCE`
--

CREATE TABLE `BATCH_JOB_INSTANCE` (
  `JOB_INSTANCE_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `JOB_NAME` varchar(100) NOT NULL,
  `JOB_KEY` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_JOB_SEQ`
--

CREATE TABLE `BATCH_JOB_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_STEP_EXECUTION`
--

CREATE TABLE `BATCH_STEP_EXECUTION` (
  `STEP_EXECUTION_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `STEP_NAME` varchar(100) NOT NULL,
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `START_TIME` datetime NOT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `STATUS` varchar(10) DEFAULT NULL,
  `COMMIT_COUNT` bigint(20) DEFAULT NULL,
  `READ_COUNT` bigint(20) DEFAULT NULL,
  `FILTER_COUNT` bigint(20) DEFAULT NULL,
  `WRITE_COUNT` bigint(20) DEFAULT NULL,
  `READ_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `WRITE_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `PROCESS_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `ROLLBACK_COUNT` bigint(20) DEFAULT NULL,
  `EXIT_CODE` varchar(2500) DEFAULT NULL,
  `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
  `LAST_UPDATED` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_STEP_EXECUTION_CONTEXT`
--

CREATE TABLE `BATCH_STEP_EXECUTION_CONTEXT` (
  `STEP_EXECUTION_ID` bigint(20) NOT NULL,
  `SHORT_CONTEXT` varchar(2500) NOT NULL,
  `SERIALIZED_CONTEXT` text
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `BATCH_STEP_EXECUTION_SEQ`
--

CREATE TABLE `BATCH_STEP_EXECUTION_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `hibernate_sequence`
--

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `random_data`
--

CREATE TABLE `random_data` (
  `id` bigint(20) NOT NULL,
  `batch_id` bigint(20) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `encoding` varchar(255) DEFAULT NULL,
  `random_data` varchar(255) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `BATCH_JOB_EXECUTION`
--
ALTER TABLE `BATCH_JOB_EXECUTION`
  ADD PRIMARY KEY (`JOB_EXECUTION_ID`),
  ADD KEY `JOB_INST_EXEC_FK` (`JOB_INSTANCE_ID`);

--
-- Indexes for table `BATCH_JOB_EXECUTION_CONTEXT`
--
ALTER TABLE `BATCH_JOB_EXECUTION_CONTEXT`
  ADD PRIMARY KEY (`JOB_EXECUTION_ID`);

--
-- Indexes for table `BATCH_JOB_EXECUTION_PARAMS`
--
ALTER TABLE `BATCH_JOB_EXECUTION_PARAMS`
  ADD KEY `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID`);

--
-- Indexes for table `BATCH_JOB_EXECUTION_SEQ`
--
ALTER TABLE `BATCH_JOB_EXECUTION_SEQ`
  ADD UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`);

--
-- Indexes for table `BATCH_JOB_INSTANCE`
--
ALTER TABLE `BATCH_JOB_INSTANCE`
  ADD PRIMARY KEY (`JOB_INSTANCE_ID`),
  ADD UNIQUE KEY `JOB_INST_UN` (`JOB_NAME`,`JOB_KEY`);

--
-- Indexes for table `BATCH_JOB_SEQ`
--
ALTER TABLE `BATCH_JOB_SEQ`
  ADD UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`);

--
-- Indexes for table `BATCH_STEP_EXECUTION`
--
ALTER TABLE `BATCH_STEP_EXECUTION`
  ADD PRIMARY KEY (`STEP_EXECUTION_ID`),
  ADD KEY `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID`);

--
-- Indexes for table `BATCH_STEP_EXECUTION_CONTEXT`
--
ALTER TABLE `BATCH_STEP_EXECUTION_CONTEXT`
  ADD PRIMARY KEY (`STEP_EXECUTION_ID`);

--
-- Indexes for table `BATCH_STEP_EXECUTION_SEQ`
--
ALTER TABLE `BATCH_STEP_EXECUTION_SEQ`
  ADD UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`);

--
-- Indexes for table `random_data`
--
ALTER TABLE `random_data`
  ADD PRIMARY KEY (`id`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `BATCH_JOB_EXECUTION`
--
ALTER TABLE `BATCH_JOB_EXECUTION`
  ADD CONSTRAINT `JOB_INST_EXEC_FK` FOREIGN KEY (`JOB_INSTANCE_ID`) REFERENCES `BATCH_JOB_INSTANCE` (`JOB_INSTANCE_ID`);

--
-- Constraints for table `BATCH_JOB_EXECUTION_CONTEXT`
--
ALTER TABLE `BATCH_JOB_EXECUTION_CONTEXT`
  ADD CONSTRAINT `JOB_EXEC_CTX_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`);

--
-- Constraints for table `BATCH_JOB_EXECUTION_PARAMS`
--
ALTER TABLE `BATCH_JOB_EXECUTION_PARAMS`
  ADD CONSTRAINT `JOB_EXEC_PARAMS_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`);

--
-- Constraints for table `BATCH_STEP_EXECUTION`
--
ALTER TABLE `BATCH_STEP_EXECUTION`
  ADD CONSTRAINT `JOB_EXEC_STEP_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`);

--
-- Constraints for table `BATCH_STEP_EXECUTION_CONTEXT`
--
ALTER TABLE `BATCH_STEP_EXECUTION_CONTEXT`
  ADD CONSTRAINT `STEP_EXEC_CTX_FK` FOREIGN KEY (`STEP_EXECUTION_ID`) REFERENCES `BATCH_STEP_EXECUTION` (`STEP_EXECUTION_ID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
