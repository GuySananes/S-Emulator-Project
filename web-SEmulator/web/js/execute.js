console.log('=== EXECUTE.JS FILE LOADING ===');

import { api, contextPath } from './api.js';
import { state } from './state.js';
import { showToast, createTable, updateCreditsDisplay } from './ui.js';
import { expandProgram } from './api.js';

console.log('=== IMPORTS COMPLETED ===');

let pollInterval = null;
let currentStep = 0;
let highlightEnabled = false;
let selectedProgramName = null;
let selectedFunctionName = null;
let availableFunctions = [];
let currentExecutionId = null;
let executionMode = null; // 'regular' or 'debug'
let currentInputVariables = []; // ADD THIS LINE
let highlightedVariable = null; // ADD THIS LINE for variable highlighting

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
    const programId = params.get('programId');
    const functionId = params.get('functionId');

    console.log('Initializing execution page with programId:', programId, 'functionId:', functionId);

    if (!programId) {
        showToast('error', 'No program selected');
        setTimeout(() => window.location.href = contextPath + '/dashboard.html', 2000);
        return;
    }

    await loadProgramsDropdown();

    if (programId) {
        await selectProgram(programId);
    }

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
        console.log('Calling /api/execute/select-program with:', programName);

        const response = await fetch(`${contextPath}/api/execute/select-program`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ programName })
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Failed to select program: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        console.log('Program selected successfully!');
        console.log('Full response data:', data);

        // Store required input variables
        currentInputVariables = data.inputVariables || [];
        console.log('Required inputs:', currentInputVariables);

        // Initialize degree state (convert from backend 1-based to display 0-based)
        const displayCurrentDegree = (data.currentDegree || 1) - 1;  // Backend sends 1-based
        const displayMaxDegree = (data.maxDegree || 1) - 1;  // Backend sends 1-based

        state.setDegreeInfo(displayCurrentDegree, 0, displayMaxDegree);
        state.minDegree = 1;
        state.selectedProgram = programName;

        // Update degree display
        updateDegreeDisplay(displayCurrentDegree, displayMaxDegree);

