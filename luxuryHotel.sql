CREATE DATABASE IF NOT EXISTS luxury_hotel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE luxury_hotel;

-- ==========================================
-- 0. TẮT KHÓA NGOẠI & XÓA BẢNG CŨ 
-- (Giúp chạy lại file nhiều lần không bị lỗi)
-- ==========================================
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reviews, booking_details, bookings, rooms, promotions, hotels, accounts;

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
  PRIMARY KEY (accountID),
  UNIQUE KEY (username)
);

INSERT INTO accounts (accountID, username, passwords, fullName, email, phoneNumber, role) VALUES
(1,'admin','123456','Quản trị viên','admin@luxury.com',NULL,'Manager'),
(2,'customer','123456','Khách hàng VIP','khach@gmail.com',NULL,'Customer'),
(3,'huy1','123456','Đỗ Quang Huy','huydorov003@gmail.com',NULL,'Customer');


CREATE TABLE hotels (
  hotelID INT NOT NULL AUTO_INCREMENT,
  nameHotel VARCHAR(255) NOT NULL,
  address TEXT NOT NULL,
  rating INT DEFAULT 0,
  description TEXT,
  image VARCHAR(255),
  bookingsCount INT DEFAULT 0,
  amenities VARCHAR(255) DEFAULT NULL, 
  createdBy_AccountID INT NULL,        -- Lưu vết người tạo KS
  PRIMARY KEY (hotelID)
);

INSERT INTO hotels (hotelID, nameHotel, address, rating, description, image, bookingsCount, amenities, createdBy_AccountID) VALUES
(1,'Penthouse Park View','TP.HCM',5,'Không gian sang trọng bậc nhất giữa lòng Sài Gòn.','https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',200,'🏊 Hồ bơi vô cực,📶 WiFi Tốc độ cao,🍽️ Buffet sáng', NULL),
(2,'Luxury Sea View','Đà Nẵng',5,'Khách sạn 5 sao sát biển Mỹ Khê, dịch vụ hoàn hảo.','https://images.unsplash.com/photo-1582719508461-905c673771fd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',150,'🏊 Hồ bơi vô cực,💆 Spa & Massage', NULL),
(3,'Hanoi Central Hotel','Hà Nội',4,'Nằm ngay trung tâm phố cổ, tiện lợi cho việc di chuyển.','https://images.unsplash.com/photo-1555881400-74d7acaacd8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',85,'📶 WiFi Tốc độ cao,🍽️ Buffet sáng', NULL);


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
  createdBy_AccountID INT NULL, -- Lưu vết người tạo KM
  PRIMARY KEY (promotionID),
  UNIQUE KEY (discountCode)
);

INSERT INTO promotions (promotionID, discountCode, namePromo, discountPercent, maxDiscountAmount, minBookingValue, usageLimit, usedCount, startDate, endDate, isValid, createdBy_AccountID) VALUES
(1,'SUMMER26','Chào Hè Rực Rỡ',20,500000,2000000,100,0,'2026-06-01','2026-08-31',1, NULL),
(2,'JULYVIP','Tri Ân Tháng 7',15,300000,1500000,50,0,'2026-07-01','2026-07-31',1, NULL),
(3,'MAYFLASH','Flash Sale T5',10,200000,1000000,200,0,'2026-05-01','2026-05-31',1, NULL);


CREATE TABLE rooms (
  roomID INT NOT NULL AUTO_INCREMENT,
  hotelID INT NOT NULL,
  capacity INT NOT NULL,
  roomType VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  defaultPrice DOUBLE NOT NULL,
  createdBy_AccountID INT NULL, -- Lưu vết người tạo Phòng
  PRIMARY KEY (roomID)
);

INSERT INTO rooms (roomID, hotelID, capacity, roomType, quantity, defaultPrice, createdBy_AccountID) VALUES
(1,1,4,'Penthouse VIP',2,5000000, NULL),
(2,1,2,'Phòng Đôi Cao Cấp',10,2000000, NULL),
(3,2,2,'Phòng Đôi Hướng Biển',15,1500000, NULL),
(4,2,4,'Phòng Gia Đình',5,2500000, NULL),
(5,3,2,'Phòng Tiêu Chuẩn',20,800000, NULL),
(6,3,1,'Phòng Đơn',10,500000, NULL);


