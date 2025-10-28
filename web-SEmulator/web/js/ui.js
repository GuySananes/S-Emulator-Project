// ui.js - UI utility functions

/**
 * Show a toast notification
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) {
        console.warn('Toast container not found');
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    // Trigger animation
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    // Remove after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    }, 3000);
}

/**
 * Show a loading spinner
 */
function showLoading(element) {
    if (!element) return;

    const spinner = document.createElement('div');
    spinner.className = 'spinner';
    element.innerHTML = '';
    element.appendChild(spinner);
}

/**
 * Hide loading spinner
 */
function hideLoading(element) {
    if (!element) return;

    const spinner = element.querySelector('.spinner');
    if (spinner) {
        spinner.remove();
    }
}

/**
 * Create an empty state message
 */
function createEmptyState(message) {
    const div = document.createElement('div');
    div.className = 'empty-state';
    div.textContent = message;
    return div;
}

/**
 * Create an error message
 */
function createErrorMessage(message) {
    const div = document.createElement('div');
    div.className = 'error-message';
    div.textContent = message;
    return div;
}

export { showToast, showLoading, hideLoading, createEmptyState, createErrorMessage };