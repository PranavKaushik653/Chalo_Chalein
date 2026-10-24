/**
 * Chalo Travel Agent widget.
 * - Floating animated button -> opens a chat panel.
 * - Before every message it sends the trips currently in State.trips to the backend,
 *   so the agent answers from exactly the trips on the website (including newly hosted ones
 *   and updated seat counts).
 * - Replies containing [[trip-id]] become "View & Lock" buttons that open the trip modal.
 *
 * Backend URL: set window.CHALO_AGENT_API before this script to change it.
 */
(function () {
  "use strict";

  var API_BASE = window.CHALO_AGENT_API || "http://localhost:8081";
  var CHAT_URL = API_BASE + "/api/agent/chat";
  var SYNC_URL = API_BASE + "/api/agent/trips/sync";
  var WELCOME =
    "Hi! I'm your Chalo Chalein travel agent. Tell me your budget, dates, group size and the kind of " +
    "trip you want, and I'll find the right batch from our live trips.";

  var root, panel, msgsEl, inputEl, sendBtn, countEl;
  var busy = false;
  var welcomed = false;

  var conversationId = sessionStorage.getItem("chalo_agent_cid");
  if (!conversationId) {
    conversationId = (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : String(Date.now());
    sessionStorage.setItem("chalo_agent_cid", conversationId);
  }

  // ---------- helpers ----------
  function getTrips() {
    return (typeof State !== "undefined" && State && Array.isArray(State.trips)) ? State.trips : [];
  }

  function escapeHtml(s) {
    return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }

  function formatBot(text) {
    var known = {};
    getTrips().forEach(function (t) { known[t.id] = true; });
    var html = escapeHtml(text);
    html = html.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
    html = html.replace(/^\s*[-*]\s+/gm, "\u2022 ");
    html = html.replace(/\[\[\s*([A-Za-z0-9_-]+)\s*\]\]/g, function (m, id) {
      return known[id]
        ? '<br><button type="button" class="ta-trip-btn" data-trip-id="' + id + '">View &amp; Lock \u2192</button>'
        : "";
    });
    return html.replace(/\n/g, "<br>");
  }

  function fetchWithTimeout(url, options, ms) {
    var controller = new AbortController();
    var timer = setTimeout(function () { controller.abort(); }, ms);
    options.signal = controller.signal;
    return fetch(url, options).finally(function () { clearTimeout(timer); });
  }

  function planeSvg() {
    return '' +
      '<svg class="ta-svg" viewBox="0 0 64 64" aria-hidden="true">' +
      '<circle cx="32" cy="32" r="22" fill="none" stroke="rgba(255,255,255,.45)" stroke-width="1.5" stroke-dasharray="3 4"/>' +
      '<path class="ta-pin" d="M32 20c-4.4 0-8 3.4-8 7.7 0 5.6 8 14.3 8 14.3s8-8.7 8-14.3C40 23.4 36.4 20 32 20zm0 10.4a2.7 2.7 0 1 1 0-5.4 2.7 2.7 0 0 1 0 5.4z" fill="#fff"/>' +
      '<g class="ta-orbit"><polygon points="8,0 -6,-5 -3,0 -6,5" transform="translate(32 10)" fill="#fff"/></g>' +
      '</svg>';
  }

  // ---------- build the widget ----------
  function mount() {
    root = document.createElement("div");
    root.className = "ta-root";
    root.innerHTML =
      '<div class="ta-fab-label" id="taLabel"><span>Travel Agent</span><span class="ta-ai-tag">AI</span></div>' +
      '<button type="button" class="ta-fab" id="taFab" aria-label="Open Travel Agent">' + planeSvg() + '</button>' +
      '<section class="ta-panel" id="taPanel" role="dialog" aria-label="Chalo Travel Agent">' +
        '<header class="ta-head">' +
          '<div class="ta-head-icon" style="background:var(--color-terracotta,#c06048)">' + planeSvg() + '</div>' +
          '<div class="ta-head-text">' +
            '<div class="ta-head-title">Chalo Travel Agent</div>' +
            '<div class="ta-head-sub">Searching <span id="taCount">0</span> live trips</div>' +
          '</div>' +
          '<button type="button" class="ta-close" id="taClose" aria-label="Close">&times;</button>' +
        '</header>' +
        '<div class="ta-msgs" id="taMsgs"></div>' +
        '<div class="ta-inputrow">' +
          '<input class="ta-input" id="taInput" type="text" maxlength="500" placeholder="e.g. 2 of us, under \u20b98,000, trekking in November" autocomplete="off">' +
          '<button type="button" class="ta-send" id="taSend" aria-label="Send">\u27a4</button>' +
        '</div>' +
      '</section>';
    document.body.appendChild(root);

    panel = root.querySelector("#taPanel");
    msgsEl = root.querySelector("#taMsgs");
    inputEl = root.querySelector("#taInput");
    sendBtn = root.querySelector("#taSend");
    countEl = root.querySelector("#taCount");

    root.querySelector("#taFab").addEventListener("click", open);
    root.querySelector("#taLabel").addEventListener("click", open);
    root.querySelector("#taClose").addEventListener("click", close);
    sendBtn.addEventListener("click", send);
    inputEl.addEventListener("keydown", function (e) { if (e.key === "Enter") send(); });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && root.classList.contains("ta-open")) close();
    });

    // "View & Lock" buttons inside bot replies open the site's own trip modal
    msgsEl.addEventListener("click", function (e) {
      var btn = e.target.closest(".ta-trip-btn");
      if (!btn) return;
      close();
      if (typeof Modals !== "undefined") Modals.openTripDetailModal(btn.getAttribute("data-trip-id"));
    });

    // Warm up the backend and fill its trip table in the background
    setTimeout(function () { syncTrips().catch(function () {}); }, 800);
  }

  function open() {
    root.classList.add("ta-open");
    countEl.textContent = getTrips().length;
    if (!welcomed) {
      welcomed = true;
      addMessage("bot", escapeHtml(WELCOME));
    }
    setTimeout(function () { inputEl.focus(); }, 250);
  }

  function close() {
    root.classList.remove("ta-open");
  }

  function addMessage(kind, html) {
    var d = document.createElement("div");
    d.className = "ta-msg ta-" + kind;
    d.innerHTML = html;
    msgsEl.appendChild(d);
    msgsEl.scrollTop = msgsEl.scrollHeight;
    return d;
  }

  // ---------- talking to the backend ----------
  function syncTrips() {
    return fetchWithTimeout(SYNC_URL, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(getTrips())
    }, 15000).then(function (res) {
      if (!res.ok) throw new Error("sync failed: " + res.status);
    });
  }

  async function send() {
    var text = inputEl.value.trim();
    if (!text || busy) return;
    busy = true;
    sendBtn.disabled = true;
    inputEl.value = "";

    addMessage("user", escapeHtml(text));
    var typing = addMessage("bot ta-typing", "<span></span><span></span><span></span>");

    try {
      await syncTrips(); // agent sees the trips exactly as they are on screen right now
      var res = await fetchWithTimeout(CHAT_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text, conversationId: conversationId })
      }, 90000);
      var data = {};
      try { data = await res.json(); } catch (e) { /* non-JSON error body */ }

      if (res.ok) {
        addMessage("bot", formatBot(data.reply || "I couldn't find an answer. Could you rephrase that?"));
      } else {
        addMessage("bot", escapeHtml(data.error || "Something went wrong. Please try again."));
      }
    } catch (e) {
      addMessage("bot", escapeHtml(
        "I can't reach the travel agent service right now. Please make sure the backend is running at " + API_BASE + "."));
    } finally {
      typing.remove();
      busy = false;
      sendBtn.disabled = false;
      inputEl.focus();
    }
  }

  window.ChaloAgent = { open: function () { open(); }, close: function () { close(); } };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mount);
  } else {
    mount();
  }
})();