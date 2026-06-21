// 1. Kiểm tra đăng nhập
const userStr = localStorage.getItem('currentUser');
if (!userStr) {
    window.location.href = 'auth.html';
}
const currentUser = JSON.parse(userStr);
document.getElementById('user-name').innerText = `Xin chào, ${currentUser.username}`;

// 2. Đăng xuất
document.getElementById('btn-logout').addEventListener('click', () => {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
});

// 3. Lấy dữ liệu Lịch sử từ Backend
async function fetchMyBookings() {
    try {
        // ĐÃ FIX BUG: Đổi currentUser.id thành currentUser.accountId
        const res = await fetch(`http://localhost:8080/api/bookings/user/${currentUser.accountId}`);
        const bookings = await res.json();
        renderBookings(bookings);
    } catch (error) {
        document.getElementById('booking-list').innerHTML = `
            <div class="empty-state" style="color: red; border-color: red;">
                <h3>Lỗi kết nối Server!</h3>
                <p>Vui lòng thử lại sau.</p>
            </div>`;
    }
}

// 4. Hiển thị dữ liệu lên giao diện (Bản nâng cấp UI)
function renderBookings(bookings) {
    const container = document.getElementById('booking-list');
    container.innerHTML = '';

    if (bookings.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>Không có chuyến đi nào</h3>
                <p>Bạn chưa thực hiện giao dịch nào trên hệ thống. Hãy bắt đầu khám phá nhé!</p>
            </div>`;
        return;
    }

    bookings.forEach(b => {
        let statusText = '';
        let statusClass = '';
        let actionBtn = ''; 

        if (b.status === 'processing') {
            statusText = 'Đang chờ duyệt';
            statusClass = 'processing';
            actionBtn = `<button class="btn-action btn-cancel" onclick="cancelMyBooking(${b.bookingID})">Hủy phòng</button>`;
        } else if (b.status === 'success') {
            statusText = 'Đã xác nhận';
            statusClass = 'success';
            actionBtn = `
                <div style="display: flex; gap: 10px;">
                    <button class="btn-action btn-cancel" onclick="cancelMyBooking(${b.bookingID})">Hủy</button>
                    <button class="btn-action btn-review" onclick="openReviewModal(${b.hotelID}, '${b.nameHotel}')">Đánh giá</button>
                </div>`;
        } else {
            statusText = 'Đã hủy';
            statusClass = 'cancelled';
            actionBtn = ''; 
        }

        const card = document.createElement('div');
        card.className = `booking-card ${statusClass}`;
        
        // Giao diện thẻ được thiết kế lại
        card.innerHTML = `
            <div class="booking-info">
                <h3>${b.nameHotel}</h3>
                <p>🏷️ <strong>Mã đơn:</strong> #${b.bookingID} &nbsp;|&nbsp; 🛏️ <strong>Loại phòng:</strong> ${b.roomType}</p>
                <p>📅 <strong>Thời gian:</strong> ${b.checkInDate} &nbsp;🡒&nbsp; ${b.checkOutDate}</p>
                <div class="booking-price">${b.totalPrice.toLocaleString('vi-VN')} ₫</div>
            </div>
            <div class="booking-actions">
                <span class="status-badge ${statusClass}">${statusText}</span>
                ${actionBtn}
            </div>
        `;
        
        container.appendChild(card);
    });
}

// 5. Hàm xử lý Hủy phòng gọi xuống Backend
window.cancelMyBooking = async (bookingId) => {
    const isConfirm = confirm("Bạn có chắc chắn muốn hủy phòng này không?\n\nLưu ý: Việc hoàn tiền sẽ được áp dụng theo chính sách hủy phòng của khách sạn (nhận lại 100% nếu hủy trước 24h).");
    
    if (!isConfirm) return;

    try {
        const res = await fetch(`http://localhost:8080/api/bookings/${bookingId}/cancel`, {
            method: 'PUT'
        });
        const data = await res.json();
        
        if (data.success) {
            alert(data.message);
            fetchMyBookings(); // Tải lại danh sách ngay lập tức
        } else {
            alert("Lỗi: " + data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối đến máy chủ!");
    }
}

// ==================== 6. LOGIC ĐÁNH GIÁ (MỚI THÊM) ====================
let currentReviewHotelId = null;
let currentRating = 0;
const reviewModal = document.getElementById('reviewModal');
const stars = document.querySelectorAll('.star-rating span');

window.openReviewModal = (hotelId, hotelName) => {
    currentReviewHotelId = hotelId;
    document.getElementById('rv-hotel-name').innerText = hotelName;
    document.getElementById('rv-comment').value = '';
    setRating(0); // Reset sao
    reviewModal.classList.add('active');
}

window.closeReviewModal = () => {
    reviewModal.classList.remove('active');
}

// Xử lý click vào Ngôi sao
stars.forEach(star => {
    star.addEventListener('click', (e) => {
        setRating(e.target.getAttribute('data-val'));
    });
});

function setRating(val) {
    currentRating = parseInt(val);
    stars.forEach(s => {
        if (parseInt(s.getAttribute('data-val')) <= currentRating) {
            s.classList.add('active');
        } else {
            s.classList.remove('active');
        }
    });
}

// Gửi đánh giá
window.submitReview = async () => {
    if (currentRating === 0) return alert("Vui lòng chọn số sao đánh giá!");
    const comment = document.getElementById('rv-comment').value.trim();
    if (!comment) return alert("Vui lòng nhập nội dung đánh giá!");

    try {
        const res = await fetch('http://localhost:8080/api/reviews', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.accountId, // ĐÃ FIX BUG: Đổi currentUser.id thành currentUser.accountId
                hotelId: currentReviewHotelId,
                rating: currentRating,
                comment: comment
            })
        });
        const data = await res.json();
        if (data.success) {
            alert(data.message);
            closeReviewModal();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối Server!");
    }
}

// Khởi chạy khi mở trang
fetchMyBookings();