// Update button states
        updateExpandCollapseButtons(displayCurrentDegree, 0, displayMaxDegree);

        // Display the input form WITH estimated cycles
        displayInputsForm(currentInputVariables, data.estimatedCycles);

        await loadFunctionsDropdown(data.contextPrograms || []);
        displayInstructions(data.instructions || []);
        displayVariables(data.variables || []);

        showToast('success', `Loaded program: ${programName}`);

    } catch (error) {
        console.error('=== SELECT PROGRAM ERROR ===', error);
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

function displayInputsForm(inputVariables, estimatedCycles) {
    const container = document.getElementById('inputsForm');

    if (!inputVariables || inputVariables.length === 0) {
        container.innerHTML = '<div class="empty-state">No inputs required</div>';
        return;
    }

    // Determine if user has enough credits
    const hasEnoughCredits = estimatedCycles ? state.credits >= estimatedCycles : true;
    const warningColor = hasEnoughCredits ? '#2196F3' : '#f44336';
    const warningText = hasEnoughCredits
        ? `✓ You have enough credits (${state.credits} available)`
        : `⚠ Insufficient credits! Need ${estimatedCycles}, have ${state.credits}`;

    container.innerHTML = `
        <div style="padding: 16px; background: #f0f7ff; border-radius: 8px; border: 2px solid ${warningColor};">
            <div style="font-weight: 600; color: #1976D2; margin-bottom: 8px;">
                📝 Required Inputs: ${inputVariables.length}
            </div>
            <div style="color: #555; font-size: 14px;">
                ${inputVariables.map(v => `<span style="display: inline-block; padding: 4px 8px; background: white; border-radius: 4px; margin: 2px; font-family: monospace;">${v}</span>`).join('')}
            </div>
            ${estimatedCycles ? `
                <div style="margin-top: 12px; padding: 8px; background: white; border-radius: 4px; border: 1px solid #ddd;">
                    <strong>Estimated Credits Required:</strong> ${estimatedCycles}
                </div>
                <div style="margin-top: 8px; font-size: 13px; color: ${hasEnoughCredits ? '#2e7d32' : '#c62828'}; font-weight: 600;">
                    ${warningText}
                </div>
            ` : ''}
            <div style="margin-top: 12px; font-size: 13px; color: #666; font-style: italic;">
                Click "Start Regular" or "Start Debug" to enter values
            </div>
        </div>
    `;
}

async function selectFunction(functionName) {
    console.log('Selecting function:', functionName);

    // ADD THIS LINE - Track the selected function
    selectedFunctionName = functionName;

    try {
        const response = await fetch(`${contextPath}/api/execute/choose-function`, {
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

        //Store and display input variables for the function
        currentInputVariables = data.inputVariables || [];
        displayInputsForm(currentInputVariables);

        // Update degree state for the function (convert from backend 1-based to display 0-based)
        const displayCurrentDegree = (data.currentDegree || 1) - 1;
        const displayMaxDegree = (data.maxDegree || 1) - 1;

        state.setDegreeInfo(displayCurrentDegree, 0, displayMaxDegree);

        // Update UI
        updateDegreeDisplay(displayCurrentDegree, displayMaxDegree);
        updateExpandCollapseButtons(displayCurrentDegree, 0, displayMaxDegree);
        state.minDegree = 1;

        displayInstructions(data.instructions || []);
        displayVariables(data.variables || []);

        showToast('success', `Loaded function: ${functionName}`);

    } catch (error) {
        console.error('Failed to select function:', error);
        showToast('error', 'Failed to load function');
    }
}

/**
 * Handle expand button click - increases degree by 1
 */
async function handleExpand() {
    console.log('=== HANDLE EXPAND ===');
    console.log('Current state:', {
        currentDegree: state.currentDegree,
        minDegree: state.minDegree,
        maxDegree: state.maxDegree
    });

    // Calculate new DISPLAY degree (0-based)
    const newDisplayDegree = state.currentDegree + 1;

    // Validate against max
    if (newDisplayDegree > state.maxDegree) {
        showToast('warning', `Already at maximum degree (${state.maxDegree})`);
        return;
    }

    // Convert to BACKEND degree (1-based): backend = display + 1
    const backendDegree = newDisplayDegree + 1;

    console.log(`Expanding: displayDegree ${state.currentDegree} -> ${newDisplayDegree}, calling backend with degree ${backendDegree}`);

    try {
        const response = await fetch(`${contextPath}/api/execute/expand`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ degree: backendDegree })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Expansion failed');
        }

        const data = await response.json();
        console.log('Expand response:', data);

        if (data.success) {
            // Backend returns 1-based degree, convert to 0-based for display
            const displayDegree = data.currentDegree - 1;

            // Update state
            state.setDegreeInfo(displayDegree, 0, data.maxDegree - 1);

            // Update UI
            updateDegreeDisplay(displayDegree, data.maxDegree - 1);
            updateExpandCollapseButtons(displayDegree, 0, data.maxDegree - 1);
            displayInstructions(data.instructions || []);
            displayVariables(data.variables || []);

            showToast('success', `Expanded to degree ${displayDegree}`);
        } else {
            showToast('error', data.error || 'Expansion failed');
        }

    } catch (error) {
        console.error('Expand error:', error);
        showToast('error', `Failed to expand: ${error.message}`);
    }
}

/**
 * Handle collapse button click - decreases degree by 1
 */
async function handleCollapse() {
    console.log('=== HANDLE COLLAPSE ===');
    console.log('Current state:', {
        currentDegree: state.currentDegree,
        minDegree: state.minDegree,
        maxDegree: state.maxDegree
    });

    // Calculate new DISPLAY degree (0-based)
    const newDisplayDegree = state.currentDegree - 1;

    // Validate against min (which is 0 for display)
    if (newDisplayDegree < 0) {
        showToast('warning', 'Already at minimum degree (0)');
        return;
    }

    // Convert to BACKEND degree (1-based): backend = display + 1
    const backendDegree = newDisplayDegree + 1;

    console.log(`Collapsing: displayDegree ${state.currentDegree} -> ${newDisplayDegree}, calling backend with degree ${backendDegree}`);

    try {
        const response = await fetch(`${contextPath}/api/execute/expand`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ degree: backendDegree })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Collapse failed');
        }

        const data = await response.json();
        console.log('Collapse response:', data);

        if (data.success) {
            // Backend returns 1-based degree, convert to 0-based for display
            const displayDegree = data.currentDegree - 1;

            // Update state
            state.setDegreeInfo(displayDegree, 0, data.maxDegree - 1);

            // Update UI
            updateDegreeDisplay(displayDegree, data.maxDegree - 1);
            updateExpandCollapseButtons(displayDegree, 0, data.maxDegree - 1);
            displayInstructions(data.instructions || []);
            displayVariables(data.variables || []);

            showToast('success', `Collapsed to degree ${displayDegree}`);
        } else {
            showToast('error', data.error || 'Collapse failed');
        }

    } catch (error) {
        console.error('Collapse error:', error);
        showToast('error', `Failed to collapse: ${error.message}`);
    }
}


