// ========== 通用工具函数 ==========

function clearErrors() {
    document.querySelectorAll('.error-hint').forEach(el => {
        el.textContent = '';
    });
    const msgBox = document.getElementById('messageBox');
    if (msgBox) msgBox.style.display = 'none';
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.textContent = message;
    }
}

function showMessage(message, type) {
    const box = document.getElementById('messageBox');
    if (box) {
        box.textContent = message;
        box.className = 'alert alert-' + type;
        box.style.display = 'block';
        setTimeout(() => { box.style.display = 'none'; }, 3000);
    }
}

function setLoading(btn, loading) {
    if (loading) {
        btn.disabled = true;
        btn._originalText = btn.textContent;
        btn.textContent = '处理中...';
    } else {
        btn.disabled = false;
        btn.textContent = btn._originalText || btn.textContent;
    }
}

// ========== 密码显示切换 ==========
function togglePassword(fieldId) {
    const input = document.getElementById(fieldId);
    if (input) {
        input.type = input.type === 'password' ? 'text' : 'password';
    }
}

// ========== 登录页逻辑（Thymeleaf 版用 fetch 提交，避免页面刷新） ==========
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        clearErrors();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const loginBtn = document.getElementById('loginBtn');

        let hasError = false;
        if (!username) {
            showError('usernameError', '请输入用户名');
            hasError = true;
        }
        if (!password) {
            showError('passwordError', '请输入密码');
            hasError = true;
        }
        if (hasError) return;

        setLoading(loginBtn, true);

        try {
            const formData = new URLSearchParams();
            formData.append('username', username);
            formData.append('password', password);

            const response = await fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            });

            if (response.ok) {
                const data = await response.json();
                sessionStorage.setItem('username', data.username);
                sessionStorage.setItem('role', data.role);
                sessionStorage.setItem('studentId', data.studentId);
                window.location.href = '/home';
            } else {
                const data = await response.json();
                // 显示错误，不跳转
                const errorDiv = document.querySelector('.alert-error');
                if (errorDiv) {
                    errorDiv.textContent = data.msg || '用户名或密码错误';
                    errorDiv.style.display = 'block';
                } else {
                    alert(data.msg || '用户名或密码错误');
                }
            }
        } catch (error) {
            alert('网络错误，请稍后重试');
        } finally {
            setLoading(loginBtn, false);
        }
    });
}

const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        clearErrors();

        const username = document.getElementById('username').value.trim();
        const studentId = document.getElementById('studentId').value.trim();
        const realName = document.getElementById('realName').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const registerBtn = document.getElementById('registerBtn');

        let hasError = false;

        if (!username || username.length < 4) {
            showError('usernameError', '用户名至少4位字母或数字');
            hasError = true;
        } else if (!/^[a-zA-Z0-9]+$/.test(username)) {
            showError('usernameError', '用户名只能包含字母和数字');
            hasError = true;
        }
        if (!studentId) {
            showError('studentIdError', '请输入学号');
            hasError = true;
        }
        if (!realName) {
            showError('realNameError', '请输入真实姓名');
            hasError = true;
        }
        if (!password || password.length < 6) {
            showError('passwordError', '密码至少6位');
            hasError = true;
        }
        if (password !== confirmPassword) {
            showError('confirmPasswordError', '两次输入的密码不一致');
            hasError = true;
        }
        if (hasError) return;

        setLoading(registerBtn, true);

        try {
            const response = await fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: username,
                    studentId: studentId,
                    realName: realName,
                    password: password
                })
            });

            if (response.ok) {
                showMessage('注册成功！即将跳转到登录页...', 'success');
                registerBtn.disabled = true;
                setTimeout(() => {
                    window.location.href = '/login-page?registered=true';
                }, 2000);
            } else {
                const text = await response.text();
                showMessage(text || '注册失败，请重试', 'error');
            }
        } catch (error) {
            showMessage('网络错误，请稍后重试', 'error');
        } finally {
            setLoading(registerBtn, false);
        }
    });
}

// ========== 退出登录 ==========
function logout() {
    fetch('/logout', { method: 'POST' })
        .then(() => {
            sessionStorage.clear();
            window.location.href = '/login-page';
        })
        .catch(() => {
            sessionStorage.clear();
            window.location.href = '/login-page';
        });
}