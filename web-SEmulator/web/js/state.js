// Global application state
export const state = {
    username: null,
    credits: 0,
    selectedUser: null,
    selectedProgram: null,
    loadedFilePath: null,
    lastUpdated: null,
    runId: null,
    executionStatus: 'idle',
    currentDegree: 0,
    maxDegree: 0,
    minDegree: 1,


    // Update methods
    setUser(username, credits) {
        this.username = username;
        this.credits = credits;
    },

    updateCredits(credits) {
        this.credits = credits;
    },

    selectUser(user) {
        this.selectedUser = user;
    },

    unselectUser() {
        this.selectedUser = null;
    },

    selectProgram(program) {
        this.selectedProgram = program;
    },

    setLoadedFilePath(path) {
        this.loadedFilePath = path;
    },

    setRunId(runId) {
        this.runId = runId;
    },

    updateExecutionStatus(status) {
        this.executionStatus = status;
    },

    reset() {
        this.username = null;
        this.credits = 0;
        this.selectedUser = null;
        this.selectedProgram = null;
        this.runId = null;
        this.executionStatus = 'idle';
    },

    setDegreeInfo(currentDegree, minDegree, maxDegree) {
        this.currentDegree = currentDegree;
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
        console.log(`[State] Degrees updated: current=${currentDegree}, min=${minDegree}, max=${maxDegree}`);
    }
};