function displayInstructions(instructions) {
    const container = document.getElementById('instructionsTableContainer');

    if (!instructions || instructions.length === 0) {
        container.innerHTML = '<div class="empty-state">No instructions to display</div>';
        return;
    }

    // Create table structure
    const table = document.createElement('table');
    table.className = 'table';

    const thead = document.createElement('thead');
    thead.innerHTML = `
        <tr>
            <th style="width: 60px;">#</th>
            <th style="width: 250px;">Instruction</th>
            <th style="width: 100px;">Type</th>
            <th>Cycles</th>
        </tr>
    `;
    table.appendChild(thead);

    const tbody = document.createElement('tbody');

    instructions.forEach((inst, idx) => {
        const row = document.createElement('tr');
        row.className = 'instruction-row';

        // Store instruction data for highlighting (create a simple object with the instruction text)
        row._instructionData = {
            instruction: inst.representation || '',
            type: inst.type || 'B',
            cycles: inst.cycles || '1',
            index: inst.index || (idx + 1)
        };

        // Apply variable highlighting if needed
        if (shouldHighlightInstruction(row._instructionData)) {
            row.classList.add('variable-highlighted');
        }

        row.innerHTML = `
            <td>${inst.index || (idx + 1)}</td>
            <td class="instruction-text">${inst.representation || ''}</td>
            <td>${inst.type || 'B'}</td>
            <td>${inst.cycles || '1'}</td>
        `;

        // Add click handler for history (if you have history functionality)
        row.addEventListener('click', () => {
            if (highlightEnabled) {
                highlightRow(row);
            }
            // If you have displayHistoryChain function, uncomment this:
            // displayHistoryChain(inst.historyChain || []);
        });

        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    container.innerHTML = '';
    container.appendChild(table);

    updateSummary(instructions.length);
}

function highlightRow(row) {
    // Remove previous highlight from other rows
    const allRows = document.querySelectorAll('.instruction-row');
    allRows.forEach(r => r.classList.remove('debug-highlighted'));

    // Add highlight to clicked row
    row.classList.add('debug-highlighted');
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
    const programSelector = document.getElementById('programSelector');
    programSelector.addEventListener('change', (e) => {
        if (e.target.value) {
            selectProgram(e.target.value);
        }
    });

    const functionSelector = document.getElementById('functionSelector');
    functionSelector.addEventListener('change', (e) => {
        if (e.target.value) {
            selectFunction(e.target.value);
        }
    });

    document.getElementById('backToDashboardBtn').addEventListener('click', goToDashboard);
    document.getElementById('backToDashboard2Btn').addEventListener('click', goToDashboard);

    document.getElementById('startRegularBtn').addEventListener('click', () => startExecution('regular'));
    document.getElementById('startDebugBtn').addEventListener('click', () => startExecution('debug'));
    document.getElementById('stopBtn').addEventListener('click', () => sendCommand('stop'));
    document.getElementById('resumeBtn').addEventListener('click', () => sendCommand('resume'));
    document.getElementById('stepForwardBtn').addEventListener('click', () => sendCommand('stepForward'));
    document.getElementById('stepBackwardBtn').addEventListener('click', () => sendCommand('stepBack'));

    document.getElementById('collapseBtn').addEventListener('click', handleCollapse);
    document.getElementById('expandBtn').addEventListener('click', handleExpand);

    document.getElementById('highlightToggle').addEventListener('change', (e) => {
        highlightEnabled = e.target.checked;
        applyHighlight();
    });

    // Variable highlighting controls - ADD THIS SECTION
    const highlightVariableBtn = document.getElementById('highlightVariableBtn');
    const clearHighlightBtn = document.getElementById('clearHighlightBtn');
    const variableHighlightInput = document.getElementById('variableHighlightInput');

    if (highlightVariableBtn && clearHighlightBtn && variableHighlightInput) {
        highlightVariableBtn.addEventListener('click', () => {
            const variableName = variableHighlightInput.value.trim();
            if (variableName) {
                highlightVariable(variableName);
            } else {
                showToast('warning', 'Please enter a variable name');
            }
        });

        clearHighlightBtn.addEventListener('click', () => {
            clearVariableHighlight();
        });

        // Allow highlighting with Enter key
        variableHighlightInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                highlightVariableBtn.click();
            }
        });
    }
}

function goToDashboard() {
    if (pollInterval) clearInterval(pollInterval);
    window.location.href = contextPath + '/dashboard.html';
}