CREATE TABLE bookings (
  bookingID INT NOT NULL AUTO_INCREMENT,
  accountID INT NULL, -- Cho phép NULL để tránh mất hóa đơn khi xóa khách
  promotionID INT DEFAULT NULL,
  totalPrice DOUBLE NOT NULL,
  status ENUM('processing','success','cancelled') DEFAULT 'processing',
  checkInDate DATE NOT NULL,
  checkOutDate DATE NOT NULL,
  createdAt TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  originalPrice DOUBLE,
  depositAmount DOUBLE,
  paymentReceipt VARCHAR(255) DEFAULT NULL, 
  approvedBy_AccountID INT NULL,            -- Lưu vết người duyệt đơn
  PRIMARY KEY (bookingID)
);

INSERT INTO bookings (bookingID, accountID, totalPrice, status, checkInDate, checkOutDate, originalPrice, depositAmount, paymentReceipt, approvedBy_AccountID) VALUES
(1,2,800000,'processing','2026-06-21','2026-06-22',800000,240000, NULL, NULL);


CREATE TABLE booking_details (
  bookingDetailID INT NOT NULL AUTO_INCREMENT,
  bookingID INT NOT NULL,
  roomID INT NOT NULL,
  unitPrice DOUBLE NOT NULL,
  PRIMARY KEY (bookingDetailID)
);

INSERT INTO booking_details (bookingDetailID, bookingID, roomID, unitPrice) VALUES
(1,1,5,800000);


CREATE TABLE reviews (
  reviewID INT NOT NULL AUTO_INCREMENT,
  accountID INT NULL, -- Cho phép NULL để không mất review khi xóa khách
  hotelID INT NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  createdAt TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  imageUrls TEXT DEFAULT NULL, 
  PRIMARY KEY (reviewID)
);

INSERT INTO reviews (reviewID, accountID, hotelID, rating, comment, createdAt, imageUrls) VALUES
(1,2,1,5,'Phòng ở cực tuyệt vời, view nhìn ra toàn thành phố rất đẹp. Sẽ quay lại!','2026-06-19 10:33:14', NULL),
(2,2,1,4,'Nhân viên nhiệt tình, tuy nhiên wifi buổi tối hơi chậm một chút.','2026-06-19 10:33:14', NULL);


-- ==========================================
-- 2. THIẾT LẬP KHÓA NGOẠI (FOREIGN KEYS) AN TOÀN
-- ==========================================

-- Rooms -> Hotels: Khách sạn bị xóa thì phòng bị xóa
ALTER TABLE rooms 
ADD CONSTRAINT fk_room_hotel FOREIGN KEY (hotelID) REFERENCES hotels(hotelID) ON DELETE CASCADE;

-- Bookings -> Accounts: Khách bị xóa thì Đơn hàng giữ nguyên, ID = NULL 
ALTER TABLE bookings 
ADD CONSTRAINT fk_booking_account FOREIGN KEY (accountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

-- Bookings -> Promotions: Xóa khuyến mãi thì ID trong đơn hàng thành NULL
ALTER TABLE bookings 
ADD CONSTRAINT fk_booking_promotion FOREIGN KEY (promotionID) REFERENCES promotions(promotionID) ON DELETE SET NULL;

-- Booking Details -> Bookings & Rooms
ALTER TABLE booking_details 
ADD CONSTRAINT fk_bd_booking FOREIGN KEY (bookingID) REFERENCES bookings(bookingID) ON DELETE CASCADE;
ALTER TABLE booking_details 
ADD CONSTRAINT fk_bd_room FOREIGN KEY (roomID) REFERENCES rooms(roomID) ON DELETE CASCADE;

-- Reviews -> Accounts: Khách xóa tài khoản thì Review vẫn còn, ID = NULL
ALTER TABLE reviews 
ADD CONSTRAINT fk_review_account FOREIGN KEY (accountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

-- Reviews -> Hotels: Xóa khách sạn thì xóa luôn Review
ALTER TABLE reviews 
ADD CONSTRAINT fk_review_hotel FOREIGN KEY (hotelID) REFERENCES hotels(hotelID) ON DELETE CASCADE;

-- THÊM MỚI: RÀNG BUỘC CHO LƯU VẾT QUẢN LÝ (AUDITING)
ALTER TABLE hotels
ADD CONSTRAINT fk_hotel_createdBy FOREIGN KEY (createdBy_AccountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

ALTER TABLE rooms
ADD CONSTRAINT fk_room_createdBy FOREIGN KEY (createdBy_AccountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

ALTER TABLE promotions
ADD CONSTRAINT fk_promo_createdBy FOREIGN KEY (createdBy_AccountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

ALTER TABLE bookings 
ADD CONSTRAINT fk_booking_approvedBy FOREIGN KEY (approvedBy_AccountID) REFERENCES accounts(accountID) ON DELETE SET NULL;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;