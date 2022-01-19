CREATE SCHEMA `dronepost` ;

CREATE TABLE `dronepost`.`clients` (
  `clientId` INT NOT NULL AUTO_INCREMENT,
  `personalId` CHAR(10) NOT NULL,
  `firstName` CHAR(30) NULL,
  `lastName` CHAR(30) NOT NULL,
  `dateOfBirth` DATE NULL,
  `street` CHAR(40) NOT NULL,
  `building` CHAR(5) NULL,
  `city` CHAR(30) NOT NULL,
  `zipCode` CHAR(10) NOT NULL,
  `email` CHAR(45) NOT NULL,
  `telephone` CHAR(15) NOT NULL Unique,
  `subscriptionCode` CHAR(1) NOT NULL,
  `posX` INT NOT NULL,
  `posY` INT NOT NULL,
  `numberOfDeliveriesLeft` CHAR(5),
  `isDeleted` INT(1) NOT NULL Default 0,
  PRIMARY KEY (`clientId`));
  
  CREATE TABLE `dronepost`.`orders` (
  `orderId` INT NOT NULL AUTO_INCREMENT,
  `senderId` INT NOT NULL,
  `recipientId` INT NOT NULL,
  `orderDateTime` DATETIME NOT NULL,
  `orderStatus` CHAR(20) NOT NULL,
  `assignedDroneId` INT(4) NOT NULL,
  `withReturn` INT(1) NOT NULL,
  `isDeleted` INT(1) NOT NULL Default 0,
  PRIMARY KEY (`orderId`),
  CONSTRAINT `senderId`
    FOREIGN KEY (`senderId`)
    REFERENCES `dronepost`.`clients` (`clientId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `recipientId`
    FOREIGN KEY (`recipientId`)
    REFERENCES `dronepost`.`clients` (`clientId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
  
  CREATE TABLE `dronepost`.`drones` (
  `droneId` INT NOT NULL Unique,
  `droneModel` CHAR(25) NOT NULL,
  `droneStatus` CHAR(10) NOT NULL,
  `isDeleted` INT(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`droneId`));

CREATE TABLE `dronepost`.`notifications` (
  `notificationId` INT NOT NULL AUTO_INCREMENT,
  `recipientId` INT NOT NULL,
  `orderId` INT NOT NULL,
  `sentDateTime` DATETIME NOT NULL,
  `message` VARCHAR(500) NULL,
  `delivered` INT(1) NULL DEFAULT 0,
  PRIMARY KEY (`notificationId`),
  INDEX `recipientId_idx` (`recipientId` ASC) ,
  INDEX `orderId_idx` (`orderId` ASC) ,
  CONSTRAINT `recipientIdConstraint`
    FOREIGN KEY (`recipientId`)
    REFERENCES `dronepost`.`clients` (`clientId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `orderIdConstraint`
    FOREIGN KEY (`orderId`)
    REFERENCES `dronepost`.`orders` (`orderId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE `dronepost`.`messages` (
      `messageId` INT NOT NULL AUTO_INCREMENT,
      `senderId` INT NOT NULL,
      `recipientId` INT NOT NULL,
      `sentDateTime` DATETIME NOT NULL,
      `message` VARCHAR(500) NULL,
      `delivered` INT(1) NULL DEFAULT 0,
      PRIMARY KEY (`messageId`),
      INDEX `senderId_idx` (`senderId` ASC) ,
      INDEX `recipientId_idx` (`recipientId` ASC) ,
      CONSTRAINT `senderIdConstraint1`
        FOREIGN KEY (`senderId`)
        REFERENCES `dronepost`.`clients` (`clientId`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
      CONSTRAINT `recipientIdConstraint1`
        FOREIGN KEY (`recipientId`)
        REFERENCES `dronepost`.`clients` (`clientId`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION);