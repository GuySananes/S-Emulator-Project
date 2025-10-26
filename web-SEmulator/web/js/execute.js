
console.log('=== EXECUTE.JS FILE LOADING ===');

import { api, contextPath } from './api.js';
import { state } from './state.js';
import { showToast, createTable, updateCreditsDisplay, setButtonLoading } from './ui.js';

console.log('=== IMPORTS COMPLETED ===');

let pollInterval = null;
let currentStep = 0;
let collapseLevel = 0;
let highlightEnabled = false;
let selectedProgramName = null;
let availableFunctions = [];

console.log('=== ADDING DOM LOADED LISTENER ===');

document.addEventListener('DOMContentLoaded', async () => {
    console.log('=== DOM CONTENT LOADED EVENT FIRED ===');

    try {
        console.log('Starting checkAuth...');
        await checkAuth();
        console.log('checkAuth completed');

        console.log('Starting initializePage...');
        await initializePage();
        console.log('initializePage completed');

        console.log('Starting setupControls...');
        setupControls();
        console.log('setupControls completed');
    } catch (error) {
        console.error('=== ERROR IN DOM LOADED ===', error);
    }
});

async function checkAuth() {
    console.log('=== CHECK AUTH START ===');
    try {
        console.log('Calling api.getSession()...');
        const session = await api.getSession();
        console.log('Session response:', session);

        if (!session || !session.username) {
            console.log('No session found, redirecting to index.html');
            window.location.href = contextPath + '/index.html';
            return;
        }
        console.log('Session valid, username:', session.username);
        state.setUser(session.username, session.credits);
        updateCreditsDisplay(session.credits);
    } catch (error) {
        console.error('Auth error:', error);
        console.log('Error occurred, redirecting to index.html');
        window.location.href = contextPath + '/index.html';
    }
    console.log('=== CHECK AUTH END ===');
}

async function initializePage() {
    const params = new URLSearchParams(window.location.search);
    const programId = params.get('programId');  // This is actually the program name
    const functionId = params.get('functionId');

    console.log('Initializing execution page with programId:', programId, 'functionId:', functionId);

    if (!programId) {
        showToast('error', 'No program selected');
        setTimeout(() => window.location.href = contextPath + '/dashboard.html', 2000);
        return;
    }

    // Load all programs first
    await loadProgramsDropdown();

    // Select the program from URL
    if (programId) {
        await selectProgram(programId);
    }

    // If function was specified, select it
    if (functionId && availableFunctions.length > 0) {
        const functionSelector = document.getElementById('functionSelector');
        functionSelector.value = functionId;
        await selectFunction(functionId);
    }
}

async function loadProgramsDropdown() {
    const programSelector = document.getElementById('programSelector');

    console.log('=== LOAD PROGRAMS DROPDOWN START ===');

    try {
        const response = await api.getPrograms();
        console.log('Programs API response:', response);
        console.log('Programs array:', response.programs);
        console.log('Programs length:', response.programs ? response.programs.length : 0);

        programSelector.innerHTML = '<option value="">Select program...</option>';

        if (response.programs && response.programs.length > 0) {
            response.programs.forEach(program => {
                console.log('Adding program to dropdown:', program.name);
                const option = document.createElement('option');
                option.value = program.name;
                option.textContent = program.name;
                programSelector.appendChild(option);
            });
            console.log('Dropdown now has', programSelector.options.length, 'options');
        } else {
            console.warn('No programs found in response');
        }
    } catch (error) {
        console.error('Failed to load programs:', error);
        showToast('error', 'Failed to load programs');
    }

    console.log('=== LOAD PROGRAMS DROPDOWN END ===');
}

async function selectProgram(programName) {
    console.log('=== SELECT PROGRAM START ===');
    console.log('Program name:', programName);

    selectedProgramName = programName;

    const programSelector = document.getElementById('programSelector');
    programSelector.value = programName;
    console.log('Dropdown value set to:', programSelector.value);

    try {
        console.log('Calling /api/execution/select-program with:', programName);

        // Call the backend to load the program (like Engine.loadProgram)
        const response = await fetch(`${contextPath}/api/execution/select-program`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ programName })
        });

        console.log('Response status:', response.status);
        console.log('Response ok:', response.ok);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Failed to select program: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        console.log('Program selected successfully!');
        console.log('Full response data:', data);
        console.log('Context programs:', data.contextPrograms);
        console.log('Instructions count:', data.instructions ? data.instructions.length : 0);
        console.log('Variables count:', data.variables ? data.variables.length : 0);

        // Load the context programs (functions) into the function selector
        await loadFunctionsDropdown(data.contextPrograms || []);

        // Display instructions and variables
        displayInstructions(data.instructions || []);
        displayVariables(data.variables || []);

        showToast('success', `Loaded program: ${programName}`);

    } catch (error) {
        console.error('=== SELECT PROGRAM ERROR ===');
        console.error('Error:', error);
        console.error('Stack:', error.stack);
        showToast('error', `Failed to load program: ${error.message}`);
    }

    console.log('=== SELECT PROGRAM END ===');
}

