// UI helper functions
export function showToast(type, message, duration = 3000) {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div style="font-weight: 600; margin-bottom: 4px;">${type.toUpperCase()}</div>
        <div>${message}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

export function createTable(headers, rows, options = {}) {
    const table = document.createElement('table');
    table.className = 'table';

    // Create header
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // Create body
    const tbody = document.createElement('tbody');
    rows.forEach((row, idx) => {
        const tr = document.createElement('tr');
        if (options.onRowClick) {
            tr.style.cursor = 'pointer';
            tr.addEventListener('click', () => options.onRowClick(row, idx, tr));
        }

        row.forEach(cell => {
            const td = document.createElement('td');
            if (typeof cell === 'string' || typeof cell === 'number') {
                td.textContent = cell;
            } else {
                td.appendChild(cell);
            }
            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });
    table.appendChild(tbody);

    return table;
}

export function createButton(text, variant = 'primary', onClick) {
    const btn = document.createElement('button');
    btn.className = `btn btn-${variant}`;
    btn.textContent = text;
    if (onClick) btn.addEventListener('click', onClick);
    return btn;
}

export function createBadge(text, type = 'credits') {
    const badge = document.createElement('span');
    badge.className = `badge badge-${type}`;
    badge.textContent = text;
    return badge;
}

export function showSpinner(container) {
    container.innerHTML = '<div class="spinner"></div>';
}

export function hideSpinner(container) {
    const spinner = container.querySelector('.spinner');
    if (spinner) spinner.remove();
}

export function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.classList.add('loading');
    } else {
        button.disabled = false;
        button.classList.remove('loading');
    }
}

export function formatDateTime(isoString) {
    if (!isoString) return 'Never';
    const date = new Date(isoString);
    return date.toLocaleString();
}

export function updateCreditsDisplay(credits) {
    const displays = document.querySelectorAll('#creditsDisplay');
    displays.forEach(display => {
        display.textContent = `Available Credits: ${credits}`;
    });
}