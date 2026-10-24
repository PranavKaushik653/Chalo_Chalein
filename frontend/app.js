/**
 * CHALO CHALEIN - Core Application JavaScript
 * Two-sided marketplace: Travellers & Tour Organisers
 * Features: Escrow Seat Locks, Search & Multi-Filters, Role Switcher,
 *           Interactive Booking State Machine, Dashboards, and Trip Hosting.
 */

// ==========================================
// 1. INITIAL MOCK DATA (PERSISTED IN STORAGE)
// ==========================================

const INITIAL_TRIPS = [
  {
    id: "trip-spiti-winter",
    title: "Spiti Valley Winter Expedition: Frozen Waterfalls & Key Monastery",
    destination: "Spiti Valley, Himachal Pradesh",
    category: "High Altitude Trek",
    duration: "8 Days • 7 Nights",
    startDate: "2026-11-10",
    endDate: "2026-11-17",
    totalSeats: 14,
    availableSeats: 4,
    tokenAmount: 1999,
    totalPrice: 18999,
    organizer: {
      id: "org-himalayan-nomads",
      name: "Himalayan Nomads",
      verified: true,
      rating: 4.9,
      tripsDone: 42,
      instagram: "@himalayan_nomads"
    },
    image: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
    description: "Conquer white winter in the middle land. Drive in 4x4 rugged Scorpios through Kaza, visit the 1000-year-old Key Monastery, frozen Dhankar lake, and experience homestays heated by traditional Bukhari.",
    itinerary: [
      { day: "Day 1", desc: "Shimla to Kalpa: Drive along the Sutlej river with stunning views of Kinnaur Kailash." },
      { day: "Day 2", desc: "Kalpa to Kaza: Crossing Nako frozen lake and the famous Gue Mummy village." },
      { day: "Day 3", desc: "Key Monastery & Kibber: Wildlife spot snow leopards, Himalayan blue sheep, and highest cafe." },
      { day: "Day 4", desc: "Hikkim & Langza: Send postcards from the World's Highest Post Office and hunt marine fossils." },
      { day: "Day 5-8", desc: "Chicham Bridge, frozen waterfalls crossing and return drive to Chandigarh." }
    ],
    inclusions: ["4x4 Winter Vehicle & Expert Snow Pilot", "Heated Village Homestays with Bukharis", "All Meals (Breakfast & Dinner)", "Oxygen Cylinders & First Aid Box", "State Permits & Escrow Guarantee"]
  },
  {
    id: "trip-meghalaya-roots",
    title: "Living Root Bridges & Dawki Camping: Meghalaya Backpacking",
    destination: "Meghalaya (Cherrapunji & Shnongpdeng)",
    category: "Backpacking & Culture",
    duration: "6 Days • 5 Nights",
    startDate: "2026-11-15",
    endDate: "2026-11-20",
    totalSeats: 16,
    availableSeats: 7,
    tokenAmount: 1499,
    totalPrice: 12499,
    organizer: {
      id: "org-east-explorers",
      name: "East Explorers",
      verified: true,
      rating: 4.8,
      tripsDone: 29,
      instagram: "@eastexplorers_in"
    },
    image: "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=800&q=80",
    description: "Trek to the Double Decker Living Root Bridge, cliff jump into crystal clear Umngot river at Dawki, and explore secret limestone caves in the wettest place on earth.",
    itinerary: [
      { day: "Day 1", desc: "Guwahati to Shillong: Stop at Umiam Lake and explore Police Bazaar cafe culture." },
      { day: "Day 2", desc: "Sohra & Wei Sawdong: Three-tier waterfall hike and Nohkalikai falls viewpoint." },
      { day: "Day 3", desc: "Nongriat Trek: Descend 3,500 stairs to the ancient Double Decker Root Bridge and Blue Lagoon." },
      { day: "Day 4", desc: "Shnongpdeng River Camping: Kayaking on transparent waters, bonfire by riverside." },
      { day: "Day 5-6", desc: "Krang Shuri blue plunge and scenic transfer back to Guwahati airport." }
    ],
    inclusions: ["Cozy Bed & Breakfasts & Riverside Tents", "All Internal Transfers in Tempo Traveller", "Local Khasi Guides for Nongriat", "Kayaking & Snorkelling Gear", "Escrow Protected Booking"]
  },
  {
    id: "trip-kasol-kheerganga",
    title: "Kasol, Kheerganga & Tosh: Parvati Valley Stargazing",
    destination: "Parvati Valley, Himachal Pradesh",
    category: "High Altitude Trek",
    duration: "4 Days • 3 Nights",
    startDate: "2026-11-05",
    endDate: "2026-11-08",
    totalSeats: 18,
    availableSeats: 2,
    tokenAmount: 999,
    totalPrice: 6999,
    organizer: {
      id: "org-wanderon",
      name: "WanderOn Community",
      verified: true,
      rating: 4.9,
      tripsDone: 110,
      instagram: "@wanderon.official"
    },
    image: "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
    description: "Relax in hippie cafes, trek through pine forests up to Kheerganga natural hot sulfur springs, sleep under billion stars in mountain camps, and chill in scenic Tosh village.",
    itinerary: [
      { day: "Day 1", desc: "Arrive in Kasol: Cafe hopping, Chalal riverside walk, and community ice-breaking games." },
      { day: "Day 2", desc: "Barshaini to Kheerganga: 12km pine forest trek, sunset dip in natural hot water springs." },
      { day: "Day 3", desc: "Sunrise over Himalayas, descend to Tosh village for cafe sunset and acoustic music night." },
      { day: "Day 4", desc: "Manikaran Sahib Gurudwara visit and evening departure to Delhi." }
    ],
    inclusions: ["Delhi to Kasol Volvo Bus (AC)", "Dome Tents & Homestay in Tosh", "Guided Trek & Campfire Nights", "Breakfast & Dinner at Camps", "100% Escrow Protection"]
  },
  {
    id: "trip-kerala-backwaters",
    title: "Kerala Coastline & Varkala Cliff: Surf, Kayak & Backwaters",
    destination: "Varkala & Alleppey, Kerala",
    category: "Beach & Chill",
    duration: "5 Days • 4 Nights",
    startDate: "2026-11-22",
    endDate: "2026-11-26",
    totalSeats: 12,
    availableSeats: 6,
    tokenAmount: 1500,
    totalPrice: 14500,
    organizer: {
      id: "org-coastal-nomads",
      name: "Coastal Nomads",
      verified: true,
      rating: 4.85,
      tripsDone: 34,
      instagram: "@coastalnomads"
    },
    image: "https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?auto=format&fit=crop&w=800&q=80",
    description: "Perch upon the red laterite cliffs of Varkala overlooking the Arabian Sea. Learn beginner surfing, enjoy Ayurvedic cafes, and canoe through serene Alleppey backwater canals.",
    itinerary: [
      { day: "Day 1", desc: "Kochi to Varkala: Sunset check-in at North Cliff boutique hostel & beach walk." },
      { day: "Day 2", desc: "Surf lessons at Black Sand Beach & evening cliff cafe acoustic jamming." },
      { day: "Day 3", desc: "Munroe Island mangrove kayaking and local Kerala fish curry feast." },
      { day: "Day 4", desc: "Alleppey village canoes through narrow canals and houseboat sunset dinner." },
      { day: "Day 5", desc: "Fort Kochi colonial street art walk and Kochi airport departure." }
    ],
    inclusions: ["Cliffside Boutique Hostel Stay", "Surf Lesson & Equipment Rental", "Private Shikkara Boat Cruise", "All Internal AC Van Transfers", "Escrow Guarantee Included"]
  },
  {
    id: "trip-pushkar-desert",
    title: "Pushkar Desert Caravans & Royal Stays: Rajasthan Vibe",
    destination: "Pushkar & Jaipur, Rajasthan",
    category: "Weekend Getaway",
    duration: "3 Days • 2 Nights",
    startDate: "2026-11-07",
    endDate: "2026-11-09",
    totalSeats: 20,
    availableSeats: 9,
    tokenAmount: 899,
    totalPrice: 5499,
    organizer: {
      id: "org-deshvidesh",
      name: "Desh Videsh Travels",
      verified: true,
      rating: 4.75,
      tripsDone: 52,
      instagram: "@deshvidesh.in"
    },
    image: "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=800&q=80",
    description: "Camel safaris, desert folk dance around a roaring bonfire, holy dip at Pushkar Sarovar ghats, Savitri Temple cable car sunset, and vibrant street shopping.",
    itinerary: [
      { day: "Day 1", desc: "Delhi to Pushkar drive: Check-in to desert luxury Swiss tents, camel safari at sunset." },
      { day: "Day 2", desc: "Pushkar Lake Ghats, Brahma temple, sunset cafe overlooking Thar desert & Kalbelia dance." },
      { day: "Day 3", desc: "Ajmer Sharif Dargah visit, Jaipur food walk, and return drive to Delhi NCR." }
    ],
    inclusions: ["AC Tempo Traveller from Delhi", "Desert Swiss Tents with Attached Washroom", "Traditional Rajasthani Buffet Dinners", "Camel Safari & Folk Dance Night", "Token Seat Lock Guarantee"]
  },
  {
    id: "trip-ladakh-overland",
    title: "Ladakh High Pass 4x4 Overland: Pangong Lake & Nubra Valley",
    destination: "Leh Ladakh, UT",
    category: "Road Trip",
    duration: "9 Days • 8 Nights",
    startDate: "2026-12-01",
    endDate: "2026-12-09",
    totalSeats: 10,
    availableSeats: 3,
    tokenAmount: 2999,
    totalPrice: 28999,
    organizer: {
      id: "org-leh-riders",
      name: "Leh Riders & Treks",
      verified: true,
      rating: 4.95,
      tripsDone: 75,
      instagram: "@lehriders"
    },
    image: "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=800&q=80",
    description: "Cross Khardung La (World's highest motorable pass), ride double-humped camels in Hunder sand dunes, and camp by the turquoise shores of Pangong Tso.",
    itinerary: [
      { day: "Day 1-2", desc: "Fly into Leh: Mandatory acclimatization day, Shanti Stupa sunset and Leh market." },
      { day: "Day 3", desc: "Leh to Nubra Valley via Khardung La (17,582 ft), Diskit Monastery giant Buddha." },
      { day: "Day 4", desc: "Hunder sand dunes camel ride and drive to Turtuk (Balti border village)." },
      { day: "Day 5-6", desc: "Nubra to Pangong Tso via Shyok river route. Stargazing at Pangong camp." },
      { day: "Day 7-9", desc: "Chang La pass crossing, Hemis Monastery, and final departure from Leh." }
    ],
    inclusions: ["Dedicated 4x4 Isuzu/Innova with Oxygen Support", "Deluxe Camps & Hotels in Leh", "All Inner Line Permits & Wildlife Fees", "Experienced Ladakhi Expedition Leader", "100% Escrow Escort"]
  }
];

