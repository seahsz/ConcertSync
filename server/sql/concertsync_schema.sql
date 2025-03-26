DROP DATABASE IF EXISTS concertsync;

CREATE DATABASE concertsync;

USE concertsync;

CREATE TABLE artists (
	id BIGINT AUTO_INCREMENT NOT NULL,
	name VARCHAR(256) NOT NULL,
	spotify_id VARCHAR(128) NOT NULL UNIQUE,
	image_url_640 VARCHAR(256),
	image_url_320 VARCHAR(256),
	image_url_160 VARCHAR(256),
	
	CONSTRAINT pk_artist_id PRIMARY KEY (id)
);

CREATE TABLE concerts(
	id BIGINT AUTO_INCREMENT NOT NULL,
	artist_id BIGINT NOT NULL,
	artist VARCHAR(64) NOT NULL,
	venue VARCHAR(128) NOT NULL,
	country VARCHAR(64) NOT NULL,
	tour VARCHAR(256),
	
	CONSTRAINT unique_Concert UNIQUE (artist, venue, country),
	CONSTRAINT pk_concert_id PRIMARY KEY (id),
	CONSTRAINT fk_artist_id FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE RESTRICT
);

CREATE TABLE concert_dates(
	concert_id BIGINT NOT NULL,
	date DATE NOT NULL,
	
	CONSTRAINT pk_concert_id_date PRIMARY KEY (concert_id, date),
	CONSTRAINT fk_concert_id FOREIGN KEY (concert_id) REFERENCES concerts(id) ON DELETE CASCADE,
	INDEX idx_date (date)
);

CREATE TABLE users (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(50) UNIQUE NOT NULL,
	email VARCHAR(255) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	name VARCHAR(100) NOT NULL,
	birth_date DATE NOT NULL,
	profile_picture_url VARCHAR(255),
	phone_number VARCHAR(20),
	email_verified BOOLEAN DEFAULT FALSE,
	email_verification_token VARCHAR(36),
	premium_status BOOLEAN DEFAULT FALSE,
	premium_expiry DATE,
	subscription_id VARCHAR(255),
	auto_renew BOOLEAN DEFAULT TRUE,
	groups_created INT DEFAULT 0,
	last_name_update DATE DEFAULT(CURRENT_DATE),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Groups table (renamed to "concert_groups" to avoid reserved word)
CREATE TABLE concert_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    concert_id BIGINT NOT NULL,
    concert_date DATE NOT NULL,
    creator_id BIGINT NOT NULL,
    capacity INT NOT NULL,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_group_concert FOREIGN KEY (concert_id) REFERENCES concerts(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_concert_date (concert_id, concert_date)
);

-- Group members junction table (updated references)
CREATE TABLE group_members (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES concert_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Group chat messages (updated references)
CREATE TABLE group_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_message_group FOREIGN KEY (group_id) REFERENCES concert_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_group_messages (group_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

CREATE TABLE deleted_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    phone_number VARCHAR(20),
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    groups_json TEXT COMMENT 'JSON array of group IDs the user was part of'
);