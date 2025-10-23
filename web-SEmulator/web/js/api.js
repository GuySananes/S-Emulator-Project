// API wrapper for all backend endpoints
const BASE_URL = '/api';

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
            throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API call failed: ${endpoint}`, error);
        throw error;
    }
}

export const api = {
    // Session
    async getSession() {
        return apiCall('/session/me');
    },

    // Login
    async login(username) {
        return apiCall('/login', {
            method: 'POST',
            body: JSON.stringify({ username })
        });
    },

    async logout() {
        return apiCall('/logout', { method: 'POST' });
    },

    // Users
    async getLiveUsers() {
        return apiCall('/users/live');
    },

    // Programs
    async getPrograms() {
        return apiCall('/programs');
    },

    async getProgramFunctions(programId) {
        return apiCall(`/programs/${programId}/functions`);
    },

    // File
    async loadFile(file) {
        const formData = new FormData();
        formData.append('file', file);

        return fetch(`${BASE_URL}/file/load`, {
            method: 'POST',
            credentials: 'include',
            body: formData
        }).then(r => {
            if (!r.ok) throw new Error('File upload failed');
            return r.json();
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
    async startExecution(programId, mode, inputs = {}) {
        return apiCall('/execute/start', {
            method: 'POST',
            body: JSON.stringify({ programId, mode, inputs })
        });
    },

    async getExecutionStatus(runId) {
        return apiCall(`/execute/status?runId=${runId}`);
    },

    async sendExecutionCommand(runId, cmd) {
        return apiCall('/execute/cmd', {
            method: 'POST',
            body: JSON.stringify({ runId, cmd })
        });
    },

    async getVariables(runId) {
        return apiCall(`/execute/variables?runId=${runId}`);
    },

    async getInputs(runId) {
        return apiCall(`/execute/inputs?runId=${runId}`);
    },

    async getInstructions(runId) {
        return apiCall(`/execute/instructions?runId=${runId}`);
    },

    async getHistory(runId, idx) {
        return apiCall(`/execute/history?runId=${runId}&idx=${idx}`);
    },

    // Statistics
    async getStatisticsHistory() {
        return apiCall('/statistics/history');
    }
};