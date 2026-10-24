/**
 * Chat Application JavaScript
 * Handles dark/light theme switching, interacting with the /chat endpoint,
 * UI animations, quick prompts, copy-to-clipboard, and markdown formatting.
 */

document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const chatContainer = document.getElementById('chatContainer');
    const messagesList = document.getElementById('messagesList');
    const welcomeScreen = document.getElementById('welcomeScreen');
    const chatInput = document.getElementById('chatInput');
    const sendButton = document.getElementById('sendButton');
    const clearChatButton = document.getElementById('clearChatButton');
    const themeToggleButton = document.getElementById('themeToggleButton');
    const themeIconLight = document.getElementById('themeIconLight');
    const themeIconDark = document.getElementById('themeIconDark');
    const quickPrompts = document.querySelectorAll('.quick-prompt-btn');

    let isWaitingForResponse = false;
    const conversationId = (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : String(Date.now());
    // --- 1. Theme Management ---
    function initTheme() {
        const storedTheme = localStorage.getItem('theme');
        const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

        if (storedTheme === 'dark' || (!storedTheme && systemPrefersDark)) {
            document.documentElement.classList.add('dark');
            showMoonIcon(true);
        } else {
            document.documentElement.classList.remove('dark');
            showMoonIcon(false);
        }
    }

    function toggleTheme() {
        const isDark = document.documentElement.classList.toggle('dark');
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
        showMoonIcon(isDark);
    }

    function showMoonIcon(isDark) {
        if (themeIconLight && themeIconDark) {
            if (isDark) {
                // In dark mode, show sun icon to allow switching to light mode
                themeIconLight.classList.remove('hidden');
                themeIconDark.classList.add('hidden');
            } else {
                // In light mode, show moon icon to allow switching to dark mode
                themeIconLight.classList.add('hidden');
                themeIconDark.classList.remove('hidden');
            }
        }
    }

    if (themeToggleButton) {
        themeToggleButton.addEventListener('click', toggleTheme);
    }

    initTheme();

    // --- 2. Auto-expanding Textarea & Keyboard Shortcuts ---
    function autoResizeTextarea() {
        chatInput.style.height = 'auto';
        const newHeight = Math.min(Math.max(chatInput.scrollHeight, 44), 160);
        chatInput.style.height = newHeight + 'px';
    }

    chatInput.addEventListener('input', () => {
        autoResizeTextarea();
        updateSendButtonState();
    });

    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (!isWaitingForResponse && chatInput.value.trim().length > 0) {
                handleSendMessage();
            }
        }
    });

    function updateSendButtonState() {
        const hasText = chatInput.value.trim().length > 0;
        sendButton.disabled = !hasText || isWaitingForResponse;
        if (hasText && !isWaitingForResponse) {
            sendButton.classList.remove('opacity-50', 'cursor-not-allowed');
            sendButton.classList.add('hover:opacity-90', 'active:scale-95');
        } else {
            sendButton.classList.add('opacity-50', 'cursor-not-allowed');
            sendButton.classList.remove('hover:opacity-90', 'active:scale-95');
        }
    }

    // --- 3. Time Formatter Helper ---
    function getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // --- 4. Simple Safe Markdown Formatter ---
    function formatMessageContent(rawText) {
        if (!rawText) return '';

        // Escape HTML to prevent XSS
        let safe = rawText
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');

        // Code blocks: ```language\ncode\n```
        safe = safe.replace(/```(?:[a-zA-Z0-9_-]+)?\n([\s\S]*?)```/g, (match, code) => {
            return `<pre><code>${code.trim()}</code></pre>`;
        });

        // Inline code: `code`
        safe = safe.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

        // Bold: **text**
        safe = safe.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

        // Italic: *text* or _text_
        safe = safe.replace(/(?:^|[^*])\*([^*]+)\*(?:[^*]|$)/g, ' <em>$1</em> ');

        // Unordered lists: line starting with - or *
        const lines = safe.split('\n');
        let formattedLines = [];
        let inList = false;

        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            const trimmed = line.trim();
            if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
                if (!inList) {
                    formattedLines.push('<ul>');
                    inList = true;
                }
                formattedLines.push(`<li>${trimmed.substring(2)}</li>`);
            } else if (trimmed.match(/^\d+\.\s+/)) {
                if (!inList) {
                    formattedLines.push('<ol>');
                    inList = true;
                }
                formattedLines.push(`<li>${trimmed.replace(/^\d+\.\s+/, '')}</li>`);
            } else {
                if (inList) {
                    formattedLines.push(formattedLines[formattedLines.length - 1].startsWith('<ol') ? '</ol>' : '</ul>');
                    inList = false;
                }
                formattedLines.push(line);
            }
        }
        if (inList) {
            formattedLines.push('</ul>');
        }

        let result = formattedLines.join('\n');

        // Convert line breaks to <p> or <br>
        const paragraphs = result.split(/\n\s*\n/).filter(p => p.trim().length > 0);
        if (paragraphs.length > 1) {
            return paragraphs.map(p => {
                if (p.startsWith('<pre>') || p.startsWith('<ul>') || p.startsWith('<ol>')) {
                    return p;
                }
                return `<p>${p.replace(/\n/g, '<br>')}</p>`;
            }).join('');
        } else {
            return `<p>${result.replace(/\n/g, '<br>')}</p>`;
        }
    }

    // --- 5. Message Rendering ---
    function scrollToBottom() {
        chatContainer.scrollTo({
            top: chatContainer.scrollHeight,
            behavior: 'smooth'
        });
    }

    function hideWelcomeScreen() {
        if (welcomeScreen && !welcomeScreen.classList.contains('hidden')) {
            welcomeScreen.classList.add('hidden');
        }
    }

    function appendUserMessage(text) {
        hideWelcomeScreen();
        const timeStr = getCurrentTime();

        const messageEl = document.createElement('div');
        messageEl.className = 'flex items-end justify-end space-x-2 space-x-reverse mb-5 animate-fade-in';
        messageEl.innerHTML = `
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-zinc-700 dark:bg-zinc-600 flex items-center justify-center text-white text-xs font-medium shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                </svg>
            </div>
            <div class="flex flex-col items-end max-w-[82%] sm:max-w-[70%]">
                <div class="bg-zinc-900 text-white dark:bg-indigo-600 dark:text-white px-4 py-2.5 rounded-2xl rounded-br-sm shadow-sm prose-chat text-sm sm:text-base leading-relaxed">
                    <p class="whitespace-pre-wrap">${escapeHtml(text)}</p>
                </div>
                <span class="text-[11px] text-zinc-400 dark:text-zinc-500 mt-1 mr-1">${timeStr}</span>
            </div>
        `;

        messagesList.appendChild(messageEl);
        scrollToBottom();
    }

    function appendAssistantMessage(text) {
        hideWelcomeScreen();
        const timeStr = getCurrentTime();
        const formattedHtml = formatMessageContent(text);

        const messageEl = document.createElement('div');
        messageEl.className = 'flex items-start space-x-3 mb-6 animate-fade-in group';
        messageEl.innerHTML = `
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-tr from-indigo-500 to-violet-600 flex items-center justify-center text-white shadow-sm ai-glow">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
            </div>
            <div class="flex flex-col max-w-[85%] sm:max-w-[78%]">
                <div class="bg-white dark:bg-zinc-800/90 text-zinc-800 dark:text-zinc-100 px-4 py-3 rounded-2xl rounded-tl-sm border border-zinc-200/80 dark:border-zinc-700/60 shadow-sm prose-chat text-sm sm:text-base">
                    ${formattedHtml}
                </div>
                <div class="flex items-center space-x-3 mt-1.5 ml-1">
                    <span class="text-[11px] text-zinc-400 dark:text-zinc-500">${timeStr}</span>
                    <button class="copy-btn opacity-0 group-hover:opacity-100 transition-opacity duration-200 text-xs text-zinc-400 hover:text-zinc-600 dark:text-zinc-500 dark:hover:text-zinc-300 flex items-center space-x-1" title="Copy message">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                        </svg>
                        <span class="copy-label">Copy</span>
                    </button>
                </div>
            </div>
        `;

        // Bind copy functionality
        const copyBtn = messageEl.querySelector('.copy-btn');
        const copyLabel = messageEl.querySelector('.copy-label');
        copyBtn.addEventListener('click', () => {
            copyTextToClipboard(text, copyLabel);
        });

        messagesList.appendChild(messageEl);
        scrollToBottom();
    }

    function showTypingIndicator() {
        const indicatorEl = document.createElement('div');
        indicatorEl.id = 'typingIndicator';
        indicatorEl.className = 'flex items-start space-x-3 mb-6 animate-fade-in';
        indicatorEl.innerHTML = `
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-tr from-indigo-500 to-violet-600 flex items-center justify-center text-white shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
            </div>
            <div class="bg-white dark:bg-zinc-800/90 border border-zinc-200/80 dark:border-zinc-700/60 px-4 py-3 rounded-2xl rounded-tl-sm shadow-sm flex items-center space-x-1.5">
                <span class="w-2 h-2 rounded-full bg-indigo-500 dark:bg-indigo-400 typing-dot"></span>
                <span class="w-2 h-2 rounded-full bg-indigo-500 dark:bg-indigo-400 typing-dot"></span>
                <span class="w-2 h-2 rounded-full bg-indigo-500 dark:bg-indigo-400 typing-dot"></span>
                <span class="text-xs text-zinc-400 dark:text-zinc-500 ml-2 font-medium">Assistant is thinking...</span>
            </div>
        `;
        messagesList.appendChild(indicatorEl);
        scrollToBottom();
    }

    function removeTypingIndicator() {
        const indicator = document.getElementById('typingIndicator');
        if (indicator) {
            indicator.remove();
        }
    }

    function appendErrorMessage(errorText, retryQuery) {
        hideWelcomeScreen();
        const errorEl = document.createElement('div');
        errorEl.className = 'flex items-start space-x-3 mb-6 animate-fade-in';
        errorEl.innerHTML = `
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-rose-500/20 text-rose-600 dark:text-rose-400 flex items-center justify-center shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
            </div>
            <div class="flex flex-col max-w-[85%] sm:max-w-[75%]">
                <div class="bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200 border border-rose-200 dark:border-rose-900/60 px-4 py-3 rounded-2xl rounded-tl-sm text-sm">
                    <p class="font-medium">Failed to get response</p>
                    <p class="text-xs mt-1 text-rose-700/80 dark:text-rose-300/80">${escapeHtml(errorText)}</p>
                    ${retryQuery ? `<button class="retry-btn mt-2 inline-flex items-center text-xs font-semibold text-rose-700 dark:text-rose-300 hover:underline">
                        <svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        Retry
                    </button>` : ''}
                </div>
            </div>
        `;

        if (retryQuery) {
            const retryBtn = errorEl.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', () => {
                    errorEl.remove();
                    executeChat(retryQuery);
                });
            }
        }

        messagesList.appendChild(errorEl);
        scrollToBottom();
    }

    // --- 6. API Interaction (/chat endpoint) ---
    async function handleSendMessage() {
        const text = chatInput.value.trim();
        if (!text || isWaitingForResponse) return;

        chatInput.value = '';
        autoResizeTextarea();
        updateSendButtonState();

        appendUserMessage(text);
        await executeChat(text);
    }

    async function executeChat(text) {
        isWaitingForResponse = true;
        updateSendButtonState();
        showTypingIndicator();

        try {
            // First attempt: POST /chat with JSON body
            let response;
            try {
                response = await fetch('/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'text/plain, application/json, */*'
                    },
                    body: JSON.stringify({ message: text, conversationId })
                });
            } catch (networkErr) {
                // Network error or fetch failed
                throw new Error("Unable to connect to server. Is the application running?");
            }

            // Fallback for servers that only accept GET /chat?message=...
            if (response.status === 405) {
                const getUrl = `/chat?message=${encodeURIComponent(text)}`;
                response = await fetch(getUrl, {
                    method: 'GET',
                    headers: { 'Accept': 'text/plain, application/json, */*' }
                });
            }

            const rawText = await response.text();

            if (!response.ok) {
                throw new Error(rawText || `Server returned error (${response.status})`);
            }

            // Handle reply: could be pure text or JSON e.g. {"response":"..."}
            let replyText = rawText;
            try {
                const parsed = JSON.parse(rawText);
                if (typeof parsed === 'string') {
                    replyText = parsed;
                } else if (parsed && typeof parsed === 'object') {
                    replyText = parsed.response || parsed.message || parsed.content || rawText;
                }
            } catch (e) {
                // Not JSON, use raw text directly
            }

            removeTypingIndicator();
            appendAssistantMessage(replyText);

        } catch (err) {
            removeTypingIndicator();
            appendErrorMessage(err.message || "An unexpected error occurred while chatting.", text);
        } finally {
            isWaitingForResponse = false;
            updateSendButtonState();
            chatInput.focus();
        }
    }

    // --- 7. Event Listeners ---
    if (sendButton) {
        sendButton.addEventListener('click', handleSendMessage);
    }

    // Clear Chat
    if (clearChatButton) {
        clearChatButton.addEventListener('click', () => {
            if (confirm('Are you sure you want to clear the conversation?')) {
                messagesList.innerHTML = '';
                if (welcomeScreen) {
                    welcomeScreen.classList.remove('hidden');
                }
                chatInput.value = '';
                autoResizeTextarea();
                updateSendButtonState();
                chatInput.focus();
            }
        });
    }

    // Quick Prompts
    quickPrompts.forEach(button => {
        button.addEventListener('click', () => {
            const prompt = button.getAttribute('data-prompt');
            if (prompt && !isWaitingForResponse) {
                chatInput.value = prompt;
                handleSendMessage();
            }
        });
    });

    // Helper: Escape HTML
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Helper: Copy to Clipboard
    function copyTextToClipboard(text, labelEl) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(() => {
                showCopiedFeedback(labelEl);
            }).catch(() => {
                fallbackCopy(text, labelEl);
            });
        } else {
            fallbackCopy(text, labelEl);
        }
    }

    function fallbackCopy(text, labelEl) {
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        try {
            document.execCommand('copy');
            showCopiedFeedback(labelEl);
        } catch (e) {
            console.error('Fallback copy failed', e);
        }
        document.body.removeChild(textarea);
    }

    function showCopiedFeedback(labelEl) {
        if (!labelEl) return;
        const originalText = labelEl.textContent;
        labelEl.textContent = 'Copied!';
        labelEl.classList.add('text-emerald-500', 'font-semibold');
        setTimeout(() => {
            labelEl.textContent = originalText;
            labelEl.classList.remove('text-emerald-500', 'font-semibold');
        }, 2000);
    }

    // Initial focus on input
    chatInput.focus();
    updateSendButtonState();
});