async function startExecution(mode) {
    console.log('=== START EXECUTION CALLED - MODE:', mode, '===');
    console.log('Starting execution in', mode, 'mode');

    if (!selectedProgramName) {
        showToast('error', 'No program selected');
        return;
    }

    if (state.credits <= 0) {
        showToast('error', 'Insufficient credits to run program');
        return;
    }

    executionMode = mode;

    // Show input dialog if inputs are required
    let inputs = {};

    if (currentInputVariables && currentInputVariables.length > 0) {
        const inputValues = await showInputDialog(currentInputVariables);

        if (inputValues === null) {
            showToast('info', 'Execution cancelled');
            return;
        }

        // Convert array to map with variable names
        currentInputVariables.forEach((varName, index) => {
            inputs[varName] = inputValues[index];
        });
    }

    console.log('Collected inputs:', inputs);
    console.log('Selected function:', selectedFunctionName);

    try {
        // Send function name along with program name
        const executionRequest = {
            programId: selectedProgramName,
            functionName: selectedFunctionName,
            mode: mode,
            inputs: inputs,
            currentDegree: state.currentDegree
        };

        // Pass the currentDegree as a 5th parameter
        const response = await api.startExecution(
            executionRequest.programId,
            mode,
            inputs,
            selectedFunctionName,
            state.currentDegree
        );
        console.log('Execution started:', response);

        currentExecutionId = response.executionId;
        state.setRunId(currentExecutionId);

        if (response.status === 'completed') {
            displayExecutionResult(response.data);

            // Update credits after execution
            if (response.data.creditsRemaining !== undefined) {
                state.updateCredits(response.data.creditsRemaining);
                updateCreditsDisplay(response.data.creditsRemaining);
            }

            updateUIState('completed');

            // Show credits consumed
            const creditsUsed = response.data.creditsConsumed || response.data.cycles || 0;
            showToast('success', `Execution completed! Result: ${response.data.result}, Cycles: ${response.data.cycles}, Credits used: ${creditsUsed}`);
        } else if (response.status === 'ready') {
            updateUIState('debug-ready');
            displayVariablesObj(response.data.variables);
            updateCyclesDisplay(response.data.cycles || 0);

            // Update credits display
            if (response.data.creditsRemaining !== undefined) {
                state.updateCredits(response.data.creditsRemaining);
                updateCreditsDisplay(response.data.creditsRemaining);
            }

            showToast('success', 'Debug mode ready - use Step Forward to execute');
        }

    } catch (error) {
        console.error('=== EXECUTION ERROR ===');
        console.error('Error object:', error);
        console.error('Error name:', error.name);
        console.error('Error message:', error.message);
        console.error('Error data:', error.data);

        // Check for insufficient credits - Use error.data
        if (error.name === 'InsufficientCreditsError' && error.data) {
            const creditsRequired = error.data.creditsRequired || null;
            const creditsAvailable = error.data.creditsAvailable || state.credits;
            const creditsToCharge = creditsRequired ? creditsRequired - creditsAvailable : null;

            console.log('Showing insufficient credits dialog:', {
                creditsRequired,
                creditsAvailable,
                creditsToCharge
            });

            showInsufficientCreditsDialog(creditsRequired, creditsAvailable, creditsToCharge);
            updateUIState('idle');
            return;
        }

        // Fallback: Check if error message contains insufficient_credits
        if (error.message && error.message.includes('insufficient_credits')) {
            console.log('Detected insufficient_credits in message');
            showInsufficientCreditsDialog(null, state.credits, null);
            updateUIState('idle');
            return;
        }

        // Other errors
        showToast('error', `Execution failed: ${error.message}`);
        updateUIState('idle');
    }
}

// Add these new functions for variable highlighting

function highlightVariable(variableName) {
    if (!variableName) {
        clearVariableHighlight();
        return;
    }

    highlightedVariable = variableName.trim();

    // Update UI to show clear button
    document.getElementById('clearHighlightBtn').style.display = 'inline-block';
    document.getElementById('variableHighlightInput').value = highlightedVariable;

    // Refresh the instructions table to apply highlighting
    refreshInstructionsTable();

    showToast('info', `Highlighting variable: ${highlightedVariable}`);
}

function clearVariableHighlight() {
    highlightedVariable = null;

    // Update UI
    document.getElementById('clearHighlightBtn').style.display = 'none';
    document.getElementById('variableHighlightInput').value = '';

    // Refresh the instructions table to remove highlighting
    refreshInstructionsTable();

    showToast('info', 'Variable highlighting cleared');
}

function shouldHighlightInstruction(instruction) {
    if (!highlightedVariable) {
        return false;
    }

    // Check if the instruction contains the highlighted variable
    const instructionText = instruction.instruction || '';

    // Match the variable as a whole word (not as part of another variable)
    const regex = new RegExp(`\\b${highlightedVariable}\\b`, 'i');
    return regex.test(instructionText);
}

