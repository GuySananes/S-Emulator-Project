// API wrapper for all backend endpoints

// Get the context path (e.g., '/web_SEmulator_Web_exploded')
const getContextPath = () => {
    const path = window.location.pathname;
    const contextPath = path.substring(0, path.indexOf('/', 1));
    return contextPath || '';
};

const BASE_URL = getContextPath() + '/api';

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

        // ✅ Handle non-OK responses
        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {
                errorData = { error: `HTTP ${response.status}` };
            }

            console.log('API Error Response:', errorData);

            // ✅ Create custom error with full data
            const error = new Error(errorData.error || `HTTP ${response.status}`);
            error.status = response.status;
            error.data = errorData; // Store full error data

            // Mark insufficient credits errors specially
            if (errorData.error === 'insufficient_credits') {
                error.name = 'InsufficientCreditsError';
            }

            throw error;
        }

        return await response.json();
    } catch (error) {
        console.error(`API call failed: ${endpoint}`, error);
        throw error;
    }
}

// Export context path helper
export const contextPath = getContextPath();
export const api = {
    // Session
    async getSession() {
        return apiCall('/session/me');
    },

    // Login
    async login(username) {
        return apiCall('/login', {
            method: 'POST',
            body: JSON.stringify({username})
        });
    },

    async logout() {
        return apiCall('/logout', {method: 'POST'});
    },

    // Users
    async getLiveUsers() {
        return apiCall('/users/live');
    },

    // Programs
    async getPrograms() {
        console.log('API: Calling /programs endpoint');
        const result = await apiCall('/programs');
        console.log('API: /programs response:', result);
        return result;
    },

    async getFunctions(programName) {
        console.log('API: Calling functions for program:', programName);
        const response = await fetch(`${contextPath}/api/programs/functions/${encodeURIComponent(programName)}`, {
            credentials: 'include'
        });
        if (!response.ok) {
            console.error('API: Functions request failed:', response.status, response.statusText);
            throw new Error('Failed to fetch functions');
        }
        const result = await response.json();
        console.log('API: Functions response:', result);
        return result;
    },

    async getProgramFunctions(programId) {
        return apiCall(`/programs/${programId}/functions`);
    },

    // File
    async loadFile(file) {
        console.log('API: Uploading file:', file.name);
        const formData = new FormData();
        formData.append('file', file);

        return fetch(`${BASE_URL}/file/load`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        }).then(r => {
            console.log('API: File upload response status:', r.status);
            if (!r.ok) {
                console.error('API: File upload failed with status:', r.status);
                throw new Error('File upload failed');
            }
            return r.json();
        }).then(data => {
            console.log('API: File upload result:', data);
            return data;
        });
    },

    // Credits
    async chargeCredits(amount) {
        return apiCall('/credits/charge', {
            method: 'POST',
            body: JSON.stringify({ amount })
        });
    },

    // Execution
    async startExecution(programId, mode, inputs = {}, functionName = null) {
        const body = {
            programId,
            mode,
            inputs
        };

        // Add functionName if provided
        if (functionName) {
            body.functionName = functionName;
        }

        // Just let apiCall throw the error - it will be caught in execute.js
        return apiCall('/execute/start', {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    async stepExecution(executionId) {
        return apiCall('/execute/step', {
            method: 'POST',
            body: JSON.stringify({ executionId })
        });
    },

    async resumeExecution(executionId) {
        return apiCall('/execute/resume', {
            method: 'POST',
            body: JSON.stringify({ executionId })
        });
    },

    async stopExecution(executionId) {
        return apiCall('/execute/stop', {
            method: 'POST',
            body: JSON.stringify({ executionId })
        });
    },

    async getExecutionStatus(executionId) {
        return apiCall(`/execute/status?executionId=${executionId}`);
    },

    async getVariables(executionId) {
        return apiCall(`/execute/variables?executionId=${executionId}`);
    },

    // Statistics
    async getStatisticsHistory() {
        return apiCall('/statistics/history');
    }
};

/**
 * Expand or collapse program to a specific degree
 */
export async function expandProgram(degree) {
    const response = await fetch(`${contextPath}/api/execute/expand`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ degree })
    });

    if (!response.ok) {
        let errorData;
        try {
            errorData = await response.json();
        } catch (e) {
            errorData = { error: `HTTP ${response.status}` };
        }
        throw new Error(errorData.error || 'Failed to expand program');
    }

    return await response.json();
}