// ==========================================
// 1. KIỂM TRA XÁC THỰC
// ==========================================
const userStr = localStorage.getItem('currentUser');
if (!userStr) {
    window.location.href = 'auth.html';
}
const currentUser = JSON.parse(userStr);

// ==========================================
// 2. LẤY PARAMS TỪ URL VÀ XỬ LÝ MẶC ĐỊNH
// ==========================================
const urlParams = new URLSearchParams(window.location.search);
const hotelId = urlParams.get('id');
let queryCheckIn = urlParams.get('checkIn');   
let queryCheckOut = urlParams.get('checkOut'); 

if (!hotelId) {
    alert("Lỗi: Không xác định được khách sạn!");
    window.location.href = 'customer-dashboard.html';
}

// FIX: Tự động gán ngày Hôm nay -> Ngày mai nếu URL trống, 
// Xử lý cẩn thận định dạng YYYY-MM-DD để tránh lỗi múi giờ
if (!queryCheckIn || !queryCheckOut) {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    
    const formatDate = (date) => {
        let d = new Date(date),
            month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear();

        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;

        return [year, month, day].join('-');
    };

    queryCheckIn = formatDate(today);
    queryCheckOut = formatDate(tomorrow);
}

// 3. Tải dữ liệu Khách sạn
let currentRooms = [];
let selectedRoom = null;

async function fetchHotelDetails() {
    try {
        let fetchUrl = `http://localhost:8080/api/hotels/${hotelId}?checkIn=${queryCheckIn}&checkOut=${queryCheckOut}`;
        const res = await fetch(fetchUrl);
        const data = await res.json();

        if (data.success) {
            const { hotel, rooms, reviews } = data;
            currentRooms = rooms;

            document.getElementById('hotel-banner').style.backgroundImage = `url('${hotel.image}')`;
            document.getElementById('hotel-name').innerText = hotel.name;
            document.getElementById('hotel-loc').innerText = `📍 ${hotel.location}`;
            document.getElementById('h-rating-big').innerText = hotel.rating ? Number(hotel.rating).toFixed(1) : "5.0";
            document.getElementById('h-reviews-count').innerText = `Tuyệt vời (${hotel.bookingsCount || 0} lượt đặt)`;
            document.getElementById('hotel-desc').innerText = hotel.description;

            renderRooms(rooms);
            renderReviews(reviews);
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error(error);
        alert("Lỗi kết nối Server! Kiểm tra lại backend Java.");
    }
}

function renderRooms(rooms) {
    const container = document.getElementById('room-list');
    if (rooms.length === 0) {
        container.innerHTML = '<p style="color:red; font-weight:bold; grid-column: 1/-1; text-align: center; padding: 20px;">Hiện tại khách sạn này đã hết phòng trống trong khoảng thời gian bạn chọn.</p>';
        return;
    }

    container.innerHTML = ''; // Clear loading state
    rooms.forEach(room => {
        const availableCount = room.availableQuantity ?? room.quantity;
        const isSoldOut = availableCount <= 0;

        const card = document.createElement('div');
        card.className = 'room-card';
        card.innerHTML = `
            <h3>${room.name}</h3>
            <p style="margin-bottom: 5px; color: #475569;">👥 Sức chứa: ${room.capacity} người</p>
            <p style="margin-bottom: 10px; color: ${isSoldOut ? '#dc2626' : '#059669'}; font-weight: 600;">
                ${isSoldOut ? '❌ Đã hết phòng' : `✅ Còn trống: ${availableCount} phòng`}
            </p>
            <div class="room-price">${room.price.toLocaleString('vi-VN')} VNĐ/đêm</div>
            <button class="btn btn-primary btn-full" 
                ${isSoldOut ? 'disabled style="background: #94a3b8; cursor: not-allowed;"' : `onclick="openBookingModal(${room.id})"`}>
                ${isSoldOut ? 'Hết Phòng' : 'Đặt Phòng Này'}
            </button>
        `;
        container.appendChild(card);
    });
}

// ==========================================
// 4. LOGIC MODAL & TÍNH TIỀN
// ==========================================
const modal = document.getElementById('bookingModal');
const paymentTypeSelect = document.getElementById('paymentType');

let totalNights = 0;
let baseTotal = 0;         
let appliedPromotionID = null; 
let discountAmount = 0;    
let finalPrice = 0;        
let amountToPay = 0;       

window.openBookingModal = async (roomId) => {
    selectedRoom = currentRooms.find(r => r.id === roomId);
    if (!selectedRoom) return;

    document.getElementById('m-room-name').innerText = selectedRoom.name;
    document.getElementById('m-room-price').innerText = selectedRoom.price.toLocaleString('vi-VN');
    document.getElementById('display-dates').innerText = `${queryCheckIn} đến ${queryCheckOut}`;

    const start = new Date(queryCheckIn);
    const end = new Date(queryCheckOut);
    totalNights = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) || 1;
    
    document.getElementById('m-nights').innerText = totalNights;

    resetPromotion();
    calculatePayment();
    modal.classList.add('active');
};

