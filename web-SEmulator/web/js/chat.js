let currentMessageCount = 0;
let currentUsername = null;
let pollInterval = null;

// Initialize chat when page loads
function initChat() {
    // Get current username from session (implement based on your session management)
    getCurrentUsername();

    // Load initial messages
    loadMessages();

    // Start polling for new messages every 2 seconds
    pollInterval = setInterval(pollNewMessages, 2000);

    // Add enter key listener
    document.getElementById('chatInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

// Get current logged-in username
function getCurrentUsername() {
    // This should be set from your login system
    // For example, you might store it in sessionStorage or get it from a global variable
    currentUsername = sessionStorage.getItem('username') || 'Anonymous';
}

// Send a new message
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();

    if (!message) {
        return;
    }

    const sendButton = document.getElementById('sendButton');
    sendButton.disabled = true;

    try {
        const response = await fetch('/api/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `message=${encodeURIComponent(message)}`
        });

        const data = await response.json();

        if (data.success) {
            input.value = '';
            // Immediately poll for new messages
            pollNewMessages();
        } else {
            alert('Failed to send message: ' + data.message);
        }
    } catch (error) {
        console.error('Error sending message:', error);
        alert('Error sending message');
    } finally {
        sendButton.disabled = false;
        input.focus();
    }
}

// Load all messages (initial load)
async function loadMessages() {
    try {
        const response = await fetch('/api/chat/messages');
        const data = await response.json();

        if (data.success) {
            currentMessageCount = data.totalCount;
            displayMessages(data.messages);
        } else if (response.status === 401) {
            // User not logged in
            window.location.href = '/login.html';
        }
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

// Poll for new messages
async function pollNewMessages() {
    try {
        const response = await fetch(`/api/chat/messages?fromIndex=${currentMessageCount}`);
        const data = await response.json();

        if (data.success && data.messages.length > 0) {
            currentMessageCount = data.totalCount;
            displayMessages(data.messages, true);
        }
    } catch (error) {
        console.error('Error polling messages:', error);
    }
}

// Display messages in the chat window
function displayMessages(messages, append = false) {
    const chatMessages = document.getElementById('chatMessages');

    if (!append) {
        chatMessages.innerHTML = '';
    }

    messages.forEach(msg => {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';

        if (msg.username === currentUsername) {
            messageDiv.classList.add('own');
        }

        messageDiv.innerHTML = `
            <div class="chat-message-header">
                <span class="chat-username">${escapeHtml(msg.username)}</span>
                <span class="chat-timestamp">${msg.timestamp}</span>
            </div>
            <div class="chat-message-text">${escapeHtml(msg.message)}</div>
        `;

        chatMessages.appendChild(messageDiv);
    });

    // Auto-scroll to bottom
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Clean up when leaving page
window.addEventListener('beforeunload', function() {
    if (pollInterval) {
        clearInterval(pollInterval);
    }
});

// Initialize chat on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initChat);
} else {
    initChat();
}