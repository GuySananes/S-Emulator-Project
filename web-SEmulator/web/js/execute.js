import { api } from './api.js';
import { state } from './state.js';
import { showToast, createTable, updateCreditsDisplay, setButtonLoading } from './ui.js';

let pollInterval = null;
let currentStep = 0;
let collapseLevel = 0;
let highlightEnabled = false;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    initializePage();
    setupControls();
});

async function checkAuth() {
    try {
        const session = await api.getSession();
        if (!session || !session.username) {
            window.location.href = '/index.html';
            return;
        }
        state.setUser(session.username, session.credits);
        updateCreditsDisplay(session.credits);
    } catch (error) {
        window.location.href = '/index.html';
    }
}

function initializePage() {
    // Get programId from query
    const params = new URLSearchParams(window.location.search);
    const programId = params.get('programId');

    if (!programId) {
        showToast('error', 'No program selected');
        setTimeout(() => window.location.href = '/dashboard.html', 2000);
        return;
    }

    state.selectProgram({ id: programId });
}

function setupControls() {
    // Back buttons
    document.getElementById('backToDashboardBtn').addEventListener('click', goToDashboard);
    document.getElementById('backToDashboard2Btn').addEventListener('click', goToDashboard);

    // Execution controls
    document.getElementById('startRegularBtn').addEventListener('click', () => startExecution('regular'));
    document.getElementById('startDebugBtn').addEventListener('click', () => startExecution('debug'));
    document.getElementById('stopBtn').addEventListener('click', () => sendCommand('stop'));
    document.getElementById('resumeBtn').addEventListener('click', () => sendCommand('resume'));
    document.getElementById('stepForwardBtn').addEventListener('click', () => sendCommand('stepForward'));
    document.getElementById('stepBackwardBtn').addEventListener('click', () => sendCommand('stepBack'));

    // View controls
    document.getElementById('collapseBtn').addEventListener('click', () => {
        collapseLevel++;
        applyCollapse();
    });

    document.getElementById('expandBtn').addEventListener('click', () => {
        if (collapseLevel > 0) collapseLevel--;
        applyCollapse();
    });

    document.getElementById('highlightToggle').addEventListener('change', (e) => {
        highlightEnabled = e.target.checked;
        applyHighlight();
    });
}

function goToDashboard() {
    if (pollInterval) clearInterval(pollInterval);
    window.location.href = '/dashboard.html';
}

async function startExecution(mode) {
    const programId = state.selectedProgram?.id;
    if (!programId) {
        showToast('error', 'No program selected');
        return;
    }

    // Collect inputs from form
    const inputsForm = document.getElementById('inputsForm');
    const inputs = {};
    const formInputs = inputsForm.querySelectorAll('input, select');
    formInputs.forEach(input => {
        if (input.name) {
            inputs[input.name] = input.value;
        }
    });

    const btn = mode === 'regular' ? document.getElementById('startRegularBtn') : document.getElementById('startDebugBtn');
    setButtonLoading(btn, true);

    try {
        const response = await api.startExecution(programId, mode, inputs);
        state.setRunId(response.runId);
        state.updateExecutionStatus('running');

        showToast('success', `Execution started in ${mode} mode`);
        updateExecutionButtons('running');

        // Update summary
        if (response.summary) {
            document.getElementById('summaryText').textContent =
                `Cycles: ${response.summary.cycles}, B/S: ${response.summary.bs}`;
        }

        // Start polling
        startExecutionPoll();
    } catch (error) {
        showToast('error', `Failed to start execution: ${error.message}`);
    } finally {
        setButtonLoading(btn, false);
    }
}

async function sendCommand(cmd) {
    if (!state.runId) return;

    try {
        const response = await api.sendExecutionCommand(state.runId, cmd);
        state.updateExecutionStatus(response.status);
        updateExecutionButtons(response.status);

        if (cmd === 'stop') {
            if (pollInterval) clearInterval(pollInterval);
            showToast('info', 'Execution stopped');
        }
    } catch (error) {
        showToast('error', `Command failed: ${error.message}`);
    }
}

