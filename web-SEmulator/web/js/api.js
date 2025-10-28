// api.js - API client for S-Emulator backend

const API_BASE = '/api';

/**
 * Make an API call with proper error handling
 */
async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;

    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    };

    const finalOptions = { ...defaultOptions, ...options };

    try {
        const response = await fetch(url, finalOptions);

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API call failed: ${endpoint}`, error);
        throw error;
    }
}

/**
 * API object with all backend endpoints
 */
const api = {
    // Authentication
    async login(username) {
        return apiCall('/login', {
            method: 'POST',
            body: JSON.stringify({ username })
        });
    },

    async logout() {
        return apiCall('/logout', {
            method: 'POST'
        });
    },

    async getSession() {
        return apiCall('/session');
    },

    // Users
    async getUsers() {
        return apiCall('/users/live');
    },

    // Credits
    async chargeCredits(amount) {
        return apiCall('/credits/charge', {
            method: 'POST',
            body: JSON.stringify({ amount })
        });
    },

    // Programs
    async getPrograms() {
        return apiCall('/programs');
    },

    async loadFile(formData) {
        return apiCall('/file/load', {
            method: 'POST',
            headers: {}, // Let browser set Content-Type for FormData
            body: formData
        });
    },

    async selectProgram(programName) {
        return apiCall('/execution/select-program', {
            method: 'POST',
            body: JSON.stringify({ programName })
        });
    },

    // This now uses the same endpoint as selectProgram since it returns all the data
    async getProgramDetails(programName) {
        return apiCall('/execution/select-program', {
            method: 'POST',
            body: JSON.stringify({ programName })
        });
    },

    async getFunctions(programName) {
        return apiCall(`/program/functions?programName=${encodeURIComponent(programName)}`);
    },

    async chooseFunction(functionName) {
        return apiCall('/program/choose-function', {
            method: 'POST',
            body: JSON.stringify({ functionName })
        });
    },

    // Execution
    async executeRegular(inputs = []) {
        return apiCall('/execution/regular', {
            method: 'POST',
            body: JSON.stringify({ inputs })
        });
    },

    async startDebug(inputs = []) {
        return apiCall('/execution/debug/start', {
            method: 'POST',
            body: JSON.stringify({ inputs })
        });
    },

    async debugStep() {
        return apiCall('/execution/debug/step', {
            method: 'POST'
        });
    },

    async debugResume() {
        return apiCall('/execution/debug/resume', {
            method: 'POST'
        });
    },

    async debugStop() {
        return apiCall('/execution/debug/stop', {
            method: 'POST'
        });
    },

    // Expand/Collapse
    async expand() {
        return apiCall('/execution/expand', {
            method: 'POST'
        });
    },

    async collapse() {
        return apiCall('/execution/collapse', {
            method: 'POST'
        });
    },

    // Rerun
    async rerun(runNumber) {
        return apiCall('/execution/rerun', {
            method: 'POST',
            body: JSON.stringify({ runNumber })
        });
    },

    // Statistics
    async getStatistics(programName) {
        return apiCall(`/statistics?program=${encodeURIComponent(programName)}`);
    }
};

export { api, apiCall };