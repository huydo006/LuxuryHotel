// 1. KIỂM TRA ĐĂNG NHẬP
const userStr = localStorage.getItem('currentUser');
if (!userStr) {
    window.location.href = 'auth.html';
}

const currentUser = JSON.parse(userStr);
document.getElementById('welcome-msg').innerText = `Xin chào, ${currentUser.fullName}`;

// Chức năng Đăng xuất
document.getElementById('btn-logout').addEventListener('click', () => {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
});

// HÀM HIỂN THỊ THÔNG BÁO CHUYÊN NGHIỆP (TOAST)
let toastTimeout;
function showToast(message, type) {
    const toast = document.getElementById('custom-toast');
    const toastMsg = document.getElementById('toast-msg');
    
    toastMsg.innerText = message;
    toast.className = `toast show ${type}`;
    
    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => {
        toast.classList.remove('show');
    }, 3500);
}

// 2. LẤY DỮ LIỆU TỪ BACKEND (KHI VỪA VÀO TRANG)
const hotelContainer = document.getElementById('hotel-list');

async function fetchHotels() {
    try {
        const response = await fetch('http://localhost:8080/api/hotels');
        const data = await response.json();
        renderHotels(data); // Render không có ngày tháng
    } catch (error) {
        showToast("Không thể kết nối máy chủ Java. Vui lòng kiểm tra lại server.", "error");
        hotelContainer.innerHTML = `<div class="empty-msg" style="color: red;">Không thể kết nối Backend.</div>`;
    }
}

// 3. HIỂN THỊ LÊN GIAO DIỆN (Đã thêm tham số checkIn, checkOut, capacity để gắn vào URL)
function renderHotels(hotels, checkIn = "", checkOut = "", capacity = "all") {
    hotelContainer.innerHTML = ''; 
    
    if (hotels.length === 0) {
        hotelContainer.innerHTML = `<div class="empty-msg">Không tìm thấy khách sạn nào phù hợp.</div>`;
        return;
    }

    hotels.forEach(hotel => {
        // Build URL: Nếu khách có tìm theo ngày và sức chứa, đẩy sang trang chi tiết
        let detailUrl = `hotel-detail.html?id=${hotel.id}`;
        if (checkIn && checkOut) {
            detailUrl += `&checkIn=${checkIn}&checkOut=${checkOut}`;
        }
        if (capacity && capacity !== 'all') {
            detailUrl += `&capacity=${capacity}`;
        }

        const card = document.createElement('div');
        card.className = 'hotel-card';
        card.innerHTML = `
            <img src="${hotel.image}" alt="${hotel.name}" class="hotel-img">
            <div class="hotel-info">
                <h3>${hotel.name}</h3>
                <div class="location">📍 ${hotel.location}</div>
                <div class="rating">⭐ ${hotel.rating}/5.0 (${hotel.bookingsCount} lượt đặt)</div>
                <p style="color: #64748b; font-size: 0.9rem; margin-bottom: 20px; flex: 1;">${hotel.description}</p>
                <a href="${detailUrl}" class="btn-view">Xem Các Phòng</a>
            </div>
        `;
        hotelContainer.appendChild(card);
    });
}

// 4. CHỨC NĂNG TÌM KIẾM MỚI (Lấy dữ liệu từ Flatpickr)
document.getElementById('btnSearch').addEventListener('click', async () => {
    const location = document.getElementById('locInput').value.trim();
    const dateRange = document.getElementById('dateRange').value; 
    const capacityVal = document.getElementById('capacitySelect').value; 

    // Validate dữ liệu: CHỈ BẮT BUỘC ĐỊA ĐIỂM
    if (!location) {
        showToast("Vui lòng nhập điểm đến để tìm kiếm.", "warning");
        return;
    }
    
    // Hỗ trợ tách chuỗi ngày theo cả 2 ngôn ngữ (to / đến)
    let checkIn = "";
    let checkOut = "";
    if (dateRange) {
        const separator = dateRange.includes(' đến ') ? ' đến ' : (dateRange.includes(' to ') ? ' to ' : null);
        if (separator) {
            const dates = dateRange.split(separator);
            checkIn = dates[0].trim();
            checkOut = dates[1].trim();
        }
    }

    try {
        hotelContainer.innerHTML = '<div class="empty-msg">Đang tìm kiếm...</div>';
        
        // XÂY DỰNG URL ĐỘNG TÙY VÀO VIỆC CÓ CHỌN NGÀY HAY KHÔNG
        let url = `http://localhost:8080/api/hotels/search?location=${encodeURIComponent(location)}`;
        if (checkIn && checkOut) {
            url += `&checkIn=${checkIn}&checkOut=${checkOut}`;
        }
        
        const response = await fetch(url);
        let resultHotels = await response.json();
        
        // LOGIC LỌC THEO SỨC CHỨA
        if (capacityVal && capacityVal !== 'all' && resultHotels.length > 0) {
            const requiredCapacity = parseInt(capacityVal);
            resultHotels = resultHotels.filter(h => h.maxCapacity >= requiredCapacity);
        }
        
        hotelContainer.innerHTML = '';
        
        if (resultHotels.length === 0) {
            hotelContainer.innerHTML = `<div class="empty-msg" style="color: #dc2626; font-size: 1.1rem; padding: 30px;">
                Không tìm thấy khách sạn nào đáp ứng được sức chứa và địa điểm của bạn.<br>
                <span style="font-size: 0.95rem; color: #64748b; font-weight: normal; margin-top: 10px; display: inline-block;">
                    Gợi ý: Hãy thử giảm số lượng người hoặc tìm ở địa điểm khác.
                </span>
            </div>`;
            return;
        }

        // Truyền thêm checkIn, checkOut, và capacityVal để gắn vào nút "Xem Các Phòng"
        renderHotels(resultHotels, checkIn, checkOut, capacityVal);

    } catch (error) {
        showToast("Lỗi truy xuất dữ liệu, vui lòng thử lại sau.", "error");
    }
});

// Chạy hàm lấy toàn bộ dữ liệu khi web vừa load xong
fetchHotels();