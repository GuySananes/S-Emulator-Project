import { api, contextPath } from './api.js';
import { state } from './state.js';
import { showToast, createTable, formatDateTime, updateCreditsDisplay } from './ui.js';

let pollInterval = null;

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    await checkExistingSession();
    startLiveUsersPoll();
    setupLoginForm();
});

async function checkExistingSession() {
    try {
        const session = await api.getSession();
        if (session && session.username) {
            window.location.href = contextPath + '/dashboard.html';
            return;
        }
    } catch (error) {
        console.log('No existing session');
    }
}

function updateUI() {
    const loginStatus = document.getElementById('loginStatus');
    const currentUser = document.getElementById('currentUser');
    const loginBtn = document.getElementById('loginBtn');
    const userCredits = document.getElementById('userCredits');

    if (state.username) {
        loginStatus.classList.remove('hidden');
        currentUser.textContent = state.username;
        loginBtn.disabled = true;
        userCredits.textContent = state.credits;
        updateCreditsDisplay(state.credits);
    }
}

function setupLoginForm() {
    const form = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const loginBtn = document.getElementById('loginBtn');
    const errorDiv = document.getElementById('loginError');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        errorDiv.classList.add('hidden');

        const username = usernameInput.value.trim();

        if (!username) {
            errorDiv.textContent = 'Username is required';
            errorDiv.classList.remove('hidden');
            return;
        }

        if (username.length > 20) {
            errorDiv.textContent = 'Username must be 20 characters or less';
            errorDiv.classList.remove('hidden');
            return;
        }

        loginBtn.disabled = true;
        loginBtn.textContent = 'Logging in...';

        try {
            const response = await api.login(username);

            if (response.ok) {
                state.setUser(response.username, response.credits);
                showToast('success', `Welcome, ${response.username}!`);

                // Redirect to dashboard
                setTimeout(() => {
                    window.location.href = contextPath + '/dashboard.html';
                }, 500);
            } else {
                throw new Error(response.error || 'Login failed');
            }
        } catch (error) {
            errorDiv.textContent = error.message;
            errorDiv.classList.remove('hidden');
            loginBtn.disabled = false;
            loginBtn.textContent = 'Login';
            showToast('error', error.message);
        }
    });
}

function startLiveUsersPoll() {
    updateLiveUsers();
    pollInterval = setInterval(updateLiveUsers, 3000);
}

async function updateLiveUsers() {
    const container = document.getElementById('usersTableContainer');
    const lastUpdatedSpan = document.getElementById('lastUpdated');

    try {
        const response = await api.getLiveUsers();

        if (response.users && response.users.length > 0) {
            const headers = ['Name', 'Mains', 'Funcs', 'Credits', 'Used', 'Runs'];
            const rows = response.users.map(user => [
                user.name,
                user.mains || 0,
                user.funcs || 0,
                user.credits || 0,
                user.used || 0,
                user.runs || 0
            ]);

            const table = createTable(headers, rows);
            container.innerHTML = '';
            container.appendChild(table);
        } else {
            container.innerHTML = '<div class="empty-state">No live users</div>';
        }

        if (response.lastUpdated) {
            lastUpdatedSpan.textContent = formatDateTime(response.lastUpdated);
            state.lastUpdated = response.lastUpdated;
        }
    } catch (error) {
        console.error('Failed to fetch live users:', error);
        // Don't show toast on polling errors to avoid spam
    }
}

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});