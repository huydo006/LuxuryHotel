// ==========================================
// 1. KIỂM TRA ĐĂNG NHẬP & PHÂN QUYỀN ADMIN
// ==========================================
const userStr = localStorage.getItem('currentUser');
if (!userStr) {
    window.location.href = 'auth.html';
}
const currentUser = JSON.parse(userStr);

// Nếu role không phải admin, đá văng về trang chủ
if (currentUser.role !== 'admin') {
    alert("Bạn không có quyền truy cập trang này!");
    window.location.href = 'customer-dashboard.html';
}

document.getElementById('admin-name').innerText = `Xin chào, Quản trị viên ${currentUser.username}`;

// Nút Đăng xuất
document.getElementById('btn-logout').addEventListener('click', () => {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
});

// ==========================================
// 2. LOGIC CHUYỂN TAB (SIDEBAR MENU)
// ==========================================
window.switchTab = (tabId) => {
    // Ẩn tất cả nội dung tab
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    // Xóa active ở tất cả menu
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });

    // Hiện tab được chọn
    document.getElementById(tabId).classList.add('active');
    event.currentTarget.classList.add('active');

    // Tải dữ liệu tương ứng với Tab
    if (tabId === 'tab-bookings') fetchAllBookings();
    if (tabId === 'tab-hotels') fetchAllHotels();
    if (tabId === 'tab-rooms') loadHotelDropdownForRooms();
    if (tabId === 'tab-customers') fetchAllCustomers();
    if (tabId === 'tab-promotions') fetchAllPromotions(); // Kích hoạt tab Khuyến mãi
};

// ==========================================
// 3. QUẢN LÝ ĐẶT PHÒNG
// ==========================================
async function fetchAllBookings() {
    try {
        const response = await fetch('http://localhost:8080/api/bookings/all');
        const bookings = await response.json();
        renderBookings(bookings);
    } catch (error) {
        document.getElementById('booking-list').innerHTML = `<tr><td colspan="7" style="color: red; text-align:center;">Lỗi kết nối Server!</td></tr>`;
    }
}

