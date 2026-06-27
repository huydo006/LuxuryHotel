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
let selectedReviewImages = [];
const reviewModal = document.getElementById('reviewModal');
const stars = document.querySelectorAll('.star-rating span');
const reviewImageInput = document.getElementById('rv-images');
const reviewPreview = document.getElementById('rv-preview');

window.openReviewModal = (hotelId, hotelName) => {
    currentReviewHotelId = hotelId;
    document.getElementById('rv-hotel-name').innerText = hotelName;
    document.getElementById('rv-comment').value = '';
    
    selectedReviewImages = []; // Xóa trắng mảng ảnh
    renderReviewImages();      // Cập nhật lại UI trống
    
    reviewImageInput.value = '';
    setRating(0); // Reset sao
    reviewModal.classList.add('active');
}

window.closeReviewModal = () => {
    reviewModal.classList.remove('active');
}

// Bắt sự kiện khi người dùng chọn file
reviewImageInput?.addEventListener('change', (e) => {
    const files = Array.from(e.target.files || []);
    const supported = files.filter(file => ['image/jpeg', 'image/png', 'image/webp'].includes(file.type));
    
    if (supported.length !== files.length) {
        alert('Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP. Các file không hợp lệ đã bị loại bỏ.');
    }

    // TÍNH NĂNG MỚI: Cộng dồn ảnh mới vào mảng hiện tại (thay vì ghi đè)
    selectedReviewImages = [...selectedReviewImages, ...supported];

    // Reset lại ô input để lần sau có thể chọn tiếp tục 
    e.target.value = '';

    // Gọi hàm vẽ lại giao diện
    renderReviewImages();
});

// Hàm chuyên xử lý hiển thị ảnh và nút X
function renderReviewImages() {
    reviewPreview.innerHTML = '';
    const uploadText = document.getElementById('rv-upload-text');

    // Cập nhật dòng chữ thông báo
    if (selectedReviewImages.length > 0) {
        uploadText.innerText = `Đã chọn ${selectedReviewImages.length} ảnh (Nhấn để thêm ảnh)`;
        uploadText.style.color = '#10b981';
    } else {
        uploadText.innerText = 'Nhấn vào đây để chọn ảnh';
        uploadText.style.color = '#334155';
    }

    // Duyệt qua từng ảnh để hiển thị
    selectedReviewImages.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (event) => {
            // Tạo 1 div bao bọc tấm ảnh để làm khung gắn nút X
            const imgContainer = document.createElement('div');
            imgContainer.style.position = 'relative';
            imgContainer.style.display = 'inline-block';

            // Tạo thẻ img
            const img = document.createElement('img');
            img.src = event.target.result;
            // CSS trực tiếp thay thế class (để đảm bảo ko bị vỡ form)
            img.style.width = '70px';
            img.style.height = '70px';
            img.style.objectFit = 'cover';
            img.style.borderRadius = '8px';
            img.style.border = '1px solid #e2e8f0';

            // Tạo nút X màu đỏ
            const removeBtn = document.createElement('span');
            removeBtn.innerHTML = '&times;';
            removeBtn.style.position = 'absolute';
            removeBtn.style.top = '-6px';
            removeBtn.style.right = '-6px';
            removeBtn.style.background = '#ef4444';
            removeBtn.style.color = 'white';
            removeBtn.style.borderRadius = '50%';
            removeBtn.style.width = '20px';
            removeBtn.style.height = '20px';
            removeBtn.style.display = 'flex';
            removeBtn.style.alignItems = 'center';
            removeBtn.style.justifyContent = 'center';
            removeBtn.style.fontSize = '14px';
            removeBtn.style.fontWeight = 'bold';
            removeBtn.style.cursor = 'pointer';
            removeBtn.style.boxShadow = '0 2px 4px rgba(0,0,0,0.2)';
            removeBtn.style.zIndex = '2';

            // Sự kiện khi bấm nút X
            removeBtn.onclick = (e) => {
                e.preventDefault();
                e.stopPropagation(); // Ngăn sự kiện click lan ra khung lớn (chống mở hộp thoại chọn ảnh)
                
                selectedReviewImages.splice(index, 1); // Xóa file khỏi mảng
                renderReviewImages(); // Vẽ lại giao diện
            };

            imgContainer.appendChild(img);
            imgContainer.appendChild(removeBtn);
            reviewPreview.appendChild(imgContainer);
        };
        reader.readAsDataURL(file);
    });
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
        const formData = new FormData();
        formData.append('review', new Blob([JSON.stringify({
            userId: currentUser.accountId,
            hotelId: currentReviewHotelId,
            rating: currentRating,
            comment: comment
        })], { type: 'application/json' }));
        selectedReviewImages.forEach(file => formData.append('images', file));

        const res = await fetch('http://localhost:8080/api/reviews', {
            method: 'POST',
            body: formData
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