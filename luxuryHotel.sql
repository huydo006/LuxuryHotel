CREATE DATABASE IF NOT EXISTS luxury_hotel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE luxury_hotel;

-- ==========================================
-- 1. TẠO BẢNG & THÊM DỮ LIỆU
-- ==========================================

CREATE TABLE accounts (
  accountID INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  passwords VARCHAR(255) NOT NULL,
  fullName VARCHAR(100),
  email VARCHAR(100),
  phoneNumber VARCHAR(20),
  role ENUM('Manager','Customer') NOT NULL,
  bankAccount VARCHAR(100),
  PRIMARY KEY (accountID),
  UNIQUE KEY (username)
);

INSERT INTO accounts VALUES
(1,'admin','123456','Quản trị viên','admin@luxury.com',NULL,'Manager',NULL),
(2,'customer','123456','Khách hàng VIP','khach@gmail.com',NULL,'Customer',NULL),
(3,'huy1','123456','Đỗ Quang Huy','huydorov003@gmail.com',NULL,'Customer',NULL);

CREATE TABLE hotels (
  hotelID INT NOT NULL AUTO_INCREMENT,
  nameHotel VARCHAR(255) NOT NULL,
  address TEXT NOT NULL,
  rating INT DEFAULT 0,
  description TEXT,
  image VARCHAR(255),
  bookingsCount INT DEFAULT 0,
  PRIMARY KEY (hotelID)
);

INSERT INTO hotels VALUES
(1,'Penthouse Park View','TP.HCM',5,'Không gian sang trọng bậc nhất giữa lòng Sài Gòn.','https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',200),
(2,'Luxury Sea View','Đà Nẵng',5,'Khách sạn 5 sao sát biển Mỹ Khê, dịch vụ hoàn hảo.','https://images.unsplash.com/photo-1582719508461-905c673771fd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',150),
(3,'Hanoi Central Hotel','Hà Nội',4,'Nằm ngay trung tâm phố cổ, tiện lợi cho việc di chuyển.','https://images.unsplash.com/photo-1555881400-74d7acaacd8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',85);

CREATE TABLE promotions (
  promotionID INT NOT NULL AUTO_INCREMENT,
  discountCode VARCHAR(50) NOT NULL,
  namePromo VARCHAR(255) NOT NULL,
  discountPercent DOUBLE NOT NULL,
  maxDiscountAmount DOUBLE NOT NULL,
  minBookingValue DOUBLE NOT NULL DEFAULT 0,
  usageLimit INT NOT NULL,
  usedCount INT DEFAULT 0,
  startDate DATE NOT NULL,
  endDate DATE NOT NULL,
  isValid TINYINT DEFAULT 1,
  PRIMARY KEY (promotionID),
  UNIQUE KEY (discountCode)
);

INSERT INTO promotions VALUES
(1,'SUMMER26','Chào Hè Rực Rỡ',20,500000,2000000,100,0,'2026-06-01','2026-08-31',1),
(2,'JULYVIP','Tri Ân Tháng 7',15,300000,1500000,50,0,'2026-07-01','2026-07-31',1),
(3,'MAYFLASH','Flash Sale T5',10,200000,1000000,200,0,'2026-05-01','2026-05-31',1);

CREATE TABLE rooms (
  roomID INT NOT NULL AUTO_INCREMENT,
  hotelID INT NOT NULL,
  capacity INT NOT NULL,
  roomType VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  defaultPrice DOUBLE NOT NULL,
  PRIMARY KEY (roomID)
);

INSERT INTO rooms VALUES
(1,1,4,'Penthouse VIP',2,5000000),
(2,1,2,'Phòng Đôi Cao Cấp',10,2000000),
(3,2,2,'Phòng Đôi Hướng Biển',15,1500000),
(4,2,4,'Phòng Gia Đình',5,2500000),
(5,3,2,'Phòng Tiêu Chuẩn',20,800000),
(6,3,1,'Phòng Đơn',10,500000);

CREATE TABLE bookings (
  bookingID INT NOT NULL AUTO_INCREMENT,
  accountID INT NOT NULL,
  promotionID INT DEFAULT NULL,
  totalPrice DOUBLE NOT NULL,
  status ENUM('processing','success','cancelled') DEFAULT 'processing',
  checkInDate DATE NOT NULL,
  checkOutDate DATE NOT NULL,
  createdAt TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  originalPrice DOUBLE,
  depositAmount DOUBLE,
  PRIMARY KEY (bookingID)
);

INSERT INTO bookings VALUES
(1,2,NULL,800000,'processing','2026-06-21','2026-06-22','2026-06-20 20:50:31',800000,240000);

CREATE TABLE booking_details (
  bookingDetailID INT NOT NULL AUTO_INCREMENT,
  bookingID INT NOT NULL,
  roomID INT NOT NULL,
  unitPrice DOUBLE NOT NULL,
  PRIMARY KEY (bookingDetailID)
);

INSERT INTO booking_details VALUES
(1,1,5,800000);

CREATE TABLE reviews (
  reviewID INT NOT NULL AUTO_INCREMENT,
  accountID INT NOT NULL,
  hotelID INT NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  createdAt TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (reviewID)
);

INSERT INTO reviews VALUES
(1,2,1,5,'Phòng ở cực tuyệt vời, view nhìn ra toàn thành phố rất đẹp. Sẽ quay lại!','2026-06-19 10:33:14'),
(2,2,1,4,'Nhân viên nhiệt tình, tuy nhiên wifi buổi tối hơi chậm một chút.','2026-06-19 10:33:14');


-- ==========================================
-- 2. THIẾT LẬP KHÓA NGOẠI (FOREIGN KEYS)
-- ==========================================

-- Bảng rooms liên kết với hotels (Xóa hotel thì xóa luôn room)
ALTER TABLE rooms 
ADD CONSTRAINT fk_room_hotel 
FOREIGN KEY (hotelID) REFERENCES hotels(hotelID) ON DELETE CASCADE;

-- Bảng bookings liên kết với accounts và promotions
ALTER TABLE bookings 
ADD CONSTRAINT fk_booking_account 
FOREIGN KEY (accountID) REFERENCES accounts(accountID) ON DELETE CASCADE;

ALTER TABLE bookings 
ADD CONSTRAINT fk_booking_promotion 
FOREIGN KEY (promotionID) REFERENCES promotions(promotionID) ON DELETE SET NULL;

-- Bảng booking_details liên kết với bookings và rooms
ALTER TABLE booking_details 
ADD CONSTRAINT fk_bd_booking 
FOREIGN KEY (bookingID) REFERENCES bookings(bookingID) ON DELETE CASCADE;

ALTER TABLE booking_details 
ADD CONSTRAINT fk_bd_room 
FOREIGN KEY (roomID) REFERENCES rooms(roomID) ON DELETE CASCADE;

-- Bảng reviews liên kết với accounts và hotels
ALTER TABLE reviews 
ADD CONSTRAINT fk_review_account 
FOREIGN KEY (accountID) REFERENCES accounts(accountID) ON DELETE CASCADE;

ALTER TABLE reviews 
ADD CONSTRAINT fk_review_hotel 
FOREIGN KEY (hotelID) REFERENCES hotels(hotelID) ON DELETE CASCADE;

ALTER TABLE hotels ADD COLUMN amenities VARCHAR(255) DEFAULT NULL;