function renderBookings(bookings) {
    const tbody = document.getElementById('booking-list');
    tbody.innerHTML = '';

    if (bookings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center;">Chưa có đơn đặt phòng nào.</td></tr>`;
        return;
    }

    bookings.forEach(b => {
        // Xử lý hiển thị trạng thái và khóa nút
        let statusHtml = '';
        let disableBtns = '';

        if (b.status === 'processing') {
            statusHtml = `<span class="status processing">Đang chờ duyệt</span>`;
        } else if (b.status === 'success') {
            statusHtml = `<span class="status success">Đã xác nhận</span>`;
            disableBtns = 'disabled'; // Đã duyệt rồi thì khóa nút
        } else {
            statusHtml = `<span class="status cancelled">Đã hủy</span>`;
            disableBtns = 'disabled'; // Đã hủy rồi thì khóa nút
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${b.bookingID}</td>
            <td style="font-weight: bold;">${b.username}</td>
            <td>
                <div>${b.nameHotel}</div>
                <div style="font-size: 0.85rem; color: #64748b;">Phòng: ${b.roomType}</div>
            </td>
            <td>${b.checkInDate} <br>đến<br> ${b.checkOutDate}</td>
            <td style="color: #dc2626; font-weight: bold;">${b.totalPrice.toLocaleString('vi-VN')} ₫</td>
            <td>${statusHtml}</td>
            <td class="action-btns">
                <button class="btn-sm btn-approve" ${disableBtns} onclick="updateStatus(${b.bookingID}, 'success')">Duyệt</button>
                <button class="btn-sm btn-delete" ${disableBtns} onclick="updateStatus(${b.bookingID}, 'cancelled')">Hủy</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

window.updateStatus = async (bookingId, newStatus) => {
    const confirmMsg = newStatus === 'success' ? 'Xác nhận duyệt đơn này?' : 'Bạn chắc chắn muốn hủy đơn này?';
    if (!confirm(confirmMsg)) return;

    try {
        const response = await fetch(`http://localhost:8080/api/bookings/${bookingId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        const result = await response.json();
        if (result.success) {
            alert(result.message);
            fetchAllBookings(); // Tải lại bảng ngay lập tức
        } else {
            alert(result.message);
        }
    } catch (error) {
        alert("Lỗi khi cập nhật!");
    }
}

// ==========================================
// 4. QUẢN LÝ KHÁCH SẠN (UC-05)
// ==========================================
let globalHotels = [];

async function fetchAllHotels() {
    try {
        const res = await fetch('http://localhost:8080/api/hotels');
        globalHotels = await res.json();
        renderAdminHotels(globalHotels);
    } catch (error) {
        document.getElementById('hotel-list').innerHTML = `<tr><td colspan="6" style="color:red; text-align:center;">Lỗi kết nối máy chủ!</td></tr>`;
    }
}

function renderAdminHotels(hotels) {
    const tbody = document.getElementById('hotel-list');
    tbody.innerHTML = '';

    if (hotels.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center;">Chưa có khách sạn nào trong hệ thống.</td></tr>`;
        return;
    }

    hotels.forEach(h => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${h.id}</td>
            <td><img src="${h.image}" style="width:60px; height:40px; border-radius:4px; object-fit:cover;"></td>
            <td><strong>${h.name}</strong></td>
            <td>${h.location}</td>
            <td>⭐ ${h.rating} (${h.bookingsCount})</td>
            <td class="action-btns">
                <button class="btn-sm btn-edit" onclick="openHotelModal(${h.id})">Sửa</button>
                <button class="btn-sm btn-delete" onclick="deleteHotel(${h.id})">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

const hotelModal = document.getElementById('hotelModal');
let isEditingHotel = false;

window.openHotelModal = (hotelId = null) => {
    isEditingHotel = !!hotelId;

    // Bỏ chọn tất cả checkbox trước để khi mở modal mới không bị lưu lại tích cũ
    document.querySelectorAll('#h-amenities-checkboxes input[type="checkbox"]').forEach(cb => cb.checked = false);

    if (isEditingHotel) {
        document.getElementById('hotelModalTitle').innerText = 'Sửa Thông Tin Khách Sạn';
        const h = globalHotels.find(x => x.id === hotelId);
        document.getElementById('h-id').value = h.id;
        document.getElementById('h-name').value = h.name;
        document.getElementById('h-location').value = h.location;
        document.getElementById('h-image').value = h.image;
        document.getElementById('h-desc').value = h.description;

        // --- ĐOẠN SỬA/THÊM MỚI: Tự động tick lại các tiện ích của khách sạn này ---
        if (h.amenities && Array.isArray(h.amenities)) {
            h.amenities.forEach(am => {
                // Tìm checkbox có giá trị trùng với tiện ích trong mảng
                const cb = document.querySelector(`#h-amenities-checkboxes input[value="${am}"]`);
                if (cb) {
                    cb.checked = true;
                }
            });
        }
        // -----------------------------------------------------------------------

    } else {
        document.getElementById('hotelModalTitle').innerText = 'Thêm Khách Sạn Mới';
        document.getElementById('h-id').value = '';
        document.getElementById('h-name').value = '';
        document.getElementById('h-location').value = '';
        document.getElementById('h-image').value = '';
        document.getElementById('h-desc').value = '';
    }
    hotelModal.classList.add('active');
}

window.closeHotelModal = () => {
    hotelModal.classList.remove('active');
}

window.saveHotel = async () => {
    // 1. Lấy dữ liệu từ các ô nhập text
    const id = document.getElementById('h-id').value;
    const name = document.getElementById('h-name').value.trim();
    const location = document.getElementById('h-location').value.trim();
    const image = document.getElementById('h-image').value.trim();
    const description = document.getElementById('h-desc').value.trim();

    // 2. Lấy dữ liệu từ các thẻ tiện ích (amenities) đã được tick chọn
    const amenities = [];
    document.querySelectorAll('#h-amenities-checkboxes input[type="checkbox"]:checked').forEach(cb => {
        amenities.push(cb.value);
    });

    // 3. Kiểm tra rỗng
    if(!name || !location || !image) {
        return alert("Vui lòng nhập đầy đủ Tên, Địa điểm và Link ảnh!");
    }

    // 4. Gộp vào payload gửi đi
    const payload = { name, location, image, description, amenities };
    
    // 5. Xác định là Thêm mới (POST) hay Cập nhật (PUT)
    const method = isEditingHotel ? 'PUT' : 'POST';
    const url = isEditingHotel ? `http://localhost:8080/api/admin/hotels/${id}` : `http://localhost:8080/api/admin/hotels`;

    // 6. Gửi API
    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        
        if (data.success) {
            alert(data.message);
            closeHotelModal(); // Đóng modal
            fetchAllHotels();  // Tải lại danh sách
        } else {
            alert("Lỗi từ Backend: " + data.message);
        }
    } catch (error) {
        console.error("Lỗi khi gửi API:", error);
        alert("Lỗi kết nối máy chủ! Hãy kiểm tra lại Backend.");
    }
}

window.deleteHotel = async (id) => {
    if (!confirm("CẢNH BÁO: Việc xóa khách sạn sẽ xóa toàn bộ phòng thuộc khách sạn này. Bạn có chắc chắn không?")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/hotels/${id}`, {
            method: 'DELETE'
        });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            fetchAllHotels();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

// ==========================================
// 5. QUẢN LÝ PHÒNG (UC-06)
// ==========================================
let globalRooms = [];

async function loadHotelDropdownForRooms() {
    try {
        const res = await fetch('http://localhost:8080/api/hotels');
        const hotels = await res.json();
        const select = document.getElementById('room-hotel-select');

        const currentVal = select.value;

        select.innerHTML = '<option value="">-- Vui lòng chọn khách sạn --</option>';
        hotels.forEach(h => {
            select.innerHTML += `<option value="${h.id}">${h.name} (${h.location})</option>`;
        });

        if (currentVal) select.value = currentVal;
    } catch (error) {
        console.error("Lỗi tải danh sách khách sạn", error);
    }
}

window.loadRoomsForSelectedHotel = async () => {
    const hotelId = document.getElementById('room-hotel-select').value;
    const btnAdd = document.getElementById('btn-add-room');
    const tbody = document.getElementById('room-list-admin');

    if (!hotelId) {
        btnAdd.disabled = true;
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b;">Vui lòng chọn khách sạn để xem danh sách phòng.</td></tr>`;
        return;
    }

    btnAdd.disabled = false;
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>`;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/hotels/${hotelId}/rooms`);
        globalRooms = await res.json();
        renderAdminRooms(globalRooms);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: red;">Lỗi kết nối máy chủ!</td></tr>`;
    }
};

function renderAdminRooms(rooms) {
    const tbody = document.getElementById('room-list-admin');
    tbody.innerHTML = '';

    if (rooms.length === 0) {
        // Tăng colspan từ 5 lên 6 vì bảng đã có thêm cột "Số lượng"
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b;">Khách sạn này chưa có phòng nào. Hãy thêm mới!</td></tr>`;
        return;
    }

    rooms.forEach(r => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${r.roomID}</td>
            <td><strong>${r.roomType}</strong></td>
            <td>${r.capacity} người</td>
            <td><span style="font-weight: 600; color: #475569;">${r.quantity}</span> phòng</td>
            <td style="color: #dc2626; font-weight: 600;">${r.defaultPrice.toLocaleString('vi-VN')} ₫</td>
            <td class="action-btns">
                <button class="btn-sm btn-edit" onclick="openRoomModal(${r.roomID})">Sửa</button>
                <button class="btn-sm btn-delete" onclick="deleteRoom(${r.roomID})">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

const roomModal = document.getElementById('roomModal');
let isEditingRoom = false;

window.openRoomModal = (roomId = null) => {
    isEditingRoom = !!roomId;

    if (isEditingRoom) {
        document.getElementById('roomModalTitle').innerText = 'Sửa Thông Tin Phòng';
        const r = globalRooms.find(x => x.roomID === roomId);
        document.getElementById('r-id').value = r.roomID;
        document.getElementById('r-type').value = r.roomType;
        document.getElementById('r-capacity').value = r.capacity;
        document.getElementById('r-price').value = r.defaultPrice;
        document.getElementById('r-quantity').value = r.quantity;
    } else {
        document.getElementById('roomModalTitle').innerText = 'Thêm Phòng Mới';
        document.getElementById('r-id').value = '';
        document.getElementById('r-type').value = '';
        document.getElementById('r-capacity').value = '';
        document.getElementById('r-price').value = '';
        document.getElementById('r-quantity').value = '';
    }
    roomModal.classList.add('active');
}

window.closeRoomModal = () => {
    roomModal.classList.remove('active');
}

window.saveRoom = async () => {
    const hotelId = document.getElementById('room-hotel-select').value;
    const roomId = document.getElementById('r-id').value;
    const roomType = document.getElementById('r-type').value.trim();
    const capacity = document.getElementById('r-capacity').value.trim();
    const quantity = document.getElementById('r-quantity').value.trim();
    const price = document.getElementById('r-price').value.trim();

    if (!roomType || !capacity || !price) {
        return alert("Vui lòng nhập đầy đủ thông tin phòng!");
    }

    const payload = { hotelId, roomType, capacity, price, quantity };
    const method = isEditingRoom ? 'PUT' : 'POST';
    const url = isEditingRoom ? `http://localhost:8080/api/admin/rooms/${roomId}` : `http://localhost:8080/api/admin/rooms`;

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (data.success) {
            alert("Hệ thống: " + data.message);
            closeRoomModal();
            loadRoomsForSelectedHotel();
        } else {
            alert("Lỗi: " + data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

window.deleteRoom = async (id) => {
    if (!confirm("Bạn có chắc chắn muốn xóa phòng này không?")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/rooms/${id}`, { method: 'DELETE' });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            loadRoomsForSelectedHotel();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

// ==========================================
// 6. QUẢN LÝ KHÁCH HÀNG (UC-08)
// ==========================================
let globalCustomers = [];
let searchTimeout;

// Tìm kiếm Khách hàng (Tự động tải lại sau khi gõ 0.5s)
window.handleSearchCustomer = (event) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        const keyword = event.target.value.trim();
        fetchAllCustomers(keyword);
    }, 500); // Kỹ thuật Debounce giúp giảm tải Server
}

async function fetchAllCustomers(keyword = '') {
    const tbody = document.getElementById('customer-list-admin');
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>`;

    try {
        let url = 'http://localhost:8080/api/admin/customers';
        if (keyword) {
            url += `?keyword=${encodeURIComponent(keyword)}`;
        }

        const response = await fetch(url);
        globalCustomers = await response.json();
        renderAdminCustomers(globalCustomers);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: red;">Lỗi kết nối máy chủ!</td></tr>`;
    }
}

function renderAdminCustomers(customers) {
    const tbody = document.getElementById('customer-list-admin');
    tbody.innerHTML = '';

    if (customers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b;">Không tìm thấy khách hàng phù hợp.</td></tr>`;
        return;
    }

    customers.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${c.accountID}</td>
            <td><strong>${c.username}</strong></td>
            <td>${c.fullName}</td>
            <td>${c.email}</td>
            <td class="action-btns">
                <button class="btn-sm btn-edit" onclick="openCustomerModal(${c.accountID})">Sửa</button>
                <button class="btn-sm btn-delete" onclick="deleteCustomer(${c.accountID})">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

const customerModal = document.getElementById('customerModal');
let isEditingCustomer = false;

window.openCustomerModal = (customerId = null) => {
    isEditingCustomer = !!customerId;
    const passGroup = document.getElementById('c-password-group');
    const usernameInput = document.getElementById('c-username');

    if (isEditingCustomer) {
        document.getElementById('customerModalTitle').innerText = 'Sửa Thông Tin Khách Hàng';
        const c = globalCustomers.find(x => x.accountID === customerId);
        document.getElementById('c-id').value = c.accountID;
        document.getElementById('c-fullname').value = c.fullName;
        document.getElementById('c-email').value = c.email;

        usernameInput.value = c.username;
        usernameInput.disabled = true;
        passGroup.style.display = 'none';
    } else {
        document.getElementById('customerModalTitle').innerText = 'Thêm Khách Hàng Mới';
        document.getElementById('c-id').value = '';
        document.getElementById('c-fullname').value = '';
        document.getElementById('c-email').value = '';

        usernameInput.value = '';
        usernameInput.disabled = false;
        document.getElementById('c-password').value = '';
        passGroup.style.display = 'block';
    }
    customerModal.classList.add('active');
}

window.closeCustomerModal = () => {
    customerModal.classList.remove('active');
}

window.saveCustomer = async () => {
    const id = document.getElementById('c-id').value;
    const fullName = document.getElementById('c-fullname').value.trim();
    const email = document.getElementById('c-email').value.trim();
    const username = document.getElementById('c-username').value.trim();
    const password = document.getElementById('c-password').value;

    if (!fullName || !email) {
        return alert("Vui lòng nhập đầy đủ Họ tên và Email!");
    }

    let payload = { fullName, email };
    let method = isEditingCustomer ? 'PUT' : 'POST';
    let url = isEditingCustomer ? `http://localhost:8080/api/admin/customers/${id}` : `http://localhost:8080/api/admin/customers`;

    if (!isEditingCustomer) {
        if (!username || !password) return alert("Vui lòng thiết lập Tên đăng nhập và Mật khẩu!");
        payload.username = username;
        payload.password = password;
    }

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (data.success) {
            alert("Hệ thống: " + data.message);
            closeCustomerModal();
            const currentKeyword = document.getElementById('search-customer').value.trim();
            fetchAllCustomers(currentKeyword);
        } else {
            alert("Lỗi: " + data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

window.deleteCustomer = async (id) => {
    if (!confirm("CẢNH BÁO: Bạn có chắc chắn muốn xóa tài khoản khách hàng này khỏi hệ thống không?")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/customers/${id}`, { method: 'DELETE' });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            const currentKeyword = document.getElementById('search-customer').value.trim();
            fetchAllCustomers(currentKeyword);
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

// ==========================================
// 7. QUẢN LÝ KHUYẾN MÃI (UC-07)
// ==========================================
let globalPromos = [];

async function fetchAllPromotions() {
    const tbody = document.getElementById('promo-list-admin');
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>`;

    try {
        const response = await fetch('http://localhost:8080/api/admin/promotions');
        globalPromos = await response.json();
        renderAdminPromos(globalPromos);
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">Lỗi kết nối máy chủ!</td></tr>`;
    }
}

function renderAdminPromos(promos) {
    const tbody = document.getElementById('promo-list-admin');
    tbody.innerHTML = '';

    if (promos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b;">Chưa có chương trình khuyến mãi nào.</td></tr>`;
        return;
    }

    // Lấy chuỗi ngày tháng hiện tại (Format: YYYY-MM-DD) để so sánh trực tiếp
    const today = new Date();
    const todayStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');

    promos.forEach(p => {
        let statusHtml = '';
        let toggleBtnHtml = '';
        let isExpired = false;

        // TỰ ĐỘNG HÓA LOGIC PHÂN LOẠI TRẠNG THÁI
        if (p.endDate < todayStr) {
            statusHtml = `<span class="status cancelled">Đã hết hạn</span>`;
            isExpired = true;
        } else if (p.usedCount >= p.usageLimit) {
            statusHtml = `<span class="status cancelled">Đã hết lượt</span>`;
            isExpired = true;
        } else if (p.isValid === 0) {
            statusHtml = `<span class="status" style="background:#fee2e2; color:#b91c1c;">Tạm dừng</span>`;
            toggleBtnHtml = `<button class="btn-sm btn-approve" onclick="togglePromoStatus(${p.promotionID}, 1)">Kích hoạt</button>`;
        } else if (p.startDate > todayStr) {
            statusHtml = `<span class="status" style="background:#e0f2fe; color:#0369a1;">Sắp tới</span>`;
            toggleBtnHtml = `<button class="btn-sm btn-delete" onclick="togglePromoStatus(${p.promotionID}, 0)">Tạm dừng</button>`;
        } else {
            statusHtml = `<span class="status success">Đang chạy</span>`;
            toggleBtnHtml = `<button class="btn-sm btn-delete" onclick="togglePromoStatus(${p.promotionID}, 0)">Tạm dừng</button>`;
        }

        // Nếu mã đã hết hạn/hết lượt, vô hiệu hóa việc đổi trạng thái
        if (isExpired) toggleBtnHtml = '';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <strong style="color: var(--gold); font-size: 1.1rem;">${p.discountCode}</strong>
                <div style="font-size: 0.85rem; color: #64748b; margin-top: 4px;">Đã dùng: <strong>${p.usedCount} / ${p.usageLimit}</strong></div>
            </td>
            <td style="font-weight: 500;">${p.namePromo}</td>
            <td>
                <div style="font-weight: bold; color: #dc2626; font-size: 1.1rem;">-${p.discountPercent}%</div>
                <div style="font-size: 0.8rem; color: #64748b;">Tối đa: ${p.maxDiscountAmount.toLocaleString('vi-VN')} ₫</div>
            </td>
            <td>
                <div style="font-size: 0.85rem; margin-bottom: 4px;">Đơn tối thiểu: <strong>${p.minBookingValue.toLocaleString('vi-VN')} ₫</strong></div>
                <div style="font-size: 0.85rem; margin-bottom: 4px;">Từ: <strong>${p.startDate}</strong></div>
                <div style="font-size: 0.85rem;">Đến: <strong>${p.endDate}</strong></div>
            </td>
            <td>${statusHtml}</td>
            <td class="action-btns">
                ${toggleBtnHtml}
                <button class="btn-sm btn-delete" onclick="deletePromo(${p.promotionID})">Xóa</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

window.openPromoModal = () => {
    document.getElementById('p-code').value = '';
    document.getElementById('p-name').value = '';
    document.getElementById('p-percent').value = '';
    document.getElementById('p-max').value = '';
    document.getElementById('p-min').value = '';
    document.getElementById('p-limit').value = '';
    document.getElementById('p-start').value = '';
    document.getElementById('p-end').value = '';
    promoModal.classList.add('active');
}

window.closePromoModal = () => {
    promoModal.classList.remove('active');
}

window.savePromo = async () => {
    const payload = {
        discountCode: document.getElementById('p-code').value.trim(),
        namePromo: document.getElementById('p-name').value.trim(),
        discountPercent: document.getElementById('p-percent').value.trim(),
        maxDiscountAmount: document.getElementById('p-max').value.trim(),
        minBookingValue: document.getElementById('p-min').value.trim() || 0, // Mặc định là 0 nếu để trống
        usageLimit: document.getElementById('p-limit').value.trim(),
        startDate: document.getElementById('p-start').value,
        endDate: document.getElementById('p-end').value
    };

    if (!payload.discountCode || !payload.namePromo || !payload.discountPercent || !payload.maxDiscountAmount || !payload.usageLimit || !payload.startDate || !payload.endDate) {
        return alert("Vui lòng nhập đầy đủ thông tin bắt buộc!");
    }

    try {
        const res = await fetch('http://localhost:8080/api/admin/promotions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            closePromoModal();
            fetchAllPromotions();
        } else {
            alert("Lỗi: " + data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

// Gọi API Tạm dừng / Kích hoạt lại
window.togglePromoStatus = async (id, newStatus) => {
    const msg = newStatus === 1
        ? "Bạn có chắc chắn muốn KÍCH HOẠT LẠI chương trình này?"
        : "Khi tạm dừng, mã giảm giá sẽ không thể sử dụng. Bạn có chắc chắn?";

    if (!confirm(msg)) return;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/promotions/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ isValid: newStatus })
        });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            fetchAllPromotions();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

// Xóa vĩnh viễn
window.deletePromo = async (id) => {
    if (!confirm("Hành động này sẽ XÓA VĨNH VIỄN mã khuyến mãi. Bạn có chắc chắn không?")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/admin/promotions/${id}`, { method: 'DELETE' });
        const data = await res.json();

        if (data.success) {
            alert(data.message);
            fetchAllPromotions();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Lỗi kết nối máy chủ!");
    }
}

async function fetchMasterAmenities() {
    try {
        const res = await fetch('http://localhost:8080/api/amenities');
        const masterAmenities = await res.json();
        const container = document.getElementById('h-amenities-checkboxes');
        
        if (container) {
            container.innerHTML = ''; 
            masterAmenities.forEach(am => {
                container.innerHTML += `
                    <label class="amenity-checkbox-label">
                        <input type="checkbox" value="${am}">
                        <span class="amenity-chip">${am}</span>
                    </label>
                `;
            });
        }
    } catch (e) {
        console.error("Lỗi tải tiện ích", e);
        const container = document.getElementById('h-amenities-checkboxes');
        if (container) {
            container.innerHTML = '<span style="color: red;">Lỗi tải kho tiện ích từ Server!</span>';
        }
    }
}

fetchMasterAmenities().then(() => {
    // Nếu có logic mở modal ngay lập tức hoặc xử lý bất đồng bộ thì tiện ích đã sẵn sàng
    console.log("Hệ thống: Kho tiện ích đã được tải thành công.");
});

// 2. Chạy mặc định hiển thị tab Quản lý đặt phòng đầu tiên
fetchAllBookings();