function refreshInstructionsTable() {
    // Re-render the instructions table with current highlighting
    const instructionsContainer = document.getElementById('instructionsTableContainer');
    if (!instructionsContainer) return;

    const rows = instructionsContainer.querySelectorAll('.instruction-row');
    rows.forEach((row, index) => {
        const instructionData = row._instructionData; // Store this when creating rows

        // Remove variable highlight class
        row.classList.remove('variable-highlighted');

        // Add variable highlight if needed
        if (instructionData && shouldHighlightInstruction(instructionData)) {
            row.classList.add('variable-highlighted');
        }
    });
}

// NEW FUNCTION: Show modal input dialog
function showInputDialog(inputVariables) {
    return new Promise((resolve) => {
        // Create overlay
        const overlay = document.createElement('div');
        overlay.id = 'inputDialogOverlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.6);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            animation: fadeIn 0.2s ease-in;
        `;

        // Create dialog box
        const dialog = document.createElement('div');
        dialog.style.cssText = `
            background: white;
            border-radius: 12px;
            padding: 0;
            min-width: 450px;
            max-width: 600px;
            max-height: 80vh;
            overflow: hidden;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
            animation: slideIn 0.3s ease-out;
            display: flex;
            flex-direction: column;
        `;

        // Dialog header
        const header = document.createElement('div');
        header.style.cssText = `
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 24px;
            border-bottom: none;
        `;
        header.innerHTML = `
            <h2 style="margin: 0; font-size: 20px; font-weight: 600;">Enter Input Values</h2>
            <p style="margin: 8px 0 0 0; font-size: 14px; opacity: 0.9;">
                Please provide values for ${inputVariables.length} input variable${inputVariables.length > 1 ? 's' : ''}
            </p>
        `;

        // Dialog content
        const content = document.createElement('div');
        content.style.cssText = `
            padding: 24px;
            overflow-y: auto;
            flex: 1;
        `;

        // Create input fields
        const inputFields = [];
        inputVariables.forEach((varName, index) => {
            const inputGroup = document.createElement('div');
            inputGroup.style.cssText = `
                margin-bottom: 20px;
            `;

            const label = document.createElement('label');
            label.textContent = `${varName}:`;
            label.style.cssText = `
                display: block;
                font-weight: 600;
                margin-bottom: 8px;
                color: #333;
                font-size: 15px;
            `;

            const input = document.createElement('input');
            input.type = 'number';
            input.min = '0';
            input.value = '0';
            input.placeholder = `Enter value for ${varName}`;
            input.style.cssText = `
                width: 100%;
                padding: 12px 16px;
                border: 2px solid #e0e0e0;
                border-radius: 8px;
                font-size: 16px;
                box-sizing: border-box;
                transition: border-color 0.2s, box-shadow 0.2s;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            `;

            // Focus styling
            input.addEventListener('focus', () => {
                input.style.borderColor = '#667eea';
                input.style.boxShadow = '0 0 0 3px rgba(102, 126, 234, 0.1)';
            });

            input.addEventListener('blur', () => {
                input.style.borderColor = '#e0e0e0';
                input.style.boxShadow = 'none';
            });

            inputGroup.appendChild(label);
            inputGroup.appendChild(input);
            content.appendChild(inputGroup);
            inputFields.push(input);

            // Focus first input after a short delay
            if (index === 0) {
                setTimeout(() => input.focus(), 100);
            }
        });

        // Dialog footer
        const footer = document.createElement('div');
        footer.style.cssText = `
            padding: 16px 24px;
            border-top: 1px solid #e0e0e0;
            display: flex;
            gap: 12px;
            justify-content: flex-end;
            background: #f8f9fa;
        `;

        const cancelBtn = document.createElement('button');
        cancelBtn.textContent = 'Cancel';
        cancelBtn.style.cssText = `
            padding: 10px 24px;
            border: 2px solid #ddd;
            background: white;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
            font-weight: 500;
            transition: all 0.2s;
            color: #666;
        `;
        cancelBtn.addEventListener('mouseover', () => {
            cancelBtn.style.background = '#f5f5f5';
            cancelBtn.style.borderColor = '#bbb';
        });
        cancelBtn.addEventListener('mouseout', () => {
            cancelBtn.style.background = 'white';
            cancelBtn.style.borderColor = '#ddd';
        });

        const confirmBtn = document.createElement('button');
        confirmBtn.textContent = 'Confirm';
        confirmBtn.style.cssText = `
            padding: 10px 24px;
            border: none;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
            font-weight: 600;
            transition: all 0.2s;
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
        `;
        confirmBtn.addEventListener('mouseover', () => {
            confirmBtn.style.transform = 'translateY(-1px)';
            confirmBtn.style.boxShadow = '0 4px 12px rgba(102, 126, 234, 0.4)';
        });
        confirmBtn.addEventListener('mouseout', () => {
            confirmBtn.style.transform = 'translateY(0)';
            confirmBtn.style.boxShadow = '0 2px 8px rgba(102, 126, 234, 0.3)';
        });

        footer.appendChild(cancelBtn);
        footer.appendChild(confirmBtn);

        // Assemble dialog
        dialog.appendChild(header);
        dialog.appendChild(content);
        dialog.appendChild(footer);
        overlay.appendChild(dialog);

        // Add animations
        const style = document.createElement('style');
        style.textContent = `
            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
            @keyframes slideIn {
                from { 
                    transform: translateY(-30px);
                    opacity: 0;
                }
                to { 
                    transform: translateY(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);

        // Event handlers
        cancelBtn.addEventListener('click', () => {
            document.body.removeChild(overlay);
            document.head.removeChild(style);
            resolve(null);
        });

        confirmBtn.addEventListener('click', () => {
            const values = [];
            let isValid = true;

            for (let i = 0; i < inputFields.length; i++) {
                const input = inputFields[i];
                const value = parseInt(input.value);

                if (isNaN(value) || value < 0) {
                    input.style.borderColor = '#f44336';
                    input.style.boxShadow = '0 0 0 3px rgba(244, 67, 54, 0.1)';
                    showToast('error', `Invalid value for ${inputVariables[i]}. Must be non-negative.`);
                    isValid = false;
                    input.focus();
                    break;
                }

                values.push(value);
            }

            if (isValid) {
                document.body.removeChild(overlay);
                document.head.removeChild(style);
                resolve(values);
            }
        });

        // Enter key support
        inputFields.forEach((input, index) => {
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    if (index < inputFields.length - 1) {
                        inputFields[index + 1].focus();
                    } else {
                        confirmBtn.click();
                    }
                }
            });
        });

        // Escape key to cancel
        overlay.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                cancelBtn.click();
            }
        });

        // Add to page
        document.body.appendChild(overlay);
    });
}

