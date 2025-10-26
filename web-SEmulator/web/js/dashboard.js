import { api, contextPath } from './api.js';
import { state } from './state.js';
import { showToast, createTable, createButton, formatDateTime, updateCreditsDisplay, setButtonLoading } from './ui.js';

let pollInterval = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    setupControls();
    startPolling();
    await loadPrograms();
    await loadHistory();
});

async function checkAuth() {
    try {
        const session = await api.getSession();
        if (!session || !session.username) {
            window.location.href = contextPath + '/index.html';
            return;
        }
        state.setUser(session.username, session.credits);
        updateUserDisplay();
    } catch (error) {
        window.location.href = contextPath + '/index.html';
    }
}

function updateUserDisplay() {
    document.getElementById('currentUser').textContent = state.username;
    updateCreditsDisplay(state.credits);
}

function setupControls() {
    // File loading
    const loadFileBtn = document.getElementById('loadFileBtn');
    const fileInput = document.getElementById('fileInput');
    const loadedFilePathInput = document.getElementById('loadedFilePath');

    loadFileBtn.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setButtonLoading(loadFileBtn, true);

        try {
            const response = await api.loadFile(file);
            state.setLoadedFilePath(response.loadedFilePath);
            loadedFilePathInput.value = response.loadedFilePath;
            showToast('success', 'File loaded successfully');
            await loadPrograms();
        } catch (error) {
            showToast('error', `File load failed: ${error.message}`);
        } finally {
            setButtonLoading(loadFileBtn, false);
            fileInput.value = '';
        }
    });

    // Charge credits
    const chargeCreditsBtn = document.getElementById('chargeCreditsBtn');
    chargeCreditsBtn.addEventListener('click', async () => {
        const amount = prompt('Enter amount to charge:');
        if (!amount) return;

        const amountNum = parseInt(amount);
        if (isNaN(amountNum) || amountNum <= 0) {
            showToast('error', 'Invalid amount');
            return;
        }

        setButtonLoading(chargeCreditsBtn, true);

        try {
            const response = await api.chargeCredits(amountNum);
            state.updateCredits(response.credits);
            updateCreditsDisplay(response.credits);
            showToast('success', `Charged ${amountNum} credits`);
        } catch (error) {
            showToast('error', `Credit charge failed: ${error.message}`);
        } finally {
            setButtonLoading(chargeCreditsBtn, false);
        }
    });

    // Unselect user
    const unselectUserBtn = document.getElementById('unselectUserBtn');
    unselectUserBtn.addEventListener('click', () => {
        state.unselectUser();
        unselectUserBtn.disabled = true;
        updateUsersTable();
    });

    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    logoutBtn.addEventListener('click', async () => {
        try {
            await api.logout();
        } catch (error) {
            console.error('Logout error:', error);
        }
        state.reset();
        window.location.href = contextPath + '/index.html';
    });

}

function startPolling() {
    updateLiveUsers();
    updateCredits();
    pollInterval = setInterval(() => {
        updateLiveUsers();
        updateCredits();
    }, 3000);
}

async function updateLiveUsers() {
    const container = document.getElementById('usersTableContainer');

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

            const table = createTable(headers, rows, {
                onRowClick: (rowData) => {
                    state.selectUser(rowData[0]);
                    document.getElementById('unselectUserBtn').disabled = false;
                    updateUsersTable();
                }
            });

            container.innerHTML = '';
            container.appendChild(table);

            // Highlight selected user
            if (state.selectedUser) {
                const rows = table.querySelectorAll('tbody tr');
                rows.forEach((row, idx) => {
                    if (response.users[idx].name === state.selectedUser) {
                        row.classList.add('selected');
                    }
                });
            }
        } else {
            container.innerHTML = '<div class="empty-state">No users available</div>';
        }
    } catch (error) {
        console.error('Failed to fetch live users:', error);
    }
}

function updateUsersTable() {
    updateLiveUsers();
}

async function updateCredits() {
    try {
        const session = await api.getSession();
        if (session && session.credits !== undefined) {
            state.updateCredits(session.credits);
            updateCreditsDisplay(session.credits);
        }
    } catch (error) {
        console.error('Failed to fetch credits:', error);
    }
}

async function loadPrograms() {
    const container = document.getElementById('programsTableContainer');

    try {
        const response = await api.getPrograms();

        if (response.programs && response.programs.length > 0) {
            const headers = ['Name', 'Functions', 'Action'];
            const rows = response.programs.map(program => {
                const executeBtn = createButton('Execute Program', 'primary', () => {
                    state.selectProgram(program);
                    window.location.href = contextPath + `/execution.html?programId=${program.id}`;
                });
                return [
                    program.name,
                    program.functions || 0,
                    executeBtn
                ];
            });

            const table = createTable(headers, rows);
            container.innerHTML = '';
            container.appendChild(table);
        } else {
            container.innerHTML = '<div class="empty-state">No programs loaded</div>';
        }

        if (response.loadedFilePath) {
            state.setLoadedFilePath(response.loadedFilePath);
            document.getElementById('loadedFilePath').value = response.loadedFilePath;
        }
    } catch (error) {
        console.error('Failed to fetch programs:', error);
        container.innerHTML = '<div class="empty-state">Failed to load programs</div>';
    }
}

async function loadHistory() {
    const container = document.getElementById('historyTableContainer');

    try {
        const response = await api.getStatisticsHistory();

        if (response.rows && response.rows.length > 0) {
            const headers = ['Time', 'User', 'Program', 'Runs', 'Used'];
            const rows = response.rows.map(row => [
                formatDateTime(row.time),
                row.user,
                row.program,
                row.runs || 0,
                row.used || 0
            ]);

            const table = createTable(headers, rows);
            container.innerHTML = '';
            container.appendChild(table);
        } else {
            container.innerHTML = '<div class="empty-state">No history available</div>';
        }
    } catch (error) {
        console.error('Failed to fetch history:', error);
    }
}

window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});