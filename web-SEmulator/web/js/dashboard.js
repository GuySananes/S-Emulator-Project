import { api, contextPath } from './api.js';
import { state } from './state.js';
import { showToast, createTable, createButton, formatDateTime, updateCreditsDisplay, setButtonLoading } from './ui.js';

let pollInterval = null;
let selectedProgramForFunctions = null;

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

        console.log('=== FILE UPLOAD START ===');
        console.log('File selected:', file.name, file.type, file.size);
        setButtonLoading(loadFileBtn, true);

        try {
            const response = await api.loadFile(file);
            console.log('File upload response:', response);
            console.log('Response keys:', Object.keys(response));
            console.log('LoadedFilePath:', response.loadedFilePath);
            console.log('Programs loaded:', response.programsLoaded);

            state.setLoadedFilePath(response.loadedFilePath);
            loadedFilePathInput.value = response.loadedFilePath;
            showToast('success', `File loaded successfully - ${response.programsLoaded || 0} program(s) loaded`);

            console.log('Calling loadPrograms after file upload...');
            await loadPrograms();
            console.log('=== FILE UPLOAD END ===');
        } catch (error) {
            console.error('File load error:', error);
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

    console.log('Loading programs...');

    try {
        const response = await api.getPrograms();

        console.log('Programs API response:', response);
        console.log('Programs array:', response.programs);
        console.log('Programs count:', response.programs ? response.programs.length : 0);

        if (response.programs && response.programs.length > 0) {
            console.log('Creating table with programs:', response.programs);

            const headers = ['Name', 'Owner', 'Functions', 'Action'];
            const rows = response.programs.map(program => {
                console.log('Creating row for program:', program);
                const executeBtn = createButton('Execute', 'primary', (e) => {
                    e.stopPropagation();
                    state.selectProgram(program);
                    window.location.href = contextPath + `/execution.html?programId=${encodeURIComponent(program.name)}`;
                });
                return [
                    program.name,
                    program.owner || 'Unknown',
                    program.functions || 0,
                    executeBtn
                ];
            });

            const table = createTable(headers, rows, {
                onRowClick: (rowData, idx) => {
                    const program = response.programs[idx];
                    selectedProgramForFunctions = program;
                    loadFunctionsForProgram(program.name);
                }
            });
            container.innerHTML = '';
            container.appendChild(table);
            console.log('Table created and appended');
        } else {
            console.log('No programs found, showing empty state');
            container.innerHTML = '<div class="empty-state">No programs loaded</div>';
        }

        if (response.loadedFilePath) {
            state.setLoadedFilePath(response.loadedFilePath);
            document.getElementById('loadedFilePath').value = response.loadedFilePath;
            console.log('Loaded file path:', response.loadedFilePath);
        }
    } catch (error) {
        console.error('Failed to fetch programs:', error);
        console.error('Error details:', error.message, error.stack);
        showToast('error', 'Failed to load programs');
        container.innerHTML = '<div class="empty-state">Failed to load programs</div>';
    }
}

async function loadFunctionsForProgram(programName) {
    const container = document.getElementById('functionsListContainer');

    try {
        const response = await api.getFunctions(programName);

        if (response.functions && response.functions.length > 0) {
            const headers = ['Function Name', 'Action'];
            const rows = response.functions.map(func => {
                const executeBtn = createButton('Execute', 'primary', (e) => {
                    e.stopPropagation();
                    window.location.href = contextPath + `/execution.html?programId=${encodeURIComponent(programName)}&functionId=${encodeURIComponent(func.name)}`;
                });
                return [
                    func.name,
                    executeBtn
                ];
            });

            const table = createTable(headers, rows);
            container.innerHTML = '';
            container.appendChild(table);
        } else {
            container.innerHTML = '<div class="empty-state">No functions available</div>';
        }
    } catch (error) {
        console.error('Failed to fetch functions:', error);
        container.innerHTML = '<div class="empty-state">Failed to load functions</div>';
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