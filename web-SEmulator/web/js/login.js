// login.js - Login page controller

import { api } from './api.js';
import { showToast } from './ui.js';

let loginForm;
let usernameInput;
let loginBtn;
let loginError;
let usersTableContainer;

/**
 * Initialize login page
 */
function init() {
    console.log('Initializing login page...');

    // Get DOM elements
    loginForm = document.getElementById('loginForm');
    usernameInput = document.getElementById('username');
    loginBtn = document.getElementById('loginBtn');
    loginError = document.getElementById('loginError');
    usersTableContainer = document.getElementById('usersTableContainer');

    // Setup event handlers
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Load live users
    loadLiveUsers();

    // Poll for live users every 5 seconds
    setInterval(loadLiveUsers, 5000);
}

/**
 * Handle login form submission
 */
async function handleLogin(event) {
    event.preventDefault();

    const username = usernameInput.value.trim();

    if (!username) {
        showError('Please enter a username');
        return;
    }

    // Disable button during login
    loginBtn.disabled = true;
    loginBtn.textContent = 'Logging in...';
    hideError();

    try {
        console.log('Attempting login for:', username);

        const response = await api.login(username);

        console.log('Login response:', response);

        // Check if login was successful
        if (response.ok) {
            showToast('Login successful!', 'success');

            // Redirect to dashboard
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 500);
        } else {
            // Login failed
            const errorMessage = response.error || 'Login failed';
            showError(errorMessage);
            loginBtn.disabled = false;
            loginBtn.textContent = 'Login';
        }

    } catch (error) {
        console.error('Login error:', error);

        // Check for specific error messages
        let errorMessage = 'Login failed. Please try again.';

        if (error.message.includes('username_taken')) {
            errorMessage = 'Username is already taken. Please choose another.';
        } else if (error.message.includes('username_required')) {
            errorMessage = 'Username is required.';
        } else if (error.message.includes('username_too_long')) {
            errorMessage = 'Username is too long (max 50 characters).';
        } else if (error.message) {
            errorMessage = error.message;
        }

        showError(errorMessage);
        showToast(errorMessage, 'error');

        loginBtn.disabled = false;
        loginBtn.textContent = 'Login';
    }
}

/**
 * Load and display live users
 */
async function loadLiveUsers() {
    try {
        const response = await api.getUsers();

        if (!response || !response.users) {
            usersTableContainer.innerHTML = '<div class="empty-state">No users online</div>';
            return;
        }

        // Create users table
        const table = document.createElement('table');
        table.className = 'data-table';

        const thead = document.createElement('thead');
        thead.innerHTML = `
            <tr>
                <th>Username</th>
                <th>Credits</th>
            </tr>
        `;
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        response.users.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${user.name}</td>
                <td>${user.credits}</td>
            `;
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        usersTableContainer.innerHTML = '';
        usersTableContainer.appendChild(table);

        // Update last updated time
        const lastUpdated = document.getElementById('lastUpdated');
        if (lastUpdated) {
            lastUpdated.textContent = new Date().toLocaleTimeString();
        }

    } catch (error) {
        console.error('Failed to load users:', error);
        // Don't show error to user, just log it
    }
}

/**
 * Show error message
 */
function showError(message) {
    if (loginError) {
        loginError.textContent = message;
        loginError.classList.remove('hidden');
    }
}

/**
 * Hide error message
 */
function hideError() {
    if (loginError) {
        loginError.classList.add('hidden');
        loginError.textContent = '';
    }
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

export { init };