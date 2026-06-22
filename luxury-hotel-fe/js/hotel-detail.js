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
// để API luôn có ngày gửi xuống Backend tính toán phòng trống.
if (!queryCheckIn || !queryCheckOut) {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    queryCheckIn = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');
    queryCheckOut = tomorrow.getFullYear() + '-' + String(tomorrow.getMonth() + 1).padStart(2, '0') + '-' + String(tomorrow.getDate()).padStart(2, '0');
}

// 3. Tải dữ liệu Khách sạn
let currentRooms = [];
let selectedRoom = null;

async function fetchHotelDetails() {
    try {
        // Lúc này queryCheckIn và queryCheckOut chắc chắn đã có dữ liệu
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
        container.innerHTML = '<p style="color:red; font-weight:bold;">Hiện tại khách sạn này đã hết phòng trống trong khoảng thời gian bạn chọn.</p>';
        return;
    }

    rooms.forEach(room => {
        // FIX BUG: Dùng ?? để lấy chính xác số 0, nếu dùng || thì số 0 sẽ bị coi là false và bị bỏ qua
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

let selectedCheckIn = "";
let selectedCheckOut = "";
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

    // Dữ liệu ngày đã được xử lý chắc chắn có ở Bước 2
    selectedCheckIn = queryCheckIn;
    selectedCheckOut = queryCheckOut;

    // Hiển thị lên UI Modal
    document.getElementById('display-dates').innerText = `${selectedCheckIn} đến ${selectedCheckOut}`;

    // Tính số đêm
    const start = new Date(selectedCheckIn);
    const end = new Date(selectedCheckOut);
    totalNights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
    if (totalNights <= 0) totalNights = 1;
    
    document.getElementById('m-nights').innerText = totalNights;

    resetPromotion();
    calculatePayment();
    modal.classList.add('active');
};

document.getElementById('closeModal').addEventListener('click', () => {
    modal.classList.remove('active');
});

function resetPromotion() {
    appliedPromotionID = null;
    discountAmount = 0;
    const promoInput = document.getElementById('customer-promo-code');
    if (promoInput) promoInput.value = '';
}

if (paymentTypeSelect) {
    paymentTypeSelect.addEventListener('change', calculatePayment);
}

function calculatePayment() {
    if (!selectedRoom) return;
    const type = paymentTypeSelect ? paymentTypeSelect.value : 'full';

    baseTotal = selectedRoom.price * totalNights;
    let actualDiscount = discountAmount > baseTotal ? baseTotal : discountAmount;
    finalPrice = baseTotal - actualDiscount;
    amountToPay = type === 'deposit' ? finalPrice * 0.3 : finalPrice;

    const elTotalPay = document.getElementById('m-total-pay');
    if (elTotalPay) elTotalPay.innerText = amountToPay.toLocaleString('vi-VN') + " VNĐ";

    document.getElementById('summary-original-price').innerText = baseTotal.toLocaleString('vi-VN') + " ₫";
    document.getElementById('summary-discount').innerText = "- " + actualDiscount.toLocaleString('vi-VN') + " ₫";
    document.getElementById('summary-final-price').innerText = finalPrice.toLocaleString('vi-VN') + " ₫";
}

// ==========================================
// 5. API KHUYẾN MÃI 
// ==========================================
window.loadAvailablePromos = async () => {
    const box = document.getElementById('promo-suggestion-box');
    if (box.style.display === 'block') {
        box.style.display = 'none';
        return;
    }

    box.style.display = 'block';
    box.innerHTML = '<div style="text-align:center; color:#64748b; font-size:0.85rem;">Đang tải mã...</div>';

    try {
        const res = await fetch('http://localhost:8080/api/promotions/available');
        const promos = await res.json();

        if (promos.length === 0) {
            box.innerHTML = '<div style="color:#64748b; font-size:0.85rem;">Hiện chưa có mã giảm giá nào phù hợp.</div>';
            return;
        }

        box.innerHTML = '';
        promos.forEach(p => {
            box.innerHTML += `
                <div style="padding: 8px; border-bottom: 1px dashed #e2e8f0; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong style="color: #d4af37; font-size: 1rem;">${p.discountCode}</strong>
                        <div style="font-size: 0.75rem; color: #475569; margin-top: 2px;">Giảm ${p.discountPercent}% (Tối đa ${p.maxDiscountAmount.toLocaleString('vi-VN')}₫)</div>
                        <div style="font-size: 0.75rem; color: #475569;">Đơn tối thiểu: ${p.minBookingValue.toLocaleString('vi-VN')}₫</div>
                    </div>
                    <button onclick="document.getElementById('customer-promo-code').value = '${p.discountCode}'" style="background:#f1f5f9; border:1px solid #cbd5e1; padding:4px 10px; border-radius:4px; font-size:0.8rem; cursor:pointer;">Chọn</button>
                </div>
            `;
        });
    } catch (error) {
        box.innerHTML = '<div style="color:red; font-size:0.85rem;">Lỗi tải mã giảm giá!</div>';
    }
}

window.applyPromoCode = async () => {
    const codeInput = document.getElementById('customer-promo-code').value.trim();
    if (!codeInput) return alert("Vui lòng nhập mã giảm giá!");

    try {
        const res = await fetch('http://localhost:8080/api/promotions/apply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.accountId,
                discountCode: codeInput,
                bookingTotal: baseTotal
            })
        });

        const data = await res.json();

        if (data.success) {
            alert(data.message);
            appliedPromotionID = data.promotionID;
            discountAmount = data.discountAmount;
            calculatePayment();
            document.getElementById('promo-suggestion-box').style.display = 'none';
        } else {
            alert(data.message);
            resetPromotion();
            calculatePayment();
        }
    } catch (error) {
        alert("Lỗi kết nối khi áp dụng mã!");
    }
}

