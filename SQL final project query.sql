create database skyport;
use skyport;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100),
  age INT,
  city VARCHAR(100),
  gender VARCHAR(20),
  email VARCHAR(100) UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
select *from users;

CREATE TABLE IF NOT EXISTS airlines (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(10) UNIQUE,        -- e.g., PIA, EK
  country VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
select *from airlines;


CREATE TABLE flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_code VARCHAR(50) NOT NULL UNIQUE,   -- string ID
    airline_id INT NOT NULL,                   -- foreign key (airlines table)
    flight_number VARCHAR(20) NOT NULL,
    origin VARCHAR(100) NOT NULL,              -- origin country/city stored as string
    destination VARCHAR(100) NOT NULL,         -- destination country/city
    depart_datetime DATETIME NOT NULL,
    arrive_datetime DATETIME NOT NULL,
    duration_minutes INT NOT NULL,
    economy_seats INT NOT NULL,
    business_seats INT NOT NULL,
    economy_price DOUBLE NOT NULL,
    business_price DOUBLE NOT NULL,
    is_international BOOLEAN NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

    FOREIGN KEY (airline_id) REFERENCES airlines(id)
);
select *from flights;


CREATE TABLE hotels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    country VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,        -- 3 Star / 5 Star / 7 Star
    address VARCHAR(255) NOT NULL,
    email VARCHAR(150),
    contact VARCHAR(50),
    price_per_night DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
select *from hotels;


-- 10) indexes (performance)
CREATE INDEX idx_flights_origin_dest ON flights(origin, destination);
CREATE INDEX idx_bookings_user ON bookings(user_id);


-- tours: main package record
CREATE TABLE IF NOT EXISTS tours (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  type ENUM('SINGLE','MULTI') NOT NULL,
  total_cost DECIMAL(12,2) DEFAULT 0,
  discount_pct DECIMAL(5,2) DEFAULT 20.00,
  final_cost DECIMAL(12,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
select *from tours;

-- Each leg = one country stay / step in tour (for multi or single)
CREATE TABLE IF NOT EXISTS tour_legs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tour_id INT NOT NULL,
  seq_no INT NOT NULL, -- 1,2,3 ... order of legs
  country VARCHAR(120) NOT NULL,
  city VARCHAR(120),
  stay_from DATE,
  stay_to DATE,
  FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
);
select *from tour_legs;

-- Selected flights for a leg (departure or return)
CREATE TABLE IF NOT EXISTS tour_flights (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tour_leg_id INT NOT NULL,
  flight_id INT NOT NULL,   -- FK to flights.id (assumes flights table)
  direction ENUM('OUTBOUND','RETURN','CONNECT') DEFAULT 'OUTBOUND',
  price DECIMAL(10,2) DEFAULT 0,
  flight_datetime DATETIME,
  FOREIGN KEY (tour_leg_id) REFERENCES tour_legs(id) ON DELETE CASCADE
);
select *from tour_flights;


 CREATE TABLE IF NOT EXISTS user_bookings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  booking_type ENUM('FLIGHT','HOTEL','TOUR') NOT NULL,
  ref_id INT NOT NULL,
  booking_subtype ENUM('ONEWAY','RETURN','ROOM') DEFAULT 'ONEWAY',
  seats_booked INT DEFAULT 1,
  price_paid DECIMAL(12,2) DEFAULT 0,
  booking_status ENUM('CONFIRMED','CANCELLED','PENDING') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_booking_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);
select *from user_bookings;


select *from users;
select *from airlines;
select *from flights;
select *from hotels;
select *from tours;
select *from tour_legs;
select *from tour_flights;
select *from user_bookings;
