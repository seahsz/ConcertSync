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
	last_name_update DATE DEFAULT(CURRENT_DATE),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);