// NEW FUNCTION: Show insufficient credits dialog
function showInsufficientCreditsDialog(creditsRequired, creditsAvailable, creditsToCharge) {
    console.log('showInsufficientCreditsDialog called with:', { creditsRequired, creditsAvailable, creditsToCharge });

    // Create overlay
    const overlay = document.createElement('div');
    overlay.id = 'insufficientCreditsOverlay';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
        animation: fadeIn 0.2s ease-in;
    `;

    // Create dialog box
    const dialog = document.createElement('div');
    dialog.style.cssText = `
        background: white;
        border-radius: 16px;
        padding: 0;
        min-width: 450px;
        max-width: 500px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        animation: slideIn 0.3s ease-out;
        overflow: hidden;
    `;

    // Dialog header (red gradient for error)
    const header = document.createElement('div');
    header.style.cssText = `
        background: linear-gradient(135deg, #f44336 0%, #e91e63 100%);
        color: white;
        padding: 24px;
        text-align: center;
    `;
    header.innerHTML = `
        <div style="font-size: 48px; margin-bottom: 8px;">⚠️</div>
        <h2 style="margin: 0; font-size: 24px; font-weight: 600;">Insufficient Credits</h2>
    `;

    // Dialog content
    const content = document.createElement('div');
    content.style.cssText = `
        padding: 32px 24px;
        text-align: center;
    `;

    let messageHTML = `
        <div style="font-size: 16px; color: #333; margin-bottom: 24px; line-height: 1.6;">
            You don't have enough credits to run this program.
        </div>
    `;

    if (creditsRequired !== null && creditsToCharge !== null) {
        messageHTML += `
            <div style="background: #f5f5f5; padding: 20px; border-radius: 12px; margin-bottom: 24px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
                    <span style="color: #666; font-weight: 500;">Required Credits:</span>
                    <span style="color: #f44336; font-weight: 700; font-size: 18px;">${creditsRequired}</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
                    <span style="color: #666; font-weight: 500;">Your Credits:</span>
                    <span style="color: #666; font-weight: 600; font-size: 18px;">${creditsAvailable}</span>
                </div>
                <div style="border-top: 2px solid #ddd; margin: 12px 0; padding-top: 12px;"></div>
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: #333; font-weight: 600;">Need to Charge:</span>
                    <span style="color: #4caf50; font-weight: 700; font-size: 20px;">+${creditsToCharge}</span>
                </div>
            </div>
        `;
    } else {
        messageHTML += `
            <div style="background: #f5f5f5; padding: 20px; border-radius: 12px; margin-bottom: 24px;">
                <div style="color: #666; font-size: 15px;">
                    Your current credits: <strong>${creditsAvailable}</strong>
                </div>
            </div>
        `;
    }

    messageHTML += `
        <div style="color: #666; font-size: 14px; margin-bottom: 20px;">
            💡 Go to the Dashboard to charge more credits
        </div>
    `;

    content.innerHTML = messageHTML;

    // Dialog footer with buttons
    const footer = document.createElement('div');
    footer.style.cssText = `
        padding: 16px 24px;
        border-top: 1px solid #e0e0e0;
        display: flex;
        gap: 12px;
        justify-content: center;
        background: #f8f9fa;
    `;

    const stayBtn = document.createElement('button');
    stayBtn.textContent = 'Stay Here';
    stayBtn.style.cssText = `
        padding: 12px 24px;
        border: 2px solid #ddd;
        background: white;
        border-radius: 8px;
        cursor: pointer;
        font-size: 15px;
        font-weight: 500;
        transition: all 0.2s;
        color: #666;
        min-width: 140px;
    `;
    stayBtn.addEventListener('mouseover', () => {
        stayBtn.style.background = '#f5f5f5';
        stayBtn.style.borderColor = '#bbb';
    });
    stayBtn.addEventListener('mouseout', () => {
        stayBtn.style.background = 'white';
        stayBtn.style.borderColor = '#ddd';
    });

    const dashboardBtn = document.createElement('button');
    dashboardBtn.textContent = 'Go to Dashboard';
    dashboardBtn.style.cssText = `
        padding: 12px 24px;
        border: none;
        background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
        color: white;
        border-radius: 8px;
        cursor: pointer;
        font-size: 15px;
        font-weight: 600;
        transition: all 0.2s;
        box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
        min-width: 140px;
    `;
    dashboardBtn.addEventListener('mouseover', () => {
        dashboardBtn.style.transform = 'translateY(-2px)';
        dashboardBtn.style.boxShadow = '0 4px 12px rgba(76, 175, 80, 0.4)';
    });
    dashboardBtn.addEventListener('mouseout', () => {
        dashboardBtn.style.transform = 'translateY(0)';
        dashboardBtn.style.boxShadow = '0 2px 8px rgba(76, 175, 80, 0.3)';
    });

    footer.appendChild(stayBtn);
    footer.appendChild(dashboardBtn);

    // Assemble dialog
    dialog.appendChild(header);
    dialog.appendChild(content);
    dialog.appendChild(footer);
    overlay.appendChild(dialog);

    // Event handlers
    stayBtn.addEventListener('click', () => {
        document.body.removeChild(overlay);
    });

    dashboardBtn.addEventListener('click', () => {
        window.location.href = contextPath + '/dashboard.html';
    });

    // Escape key to close
    const escapeHandler = (e) => {
        if (e.key === 'Escape') {
            document.body.removeChild(overlay);
            document.removeEventListener('keydown', escapeHandler);
        }
    };
    document.addEventListener('keydown', escapeHandler);

    // Add to page
    document.body.appendChild(overlay);
    console.log('Dialog added to page');
}

async function apiCall(endpoint, options = {}) {
    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, { ...defaultOptions, ...options });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));

            // ✅ If it's an insufficient_credits error, include full details in error message
            if (errorData.error === 'insufficient_credits') {
                const error = new Error(JSON.stringify(errorData));
                error.name = 'InsufficientCreditsError';
                throw error;
            }

            throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API call failed: ${endpoint}`, error);
        throw error;
    }
}