// ==========================================
// 6. XÁC NHẬN THANH TOÁN (LƯU DB)
// ==========================================
document.getElementById('btnConfirmPayment').addEventListener('click', async () => {
    try {
        const res = await fetch('http://localhost:8080/api/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.accountId,
                hotelId: parseInt(hotelId),
                roomId: selectedRoom.id,
                promotionId: appliedPromotionID, 
                originalPrice: baseTotal,        
                totalPaid: finalPrice,           
                depositAmount: amountToPay,      
                checkInDate: selectedCheckIn,    
                checkOutDate: selectedCheckOut
            })
        });

        const data = await res.json();
        if (data.success) {
            alert(`[HỆ THỐNG] ${data.message}`);
            modal.classList.remove('active');
            window.location.href = 'my-bookings.html'; 
        } else {
            alert("Lỗi từ Server: " + data.message);
        }
    } catch (error) {
        console.error("Chi tiết lỗi:", error);
        alert("Lỗi kết nối khi xử lý đặt phòng! Hãy kiểm tra tab Console (F12).");
    }
});

// ==========================================
// 7. RENDER ĐÁNH GIÁ
// ==========================================
function renderReviews(reviews) {
    const container = document.getElementById('review-list');
    container.innerHTML = '';

    if (!reviews || reviews.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px; background: #f8fafc; border-radius: 12px; color: #64748b;">
                Khách sạn này chưa có đánh giá nào. Trở thành người đầu tiên trải nghiệm nhé!
            </div>`;
        return;
    }

    reviews.forEach(rv => {
        let stars = '⭐'.repeat(rv.rating);
        let avatarChar = rv.username.charAt(0).toUpperCase();
        const dateStr = new Date(rv.createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });

        const reviewCard = document.createElement('div');
        reviewCard.className = 'review-card';

        reviewCard.innerHTML = `
            <div class="review-header">
                <div class="reviewer-info">
                    <div class="reviewer-avatar">${avatarChar}</div>
                    <div class="reviewer-name">${rv.username}</div>
                </div>
                <div class="review-date">${dateStr}</div>
            </div>
            <div class="review-stars">${stars}</div>
            <p class="review-comment">"${rv.comment}"</p>
        `;
        container.appendChild(reviewCard);
    });
}

// Khởi chạy
fetchHotelDetails();