function updateExecutionButtons(status) {
    const statusBadge = document.getElementById('executionStatus');
    statusBadge.textContent = status;
    statusBadge.className = `badge badge-${status}`;

    const isRunning = status === 'running';
    const isPaused = status === 'paused';
    const isDone = status === 'done';

    document.getElementById('startRegularBtn').disabled = isRunning || isPaused || isDone;
    document.getElementById('startDebugBtn').disabled = isRunning || isPaused || isDone;
    document.getElementById('stopBtn').disabled = !isRunning && !isPaused;
    document.getElementById('resumeBtn').disabled = !isPaused;
    document.getElementById('stepForwardBtn').disabled = !isPaused;
    document.getElementById('stepBackwardBtn').disabled = !isPaused;
}

function startExecutionPoll() {
    if (pollInterval) clearInterval(pollInterval);

    pollInterval = setInterval(async () => {
        await updateExecutionStatus();
        await updateVariables();
        await updateInstructions();
    }, 1000);
}

async function updateExecutionStatus() {
    if (!state.runId) return;

    try {
        const response = await api.getExecutionStatus(state.runId);
        state.updateExecutionStatus(response.status);
        updateExecutionButtons(response.status);

        document.getElementById('executionStep').textContent = response.step || 0;
        document.getElementById('cyclesValue').textContent = response.cycles || 0;

        if (response.step !== currentStep) {
            currentStep = response.step;
            await updateInstructions();
        }

        if (response.status === 'done') {
            if (pollInterval) clearInterval(pollInterval);
            showToast('success', 'Execution completed');
        }
    } catch (error) {
        console.error('Failed to fetch execution status:', error);
    }
}

async function updateVariables() {
    if (!state.runId) return;

    const container = document.getElementById('variablesTableContainer');

    try {
        const response = await api.getVariables(state.runId);

        if (response.variables && response.variables.length > 0) {
            const headers = ['Name', 'Value'];
            const rows = response.variables.map(v => [v.name, v.value]);
            const table = createTable(headers, rows);
            container.innerHTML = '';
            container.appendChild(table);
        } else {
            container.innerHTML = '<div class="empty-state">No variables</div>';
        }
    } catch (error) {
        console.error('Failed to fetch variables:', error);
    }
}

async function updateInstructions() {
    if (!state.runId) return;

    const container = document.getElementById('instructionsTableContainer');

    try {
        const response = await api.getInstructions(state.runId);

        if (response.rows && response.rows.length > 0) {
            const headers = ['#', 'B\\S', 'Cycles', 'Instruction'];
            const rows = response.rows.map(instr => [
                instr.idx,
                instr.bs,
                instr.cycles,
                instr.text
            ]);

            const table = createTable(headers, rows, {
                onRowClick: async (rowData, idx) => {
                    await loadInstructionHistory(rowData[0]);
                }
            });

            container.innerHTML = '';
            container.appendChild(table);

            // Update summary
            if (response.summary) {
                document.getElementById('summaryText').textContent =
                    `Total: ${response.summary.cycles || 0} cycles, ${response.summary.bs || 0} B/S`;
            }
        }
    } catch (error) {
        console.error('Failed to fetch instructions:', error);
    }
}

async function loadInstructionHistory(idx) {
    if (!state.runId) return;

    const container = document.getElementById('historyChainContainer');

    try {
        const response = await api.getHistory(state.runId, idx);

        if (response.chain && response.chain.length > 0) {
            container.innerHTML = '';
            response.chain.forEach(item => {
                const card = document.createElement('div');
                card.className = 'chain-card';
                card.innerHTML = `
                    <span class="degree-badge">Degree: ${item.degree}</span>
                    <div>${item.text}</div>
                    <small>ID: ${item.id}</small>
                `;
                container.appendChild(card);
            });
        } else {
            container.innerHTML = '<div class="empty-state">No history available</div>';
        }
    } catch (error) {
        console.error('Failed to fetch instruction history:', error);
        showToast('error', 'Failed to load history');
    }
}

function applyCollapse() {
    const table = document.querySelector('#instructionsTableContainer table');
    if (!table) return;

    const rows = table.querySelectorAll('tbody tr');
    rows.forEach(row => {
        // Logic to collapse based on degree would go here
        // For now, just a placeholder
    });
}

function applyHighlight() {
    const table = document.querySelector('#instructionsTableContainer table');
    if (!table) return;

    const rows = table.querySelectorAll('tbody tr');
    rows.forEach(row => {
        if (highlightEnabled) {
            // Apply highlight logic based on BS or variables
            // Placeholder implementation
            const bsCell = row.cells[1];
            if (bsCell && bsCell.textContent === 'B') {
                row.classList.add('highlighted');
            }
        } else {
            row.classList.remove('highlighted');
        }
    });
}

window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});