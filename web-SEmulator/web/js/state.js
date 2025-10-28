// state.js - Global application state

const state = {
    currentUser: null,
    credits: 0,
    selectedProgram: null,
    selectedFunction: null,
    execution: {
        isRunning: false,
        isDebugging: false,
        selectedProgram: null,
        selectedFunction: null,
        currentStep: 0,
        cycles: 0
    },
    programs: [],
    users: []
};

export { state };