document.getElementById('closeModal').addEventListener('click', () => modal.classList.remove('active'));

function resetPromotion() {
    appliedPromotionID = null;
    discountAmount = 0;
    const promoInput = document.getElementById('customer-promo-code');
    if (promoInput) promoInput.value = '';
}

if (paymentTypeSelect) paymentTypeSelect.addEventListener('change', calculatePayment);

function calculatePayment() {
    if (!selectedRoom) return;
    const type = paymentTypeSelect ? paymentTypeSelect.value : 'full';

    baseTotal = selectedRoom.price * totalNights;
    let actualDiscount = discountAmount > baseTotal ? baseTotal : discountAmount;
    finalPrice = baseTotal - actualDiscount;
    amountToPay = type === 'deposit' ? finalPrice * 0.3 : finalPrice;

    document.getElementById('m-total-pay').innerText = amountToPay.toLocaleString('vi-VN') + " VNĐ";
    document.getElementById('summary-original-price').innerText = baseTotal.toLocaleString('vi-VN') + " ₫";
    document.getElementById('summary-discount').innerText = "- " + actualDiscount.toLocaleString('vi-VN') + " ₫";
    document.getElementById('summary-final-price').innerText = finalPrice.toLocaleString('vi-VN') + " ₫";
}

// ==========================================
// 5. API KHUYẾN MÃI & XÁC NHẬN THANH TOÁN
// ==========================================
window.loadAvailablePromos = async () => {
    const box = document.getElementById('promo-suggestion-box');
    if (box.style.display === 'block') { box.style.display = 'none'; return; }
    box.style.display = 'block';
    
    try {
        const res = await fetch('http://localhost:8080/api/promotions/available');
        const promos = await res.json();
        box.innerHTML = promos.length === 0 ? '<p style="font-size:0.9rem; color:#64748b;">Không có mã nào.</p>' : '';
        promos.forEach(p => {
            box.innerHTML += `<div style="padding: 8px; border-bottom: 1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <strong style="color: #d4af37;">${p.discountCode}</strong>
                    <div style="font-size: 0.75rem; color: #64748b;">Giảm ${p.discountPercent}% (Tối đa ${p.maxDiscountAmount.toLocaleString('vi-VN')}₫)</div>
                </div>
                <button onclick="document.getElementById('customer-promo-code').value='${p.discountCode}'" style="padding:4px 8px; font-size:0.8rem; cursor:pointer;">Chọn</button>
            </div>`;
        });
    } catch (e) { box.innerHTML = '<p style="color:red;">Lỗi tải mã.</p>'; }
}

window.applyPromoCode = async () => {
    const code = document.getElementById('customer-promo-code').value.trim();
    if (!code) return alert("Vui lòng nhập mã giảm giá!");
    
    try {
        const res = await fetch('http://localhost:8080/api/promotions/apply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: currentUser.accountId, discountCode: code, bookingTotal: baseTotal })
        });
        const data = await res.json();
        if (data.success) {
            appliedPromotionID = data.promotionID;
            discountAmount = data.discountAmount;
            calculatePayment();
            alert(data.message);
            document.getElementById('promo-suggestion-box').style.display = 'none';
        } else alert(data.message);
    } catch (e) { alert("Lỗi kết nối!"); }
}

