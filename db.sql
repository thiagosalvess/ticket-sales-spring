CREATE DATABASE IF NOT EXISTS `tickets`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE `tickets`;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `partners` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `company_name` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_partners_user_id` (`user_id`),
  CONSTRAINT `fk_partners_users`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `customers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `address` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_customers_user_id` (`user_id`),
  CONSTRAINT `fk_customers_users`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `events` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255) NULL,
  `event_date` DATETIME NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `partner_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_events_partner_date` (`partner_id`, `event_date`),
  CONSTRAINT `fk_events_partners`
    FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tickets` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `location` VARCHAR(255) NOT NULL,
  `event_id` BIGINT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `status` ENUM('AVAILABLE','RESERVED','SOLD') NOT NULL DEFAULT 'AVAILABLE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_tickets_event_location` (`event_id`, `location`),
  KEY `ix_tickets_event_status` (`event_id`, `status`),
  CONSTRAINT `fk_tickets_events`
    FOREIGN KEY (`event_id`) REFERENCES `events` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `purchases` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT NOT NULL,
  `purchase_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total_amount` DECIMAL(12,2) NOT NULL,
  `status` ENUM('PENDING','PAID','ERROR','CANCELLED') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `ix_purchases_customer` (`customer_id`),
  KEY `ix_purchases_status_date` (`status`, `purchase_date`),
  CONSTRAINT `fk_purchases_customers`
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `purchase_tickets` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `purchase_id` BIGINT NOT NULL,
  `ticket_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_pt_ticket` (`ticket_id`),
  KEY `ix_pt_purchase` (`purchase_id`),
  CONSTRAINT `fk_pt_purchase`
    FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_pt_ticket`
    FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `reservation_tickets` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT NOT NULL,
  `ticket_id` BIGINT NOT NULL,
  `status` ENUM('RESERVED','CANCELLED') NOT NULL DEFAULT 'RESERVED',
  `reservation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_reservation_ticket` (`ticket_id`),
  KEY `ix_reservation_created` (`created_at`),
  CONSTRAINT `fk_reservation_customer`
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_ticket`
    FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `purchase_status_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `purchase_id` BIGINT NOT NULL,
  `from_status` ENUM('PENDING','PAID','ERROR','CANCELLED') NULL,
  `to_status` ENUM('PENDING','PAID','ERROR','CANCELLED') NOT NULL,
  `reason` VARCHAR(255) NULL,
  `changed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `ix_history_purchase` (`purchase_id`, `changed_at`),
  CONSTRAINT `fk_history_purchase`
    FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;