const INITIAL_BOOKINGS = [
  {
    id: "BK-2026-891",
    tripId: "trip-spiti-winter",
    tripTitle: "Spiti Valley Winter Expedition: Frozen Waterfalls & Key Monastery",
    destination: "Spiti Valley, Himachal Pradesh",
    dates: "10 Nov – 17 Nov 2026",
    seatsBooked: 1,
    tokenAmount: 1999,
    totalPrice: 18999,
    bookingStatus: "TOKEN_PAID", // PRD state
    payoutStatus: "HELD_IN_ESCROW",
    bookedAt: "2026-10-21T14:30:00Z",
    image: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80",
    organizerName: "Himalayan Nomads"
  },
  {
    id: "BK-2026-642",
    tripId: "trip-kasol-kheerganga",
    tripTitle: "Kasol, Kheerganga & Tosh: Parvati Valley Stargazing",
    destination: "Parvati Valley, Himachal Pradesh",
    dates: "05 Nov – 08 Nov 2026",
    seatsBooked: 1,
    tokenAmount: 999,
    totalPrice: 6999,
    bookingStatus: "CONFIRMED", // PRD state: fully paid
    payoutStatus: "HELD_IN_ESCROW",
    bookedAt: "2026-10-14T09:15:00Z",
    image: "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=400&q=80",
    organizerName: "WanderOn Community"
  }
];

// Pre-seeded attendees for organisers
const INITIAL_ATTENDEES = {
  "trip-spiti-winter": [
    { name: "Aarav Sharma (You)", phone: "+91 98765 43210", email: "aarav.sharma@travelmail.com", status: "TOKEN_PAID", token: 1999, emergencyContact: "+91 98111 22334" },
    { name: "Ananya Iyer", phone: "+91 94451 88921", email: "ananya.iyer@gmail.com", status: "CONFIRMED", token: 1999, emergencyContact: "+91 94451 11223" },
    { name: "Rohan Varma", phone: "+91 98200 44512", email: "rohan.v@outlook.com", status: "CONFIRMED", token: 1999, emergencyContact: "+91 98200 99887" },
    { name: "Meera Sen", phone: "+91 97114 67329", email: "meera.sen@gmail.com", status: "TOKEN_PAID", token: 1999, emergencyContact: "+91 97114 00112" }
  ],
  "trip-meghalaya-roots": [
    { name: "Pooja Hegde", phone: "+91 99002 33412", email: "pooja.h@yahoo.com", status: "TOKEN_PAID", token: 1499, emergencyContact: "+91 99002 99887" },
    { name: "Kabir Malhotra", phone: "+91 98199 12345", email: "kabir.m@gmail.com", status: "CONFIRMED", token: 1499, emergencyContact: "+91 98199 54321" }
  ]
};

// ==========================================
// 2. STATE CONTROLLER
// ==========================================

const State = {
  currentUser: null,
  activeRole: "traveller", // 'traveller' | 'organiser'
  trips: [],
  bookings: [],
  attendeesMap: {},
  wishlist: [],
  currentFilters: {
    searchQuery: "",
    category: "ALL",
    maxBudget: "ALL",
    sort: "recommended"
  },
  seatLockCountdownInterval: null,

  init() {
    // 1. Load or initialise user
    const savedUser = localStorage.getItem("chalo_user");
    if (savedUser) {
      try {
        this.currentUser = JSON.parse(savedUser);
        this.activeRole = this.currentUser.role || "traveller";
      } catch (e) {
        this.setDefaultUser();
      }
    } else {
      this.setDefaultUser();
    }

    // 2. Load or initialise trips
    const savedTrips = localStorage.getItem("chalo_trips");
    if (savedTrips) {
      try {
        this.trips = JSON.parse(savedTrips);
      } catch (e) {
        this.trips = [...INITIAL_TRIPS];
      }
    } else {
      this.trips = [...INITIAL_TRIPS];
      localStorage.setItem("chalo_trips", JSON.stringify(this.trips));
    }

    // 3. Load or initialise bookings
    const savedBookings = localStorage.getItem("chalo_bookings");
    if (savedBookings) {
      try {
        this.bookings = JSON.parse(savedBookings);
      } catch (e) {
        this.bookings = [...INITIAL_BOOKINGS];
      }
    } else {
      this.bookings = [...INITIAL_BOOKINGS];
      localStorage.setItem("chalo_bookings", JSON.stringify(this.bookings));
    }

    // 4. Load attendees
    const savedAttendees = localStorage.getItem("chalo_attendees");
    if (savedAttendees) {
      try {
        this.attendeesMap = JSON.parse(savedAttendees);
      } catch (e) {
        this.attendeesMap = { ...INITIAL_ATTENDEES };
      }
    } else {
      this.attendeesMap = { ...INITIAL_ATTENDEES };
      localStorage.setItem("chalo_attendees", JSON.stringify(this.attendeesMap));
    }

    // 5. Load wishlist
    const savedWishlist = localStorage.getItem("chalo_wishlist");
    if (savedWishlist) {
      try {
        this.wishlist = JSON.parse(savedWishlist);
      } catch (e) {
        this.wishlist = [];
      }
    }
  },

  setDefaultUser(role = "traveller") {
    if (role === "traveller") {
      this.currentUser = {
        name: "Aarav Sharma",
        email: "aarav.sharma@travelmail.com",
        phone: "+91 98765 43210",
        role: "traveller",
        avatar: "AS",
        chaloCash: 850
      };
      this.activeRole = "traveller";
    } else {
      this.currentUser = {
        name: "Wanderlust India Expeditions",
        email: "host@wanderlustindia.com",
        phone: "+91 99100 88776",
        role: "organiser",
        avatar: "WI",
        instagram: "@wanderlust_india",
        gstin: "07AAAAA0000A1Z5",
        chaloCash: 0
      };
      this.activeRole = "organiser";
    }
    localStorage.setItem("chalo_user", JSON.stringify(this.currentUser));
  },

  saveUser() {
    localStorage.setItem("chalo_user", JSON.stringify(this.currentUser));
  },

  saveTrips() {
    localStorage.setItem("chalo_trips", JSON.stringify(this.trips));
  },

  saveBookings() {
    localStorage.setItem("chalo_bookings", JSON.stringify(this.bookings));
  },

  saveAttendees() {
    localStorage.setItem("chalo_attendees", JSON.stringify(this.attendeesMap));
  },

  saveWishlist() {
    localStorage.setItem("chalo_wishlist", JSON.stringify(this.wishlist));
  }
};

// ==========================================
// 3. UI RENDERING & COMPONENT BUILDERS
// ==========================================