async function loadFunctionsDropdown(contextPrograms) {
    const functionSelector = document.getElementById('functionSelector');

    functionSelector.innerHTML = '<option value="">Select function...</option>';
    availableFunctions = contextPrograms;

    if (contextPrograms && contextPrograms.length > 0) {
        contextPrograms.forEach(funcName => {
            const option = document.createElement('option');
            option.value = funcName;
            option.textContent = funcName;
            functionSelector.appendChild(option);
        });
        functionSelector.disabled = false;
    } else {
        functionSelector.disabled = true;
    }
}

async function selectFunction(functionName) {
    console.log('Selecting function:', functionName);

    try {
        // Call engine.chooseContextProgram (like JavaFX does)
        const response = await fetch(`${contextPath}/api/execution/choose-function`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ programName: selectedProgramName, functionName })
        });

        if (!response.ok) {
            throw new Error('Failed to select function');
        }

        const data = await response.json();
        console.log('Function selected:', data);

        // Display instructions and variables for the selected function
        displayInstructions(data.instructions || []);
        displayVariables(data.variables || []);

        showToast('success', `Loaded function: ${functionName}`);

    } catch (error) {
        console.error('Failed to select function:', error);
        showToast('error', 'Failed to load function');
    }
}

function displayInstructions(instructions) {
    console.log('=== DISPLAY INSTRUCTIONS ===');
    console.log('Instructions to display:', instructions.length);

    const container = document.getElementById('instructionsTableContainer');

    if (instructions.length === 0) {
        console.log('No instructions to display');
        container.innerHTML = '<div class="empty-state">No instructions</div>';
        return;
    }

    console.log('First instruction:', instructions[0]);

    const headers = ['#', 'B/S', 'Cycles', 'Instruction'];
    const rows = instructions.map(instr => [
        instr.index,
        instr.type,
        instr.cycles,
        instr.representation
    ]);

    console.log('Creating table with', rows.length, 'rows');
    const table = createTable(headers, rows);
    container.innerHTML = '';
    container.appendChild(table);
    console.log('Table appended to container');
}

function displayVariables(variables) {
    console.log('=== DISPLAY VARIABLES ===');
    console.log('Variables to display:', variables.length);

    const container = document.getElementById('variablesTableContainer');

    if (variables.length === 0) {
        console.log('No variables to display');
        container.innerHTML = '<div class="empty-state">No variables</div>';
        return;
    }

    console.log('First variable:', variables[0]);

    const headers = ['Name', 'Value'];
    const rows = variables.map(v => [v.name, v.value]);

    console.log('Creating table with', rows.length, 'rows');
    const table = createTable(headers, rows);
    container.innerHTML = '';
    container.appendChild(table);
    console.log('Table appended to container');
}

function setupControls() {
    // Program selector
    const programSelector = document.getElementById('programSelector');
    programSelector.addEventListener('change', (e) => {
        if (e.target.value) {
            selectProgram(e.target.value);
        }
    });

    // Function selector
    const functionSelector = document.getElementById('functionSelector');
    functionSelector.addEventListener('change', (e) => {
        if (e.target.value) {
            selectFunction(e.target.value);
        }
    });

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
    window.location.href = contextPath + '/dashboard.html';
}

async function startExecution(mode) {
    console.log('Starting execution in', mode, 'mode');
    showToast('info', 'Execution feature coming soon');
}

async function sendCommand(command) {
    console.log('Sending command:', command);
    showToast('info', 'Command feature coming soon');
}

function applyCollapse() {
    console.log('Collapse level:', collapseLevel);
    // TODO: Implement collapse logic
}

function applyHighlight() {
    console.log('Highlight enabled:', highlightEnabled);
    // TODO: Implement highlight logic
}

window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});