// execute.js - Execution page controller

import { api } from './api.js';
import { state } from './state.js';
import { showToast } from './ui.js';

// DOM Elements
let programSelector;
let functionSelector;
let startExecutionBtn;
let startRegularBtn;
let startDebugBtn;
let stopBtn;
let resumeBtn;
let stepForwardBtn;
let stepBackwardBtn;
let collapseBtn;
let expandBtn;
let highlightToggle;
let executionStatus;
let executionStep;
let cyclesValue;
let inputsForm;
let variablesTableContainer;
let instructionsTableContainer;
let summaryText;
let creditsDisplay;
let backToDashboardBtn;
let backToDashboard2Btn;

// Store the current program data
let currentProgramData = null;

/**
 * Initialize the execution page
 */
function init() {
    console.log('Initializing execution page...');

    // Get DOM elements
    programSelector = document.getElementById('programSelector');
    functionSelector = document.getElementById('functionSelector');
    startExecutionBtn = document.getElementById('startExecutionBtn');
    startRegularBtn = document.getElementById('startRegularBtn');
    startDebugBtn = document.getElementById('startDebugBtn');
    stopBtn = document.getElementById('stopBtn');
    resumeBtn = document.getElementById('resumeBtn');
    stepForwardBtn = document.getElementById('stepForwardBtn');
    stepBackwardBtn = document.getElementById('stepBackwardBtn');
    collapseBtn = document.getElementById('collapseBtn');
    expandBtn = document.getElementById('expandBtn');
    highlightToggle = document.getElementById('highlightToggle');
    executionStatus = document.getElementById('executionStatus');
    executionStep = document.getElementById('executionStep');
    cyclesValue = document.getElementById('cyclesValue');
    inputsForm = document.getElementById('inputsForm');
    variablesTableContainer = document.getElementById('variablesTableContainer');
    instructionsTableContainer = document.getElementById('instructionsTableContainer');
    summaryText = document.getElementById('summaryText');
    creditsDisplay = document.getElementById('creditsDisplay');
    backToDashboardBtn = document.getElementById('backToDashboardBtn');
    backToDashboard2Btn = document.getElementById('backToDashboard2Btn');

    // Setup event handlers
    setupEventHandlers();

    // Load initial data
    loadInitialData();
}

/**
 * Setup all event handlers
 */
function setupEventHandlers() {
    // Program selector
    if (programSelector) {
        programSelector.addEventListener('change', handleProgramChange);
    }

    // Function selector
    if (functionSelector) {
        functionSelector.addEventListener('change', handleFunctionChange);
    }

    // Execution buttons
    if (startExecutionBtn) {
        startExecutionBtn.addEventListener('click', handleStartExecution);
    }
    if (startRegularBtn) {
        startRegularBtn.addEventListener('click', handleStartRegular);
    }
    if (startDebugBtn) {
        startDebugBtn.addEventListener('click', handleStartDebug);
    }
    if (stopBtn) {
        stopBtn.addEventListener('click', handleStop);
    }
    if (resumeBtn) {
        resumeBtn.addEventListener('click', handleResume);
    }
    if (stepForwardBtn) {
        stepForwardBtn.addEventListener('click', handleStepForward);
    }
    if (stepBackwardBtn) {
        stepBackwardBtn.addEventListener('click', handleStepBackward);
    }

    // View controls
    if (collapseBtn) {
        collapseBtn.addEventListener('click', handleCollapse);
    }
    if (expandBtn) {
        expandBtn.addEventListener('click', handleExpand);
    }

    // Navigation
    if (backToDashboardBtn) {
        backToDashboardBtn.addEventListener('click', () => {
            window.location.href = 'dashboard.html';
        });
    }
    if (backToDashboard2Btn) {
        backToDashboard2Btn.addEventListener('click', () => {
            window.location.href = 'dashboard.html';
        });
    }
}

/**
 * Load initial data (programs, session)
 */
async function loadInitialData() {
    try {
        // Check session
        const session = await api.getSession();
        if (!session || !session.username) {
            window.location.href = 'index.html';
            return;
        }

        state.currentUser = session.username;
        updateCreditsDisplay(session.credits);

        // Load programs
        await loadPrograms();

    } catch (error) {
        console.error('Failed to load initial data:', error);
        showToast('Failed to load session data', 'error');
    }
}

