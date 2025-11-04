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
    await loadAllFunctions();  // ADD THIS LINE
    await loadUserStatistics(null);
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
        loadUserStatistics(null);  // ← ADD THIS LINE
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
                    loadUserStatistics(rowData[0]);
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

            // Updated headers with all required columns
            const headers = [
                'Program Name',
                'Uploaded By',
                'Instructions (deg 0)',
                'Max Degree',
                'Times Run',
                'Avg Credit Cost',
                'Action'
            ];

            const rows = response.programs.map(program => {
                console.log('Creating row for program:', program);
                const executeBtn = createButton('Execute', 'primary', (e) => {
                    e.stopPropagation();
                    state.selectProgram(program);
                    window.location.href = contextPath + `/execution.html?programId=${encodeURIComponent(program.name)}`;
                });

                // Format avg credit cost to 2 decimal places
                const avgCreditCost = program.avgCreditCost !== undefined
                    ? program.avgCreditCost.toFixed(2)
                    : '0.00';

                return [
                    program.name,
                    program.owner || 'Unknown',
                    program.instructionCount || 0,
                    program.maxDegree !== undefined ? program.maxDegree : 'N/A',
                    program.executionCount || 0,
                    avgCreditCost,
                    executeBtn
                ];
            });

            const table = createTable(headers, rows);

            // Add custom class for programs table styling
            table.classList.add('programs-table');

            // Add 'never-run' class to rows that haven't been executed
            const tbody = table.querySelector('tbody');
            if (tbody) {
                tbody.querySelectorAll('tr').forEach((tr, idx) => {
                    const program = response.programs[idx];
                    if (program.executionCount === 0) {
                        tr.classList.add('never-run');
                    }
                });
            }

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

async function loadAllFunctions() {
    const container = document.getElementById('functionsTableContainer');

    console.log('Loading all functions...');

    try {
        const response = await api.getAllFunctions();

        console.log('Functions API response:', response);

        if (response.functions && response.functions.length > 0) {
            console.log('Creating table with', response.functions.length, 'functions');

            const headers = [
                'Function Name',
                'Parent Program',
                'Uploaded By',
                'Instructions (deg 0)',
                'Max Degree',
                'Action'
            ];

            const rows = response.functions.map(func => {
                const executeBtn = createButton('Execute', 'primary', (e) => {
                    e.stopPropagation();
                    window.location.href = contextPath +
                        `/execution.html?programId=${encodeURIComponent(func.parentProgramName)}&functionId=${encodeURIComponent(func.functionName)}`;
                });

                return [
                    func.functionName,
                    func.parentProgramName,
                    func.owner || 'Unknown',
                    func.instructionCount || 0,
                    func.maxDegree !== undefined ? func.maxDegree : 'N/A',
                    executeBtn
                ];
            });

            const table = createTable(headers, rows);
            table.classList.add('functions-table');

            container.innerHTML = '';
            container.appendChild(table);
            console.log('Functions table created');
        } else {
            console.log('No functions found');
            container.innerHTML = '<div class="empty-state">No functions available</div>';
        }
    } catch (error) {
        console.error('Failed to fetch functions:', error);
        showToast('error', 'Failed to load functions');
        container.innerHTML = '<div class="empty-state">Failed to load functions</div>';
    }
}

async function loadUserStatistics(username = null) {
    const container = document.getElementById('statisticsTableContainer');
    const table = document.getElementById('statisticsTable');
    const tbody = document.getElementById('statisticsTableBody');
    const emptyState = document.getElementById('statisticsEmptyState');
    const usernameDisplay = document.getElementById('statisticsUsername');

    // Show loading state
    if (table) table.style.display = 'none';
    if (emptyState) {
        emptyState.textContent = 'Loading statistics...';
        emptyState.style.display = 'block';
    }

    try {
        const data = await api.getUserStatistics(username);

        // ADD THIS DEBUG CODE
        console.log('=== User Statistics Response ===');
        console.log('Full data:', data);
        if (data.statistics && data.statistics.length > 0) {
            console.log('First statistic:', data.statistics[0]);
            console.log('Fields:', Object.keys(data.statistics[0]));
        }
        // END DEBUG CODE

        // Update username display
        if (usernameDisplay) {
            usernameDisplay.textContent = username || 'yourself';
        }

        if (!data.statistics || data.statistics.length === 0) {
            // No statistics available
            if (table) table.style.display = 'none';
            if (emptyState) {
                emptyState.textContent = `No execution history for ${username || 'you'}`;
                emptyState.style.display = 'block';
            }
            return;
        }

        // Render statistics table
        renderStatisticsTable(data.statistics);

        // Show table, hide empty state
        if (table) table.style.display = 'table';
        if (emptyState) emptyState.style.display = 'none';

    } catch (error) {
        console.error('Failed to load user statistics:', error);
        showToast('error', 'Failed to load statistics');

        if (table) table.style.display = 'none';
        if (emptyState) {
            emptyState.textContent = 'Failed to load statistics';
            emptyState.style.display = 'block';
        }
    }
}

