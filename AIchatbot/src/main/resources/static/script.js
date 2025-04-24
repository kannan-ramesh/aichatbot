<script>
document.getElementById('chat-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const input = document.getElementById('user-input');
    const userMessage = input.value.trim();
    if (!userMessage) return;

    const chatMessages = document.getElementById('chat-messages');

    // Append user's message
    chatMessages.innerHTML += `
        <div class="message user-message">
            <p>${userMessage}</p>
        </div>
    `;

    // Show typing...
    const typingDiv = document.createElement('div');
    typingDiv.className = "message bot-message";
    typingDiv.id = "typing";
    typingDiv.innerHTML = "<p>Typing...</p>";
    chatMessages.appendChild(typingDiv);

    // Send AJAX request
    fetch('/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({ reqChat: userMessage })
    })
    .then(response => response.text())
    .then(html => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        const newAiMessage = doc.querySelector('.bot-message');
        document.getElementById('typing').replaceWith(newAiMessage);

        input.value = ''; // clear input
    })
    .catch(error => {
        document.getElementById('typing').innerHTML = "<p>Something went wrong.</p>";
    });
});
</script>