document.getElementById('btnConfirmPayment').addEventListener('click', async () => {
    try {
        const res = await fetch('http://localhost:8080/api/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.accountId, hotelId: parseInt(hotelId), roomId: selectedRoom.id,
                promotionId: appliedPromotionID, originalPrice: baseTotal, totalPaid: finalPrice,
                depositAmount: amountToPay, checkInDate: queryCheckIn, checkOutDate: queryCheckOut
            })
        });
        const data = await res.json();
        if (data.success) { 
            alert(`[HỆ THỐNG] ${data.message}`); 
            modal.classList.remove('active'); 
            window.location.href = 'my-bookings.html'; 
        } else alert("Lỗi: " + data.message);
    } catch (e) { alert("Lỗi hệ thống!"); }
});

// ==========================================
// 7. RENDER ĐÁNH GIÁ
// ==========================================
function renderReviews(reviews) {
    const container = document.getElementById('review-list');
    container.innerHTML = (reviews && reviews.length > 0) ? '' : '<div style="grid-column: 1 / -1; text-align: center; padding: 40px; background: #f8fafc; border-radius: 12px; color: #64748b;">Khách sạn này chưa có đánh giá nào.</div>';
    
    reviews?.forEach(rv => {
        let stars = '⭐'.repeat(rv.rating);
        let avatarChar = rv.username.charAt(0).toUpperCase();
        const dateStr = new Date(rv.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });

        container.innerHTML += `
            <div class="review-card">
                <div class="review-header">
                    <div class="reviewer-info">
                        <div class="reviewer-avatar">${avatarChar}</div>
                        <div class="reviewer-name">${rv.username}</div>
                    </div>
                    <div class="review-date">${dateStr}</div>
                </div>
                <div class="review-stars">${stars}</div>
                <p class="review-comment">"${rv.comment}"</p>
            </div>
        `;
    });
}

// ==========================================
// 8. LOGIC THANH TÌM KIẾM ĐƯỢC GHIM (PINNED SEARCH)
// ==========================================
// Khởi tạo lịch Flatpickr và đồng bộ ngày từ URL
flatpickr("#dateRange", {
    mode: "range",
    minDate: "today",
    dateFormat: "Y-m-d",
    locale: "vn",
    // Gán trực tiếp ngày từ URL vào thanh bộ lọc
    defaultDate: [queryCheckIn, queryCheckOut]
});

// Gán địa điểm vào ô location
setTimeout(() => {
    const loc = document.getElementById('hotel-loc')?.innerText.replace('📍 ', '');
    if (loc) {
        const locInput = document.getElementById('locInput');
        if(locInput) {
            locInput.value = loc;
            locInput.title = "Địa điểm được cố định theo khách sạn bạn đã chọn";
        }
    }
}, 500);

// Xử lý nút cập nhật giá (Reload với URL mới)
document.getElementById('btnSearch').addEventListener('click', () => {
    const range = document.getElementById('dateRange').value;
    
    let cIn = "";
    let cOut = "";

    // BẢN FIX: Hỗ trợ tách chuỗi ngày theo cả 2 ngôn ngữ (to / đến)
    if (range) {
        const separator = range.includes(' đến ') ? ' đến ' : (range.includes(' to ') ? ' to ' : null);
        if (separator) {
            const dates = range.split(separator);
            cIn = dates[0].trim();
            cOut = dates[1].trim();
        }
    }

    if (!cIn || !cOut) return alert("Vui lòng chọn đầy đủ ngày Nhận phòng và Trả phòng!");
    
    window.location.href = `hotel-detail.html?id=${hotelId}&checkIn=${cIn}&checkOut=${cOut}`;
});

// Khởi chạy
fetchHotelDetails();