function renderStatisticsTable(statistics) {
    const tbody = document.getElementById('statisticsTableBody');
    if (!tbody) return;

    tbody.innerHTML = ''; // Clear existing rows

    statistics.forEach(stat => {
        const row = document.createElement('tr');

        // Run number (Run #)
        const runNumCell = document.createElement('td');
        runNumCell.textContent = stat.runNumber;
        row.appendChild(runNumCell);

        // Type (Main Program / Function)
        const typeCell = document.createElement('td');
        const typeBadge = document.createElement('span');
        typeBadge.className = stat.isMainProgram ? 'badge badge-primary' : 'badge badge-secondary';
        typeBadge.textContent = stat.isMainProgram ? 'Main' : 'Function';
        typeCell.appendChild(typeBadge);
        row.appendChild(typeCell);

        // Program/Function name
        const nameCell = document.createElement('td');
        nameCell.textContent = stat.programName;
        row.appendChild(nameCell);

        // Architecture (with styled badge)
        const archCell = document.createElement('td');
        const archBadge = document.createElement('span');
        const archType = stat.architectureType || 'IV'; // Default to IV if not set
        archBadge.className = `badge badge-arch-${archType}`;
        archBadge.textContent = archType;
        archCell.appendChild(archBadge);
        row.appendChild(archCell);

        // Degree (this is the run degree)
        const degreeCell = document.createElement('td');
        degreeCell.textContent = stat.runDegree !== undefined ? stat.runDegree : 0;
        row.appendChild(degreeCell);

        // Final Y value
        const yValueCell = document.createElement('td');
        yValueCell.textContent = stat.finalYValue;
        row.appendChild(yValueCell);

        // Cycles consumed
        const cyclesCell = document.createElement('td');
        cyclesCell.textContent = stat.cyclesConsumed;
        row.appendChild(cyclesCell);

        tbody.appendChild(row);
    });
}

window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});


let programsRefreshInterval = null;

function startProgramsAutoRefresh() {
    // Clear any existing interval
    if (programsRefreshInterval) {
        clearInterval(programsRefreshInterval);
    }

    // Refresh every 5 seconds if the page is visible
    programsRefreshInterval = setInterval(() => {
        if (document.visibilityState === 'visible') {
            loadPrograms();
        }
    }, 5000);

    console.log('Auto-refresh enabled for programs table (every 5 seconds)');
}

function stopProgramsAutoRefresh() {
    if (programsRefreshInterval) {
        clearInterval(programsRefreshInterval);
        programsRefreshInterval = null;
        console.log('Auto-refresh disabled for programs table');
    }
}

let functionsRefreshInterval = null;

function startFunctionsAutoRefresh() {
    if (functionsRefreshInterval) {
        clearInterval(functionsRefreshInterval);
    }

    functionsRefreshInterval = setInterval(() => {
        if (document.visibilityState === 'visible') {
            loadAllFunctions();
        }
    }, 5000);

    console.log('Auto-refresh enabled for functions table (every 5 seconds)');
}

function stopFunctionsAutoRefresh() {
    if (functionsRefreshInterval) {
        clearInterval(functionsRefreshInterval);
        functionsRefreshInterval = null;
        console.log('Auto-refresh disabled for functions table');
    }
}

// Initialize auto-refresh when page loads
document.addEventListener('DOMContentLoaded', () => {
    // Start auto-refresh for programs
    startProgramsAutoRefresh();

    // Start auto-refresh for functions
    startFunctionsAutoRefresh();

    // Stop refresh when user leaves the page
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') {
            stopProgramsAutoRefresh();
            stopFunctionsAutoRefresh();  // ADD THIS
        } else {
            startProgramsAutoRefresh();
            startFunctionsAutoRefresh();  // ADD THIS
        }
    });
});