const UI = {
  // Format numbers to Indian Rupee (₹) format
  formatPrice(num) {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0
    }).format(num);
  },

  // Update Top Navigation, Badges, and Profile
  renderHeader() {
    const { currentUser, activeRole } = State;
    const roleBadge = document.getElementById("headerRoleBadge");
    const roleToggleTraveller = document.getElementById("roleToggleTraveller");
    const roleToggleOrganiser = document.getElementById("roleToggleOrganiser");
    const authWrapper = document.getElementById("authWrapper");
    const mobileAuthArea = document.getElementById("mobileAuthArea");

    // Role toggles active state
    if (activeRole === "traveller") {
      roleBadge.textContent = "Traveller";
      roleBadge.style.backgroundColor = "var(--color-sage-light)";
      roleBadge.style.color = "var(--color-sage-dark)";
      roleToggleTraveller.classList.add("active");
      roleToggleOrganiser.classList.remove("active");
    } else {
      roleBadge.textContent = "Organiser";
      roleBadge.style.backgroundColor = "var(--color-terracotta-light)";
      roleBadge.style.color = "var(--color-terracotta-dark)";
      roleToggleOrganiser.classList.add("active");
      roleToggleTraveller.classList.remove("active");
    }

    // Profile or Sign In pill
    const userInitials = currentUser?.avatar || (currentUser?.name ? currentUser.name.substring(0, 2).toUpperCase() : "CC");
    const profileHtml = `
      <div class="user-profile-btn" id="userProfileBtn" title="Click to manage account or switch roles">
        <span class="header-user-avatar">${userInitials}</span>
        <span>${currentUser ? currentUser.name.split(" ")[0] : "Account"}</span>
      </div>
    `;

    authWrapper.innerHTML = profileHtml;
    mobileAuthArea.innerHTML = `
      <div style="padding: 1rem 0; border-top: 1px solid var(--color-sand-border); display: flex; align-items: center; justify-content: space-between;">
        <div>
          <strong>${currentUser ? currentUser.name : "Guest"}</strong>
          <div style="font-size: 0.75rem; color: var(--color-text-muted);">${activeRole === "traveller" ? "Traveller" : "Host Organiser"}</div>
        </div>
        <button class="btn btn-sm btn-outline-earth" id="mobileSwitchAccountBtn">Switch Account</button>
      </div>
    `;

    // Listeners on dynamic elements
    document.getElementById("userProfileBtn")?.addEventListener("click", () => {
      Modals.openAuthModal();
    });
    document.getElementById("mobileSwitchAccountBtn")?.addEventListener("click", () => {
      UI.closeMobileDrawer();
      Modals.openAuthModal();
    });
  },

  // Render Trip Catalog
  renderTrips() {
    const grid = document.getElementById("tripsGrid");
    const emptyState = document.getElementById("tripsEmptyState");
    const resultsCount = document.getElementById("resultsCount");

    // Filter logic
    let filtered = State.trips.filter(trip => {
      // 1. Search text filter
      const q = State.currentFilters.searchQuery.toLowerCase().trim();
      const matchSearch = !q ||
        trip.title.toLowerCase().includes(q) ||
        trip.destination.toLowerCase().includes(q) ||
        trip.organizer.name.toLowerCase().includes(q);

      // 2. Category filter
      const matchCategory = State.currentFilters.category === "ALL" || trip.category === State.currentFilters.category;

      // 3. Budget filter
      let matchBudget = true;
      if (State.currentFilters.maxBudget !== "ALL") {
        matchBudget = trip.totalPrice <= parseInt(State.currentFilters.maxBudget, 10);
      }

      return matchSearch && matchCategory && matchBudget;
    });

    // Sort logic
    if (State.currentFilters.sort === "price-low") {
      filtered.sort((a, b) => a.totalPrice - b.totalPrice);
    } else if (State.currentFilters.sort === "price-high") {
      filtered.sort((a, b) => b.totalPrice - a.totalPrice);
    } else if (State.currentFilters.sort === "date-soon") {
      filtered.sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
    } else if (State.currentFilters.sort === "seats-left") {
      filtered.sort((a, b) => a.availableSeats - b.availableSeats);
    }

    resultsCount.textContent = `Showing ${filtered.length} curated ${filtered.length === 1 ? "batch" : "batches"}`;

    if (filtered.length === 0) {
      grid.innerHTML = "";
      emptyState.style.display = "block";
      return;
    }

    emptyState.style.display = "none";

    grid.innerHTML = filtered.map(trip => {
      const bookedPercentage = Math.round(((trip.totalSeats - trip.availableSeats) / trip.totalSeats) * 100);
      const isFillingFast = trip.availableSeats <= 3;
      const isFavorited = State.wishlist.includes(trip.id);

      return `
        <article class="trip-card" data-trip-id="${trip.id}">
          <div class="trip-card-image-wrap">
            <img src="${trip.image}" alt="${trip.title}" class="trip-card-image" loading="lazy">
            <div class="trip-escrow-badge">
              <span>🛡️</span> Escrow Safe
            </div>
            <button class="trip-fav-btn ${isFavorited ? "favorited" : ""}" data-trip-id="${trip.id}" title="${isFavorited ? "Remove from saved" : "Save to wishlist"}">
              ${isFavorited ? "♥" : "♡"}
            </button>
            <div class="trip-vibe-tag">${trip.category}</div>
            ${isFillingFast ? `<div class="trip-spots-tag">🔥 Only ${trip.availableSeats} left!</div>` : ""}
          </div>

          <div class="trip-card-body">
            <div class="trip-card-organizer">
              <span class="organizer-icon">🏕️</span>
              <span>${trip.organizer.name}</span>
              ${trip.organizer.verified ? '<span class="verified-badge" title="Verified Organizer">✓</span>' : ""}
              <span>• ★ ${trip.organizer.rating}</span>
            </div>

            <h3 class="trip-card-title">${trip.title}</h3>
            
            <div class="trip-card-dest">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
              <span>${trip.destination}</span>
            </div>

            <div class="trip-meta-row">
              <div class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                <span>${trip.duration}</span>
              </div>
              <div class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                <span>Starts ${new Date(trip.startDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })}</span>
              </div>
            </div>

            <div class="seat-progress-container">
              <div class="seat-progress-header">
                <span>Available: <strong>${trip.availableSeats} of ${trip.totalSeats} seats</strong></span>
                <span>${bookedPercentage}% Booked</span>
              </div>
              <div class="seat-bar">
                <div class="seat-fill" style="width: ${bookedPercentage}%"></div>
              </div>
            </div>

            <div class="trip-card-footer">
              <div class="trip-pricing">
                <span class="token-label">Lock Seat for:</span>
                <span class="token-amount">${UI.formatPrice(trip.tokenAmount)}</span>
                <span class="total-amount">Total Trip: ${UI.formatPrice(trip.totalPrice)}</span>
              </div>
              <button class="btn btn-sm btn-primary-earth view-trip-btn" data-trip-id="${trip.id}">
                View &amp; Lock
              </button>
            </div>
          </div>
        </article>
      `;
    }).join("");

    // Attach card click handlers
    grid.querySelectorAll(".trip-card").forEach(card => {
      card.addEventListener("click", (e) => {
        // Prevent modal open when clicking favorite button
        if (e.target.closest(".trip-fav-btn")) return;
        const tripId = card.getAttribute("data-trip-id");
        Modals.openTripDetailModal(tripId);
      });
    });

    // Attach favorite heart button handlers
    grid.querySelectorAll(".trip-fav-btn").forEach(btn => {
      btn.addEventListener("click", (e) => {
        e.stopPropagation();
        const tripId = btn.getAttribute("data-trip-id");
        if (State.wishlist.includes(tripId)) {
          State.wishlist = State.wishlist.filter(id => id !== tripId);
          btn.classList.remove("favorited");
          btn.textContent = "♡";
          UI.showToast("Removed from saved expeditions", "info");
        } else {
          State.wishlist.push(tripId);
          btn.classList.add("favorited");
          btn.textContent = "♥";
          UI.showToast("Saved to your wishlist!", "success");
        }
        State.saveWishlist();
      });
    });
  },

  // Render Dashboards (Traveller & Organiser)
  renderDashboard() {
    const { currentUser, activeRole } = State;

    // Header Card
    document.getElementById("dashUserName").textContent = currentUser.name;
    document.getElementById("dashUserContact").textContent = `${currentUser.email} • WhatsApp: ${currentUser.phone || "+91 98765 43210"}`;
    document.getElementById("dashAvatar").textContent = currentUser.avatar || currentUser.name.substring(0, 2).toUpperCase();

    const roleTag = document.getElementById("dashRoleTag");
    const travellerSubView = document.getElementById("travellerDashboardView");
    const organiserSubView = document.getElementById("organiserDashboardView");
    const btnDashTraveller = document.getElementById("btnDashTraveller");
    const btnDashOrganiser = document.getElementById("btnDashOrganiser");

    if (activeRole === "traveller") {
      roleTag.textContent = "🎒 Traveller";
      roleTag.style.backgroundColor = "var(--color-terracotta-light)";
      roleTag.style.color = "var(--color-terracotta-dark)";
      travellerSubView.style.display = "block";
      organiserSubView.style.display = "none";
      btnDashTraveller.classList.add("active");
      btnDashOrganiser.classList.remove("active");

      // Render Traveller Details
      UI.renderTravellerHub();
    } else {
      roleTag.textContent = "🏕️ Trip Organiser";
      roleTag.style.backgroundColor = "var(--color-sage-light)";
      roleTag.style.color = "var(--color-sage-dark)";
      travellerSubView.style.display = "none";
      organiserSubView.style.display = "block";
      btnDashOrganiser.classList.add("active");
      btnDashTraveller.classList.remove("active");

      // Render Organiser Details
      UI.renderOrganiserHub();
    }
  },

  // Render Traveller Hub (Trips Taken / Bookings)
  renderTravellerHub() {
    const { bookings, currentUser } = State;
    const countEl = document.getElementById("travellerTripsCount");
    const escrowEl = document.getElementById("travellerEscrowLocked");
    const cashEl = document.getElementById("travellerChaloCash");
    const listEl = document.getElementById("travellerBookingsList");

    countEl.textContent = bookings.length;
    cashEl.textContent = UI.formatPrice(currentUser.chaloCash || 850);

    // Calculate escrow total locked
    const totalEscrow = bookings.reduce((sum, b) => sum + (b.tokenAmount * (b.seatsBooked || 1)), 0);
    escrowEl.textContent = UI.formatPrice(totalEscrow);

    if (bookings.length === 0) {
      listEl.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">🎒</div>
          <h3>No booked trips yet</h3>
          <p>Explore verified batches and reserve your seat with safe token escrow!</p>
          <button class="btn btn-primary-earth" onclick="Navigation.switchView('explore')">Browse Marketplace</button>
        </div>
      `;
      return;
    }

    listEl.innerHTML = bookings.map(b => {
      const isTokenPaid = b.bookingStatus === "TOKEN_PAID";
      const statusBadge = isTokenPaid
        ? `<span class="booking-status-badge status-token-paid">🛡️ TOKEN_PAID (Held in Escrow)</span>`
        : `<span class="booking-status-badge status-confirmed">✓ CONFIRMED (Ready for Departure)</span>`;

      const remainingBalance = b.totalPrice - b.tokenAmount;

      return `
        <div class="booking-item-card" data-booking-id="${b.id}">
          <img src="${b.image}" alt="${b.tripTitle}" class="booking-thumb">
          
          <div class="booking-info">
            <div style="margin-bottom: 0.4rem;">${statusBadge}</div>
            <h4>${b.tripTitle}</h4>
            <div class="booking-meta-line">
              <span>📍 ${b.destination}</span>
              <span>📅 ${b.dates}</span>
              <span>🏕️ Organiser: <strong>${b.organizerName || "Verified Host"}</strong></span>
              <span>🎟️ ${b.seatsBooked || 1} Seat</span>
            </div>
            <div style="font-size: 0.82rem; color: var(--color-text-muted);">
              Booking ID: <code>${b.id}</code> &bull; Escrow Protection Status: <span style="color: var(--color-success); font-weight: 700;">100% Guaranteed</span>
            </div>
          </div>

          <div class="booking-actions">
            <div class="booking-escrow-amount">
              Token Paid: <strong>${UI.formatPrice(b.tokenAmount)}</strong><br>
              <small>${isTokenPaid ? `Balance Due: ${UI.formatPrice(remainingBalance)}` : "Fully Paid & Confirmed"}</small>
            </div>

            ${isTokenPaid ? `
              <button class="btn btn-sm btn-primary-earth pay-balance-btn" data-booking-id="${b.id}">
                💳 Pay Balance (${UI.formatPrice(remainingBalance)})
              </button>
            ` : ""}
            
            <button class="btn btn-sm btn-outline-earth download-pass-btn" data-booking-id="${b.id}">
              🎫 Boarding Pass
            </button>
            <button class="btn btn-sm btn-subtle-earth trigger-sos-btn" data-host="${b.organizerName || "Organiser"}">
              🚨 SOS Emergency
            </button>
          </div>
        </div>
      `;
    }).join("");

    // SOS triggers
    listEl.querySelectorAll(".trigger-sos-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const host = btn.getAttribute("data-host");
        Modals.openSosModal(host);
      });
    });

    // Boarding Pass modal
    listEl.querySelectorAll(".download-pass-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-booking-id");
        Modals.openTicketModal(id);
      });
    });

    // Pay Balance modal
    listEl.querySelectorAll(".pay-balance-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-booking-id");
        Modals.openBalanceModal(id);
      });
    });
  },

  // Render Organiser Hub (Batches & Operations)
  renderOrganiserHub() {
    const { trips, currentUser, attendeesMap } = State;
    const activeBatchesCountEl = document.getElementById("orgActiveBatchesCount");
    const totalSeatsFilledEl = document.getElementById("orgTotalSeatsBooked");
    const escrowHoldingsEl = document.getElementById("orgEscrowHoldings");
    const listEl = document.getElementById("organiserTripsList");

    // Filter trips hosted by this organiser or show all for demo
    const orgTrips = trips;
    activeBatchesCountEl.textContent = orgTrips.length;

    let totalSeatsCapacity = 0;
    let totalSeatsBooked = 0;
    let totalEscrowRevenue = 0;

    orgTrips.forEach(t => {
      totalSeatsCapacity += t.totalSeats;
      const booked = t.totalSeats - t.availableSeats;
      totalSeatsBooked += booked;
      totalEscrowRevenue += booked * t.tokenAmount;
    });

    totalSeatsFilledEl.textContent = `${totalSeatsBooked} / ${totalSeatsCapacity}`;
    escrowHoldingsEl.textContent = UI.formatPrice(totalEscrowRevenue);

    listEl.innerHTML = orgTrips.map(trip => {
      const booked = trip.totalSeats - trip.availableSeats;
      const attendees = attendeesMap[trip.id] || [];

      return `
        <div class="org-batch-card" data-trip-id="${trip.id}">
          <div class="org-batch-top">
            <div class="org-batch-title">
              <h4>${trip.title}</h4>
              <div class="org-batch-dest">📍 ${trip.destination} &bull; Category: ${trip.category} &bull; ${trip.duration}</div>
            </div>
            <span class="badge-role" style="background-color: var(--color-ochre-light); color: var(--color-ochre-dark);">
              BATCH LIVE
            </span>
          </div>

          <div class="org-batch-metrics">
            <div class="metric-box">
              <span class="metric-title">Dates</span>
              <span class="metric-val" style="font-size: 0.95rem;">
                ${new Date(trip.startDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })} – ${new Date(trip.endDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })}
              </span>
            </div>
            <div class="metric-box">
              <span class="metric-title">Seats Booked</span>
              <span class="metric-val">${booked} of ${trip.totalSeats}</span>
            </div>
            <div class="metric-box">
              <span class="metric-title">Token Escrow Held</span>
              <span class="metric-val" style="color: var(--color-terracotta);">${UI.formatPrice(booked * trip.tokenAmount)}</span>
            </div>
            <div class="metric-box">
              <span class="metric-title">Projected Revenue</span>
              <span class="metric-val" style="color: var(--color-sage-dark);">${UI.formatPrice(booked * trip.totalPrice)}</span>
            </div>
          </div>

          <div class="org-batch-footer">
            <div class="batch-payout-status">
              <span>🛡️ Payout Status:</span>
              <span>HELD_IN_ESCROW (Releases 24h prior to start)</span>
            </div>
            <div class="org-batch-btn-group">
              <button class="btn btn-sm btn-outline-earth view-guests-btn" data-trip-id="${trip.id}">
                👥 View Guest List (${attendees.length || booked})
              </button>
              <button class="btn btn-sm btn-secondary-earth share-batch-btn" data-title="${trip.title}">
                🔗 Share Booking Link
              </button>
            </div>
          </div>
        </div>
      `;
    }).join("");

    // Attach guest list listeners
    listEl.querySelectorAll(".view-guests-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const tripId = btn.getAttribute("data-trip-id");
        Modals.openAttendeesModal(tripId);
      });
    });

    listEl.querySelectorAll(".share-batch-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const title = btn.getAttribute("data-title");
        navigator.clipboard?.writeText(window.location.href);
        UI.showToast(`Batch link for "${title}" copied to clipboard!`, "info");
      });
    });
  },

  // Toast System
  showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    
    let icon = "🔔";
    if (type === "success") icon = "✨";
    if (type === "warning") icon = "⚠️";

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transform = "translateY(10px)";
      setTimeout(() => toast.remove(), 300);
    }, 3800);
  },

  openMobileDrawer() {
    document.getElementById("mobileDrawer").classList.add("open");
  },

  closeMobileDrawer() {
    document.getElementById("mobileDrawer").classList.remove("open");
  }
};

// ==========================================
// 4. NAVIGATION & VIEW CONTROLLER
// ==========================================

const Navigation = {
  activeView: "explore",

  init() {
    // Desktop Nav Buttons
    document.querySelectorAll(".nav-link").forEach(btn => {
      btn.addEventListener("click", (e) => {
        const view = btn.getAttribute("data-view");
        this.switchView(view);
      });
    });

    // Mobile Drawer Links
    document.querySelectorAll(".mobile-nav-link").forEach(btn => {
      btn.addEventListener("click", (e) => {
        const view = btn.getAttribute("data-view");
        UI.closeMobileDrawer();
        this.switchView(view);
      });
    });

    // Footer Links
    document.querySelectorAll(".footer-link").forEach(link => {
      link.addEventListener("click", (e) => {
        const view = link.getAttribute("data-view");
        if (view) {
          e.preventDefault();
          this.switchView(view);
          window.scrollTo({ top: 0, behavior: "smooth" });
        }
      });
    });

    // Header Logo Click
    document.getElementById("logoLink").addEventListener("click", (e) => {
      e.preventDefault();
      this.switchView("explore");
      window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // Mobile menu toggle
    document.getElementById("mobileMenuBtn").addEventListener("click", () => {
      UI.openMobileDrawer();
    });
    document.getElementById("mobileDrawerClose").addEventListener("click", () => {
      UI.closeMobileDrawer();
    });

    // Top announcement banner close
    document.getElementById("bannerCloseBtn")?.addEventListener("click", () => {
      document.querySelector(".top-announcement").style.display = "none";
    });

    // CTA in How Escrow Works
    document.getElementById("trustCtaExploreBtn")?.addEventListener("click", () => {
      this.switchView("explore");
      window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // Browse more from Dashboard
    document.getElementById("browseMoreTripsBtn")?.addEventListener("click", () => {
      this.switchView("explore");
      window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // "Learn about Security" in Dashboard
    document.getElementById("viewEscrowPolicyBtn")?.addEventListener("click", () => {
      this.switchView("how-it-works");
      window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // Footer Host Trip Link
    document.getElementById("footerHostTripLink")?.addEventListener("click", (e) => {
      e.preventDefault();
      State.activeRole = "organiser";
      State.currentUser.role = "organiser";
      State.saveUser();
      UI.renderHeader();
      this.switchView("dashboard");
      Modals.openCreateTripModal();
    });

    // Dashboard Switcher Buttons
    document.getElementById("btnDashTraveller").addEventListener("click", () => {
      State.activeRole = "traveller";
      State.currentUser.role = "traveller";
      State.saveUser();
      UI.renderHeader();
      UI.renderDashboard();
      UI.showToast("Switched to Traveller Hub", "info");
    });

    document.getElementById("btnDashOrganiser").addEventListener("click", () => {
      State.activeRole = "organiser";
      State.currentUser.role = "organiser";
      State.saveUser();
      UI.renderHeader();
      UI.renderDashboard();
      UI.showToast("Switched to Organiser Console", "info");
    });

    // Header Role Switch Pill
    document.getElementById("roleToggleTraveller").addEventListener("click", () => {
      State.activeRole = "traveller";
      State.currentUser.role = "traveller";
      State.saveUser();
      UI.renderHeader();
      if (this.activeView === "dashboard") UI.renderDashboard();
      UI.showToast("Active Role: Traveller", "info");
    });

    document.getElementById("roleToggleOrganiser").addEventListener("click", () => {
      State.activeRole = "organiser";
      State.currentUser.role = "organiser";
      State.saveUser();
      UI.renderHeader();
      if (this.activeView === "dashboard") UI.renderDashboard();
      UI.showToast("Active Role: Organiser", "info");
    });

    // Mobile Role selector buttons
    document.querySelectorAll(".role-select-mobile").forEach(btn => {
      btn.addEventListener("click", () => {
        const role = btn.getAttribute("data-role");
        State.activeRole = role;
        State.currentUser.role = role;
        State.saveUser();
        UI.renderHeader();
        if (this.activeView === "dashboard") UI.renderDashboard();
        UI.closeMobileDrawer();
        UI.showToast(`Active Role: ${role === "traveller" ? "Traveller" : "Organiser"}`, "info");
      });
    });
  },

  switchView(viewName) {
    this.activeView = viewName;

    // Update active nav links
    document.querySelectorAll(".nav-link").forEach(btn => {
      if (btn.getAttribute("data-view") === viewName) {
        btn.classList.add("active");
      } else {
        btn.classList.remove("active");
      }
    });

    // Update mobile nav links
    document.querySelectorAll(".mobile-nav-link").forEach(btn => {
      if (btn.getAttribute("data-view") === viewName) {
        btn.classList.add("active");
      } else {
        btn.classList.remove("active");
      }
    });

    // Switch view containers
    document.getElementById("viewExplore").classList.remove("active");
    document.getElementById("viewDashboard").classList.remove("active");
    document.getElementById("viewHowItWorks").classList.remove("active");

    if (viewName === "explore") {
      document.getElementById("viewExplore").classList.add("active");
      UI.renderTrips();
    } else if (viewName === "dashboard") {
      document.getElementById("viewDashboard").classList.add("active");
      UI.renderDashboard();
    } else if (viewName === "how-it-works") {
      document.getElementById("viewHowItWorks").classList.add("active");
    }
  }
};

// ==========================================
// 5. SEARCH & FILTER CONTROLLER
// ==========================================

const FilterController = {
  init() {
    const searchInput = document.getElementById("searchInput");
    const heroSubmitBtn = document.getElementById("heroSearchSubmitBtn");
    const vibeSelect = document.getElementById("vibeFilterSelect");
    const budgetSelect = document.getElementById("budgetFilterSelect");
    const sortSelect = document.getElementById("sortSelect");
    const chipsContainer = document.getElementById("categoryChipsContainer");
    const clearFiltersBtn = document.getElementById("clearFiltersBtn");

    // Realtime search input
    searchInput.addEventListener("input", (e) => {
      State.currentFilters.searchQuery = e.target.value;
      UI.renderTrips();
    });

    // Hero search submit button
    heroSubmitBtn.addEventListener("click", () => {
      State.currentFilters.searchQuery = searchInput.value;
      State.currentFilters.category = vibeSelect.value;
      State.currentFilters.maxBudget = budgetSelect.value;
      
      // Sync chips
      this.syncActiveChip(vibeSelect.value);

      UI.renderTrips();
      // Scroll to catalog
      document.getElementById("catalogSection").scrollIntoView({ behavior: "smooth" });
    });

    // Dropdown filters
    vibeSelect.addEventListener("change", (e) => {
      State.currentFilters.category = e.target.value;
      this.syncActiveChip(e.target.value);
      UI.renderTrips();
    });

    budgetSelect.addEventListener("change", (e) => {
      State.currentFilters.maxBudget = e.target.value;
      UI.renderTrips();
    });

    sortSelect.addEventListener("change", (e) => {
      State.currentFilters.sort = e.target.value;
      UI.renderTrips();
    });

    // Category Chips
    chipsContainer.querySelectorAll(".chip").forEach(chip => {
      chip.addEventListener("click", () => {
        chipsContainer.querySelectorAll(".chip").forEach(c => c.classList.remove("active"));
        chip.classList.add("active");

        const cat = chip.getAttribute("data-category");
        State.currentFilters.category = cat;
        vibeSelect.value = cat;
        UI.renderTrips();
      });
    });

    // Reset filters
    clearFiltersBtn.addEventListener("click", () => {
      State.currentFilters = {
        searchQuery: "",
        category: "ALL",
        maxBudget: "ALL",
        sort: "recommended"
      };
      searchInput.value = "";
      vibeSelect.value = "ALL";
      budgetSelect.value = "ALL";
      sortSelect.value = "recommended";
      this.syncActiveChip("ALL");
      UI.renderTrips();
    });
  },

  syncActiveChip(category) {
    const chips = document.querySelectorAll("#categoryChipsContainer .chip");
    chips.forEach(c => {
      if (c.getAttribute("data-category") === category) {
        c.classList.add("active");
      } else {
        c.classList.remove("active");
      }
    });
  }
};

// ==========================================
// 6. MODALS & INTERACTION FLOWS
// ==========================================

const Modals = {
  selectedTripForBooking: null,

  init() {
    // Auth Modal Triggers & Tab Switching
    document.getElementById("openAuthModalBtn")?.addEventListener("click", () => {
      this.openAuthModal();
    });
    document.getElementById("authModalClose").addEventListener("click", () => {
      this.closeModal("authModal");
    });

    // Auth Role Tabs inside modal
    const authTabTraveller = document.getElementById("authTabTraveller");
    const authTabOrganiser = document.getElementById("authTabOrganiser");
    const groupOrgFields = document.getElementById("groupOrgFields");

    authTabTraveller.addEventListener("click", () => {
      authTabTraveller.classList.add("active");
      authTabOrganiser.classList.remove("active");
      groupOrgFields.style.display = "none";
      document.getElementById("authNameLabel").textContent = "Full Name";
      document.getElementById("authNameInput").placeholder = "e.g. Aarav Sharma";
    });

    authTabOrganiser.addEventListener("click", () => {
      authTabOrganiser.classList.add("active");
      authTabTraveller.classList.remove("active");
      if (document.getElementById("authSwitchSignup").classList.contains("active")) {
        groupOrgFields.style.display = "block";
      }
      document.getElementById("authNameLabel").textContent = "Business / Brand Name";
      document.getElementById("authNameInput").placeholder = "e.g. Wanderlust Expeditions";
    });

    // Sign In vs Sign Up switch
    const authSwitchLogin = document.getElementById("authSwitchLogin");
    const authSwitchSignup = document.getElementById("authSwitchSignup");
    const signupOnlyFields = document.querySelectorAll(".signup-only");
    const authSubmitBtn = document.getElementById("authSubmitBtn");

    authSwitchLogin.addEventListener("click", () => {
      authSwitchLogin.classList.add("active");
      authSwitchSignup.classList.remove("active");
      signupOnlyFields.forEach(el => el.style.display = "none");
      authSubmitBtn.textContent = "Sign In to Chalo Chalein";
    });

    authSwitchSignup.addEventListener("click", () => {
      authSwitchSignup.classList.add("active");
      authSwitchLogin.classList.remove("active");
      signupOnlyFields.forEach(el => {
        if (!el.classList.contains("organizer-only")) {
          el.style.display = "flex";
        }
      });
      if (authTabOrganiser.classList.contains("active")) {
        groupOrgFields.style.display = "block";
      }
      authSubmitBtn.textContent = "Create Account & Get Started";
    });

    // Demo Login Buttons for frictionless testing
    document.getElementById("demoTravellerBtn").addEventListener("click", () => {
      State.setDefaultUser("traveller");
      UI.renderHeader();
      this.closeModal("authModal");
      Navigation.switchView("dashboard");
      UI.showToast("Welcome back, Aarav! Logged in as Traveller.", "success");
    });

    document.getElementById("demoOrganiserBtn").addEventListener("click", () => {
      State.setDefaultUser("organiser");
      UI.renderHeader();
      this.closeModal("authModal");
      Navigation.switchView("dashboard");
      UI.showToast("Welcome back! Logged in as Tour Organiser.", "success");
    });

    // Auth Form Submission
    document.getElementById("authForm").addEventListener("submit", (e) => {
      e.preventDefault();
      const isOrganiser = authTabOrganiser.classList.contains("active");
      const email = document.getElementById("authEmailInput").value;
      const name = document.getElementById("authNameInput").value || (isOrganiser ? "Summit Travels" : "Travel Enthusiast");
      const phone = document.getElementById("authPhoneInput").value || "+91 98765 43210";

      State.currentUser = {
        name,
        email,
        phone,
        role: isOrganiser ? "organiser" : "traveller",
        avatar: name.substring(0, 2).toUpperCase(),
        chaloCash: isOrganiser ? 0 : 500
      };
      State.activeRole = isOrganiser ? "organiser" : "traveller";
      State.saveUser();

      UI.renderHeader();
      this.closeModal("authModal");
      Navigation.switchView("dashboard");
      UI.showToast(`Signed in successfully as ${isOrganiser ? "Organiser" : "Traveller"}!`, "success");
    });

    // Close buttons on all modals
    document.getElementById("tripModalClose").addEventListener("click", () => this.closeModal("tripDetailModal"));
    document.getElementById("bookingModalClose").addEventListener("click", () => this.closeModal("bookingConfirmModal"));
    document.getElementById("createTripClose").addEventListener("click", () => this.closeModal("createTripModal"));
    document.getElementById("cancelCreateTripBtn").addEventListener("click", () => this.closeModal("createTripModal"));
    document.getElementById("attendeesModalClose").addEventListener("click", () => this.closeModal("attendeesModal"));
    document.getElementById("sosModalClose").addEventListener("click", () => this.closeModal("sosModal"));
    document.getElementById("ticketModalClose")?.addEventListener("click", () => this.closeModal("ticketModal"));
    document.getElementById("balanceModalClose")?.addEventListener("click", () => this.closeModal("balanceModal"));

    // Organiser "Host a New Trip" open
    document.getElementById("btnOpenCreateTripModal").addEventListener("click", () => {
      this.openCreateTripModal();
    });

    // Create Trip Form Submit
    document.getElementById("createTripForm").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleCreateTripSubmit();
    });

    // SOS broadcast trigger
    document.getElementById("sosBroadcastBtn").addEventListener("click", () => {
      UI.showToast("SOS Broadcasted! GPS coordinates shared with host & response team.", "success");
      setTimeout(() => this.closeModal("sosModal"), 1200);
    });

    // Backdrop clicks to close
    document.querySelectorAll(".modal-backdrop").forEach(backdrop => {
      backdrop.addEventListener("click", (e) => {
        if (e.target === backdrop) {
          backdrop.classList.remove("open");
          if (State.seatLockCountdownInterval) clearInterval(State.seatLockCountdownInterval);
        }
      });
    });
  },

  openModal(modalId) {
    document.getElementById(modalId).classList.add("open");
  },

  closeModal(modalId) {
    document.getElementById(modalId).classList.remove("open");
    if (modalId === "bookingConfirmModal" && State.seatLockCountdownInterval) {
      clearInterval(State.seatLockCountdownInterval);
    }
  },

  openAuthModal() {
    this.openModal("authModal");
  },

  // Open Trip Details & Day-by-day Itinerary
  openTripDetailModal(tripId) {
    const trip = State.trips.find(t => t.id === tripId);
    if (!trip) return;

    this.selectedTripForBooking = trip;
    const content = document.getElementById("tripModalContent");

    const daysHtml = trip.itinerary ? trip.itinerary.map(item => `
      <div class="itinerary-day-item">
        <h5>${item.day}: ${item.desc.split(":")[0]}</h5>
        <p>${item.desc.split(":").slice(1).join(":") || item.desc}</p>
      </div>
    `).join("") : "<p>Detailed route map provided after token reservation.</p>";

    const inclusionsHtml = trip.inclusions ? trip.inclusions.map(inc => `
      <li style="font-size: 0.85rem; margin-bottom: 0.35rem; display: flex; align-items: center; gap: 0.4rem;">
        <span style="color: var(--color-success);">✓</span> ${inc}
      </li>
    `).join("") : "";

    content.innerHTML = `
      <img src="${trip.image}" alt="${trip.title}" class="trip-detail-cover">
      
      <div class="trip-detail-top-badges">
        <span class="badge-role">${trip.category}</span>
        <span class="badge-role" style="background-color: var(--color-sage-light); color: var(--color-sage-dark);">
          🛡️ Escrow Protected Batch
        </span>
        <span class="badge-role" style="background-color: var(--color-ochre-light); color: var(--color-ochre-dark);">
          ${trip.availableSeats} Seats Left
        </span>
      </div>

      <h2 class="trip-detail-title">${trip.title}</h2>
      <div style="font-size: 0.95rem; color: var(--color-text-muted); margin-bottom: 1.25rem;">
        📍 <strong>${trip.destination}</strong> &bull; Hosted by <strong>${trip.organizer.name}</strong> (★ ${trip.organizer.rating})
      </div>

      <div class="trip-detail-grid-specs">
        <div>
          <span style="font-size: 0.74rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Duration</span>
          <div style="font-weight: 800; font-size: 1.05rem;">${trip.duration}</div>
        </div>
        <div>
          <span style="font-size: 0.74rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Dates</span>
          <div style="font-weight: 800; font-size: 1.05rem;">${new Date(trip.startDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })} – ${new Date(trip.endDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })}</div>
        </div>
        <div>
          <span style="font-size: 0.74rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Token To Lock</span>
          <div style="font-weight: 800; font-size: 1.05rem; color: var(--color-terracotta);">${UI.formatPrice(trip.tokenAmount)}</div>
        </div>
        <div>
          <span style="font-size: 0.74rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase;">Total Batch Fee</span>
          <div style="font-weight: 800; font-size: 1.05rem;">${UI.formatPrice(trip.totalPrice)}</div>
        </div>
      </div>

      <div class="trip-detail-escrow-box">
        <h4>🛡️ Chalo Chalein Escrow Guarantee</h4>
        <p>You only pay <strong>${UI.formatPrice(trip.tokenAmount)}</strong> today to securely lock your seat. The remaining balance (${UI.formatPrice(trip.totalPrice - trip.tokenAmount)}) is payable 3 days before departure. Your funds are protected in our platform escrow account until 24h before the trip begins.</p>
      </div>

      <h4 style="font-family: var(--font-serif); font-size: 1.3rem; margin-bottom: 0.5rem;">Expedition Overview</h4>
      <p style="font-size: 0.92rem; color: var(--color-text-muted); line-height: 1.65; margin-bottom: 1.5rem;">${trip.description}</p>

      <h4 style="font-family: var(--font-serif); font-size: 1.3rem; margin-bottom: 0.75rem;">Day-by-Day Itinerary</h4>
      <div class="itinerary-days-list">
        ${daysHtml}
      </div>

      <h4 style="font-family: var(--font-serif); font-size: 1.3rem; margin: 1.5rem 0 0.5rem;">What's Included</h4>
      <ul style="list-style: none; margin-bottom: 1.5rem;">
        ${inclusionsHtml}
      </ul>

      <div class="trip-detail-action-bar">
        <div>
          <span style="font-size: 0.8rem; color: var(--color-text-muted); display: block;">Instant Token Lock:</span>
          <strong style="font-size: 1.5rem; color: var(--color-text-main);">${UI.formatPrice(trip.tokenAmount)}</strong>
          <span style="font-size: 0.78rem; color: var(--color-text-muted);">/ person</span>
        </div>
        <button class="btn btn-primary-earth" id="initiateLockBtn">
          ⚡ Lock Seat for ${UI.formatPrice(trip.tokenAmount)}
        </button>
      </div>
    `;

    document.getElementById("initiateLockBtn").addEventListener("click", () => {
      this.closeModal("tripDetailModal");
      this.openBookingConfirmModal(trip);
    });

    this.openModal("tripDetailModal");
  },

  // Open Seat Reservation & 10-Minute Lock Simulation Modal
  openBookingConfirmModal(trip) {
    this.selectedTripForBooking = trip;
    const content = document.getElementById("bookingDialogContent");

    content.innerHTML = `
      <div class="lock-timer-badge">
        <span>⏱️ Redis Seat Lock Active:</span>
        <strong id="lockTimer">10:00</strong>
      </div>

      <h3>Lock Your Seat in Escrow</h3>
      <p style="font-size: 0.86rem; color: var(--color-text-muted);">
        You are reserving 1 seat for <strong>${trip.title}</strong>.
      </p>

      <div class="escrow-payment-breakdown">
        <div class="payment-line">
          <span>Batch Token Deposit (Pay Today):</span>
          <strong>${UI.formatPrice(trip.tokenAmount)}</strong>
        </div>
        <div class="payment-line">
          <span>Remaining Balance (Due before departure):</span>
          <span>${UI.formatPrice(trip.totalPrice - trip.tokenAmount)}</span>
        </div>
        <div class="payment-line highlight">
          <span>Amount Held in Safe Escrow:</span>
          <span>${UI.formatPrice(trip.tokenAmount)}</span>
        </div>
      </div>

      <div style="background-color: var(--color-sage-light); border-radius: var(--radius-md); padding: 0.9rem; margin-bottom: 1.25rem; font-size: 0.82rem; color: var(--color-sage-dark);">
        🛡️ <strong>Zero Disintermediation Risk:</strong> Chalo Chalein acts as your neutral trustee. Funds are only disbursed to <em>${trip.organizer.name}</em> 24 hours prior to journey kickoff.
      </div>

      <button class="btn btn-primary-earth w-100" id="confirmTokenPaymentBtn">
        Confirm Token Payment &amp; Hold Seat
      </button>
      
      <p style="font-size: 0.72rem; color: var(--color-text-light); text-align: center; margin-top: 0.75rem;">
        Simulated UPI / NetBanking / Razorpay split gateway payment.
      </p>
    `;

    // Start 10-minute timer simulation
    let secondsLeft = 600;
    const timerEl = document.getElementById("lockTimer");
    if (State.seatLockCountdownInterval) clearInterval(State.seatLockCountdownInterval);

    State.seatLockCountdownInterval = setInterval(() => {
      secondsLeft--;
      if (secondsLeft <= 0) {
        clearInterval(State.seatLockCountdownInterval);
        timerEl.textContent = "00:00 (Expired)";
        UI.showToast("10-minute seat lock expired. Please select your seat again.", "warning");
        Modals.closeModal("bookingConfirmModal");
        return;
      }
      const mins = Math.floor(secondsLeft / 60);
      const secs = secondsLeft % 60;
      timerEl.textContent = `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
    }, 1000);

    document.getElementById("confirmTokenPaymentBtn").addEventListener("click", () => {
      this.completeBooking(trip);
    });

    this.openModal("bookingConfirmModal");
  },

  // Complete Booking (PRD state machine: PENDING_TOKEN -> TOKEN_PAID)
  completeBooking(trip) {
    if (State.seatLockCountdownInterval) clearInterval(State.seatLockCountdownInterval);

    // Decrement available seat in trip inventory
    if (trip.availableSeats > 0) {
      trip.availableSeats -= 1;
      State.saveTrips();
    }

    // Create new booking record
    const newBooking = {
      id: "BK-2026-" + Math.floor(100 + Math.random() * 900),
      tripId: trip.id,
      tripTitle: trip.title,
      destination: trip.destination,
      dates: `${new Date(trip.startDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })} – ${new Date(trip.endDate).toLocaleDateString("en-IN", { month: "short", day: "numeric" })}`,
      seatsBooked: 1,
      tokenAmount: trip.tokenAmount,
      totalPrice: trip.totalPrice,
      bookingStatus: "TOKEN_PAID",
      payoutStatus: "HELD_IN_ESCROW",
      bookedAt: new Date().toISOString(),
      image: trip.image,
      organizerName: trip.organizer.name
    };

    State.bookings.unshift(newBooking);
    State.saveBookings();

    // Add to attendees list for the organiser
    if (!State.attendeesMap[trip.id]) {
      State.attendeesMap[trip.id] = [];
    }
    State.attendeesMap[trip.id].push({
      name: `${State.currentUser.name} (You)`,
      phone: State.currentUser.phone || "+91 98765 43210",
      email: State.currentUser.email,
      status: "TOKEN_PAID",
      token: trip.tokenAmount,
      emergencyContact: "+91 98765 00000"
    });
    State.saveAttendees();

    // Reward ChaloCash (5% loyalty)
    const loyaltyReward = Math.round(trip.tokenAmount * 0.05);
    State.currentUser.chaloCash = (State.currentUser.chaloCash || 0) + loyaltyReward;
    State.saveUser();

    this.closeModal("bookingConfirmModal");
    UI.showToast(`🎉 Seat locked! Token ₹${trip.tokenAmount} is safely held in Escrow. +${loyaltyReward} ChaloCash earned!`, "success");

    // Automatically navigate to dashboard to view the booking pass
    Navigation.switchView("dashboard");
  },

  // Open Organiser "Create Trip" modal
  openCreateTripModal() {
    // Set default dates
    const today = new Date();
    const nextWeek = new Date(today);
    nextWeek.setDate(today.getDate() + 14);
    const weekAfter = new Date(nextWeek);
    weekAfter.setDate(nextWeek.getDate() + 6);

    document.getElementById("tripStartDateInput").value = nextWeek.toISOString().split("T")[0];
    document.getElementById("tripEndDateInput").value = weekAfter.toISOString().split("T")[0];

    this.openModal("createTripModal");
  },

  // Handle Trip Publication by Organiser
  handleCreateTripSubmit() {
    const title = document.getElementById("tripTitleInput").value;
    const dest = document.getElementById("tripDestInput").value;
    const vibe = document.getElementById("tripVibeInput").value;
    const startDate = document.getElementById("tripStartDateInput").value;
    const endDate = document.getElementById("tripEndDateInput").value;
    const seats = parseInt(document.getElementById("tripSeatsInput").value, 10);
    const token = parseInt(document.getElementById("tripTokenAmountInput").value, 10);
    const total = parseInt(document.getElementById("tripTotalAmountInput").value, 10);
    const imageUrl = document.getElementById("tripImageInput").value || "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80";
    const desc = document.getElementById("tripDescInput").value;

    // Calculate days duration
    const d1 = new Date(startDate);
    const d2 = new Date(endDate);
    const diffTime = Math.abs(d2 - d1);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 5;

    const newTrip = {
      id: "trip-custom-" + Date.now(),
      title,
      destination: dest,
      category: vibe,
      duration: `${diffDays} Days • ${diffDays - 1} Nights`,
      startDate,
      endDate,
      totalSeats: seats,
      availableSeats: seats,
      tokenAmount: token,
      totalPrice: total,
      organizer: {
        id: "org-" + Date.now(),
        name: State.currentUser.name || "Curated Expeditions",
        verified: true,
        rating: 4.9,
        tripsDone: 1,
        instagram: State.currentUser.instagram || "@chalo_trips"
      },
      image: imageUrl,
      description: desc,
      itinerary: [
        { day: "Day 1", desc: "Arrival & Welcome: Community circle, group orientation, and authentic dinner." },
        { day: `Day 2-${diffDays - 1}`, desc: "Expedition Phase: Core adventure route, hidden sights, photography, and evening campfires." },
        { day: `Day ${diffDays}`, desc: "Farewell & Return: Group memories, souvenir swap, and departure." }
      ],
      inclusions: ["All Group Accommodation", "Internal Batch Transport", "Experienced Host & Guide", "100% Escrow Protection"]
    };

    State.trips.unshift(newTrip);
    State.saveTrips();

    // Initialise empty attendees list
    State.attendeesMap[newTrip.id] = [];
    State.saveAttendees();

    // Reset form & close
    document.getElementById("createTripForm").reset();
    this.closeModal("createTripModal");

    UI.showToast(`✨ Batch "${title}" published to marketplace with Escrow protection!`, "success");
    UI.renderTrips();
    UI.renderOrganiserHub();
  },

  // Open Attendees Modal for Organiser
  openAttendeesModal(tripId) {
    const trip = State.trips.find(t => t.id === tripId);
    if (!trip) return;

    const header = document.getElementById("attendeesHeader");
    const content = document.getElementById("attendeesContent");
    const attendees = State.attendeesMap[tripId] || [];

    header.innerHTML = `
      <span class="badge-role">Passenger Roster &amp; Escrow</span>
      <h3 style="margin: 0.35rem 0 0.2rem; font-family: var(--font-serif); font-size: 1.4rem;">${trip.title}</h3>
      <p style="font-size: 0.85rem; color: var(--color-text-muted);">
        ${attendees.length} Verified Bookings &bull; Total Escrow Held: <strong>${UI.formatPrice(attendees.length * trip.tokenAmount)}</strong>
      </p>
    `;

    if (attendees.length === 0) {
      content.innerHTML = `
        <div style="text-align: center; padding: 2rem; color: var(--color-text-muted);">
          No bookings yet for this batch. Share your trip link to get passengers!
        </div>
      `;
    } else {
      content.innerHTML = `
        <table class="attendees-table">
          <thead>
            <tr>
              <th>Traveler Name</th>
              <th>Contact / WhatsApp</th>
              <th>Token Status</th>
              <th>Emergency Contact</th>
            </tr>
          </thead>
          <tbody>
            ${attendees.map(a => `
              <tr>
                <td><strong>${a.name}</strong><br><small style="color: var(--color-text-muted);">${a.email}</small></td>
                <td>${a.phone}</td>
                <td><span class="badge-role" style="background-color: var(--color-ochre-light); color: var(--color-ochre-dark); font-size: 0.7rem;">${a.status} (₹${a.token})</span></td>
                <td>${a.emergencyContact}</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      `;
    }

    // Attach WhatsApp copy handler
    document.getElementById("copyWhatsAppNumbersBtn")?.addEventListener("click", () => {
      const phones = attendees.map(a => a.phone).filter(Boolean);
      if (phones.length === 0) {
        UI.showToast("No attendee contact numbers found.", "warning");
        return;
      }
      navigator.clipboard?.writeText(phones.join(", "));
      UI.showToast(`Copied ${phones.length} WhatsApp numbers to clipboard!`, "success");
    });

    // Attach CSV export handler
    document.getElementById("exportRosterCsvBtn")?.addEventListener("click", () => {
      if (attendees.length === 0) {
        UI.showToast("No attendees to export yet.", "info");
        return;
      }
      let csv = "Name,Phone,Email,TokenStatus,TokenAmount,EmergencyContact\n";
      attendees.forEach(a => {
        csv += `"${a.name}","${a.phone}","${a.email}","${a.status}","${a.token}","${a.emergencyContact}"\n`;
      });
      const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", `chalo-roster-${trip.id}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      UI.showToast("Passenger roster CSV exported!", "success");
    });

    this.openModal("attendeesModal");
  },

  // Open Digital Ticket / Boarding Pass Modal
  openTicketModal(bookingId) {
    const booking = State.bookings.find(b => b.id === bookingId);
    if (!booking) return;

    const content = document.getElementById("ticketContent");
    const isConfirmed = booking.bookingStatus === "CONFIRMED";

    content.innerHTML = `
      <div class="ticket-top-banner">
        <div class="ticket-brand">
          <span class="badge-role" style="background-color: rgba(255,255,255,0.25); color: #ffffff;">
            🛡️ 100% ESCROW PROTECTED
          </span>
          <span style="font-weight: 800; font-size: 0.9rem; letter-spacing: 1px;">CHALO CHALEIN</span>
        </div>
        <h3>${booking.tripTitle}</h3>
        <p style="font-size: 0.85rem; opacity: 0.9; margin-top: 0.25rem;">📍 ${booking.destination}</p>
      </div>

      <div class="ticket-body">
        <div class="ticket-grid-fields">
          <div class="ticket-field">
            <span class="ticket-field-label">Passenger Name</span>
            <span class="ticket-field-val">${State.currentUser.name}</span>
          </div>
          <div class="ticket-field">
            <span class="ticket-field-label">Booking Reference</span>
            <span class="ticket-field-val">${booking.id}</span>
          </div>
          <div class="ticket-field">
            <span class="ticket-field-label">Expedition Dates</span>
            <span class="ticket-field-val">${booking.dates}</span>
          </div>
          <div class="ticket-field">
            <span class="ticket-field-label">Reserved Seats</span>
            <span class="ticket-field-val">${booking.seatsBooked || 1} Seat (Token Locked)</span>
          </div>
          <div class="ticket-field">
            <span class="ticket-field-label">Lead Host / Organiser</span>
            <span class="ticket-field-val">${booking.organizerName}</span>
          </div>
          <div class="ticket-field">
            <span class="ticket-field-label">Token Held in Escrow</span>
            <span class="ticket-field-val" style="color: var(--color-terracotta);">${UI.formatPrice(booking.tokenAmount)}</span>
          </div>
        </div>

        <div class="ticket-perforation"></div>

        <div class="ticket-qr-section">
          <div>
            <span class="ticket-field-label">Departure Status</span>
            <div style="font-weight: 800; color: ${isConfirmed ? "var(--color-success)" : "var(--color-ochre-dark)"};">
              ${isConfirmed ? "✓ CONFIRMED (Ready For Departure)" : "🛡️ TOKEN_PAID (Held in Escrow)"}
            </div>
            <small style="color: var(--color-text-muted); font-size: 0.75rem;">Show this digital pass at pickup point with Govt ID.</small>
          </div>
          <div class="ticket-barcode-mock" title="Verified Digital Boarding Barcode">
            <div class="barcode-line" style="width: 3px;"></div>
            <div class="barcode-line" style="width: 1px;"></div>
            <div class="barcode-line" style="width: 4px;"></div>
            <div class="barcode-line" style="width: 2px;"></div>
            <div class="barcode-line" style="width: 6px;"></div>
            <div class="barcode-line" style="width: 2px;"></div>
            <div class="barcode-line" style="width: 4px;"></div>
            <div class="barcode-line" style="width: 1px;"></div>
            <div class="barcode-line" style="width: 5px;"></div>
            <div class="barcode-line" style="width: 2px;"></div>
            <div class="barcode-line" style="width: 4px;"></div>
          </div>
        </div>
      </div>

      <div class="ticket-footer-actions">
        <button class="btn btn-sm btn-outline-earth" id="printTicketBtn">
          🖨️ Print / Save Pass
        </button>
        <button class="btn btn-sm btn-primary-earth" id="closeTicketActionBtn">
          Close Pass
        </button>
      </div>
    `;

    document.getElementById("printTicketBtn")?.addEventListener("click", () => {
      window.print();
    });
    document.getElementById("closeTicketActionBtn")?.addEventListener("click", () => {
      this.closeModal("ticketModal");
    });

    this.openModal("ticketModal");
  },

  // Open Pay Remaining Balance Modal
  openBalanceModal(bookingId) {
    const booking = State.bookings.find(b => b.id === bookingId);
    if (!booking) return;

    const content = document.getElementById("balanceContent");
    const remaining = booking.totalPrice - booking.tokenAmount;

    content.innerHTML = `
      <h3>Pay Remaining Balance</h3>
      <p style="font-size: 0.86rem; color: var(--color-text-muted);">
        Trip: <strong>${booking.tripTitle}</strong>
      </p>

      <div class="escrow-payment-breakdown">
        <div class="payment-line">
          <span>Total Batch Price:</span>
          <span>${UI.formatPrice(booking.totalPrice)}</span>
        </div>
        <div class="payment-line">
          <span>Token Already Paid:</span>
          <span>- ${UI.formatPrice(booking.tokenAmount)}</span>
        </div>
        <div class="payment-line highlight">
          <span>Remaining Due Today:</span>
          <span style="color: var(--color-terracotta);">${UI.formatPrice(remaining)}</span>
        </div>
      </div>

      <div style="background-color: var(--color-sage-light); border-radius: var(--radius-md); padding: 0.85rem; font-size: 0.82rem; color: var(--color-sage-dark); margin-bottom: 1.25rem;">
        🛡️ <strong>Pre-Departure Guarantee:</strong> Completing this payment confirms your seat and activates your digital boarding pass. Escrow payout to ${booking.organizerName} is held until 24 hours prior to departure.
      </div>

      <button class="btn btn-primary-earth w-100" id="confirmBalancePaymentBtn">
        Confirm Payment of ${UI.formatPrice(remaining)}
      </button>
    `;

    document.getElementById("confirmBalancePaymentBtn")?.addEventListener("click", () => {
      booking.bookingStatus = "CONFIRMED";
      State.saveBookings();
      this.closeModal("balanceModal");
      UI.showToast("🎉 Seat fully confirmed! Your digital boarding pass is now active.", "success");
      UI.renderTravellerHub();
    });

    this.openModal("balanceModal");
  },

  // Open SOS Emergency Modal
  openSosModal(hostName) {
    document.getElementById("sosHostName").textContent = hostName || "Himalayan Nomads";
    this.openModal("sosModal");
  }
};

// ==========================================
// 7. APP BOOTSTRAP
// ==========================================

document.addEventListener("DOMContentLoaded", () => {
  State.init();
  UI.renderHeader();
  Navigation.init();
  FilterController.init();
  Modals.init();

  // Initial render of marketplace catalog
  UI.renderTrips();

  console.log("Chalo Chalein application initialized successfully with Escrow architecture.");
});