/**
 * Load available programs
 */
async function loadPrograms() {
    try {
        const response = await api.getPrograms();

        // Handle both array and object with programs property
        const programs = Array.isArray(response) ? response : (response.programs || []);

        if (!programs || programs.length === 0) {
            showToast('No programs available', 'info');
            return;
        }

        // Populate program selector
        programSelector.innerHTML = '<option value="">Select program...</option>';
        programs.forEach(program => {
            const option = document.createElement('option');
            option.value = program.name || program;
            option.textContent = program.name || program;
            programSelector.appendChild(option);
        });

    } catch (error) {
        console.error('Failed to load programs:', error);
        showToast('Failed to load programs', 'error');
    }
}

/**
 * Handle program selection change
 */
async function handleProgramChange(event) {
    const programName = event.target.value;

    if (!programName) {
        state.execution.selectedProgram = null;
        currentProgramData = null;
        startExecutionBtn.disabled = true;
        functionSelector.disabled = true;
        functionSelector.innerHTML = '<option value="">Select function...</option>';
        return;
    }

    try {
        state.execution.selectedProgram = programName;

        // Select program on backend - this returns all the data we need
        currentProgramData = await api.selectProgram(programName);

        console.log('Program data loaded:', currentProgramData);

        // Load functions
        await loadFunctions();

        // Load input fields based on variables
        await loadInputFields();

        // Load instructions
        await loadInstructions();

        showToast(`Program ${programName} loaded successfully`, 'success');
        updateSummary(`Program ${programName} loaded`);

    } catch (error) {
        console.error('Failed to select program:', error);
        showToast(`Failed to select program: ${error.message}`, 'error');
        startExecutionBtn.disabled = true;
    }
}

/**
 * Load functions for the selected program
 */
async function loadFunctions() {
    try {
        functionSelector.innerHTML = '<option value="">Main Program</option>';

        if (currentProgramData && currentProgramData.contextPrograms && currentProgramData.contextPrograms.length > 0) {
            currentProgramData.contextPrograms.forEach(funcName => {
                const option = document.createElement('option');
                option.value = funcName;
                option.textContent = funcName;
                functionSelector.appendChild(option);
            });
            functionSelector.disabled = false;
        } else {
            functionSelector.disabled = true;
        }

    } catch (error) {
        console.error('Failed to load functions:', error);
        functionSelector.disabled = true;
    }
}

/**
 * Handle function selection change
 */
async function handleFunctionChange(event) {
    const functionName = event.target.value;

    if (!functionName) {
        return;
    }

    try {
        await api.chooseFunction(functionName);
        // Reload program data
        currentProgramData = await api.getProgramDetails(state.execution.selectedProgram);
        await loadInputFields();
        await loadInstructions();
        showToast(`Function ${functionName} selected`, 'success');
        updateSummary(`Function ${functionName} selected`);
    } catch (error) {
        console.error('Failed to select function:', error);
        showToast(`Failed to select function: ${error.message}`, 'error');
    }
}

/**
 * Load and display input fields for the selected program
 */
async function loadInputFields() {
    if (!currentProgramData) {
        inputsForm.innerHTML = '<div class="empty-state">Select a program first</div>';
        return;
    }

    try {
        if (!currentProgramData.variables || currentProgramData.variables.length === 0) {
            inputsForm.innerHTML = '<div class="empty-state">No inputs required</div>';
            startExecutionBtn.disabled = false;
            return;
        }

        // For now, assume all variables need input (you can filter by type if the backend provides it)
        const inputVars = currentProgramData.variables;

        if (inputVars.length === 0) {
            inputsForm.innerHTML = '<div class="empty-state">No inputs required</div>';
            startExecutionBtn.disabled = false;
            return;
        }

        // Generate input fields
        inputsForm.innerHTML = '';
        inputVars.forEach((variable, index) => {
            const formGroup = document.createElement('div');
            formGroup.className = 'form-group';

            const label = document.createElement('label');
            label.textContent = variable.name;
            label.setAttribute('for', `input_${index}`);

            const input = document.createElement('input');
            input.type = 'number';
            input.id = `input_${index}`;
            input.name = variable.name;
            input.placeholder = `Enter value for ${variable.name}`;
            input.required = true;
            input.value = variable.value || 0;

            formGroup.appendChild(label);
            formGroup.appendChild(input);
            inputsForm.appendChild(formGroup);
        });

        startExecutionBtn.disabled = false;

    } catch (error) {
        console.error('Failed to load input fields:', error);
        inputsForm.innerHTML = '<div class="error-message">Failed to load inputs</div>';
        startExecutionBtn.disabled = true;
    }
}