async function sendCommand(command) {
    console.log('Sending command:', command);

    if (!currentExecutionId) {
        showToast('error', 'No active execution');
        return;
    }

    try {
        let response;

        switch (command) {
            case 'stepForward':
                response = await fetch(`${contextPath}/api/execute/step`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ executionId: currentExecutionId })
                });
                break;

            case 'resume':
                response = await fetch(`${contextPath}/api/execute/resume`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ executionId: currentExecutionId })
                });
                break;

            case 'stop':
                response = await fetch(`${contextPath}/api/execute/stop`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ executionId: currentExecutionId })
                });
                if (response.ok) {
                    currentExecutionId = null;
                    updateUIState('idle');
                    showToast('info', 'Execution stopped');
                }
                return;

            default:
                showToast('error', 'Unknown command');
                return;
        }

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Command failed');
        }

        const data = await response.json();
        console.log('Command response:', data);

// ✅ ADD THIS BLOCK - Update credits after each step/resume
        if (data.creditsRemaining !== undefined) {
            state.updateCredits(data.creditsRemaining);
            updateCreditsDisplay(data.creditsRemaining);
        }

        if (data.status === 'execution_finished') {
            displayExecutionResult({ result: data.result, cycles: data.totalCycles, variables: data.variables });
            updateUIState('completed');

            //Show credits consumed
            const creditsUsed = data.creditsConsumed || data.totalCycles || 0;
            showToast('success', `Execution finished! Result: ${data.result}, Cycles: ${data.totalCycles}, Credits used: ${creditsUsed}`);
            currentExecutionId = null;

        } else if (data.status === 'step_completed') {
            displayVariablesObj(data.variables);
            updateCyclesDisplay(data.totalCycles);
            highlightInstruction(data.nextIndex);

            if (data.changedVariable) {
                showToast('info', `${data.changedVariable} = ${data.changedValue}`, 2000);
            }
        }

    } catch (error) {
        console.error('Command failed:', error);

        //Handle insufficient credits during execution
        if (error.message && error.message.includes('insufficient_credits')) {
            showToast('error', 'Out of credits! Returning to dashboard...');
            updateUIState('idle');
            setTimeout(() => window.location.href = contextPath + '/dashboard.html', 3000);
            return;
        }

        showToast('error', `Command failed: ${error.message}`);
    }
}

