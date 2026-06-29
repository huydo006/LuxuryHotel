const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const formTitle = document.getElementById('form-title');
const errorMsg = document.getElementById('error-msg');
const successMsg = document.getElementById('success-msg');

// Chuyển đổi giao diện Đăng ký / Đăng nhập
document.getElementById('show-register').addEventListener('click', () => {
    loginForm.classList.add('hidden');
    registerForm.classList.remove('hidden');
    formTitle.innerText = "Đăng Ký";
    errorMsg.style.display = 'none';
    successMsg.style.display = 'none';
});

document.getElementById('show-login').addEventListener('click', () => {
    registerForm.classList.add('hidden');
    loginForm.classList.remove('hidden');
    formTitle.innerText = "Đăng Nhập";
    errorMsg.style.display = 'none';
    successMsg.style.display = 'none';
});

// Xử lý Đăng Nhập
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorMsg.style.display = 'none';

    try {
        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: document.getElementById('l-username').value.trim(),
                password: document.getElementById('l-password').value
            })
        });

        const data = await response.json();

        if (data.success) {
            // Lưu thông tin user (đã bao gồm fullName và email từ backend trả về) vào localStorage
            localStorage.setItem('currentUser', JSON.stringify(data.user));
            window.location.href = data.user.role === 'admin' ? 'admin-dashboard.html' : 'customer-dashboard.html';
        } else {
            errorMsg.innerText = data.message;
            errorMsg.style.display = 'block';
        }
    } catch (error) {
        errorMsg.innerText = "Lỗi kết nối Server! Vui lòng kiểm tra lại Backend Java (cổng 8080).";
        errorMsg.style.display = 'block';
    }
});

// Xử lý Đăng Ký
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorMsg.style.display = 'none';

    try {
        const response = await fetch('http://localhost:8080/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fullName: document.getElementById('r-fullname').value.trim(), // Đã có fullName chuẩn
                email: document.getElementById('r-email').value.trim(), 
                phoneNumber: document.getElementById('r-phone').value.trim(),
                username: document.getElementById('r-username').value.trim(),
                password: document.getElementById('r-password').value
            })
        });

        const data = await response.json();

        if (data.success) {
            successMsg.innerText = "Đăng ký thành công! Đang chuyển về đăng nhập...";
            successMsg.style.display = 'block';
            setTimeout(() => { document.getElementById('show-login').click(); }, 1500);
        } else {
            errorMsg.innerText = data.message;
            errorMsg.style.display = 'block';
        }
    } catch (error) {
        errorMsg.innerText = "Lỗi kết nối Backend Java!";
        errorMsg.style.display = 'block';
    }
});