/**
 * Load instructions table
 */
async function loadInstructions() {
    if (!currentProgramData) {
        instructionsTableContainer.innerHTML = '<div class="empty-state">Select a program first</div>';
        return;
    }

    try {
        if (!currentProgramData.instructions || currentProgramData.instructions.length === 0) {
            instructionsTableContainer.innerHTML = '<div class="empty-state">No instructions</div>';
            return;
        }

        // Create table
        const table = document.createElement('table');
        table.className = 'data-table';

        const thead = document.createElement('thead');
        thead.innerHTML = `
            <tr>
                <th>#</th>
                <th>Type</th>
                <th>Instruction</th>
            </tr>
        `;
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        currentProgramData.instructions.forEach((instruction, index) => {
            const row = document.createElement('tr');
            const type = instruction.type || 'B';
            row.innerHTML = `
                <td>${instruction.index !== undefined ? instruction.index : index}</td>
                <td><span class="badge badge-type-${type.toLowerCase()}">${type}</span></td>
                <td>${instruction.representation || instruction.text || ''}</td>
            `;
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        instructionsTableContainer.innerHTML = '';
        instructionsTableContainer.appendChild(table);

    } catch (error) {
        console.error('Failed to load instructions:', error);
        instructionsTableContainer.innerHTML = '<div class="error-message">Failed to load instructions</div>';
    }
}

/**
 * Handle start execution button click
 */
async function handleStartExecution() {
    try {
        // Disable button during execution
        startExecutionBtn.disabled = true;
        updateStatus('running');

        // Collect input values from the form
        const inputs = collectInputValues();
        if (inputs === null) {
            updateStatus('idle');
            startExecutionBtn.disabled = false;
            return;
        }

        console.log('Executing with inputs:', inputs);

        // Call the execution API
        const response = await api.executeRegular(inputs);

        console.log('Execution response:', response);

        if (response.success) {
            // Update UI with results
            updateStatus('completed');
            updateCycles(response.cycles || 0);

            // Update variables table
            await loadVariables();

            showToast(`Execution completed: Result = ${response.result}, Cycles = ${response.cycles}`, 'success');
            updateSummary(`Execution completed: Result = ${response.result}, Cycles = ${response.cycles}`);
        } else {
            throw new Error('Execution failed');
        }

    } catch (error) {
        console.error('Execution error:', error);
        updateStatus('error');
        showToast(`Execution failed: ${error.message}`, 'error');
        updateSummary(`Execution failed: ${error.message}`);
    } finally {
        startExecutionBtn.disabled = false;
    }
}

/**
 * Handle start regular execution
 */
async function handleStartRegular() {
    await handleStartExecution();
}

/**
 * Handle start debug execution
 */
async function handleStartDebug() {
    try {
        const inputs = collectInputValues();
        if (inputs === null) {
            return;
        }

        const response = await api.startDebug(inputs);

        updateStatus('debugging');
        showToast('Debug session started', 'success');
        updateSummary('Debug session started');

        // Enable debug controls
        stopBtn.disabled = false;
        resumeBtn.disabled = false;
        stepForwardBtn.disabled = false;

    } catch (error) {
        console.error('Failed to start debug:', error);
        showToast(`Failed to start debug: ${error.message}`, 'error');
    }
}

/**
 * Handle stop debug
 */
async function handleStop() {
    try {
        await api.debugStop();
        updateStatus('idle');
        showToast('Debug session stopped', 'info');
        updateSummary('Debug session stopped');

        // Disable debug controls
        stopBtn.disabled = true;
        resumeBtn.disabled = true;
        stepForwardBtn.disabled = true;
        stepBackwardBtn.disabled = true;

    } catch (error) {
        console.error('Failed to stop debug:', error);
        showToast(`Failed to stop debug: ${error.message}`, 'error');
    }
}

/**
 * Handle resume debug
 */
async function handleResume() {
    try {
        const response = await api.debugResume();
        updateStatus('completed');
        showToast('Debug completed', 'success');
        updateSummary(`Debug completed: Result = ${response.result}`);

        // Disable debug controls
        stopBtn.disabled = true;
        resumeBtn.disabled = true;
        stepForwardBtn.disabled = true;
        stepBackwardBtn.disabled = true;

    } catch (error) {
        console.error('Failed to resume debug:', error);
        showToast(`Failed to resume debug: ${error.message}`, 'error');
    }
}

/**
 * Handle step forward in debug
 */
async function handleStepForward() {
    try {
        const response = await api.debugStep();
        updateSummary(`Step: ${response.step}, Cycles: ${response.cycles}`);
        updateCycles(response.cycles);

        if (response.completed) {
            updateStatus('completed');
            showToast('Debug completed', 'success');
            stopBtn.disabled = true;
            resumeBtn.disabled = true;
            stepForwardBtn.disabled = true;
        }

    } catch (error) {
        console.error('Failed to step forward:', error);
        showToast(`Failed to step forward: ${error.message}`, 'error');
    }
}

/**
 * Handle step backward in debug
 */
async function handleStepBackward() {
    showToast('Step backward not yet implemented', 'info');
}

/**
 * Handle collapse
 */
async function handleCollapse() {
    try {
        await api.collapse();
        // Reload program data
        currentProgramData = await api.getProgramDetails(state.execution.selectedProgram);
        await loadInstructions();
        showToast('Program collapsed', 'success');
        updateSummary('Program collapsed');
    } catch (error) {
        console.error('Failed to collapse:', error);
        showToast(`Failed to collapse: ${error.message}`, 'error');
    }
}

/**
 * Handle expand
 */
async function handleExpand() {
    try {
        await api.expand();
        // Reload program data
        currentProgramData = await api.getProgramDetails(state.execution.selectedProgram);
        await loadInstructions();
        showToast('Program expanded', 'success');
        updateSummary('Program expanded');
    } catch (error) {
        console.error('Failed to expand:', error);
        showToast(`Failed to expand: ${error.message}`, 'error');
    }
}

/**
 * Collect input values from form
 */
function collectInputValues() {
    const inputs = [];
    const inputFields = inputsForm.querySelectorAll('input[type="number"]');

    for (const field of inputFields) {
        const value = parseInt(field.value, 10);
        if (isNaN(value)) {
            showToast('Please fill in all input fields with valid numbers', 'error');
            return null;
        }
        inputs.push(value);
    }

    return inputs;
}

/**
 * Load variables after execution
 */
async function loadVariables() {
    if (!currentProgramData) {
        variablesTableContainer.innerHTML = '<div class="empty-state">No variables</div>';
        return;
    }

    try {
        if (!currentProgramData.variables || currentProgramData.variables.length === 0) {
            variablesTableContainer.innerHTML = '<div class="empty-state">No variables</div>';
            return;
        }

        // Create table
        const table = document.createElement('table');
        table.className = 'data-table';

        const thead = document.createElement('thead');
        thead.innerHTML = `
            <tr>
                <th>Name</th>
                <th>Value</th>
            </tr>
        `;
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        currentProgramData.variables.forEach(variable => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${variable.name}</td>
                <td>${variable.value !== undefined ? variable.value : '-'}</td>
            `;
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        variablesTableContainer.innerHTML = '';
        variablesTableContainer.appendChild(table);

    } catch (error) {
        console.error('Failed to load variables:', error);
        variablesTableContainer.innerHTML = '<div class="error-message">Failed to load variables</div>';
    }
}

/**
 * Update execution status display
 */
function updateStatus(status) {
    if (!executionStatus) return;

    executionStatus.textContent = status;
    executionStatus.className = `badge badge-${status}`;
}

/**
 * Update cycles display
 */
function updateCycles(cycles) {
    if (cyclesValue) {
        cyclesValue.textContent = cycles;
    }
}

/**
 * Update summary text
 */
function updateSummary(message) {
    if (summaryText) {
        summaryText.textContent = message;
    }
}

/**
 * Update credits display
 */
function updateCreditsDisplay(credits) {
    if (creditsDisplay) {
        creditsDisplay.textContent = `Available Credits: ${credits}`;
    }
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

export { init };