function displayExecutionResult(data) {
    if (data.variables) {
        displayVariablesObj(data.variables);
    }
    if (data.cycles !== undefined) {
        updateCyclesDisplay(data.cycles);
    }

    const summaryText = document.getElementById('summaryText');
    if (summaryText) {
        summaryText.textContent = `Result: ${data.result}, Cycles: ${data.cycles}`;
    }
}

function displayVariablesObj(variablesObj) {
    const container = document.getElementById('variablesTableContainer');

    if (!variablesObj || Object.keys(variablesObj).length === 0) {
        container.innerHTML = '<div class="empty-state">No variables</div>';
        return;
    }

    const headers = ['Name', 'Value'];
    const rows = Object.entries(variablesObj).map(([name, value]) => [name, value]);

    const table = createTable(headers, rows);
    container.innerHTML = '';
    container.appendChild(table);
}

function updateCyclesDisplay(cycles) {
    const cyclesValue = document.getElementById('cyclesValue');
    if (cyclesValue) {
        cyclesValue.textContent = cycles;
    }
}

function highlightInstruction(index) {
    const allRows = document.querySelectorAll('#instructionsTableContainer tbody tr');
    allRows.forEach(row => row.classList.remove('highlighted'));

    if (index >= 0 && index < allRows.length) {
        allRows[index].classList.add('highlighted');
        allRows[index].scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

function updateUIState(status) {
    const statusBadge = document.getElementById('executionStatus');

    const startRegularBtn = document.getElementById('startRegularBtn');
    const startDebugBtn = document.getElementById('startDebugBtn');
    const stopBtn = document.getElementById('stopBtn');
    const resumeBtn = document.getElementById('resumeBtn');
    const stepForwardBtn = document.getElementById('stepForwardBtn');

    //Check if user has credits
    const hasCredits = state.credits > 0;

    switch (status) {
        case 'idle':
            statusBadge.textContent = 'idle';
            statusBadge.className = 'badge badge-idle';
            startRegularBtn.disabled = !hasCredits;
            startDebugBtn.disabled = !hasCredits;
            stopBtn.disabled = true;
            resumeBtn.disabled = true;
            stepForwardBtn.disabled = true;
            break;

        case 'debug-ready':
        case 'debug-running':
            statusBadge.textContent = 'debugging';
            statusBadge.className = 'badge badge-running';
            startRegularBtn.disabled = true;
            startDebugBtn.disabled = true;
            stopBtn.disabled = false;
            resumeBtn.disabled = false;
            stepForwardBtn.disabled = false;
            break;

        case 'completed':
            statusBadge.textContent = 'completed';
            statusBadge.className = 'badge badge-success';
            startRegularBtn.disabled = false;
            startDebugBtn.disabled = false;
            stopBtn.disabled = true;
            resumeBtn.disabled = true;
            stepForwardBtn.disabled = true;
            break;
    }

    state.updateExecutionStatus(status);
}

/**
 * Update degree display in UI
 */
function updateDegreeDisplay(currentDegree, maxDegree) {
    const degreeValueElement = document.getElementById('degreeValue');
    if (degreeValueElement) {
        degreeValueElement.textContent = `${currentDegree} / ${maxDegree}`;
    }
}

/**
 * Enable/disable expand and collapse buttons based on current degree
 */
function updateExpandCollapseButtons(currentDegree, minDegree, maxDegree) {
    const expandBtn = document.getElementById('expandBtn');
    const collapseBtn = document.getElementById('collapseBtn');

    if (expandBtn) {
        expandBtn.disabled = (currentDegree >= maxDegree);
    }

    if (collapseBtn) {
        collapseBtn.disabled = (currentDegree <= minDegree);
    }
}


function applyHighlight() {
    console.log('Highlight enabled:', highlightEnabled);
    // TODO: Implement highlight logic
}

window.addEventListener('beforeunload', () => {
    if (pollInterval) clearInterval(pollInterval);
});