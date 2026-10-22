/**
 * Main Single Page Application Controller - CurrencyX (25 Features)
 */

document.addEventListener('DOMContentLoaded', () => {

    // --- STATE MANAGEMENT ---
    const state = {
        currencies: {},
        rates: {},
        baseCurrency: 'USD',
        fromCurrency: 'USD',
        toCurrency: 'INR',
        amount: 100,
        convertedAmount: 0,
        currentRate: 1,
        precision: 2,
        refreshInterval: 5,
        theme: 'light',
        favorites: ['USD', 'EUR', 'GBP', 'INR', 'AED'],
        multiTargets: ['INR', 'EUR', 'GBP', 'AED', 'JPY'],
        activeModalTarget: null,
        chartPair: 'USD_INR',
        chartTimeframe: '1M',
        fxChart: null,
        isOffline: false,
        lastUpdatedTimeStr: '',
        
        // Auth State
        authToken: localStorage.getItem('currencyx_token') || null,
        currentUser: null,

        // Travel Mode State
        travelHomeCurr: 'USD',
        travelDestCurr: 'EUR',
        travelBudget: 2000,
        expenses: []
    };

    // --- DOM ELEMENTS ---
    const elements = {
        amountInput: document.getElementById('amountInput'),
        fromCurrencyBtn: document.getElementById('fromCurrencyBtn'),
        toCurrencyBtn: document.getElementById('toCurrencyBtn'),
        fromFlag: document.getElementById('fromFlag'),
        fromCode: document.getElementById('fromCode'),
        fromName: document.getElementById('fromName'),
        toFlag: document.getElementById('toFlag'),
        toCode: document.getElementById('toCode'),
        toName: document.getElementById('toName'),
        fromSymbolPrefix: document.getElementById('fromCurrencySymbolPrefix'),
        swapBtn: document.getElementById('swapBtn'),
        convertBtn: document.getElementById('convertBtn'),
        resultFromDisplay: document.getElementById('resultFromDisplay'),
        resultAmountDisplay: document.getElementById('resultAmountDisplay'),
        exchangeRateLine: document.getElementById('exchangeRateLine'),
        lastUpdatedTime: document.getElementById('lastUpdatedTime'),
        refreshSpinner: document.getElementById('refreshSpinner'),
        connectionStatus: document.getElementById('connectionStatus'),
        statusText: document.getElementById('statusText'),
        
        // Modal
        currencyModal: document.getElementById('currencyModal'),
        closeModalBtn: document.getElementById('closeModalBtn'),
        modalSearchInput: document.getElementById('modalSearchInput'),
        modalCurrencyList: document.getElementById('modalCurrencyList'),

        // Tab Nav
        navLinks: document.querySelectorAll('.nav-link'),
        tabSections: document.querySelectorAll('.tab-section'),

        // Favorites & Popular
        favChipsContainer: document.getElementById('favChipsContainer'),
        popularPairsGrid: document.getElementById('popularPairsGrid'),

        // Live Rates Table
        ratesTableBody: document.getElementById('ratesTableBody'),
        ratesTableSearch: document.getElementById('ratesTableSearch'),

        // Multi Currency
        multiBaseAmount: document.getElementById('multiBaseAmount'),
        multiBaseCurrencyBtn: document.getElementById('multiBaseCurrencyBtn'),
        multiBaseFlag: document.getElementById('multiBaseFlag'),
        multiBaseCode: document.getElementById('multiBaseCode'),
        multiResultsGrid: document.getElementById('multiResultsGrid'),
        addMultiTargetBtn: document.getElementById('addMultiTargetBtn'),

        // Fee Calc
        feeCalcAmount: document.getElementById('feeCalcAmount'),
        feeCalcFromSelect: document.getElementById('feeCalcFromSelect'),
        feeCalcToSelect: document.getElementById('feeCalcToSelect'),
        feePctSlider: document.getElementById('feePctSlider'),
        feePctValDisplay: document.getElementById('feePctValDisplay'),
        feeSummaryBox: document.getElementById('feeSummaryBox'),

        // Travel Mode
        travelHomeCurrSelect: document.getElementById('travelHomeCurrSelect'),
        travelDestCurrSelect: document.getElementById('travelDestCurrSelect'),
        travelBudgetInput: document.getElementById('travelBudgetInput'),
        spentValDisplay: document.getElementById('spentValDisplay'),
        budgetValDisplay: document.getElementById('budgetValDisplay'),
        budgetProgressBar: document.getElementById('budgetProgressBar'),
        addExpenseForm: document.getElementById('addExpenseForm'),
        expenseTitleInput: document.getElementById('expenseTitleInput'),
        expenseCategorySelect: document.getElementById('expenseCategorySelect'),
        expenseAmountInput: document.getElementById('expenseAmountInput'),
        expensesTableBody: document.getElementById('expensesTableBody'),
        exportExpensesCsvBtn: document.getElementById('exportExpensesCsvBtn'),

        // Comparison Matrix
        compareBaseSelect: document.getElementById('compareBaseSelect'),
        comparisonTableBody: document.getElementById('comparisonTableBody'),

        // AI Chatbot
        aiChatMessages: document.getElementById('aiChatMessages'),
        aiChatForm: document.getElementById('aiChatForm'),
        aiChatInput: document.getElementById('aiChatInput'),

        // Auth Modal
        userAuthContainer: document.getElementById('userAuthContainer'),
        loginModalBtn: document.getElementById('loginModalBtn'),
        authModal: document.getElementById('authModal'),
        closeAuthModalBtn: document.getElementById('closeAuthModalBtn'),
        authTabLogin: document.getElementById('authTabLogin'),
        authTabRegister: document.getElementById('authTabRegister'),
        loginForm: document.getElementById('loginForm'),
        registerForm: document.getElementById('registerForm'),

        // Charts
        fxTrendChartCanvas: document.getElementById('fxTrendChart'),
        timeframeBtnGroup: document.getElementById('timeframeBtnGroup'),
        chartPairTitle: document.getElementById('chartPairTitle'),
        chartStatHigh: document.getElementById('chartStatHigh'),
        chartStatLow: document.getElementById('chartStatLow'),
        chartStatAvg: document.getElementById('chartStatAvg'),
        chartStatChange: document.getElementById('chartStatChange'),

        // Alerts
        createAlertForm: document.getElementById('createAlertForm'),
        alertFromSelect: document.getElementById('alertFromSelect'),
        alertToSelect: document.getElementById('alertToSelect'),
        alertTargetRateInput: document.getElementById('alertTargetRateInput'),
        alertConditionSelect: document.getElementById('alertConditionSelect'),
        activeAlertsList: document.getElementById('activeAlertsList'),

        // History
        historyTableBody: document.getElementById('historyTableBody'),
        clearAllHistoryBtn: document.getElementById('clearAllHistoryBtn'),
        exportHistoryCsvBtn: document.getElementById('exportHistoryCsvBtn'),

        // Header Actions
        langSelect: document.getElementById('langSelect'),
        themeToggleBtn: document.getElementById('themeToggleBtn'),
        settingsBtn: document.getElementById('settingsBtn'),
        notifBellBtn: document.getElementById('notifBellBtn'),
        notifCountBadge: document.getElementById('notifCountBadge'),
        notifDrawer: document.getElementById('notifDrawer'),
        closeNotifDrawerBtn: document.getElementById('closeNotifDrawerBtn'),
        notifListContainer: document.getElementById('notifListContainer'),
        clearNotifsBtn: document.getElementById('clearNotifsBtn'),

        // Result Actions
        copyResultBtn: document.getElementById('copyResultBtn'),
        shareResultBtn: document.getElementById('shareResultBtn'),
        printResultBtn: document.getElementById('printResultBtn'),
        toggleFavPairBtn: document.getElementById('toggleFavPairBtn'),

        // Settings Modal
        settingsModal: document.getElementById('settingsModal'),
        closeSettingsBtn: document.getElementById('closeSettingsBtn'),
        settingsForm: document.getElementById('settingsForm'),
        settingDefaultCurrency: document.getElementById('settingDefaultCurrency'),
        settingPrecision: document.getElementById('settingPrecision'),
        settingRefreshInterval: document.getElementById('settingRefreshInterval'),
        settingPushNotifications: document.getElementById('settingPushNotifications')
    };

    // --- INITIALIZATION ---
    async function init() {
        setupEventListeners();
        loadSavedTheme();
        loadSettings();
        await checkAuthUser();
        await fetchCurrencies();
        await fetchLiveRates();
        setupChart();
        fetchExpenses();
        renderComparisonMatrix();
        registerServiceWorker();

        setInterval(() => fetchLiveRates(), state.refreshInterval * 60 * 1000);
    }

    // --- AUTHENTICATION MODULE ---
    async function checkAuthUser() {
        if (!state.authToken) {
            renderAuthHeader();
            return;
        }
        try {
            const res = await fetch('/api/auth/me', {
                headers: { 'Authorization': `Bearer ${state.authToken}` }
            });
            const data = await res.json();
            if (data.authenticated) {
                state.currentUser = data.user;
            } else {
                state.authToken = null;
                localStorage.removeItem('currencyx_token');
            }
        } catch (e) {
            // ignore
        }
        renderAuthHeader();
    }

    function renderAuthHeader() {
        if (state.currentUser) {
            elements.userAuthContainer.innerHTML = `
                <div class="user-profile-badge">
                    <span class="user-avatar">${state.currentUser.name.charAt(0).toUpperCase()}</span>
                    <span>${state.currentUser.name}</span>
                    <button class="icon-btn sm" id="logoutBtn" title="Logout"><i class="fa-solid fa-right-from-bracket"></i></button>
                </div>
            `;
            document.getElementById('logoutBtn').addEventListener('click', logoutUser);
        } else {
            elements.userAuthContainer.innerHTML = `
                <button id="loginModalBtn" class="btn btn-outline btn-sm"><i class="fa-solid fa-user"></i> <span data-i18n="btn_login">Login</span></button>
            `;
            document.getElementById('loginModalBtn').addEventListener('click', () => elements.authModal.classList.add('show'));
        }
    }

    async function logoutUser() {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${state.authToken}` }
        });
        state.authToken = null;
        state.currentUser = null;
        localStorage.removeItem('currencyx_token');
        renderAuthHeader();
        showToast('Logged out successfully', 'info');
    }

    // --- API FETCHERS ---
    async function fetchCurrencies() {
        try {
            const res = await fetch('/api/currencies');
            state.currencies = await res.json();
            populateSelectDropdowns();
        } catch (e) {
            console.error('Error fetching currency dictionary:', e);
            showToast('Failed to load currency list', 'danger');
        }
    }

    async function fetchLiveRates() {
        elements.refreshSpinner.classList.add('spinning');
        try {
            const res = await fetch('/api/rates');
            const data = await res.json();

            state.rates = data.rates || {};
            state.isOffline = data.isOffline || !navigator.onLine;

            const dateObj = data.lastUpdated ? new Date(data.lastUpdated * 1000) : new Date();
            state.lastUpdatedTimeStr = dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            elements.lastUpdatedTime.textContent = state.lastUpdatedTimeStr;

            updateConnectionBadge();
            performConversion();
            renderPopularPairs();
            renderLiveRatesTable();
            renderMultiCurrencyGrid();
            updateFeeSummary();
            fetchHistory();
            fetchAlerts();
            fetchNotifications();

        } catch (e) {
            state.isOffline = true;
            updateConnectionBadge();
            performConversion();
        } finally {
            elements.refreshSpinner.classList.remove('spinning');
        }
    }

    // --- CONVERSION ENGINE ---
    function performConversion(saveHistory = false) {
        state.amount = parseFloat(elements.amountInput.value) || 0;
        if (state.amount < 0) state.amount = 0;

        const fromRate = state.rates[state.fromCurrency] || 1;
        const toRate = state.rates[state.toCurrency] || 1;

        state.currentRate = toRate / fromRate;
        state.convertedAmount = state.amount * state.currentRate;

        updateConversionUI();
        if (saveHistory) {
            saveConversionHistoryApi(state.fromCurrency, state.toCurrency, state.amount, state.currentRate, state.convertedAmount);
        }
    }

    function updateConversionUI() {
        const fromInfo = state.currencies[state.fromCurrency] || { symbol: state.fromCurrency, flag: '🏳️' };
        const toInfo = state.currencies[state.toCurrency] || { symbol: state.toCurrency, flag: '🏳️' };

        elements.fromSymbolPrefix.textContent = fromInfo.symbol;
        elements.resultFromDisplay.textContent = `${formatNumber(state.amount, 2)} ${state.fromCurrency} =`;
        elements.resultAmountDisplay.textContent = `${toInfo.symbol} ${formatNumber(state.convertedAmount, state.precision)}`;
        elements.exchangeRateLine.innerHTML = `<i class="fa-solid fa-circle-info"></i> 1 ${state.fromCurrency} = ${formatNumber(state.currentRate, 4)} ${state.toCurrency}`;
    }

    async function saveConversionHistoryApi(from, to, amount, rate, convertedAmount) {
        if (amount <= 0) return;
        try {
            await fetch(`/api/convert?from=${from}&to=${to}&amount=${amount}`);
            fetchHistory();
        } catch (e) {}
    }

    // --- FEE CALCULATOR ---
    async function updateFeeSummary() {
        const amount = parseFloat(elements.feeCalcAmount.value) || 1000;
        const from = elements.feeCalcFromSelect.value || 'USD';
        const to = elements.feeCalcToSelect.value || 'EUR';
        const feePct = parseFloat(elements.feePctSlider.value) || 2.5;

        elements.feePctValDisplay.textContent = `${feePct}%`;

        try {
            const res = await fetch(`/api/fee-calc?from=${from}&to=${to}&amount=${amount}&feePct=${feePct}`);
            const data = await res.json();

            const fromInfo = state.currencies[from] || { symbol: from };
            const toInfo = state.currencies[to] || { symbol: to };

            elements.feeSummaryBox.innerHTML = `
                <h3><i class="fa-solid fa-receipt"></i> Fee Breakdown</h3>
                <div class="fee-line"><span>Gross Converted Amount:</span> <strong>${toInfo.symbol} ${formatNumber(data.grossConverted, state.precision)}</strong></div>
                <div class="fee-line text-danger"><span>Bank Fee Cost (${feePct}%):</span> <strong>-${toInfo.symbol} ${formatNumber(data.feeCostTo, state.precision)}</strong> (${fromInfo.symbol} ${formatNumber(data.feeCostFrom, 2)})</div>
                <hr style="border-color:rgba(0,0,0,0.1)">
                <div class="fee-line highlight"><span>Net Received Amount:</span> <h2 class="text-primary" style="margin:0">${toInfo.symbol} ${formatNumber(data.netConverted, state.precision)}</h2></div>
                <small class="text-muted">Effective Rate with Fee: 1 ${from} = ${formatNumber(data.effectiveRate, 4)} ${to} (Real: ${formatNumber(data.realRate, 4)})</small>
            `;
        } catch (e) {}
    }

    // --- TRAVEL MODE & EXPENSES ---
    async function fetchExpenses() {
        try {
            const res = await fetch('/api/expenses');
            state.expenses = await res.json();
            renderExpensesUI();
        } catch (e) {}
    }

    function renderExpensesUI() {
        state.travelBudget = parseFloat(elements.travelBudgetInput.value) || 2000;
        const homeCurr = elements.travelHomeCurrSelect.value || 'USD';
        const homeInfo = state.currencies[homeCurr] || { symbol: '$' };

        let totalSpentHome = 0;
        elements.expensesTableBody.innerHTML = state.expenses.map(exp => {
            totalSpentHome += exp.amountHome || 0;
            return `
                <tr>
                    <td><span class="category-tag">${exp.category}</span></td>
                    <td><strong>${exp.title}</strong></td>
                    <td>${exp.amountTravel} ${exp.travelCurr}</td>
                    <td><strong class="text-primary">${homeInfo.symbol} ${formatNumber(exp.amountHome, 2)}</strong></td>
                    <td><button class="icon-btn text-danger sm" onclick="deleteExpense('${exp.id}')"><i class="fa-solid fa-trash"></i></button></td>
                </tr>
            `;
        }).join('') || '<tr><td colspan="5" class="text-center text-muted">No trip expenses logged yet.</td></tr>';

        elements.spentValDisplay.textContent = `${homeInfo.symbol} ${formatNumber(totalSpentHome, 2)}`;
        elements.budgetValDisplay.textContent = `${homeInfo.symbol} ${formatNumber(state.travelBudget, 2)}`;

        const pct = Math.min((totalSpentHome / state.travelBudget) * 100, 100);
        elements.budgetProgressBar.style.width = `${pct}%`;
    }

    window.deleteExpense = async function(id) {
        await fetch(`/api/expenses?id=${id}`, { method: 'DELETE' });
        fetchExpenses();
        showToast('Expense deleted', 'info');
    };

    // --- CURRENCY COMPARISON MATRIX ---
    async function renderComparisonMatrix() {
        const base = elements.compareBaseSelect.value || 'USD';
        try {
            const res = await fetch(`/api/compare?base=${base}&targets=EUR,GBP,INR,JPY,AED,CAD,AUD`);
            const data = await res.json();

            elements.comparisonTableBody.innerHTML = data.targets.map(item => `
                <tr>
                    <td><strong>${item.flag} ${item.code}</strong> - ${item.name}</td>
                    <td><strong>${item.symbol} ${formatNumber(item.rate, 4)}</strong></td>
                    <td class="${item.change24h >= 0 ? 'text-success' : 'text-danger'}"><strong>${item.change24h >= 0 ? '+' : ''}${formatNumber(item.change24h, 2)}%</strong></td>
                    <td class="${item.change7d >= 0 ? 'text-success' : 'text-danger'}">${item.change7d >= 0 ? '+' : ''}${formatNumber(item.change7d, 2)}%</td>
                    <td class="${item.change30d >= 0 ? 'text-success' : 'text-danger'}">${item.change30d >= 0 ? '+' : ''}${formatNumber(item.change30d, 2)}%</td>
                    <td><small>${formatNumber(item.low30d, 2)} - ${formatNumber(item.high30d, 2)}</small></td>
                </tr>
            `).join('');
        } catch (e) {}
    }

    // --- AI CHATBOT ---
    async function sendAiMessage(prompt) {
        if (!prompt) return;

        // Render User Bubble
        const userBubble = document.createElement('div');
        userBubble.className = 'chat-bubble user';
        userBubble.textContent = prompt;
        elements.aiChatMessages.appendChild(userBubble);
        elements.aiChatInput.value = '';
        elements.aiChatMessages.scrollTop = elements.aiChatMessages.scrollHeight;

        // Fetch AI Answer
        try {
            const res = await fetch('/api/ai-chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt })
            });
            const data = await res.json();

            const botBubble = document.createElement('div');
            botBubble.className = 'chat-bubble bot';
            botBubble.innerHTML = data.reply.replace(/\n/g, '<br>');
            elements.aiChatMessages.appendChild(botBubble);
            elements.aiChatMessages.scrollTop = elements.aiChatMessages.scrollHeight;

        } catch (e) {
            showToast('AI Chat service error', 'danger');
        }
    }

    // --- POPULAR PAIRS & FAVORITES ---
    function renderPopularPairs() {
        const popularList = [
            { from: 'USD', to: 'INR' },
            { from: 'EUR', to: 'INR' },
            { from: 'GBP', to: 'INR' },
            { from: 'USD', to: 'EUR' },
            { from: 'USD', to: 'GBP' },
            { from: 'EUR', to: 'GBP' },
            { from: 'AED', to: 'INR' },
            { from: 'SAR', to: 'INR' },
            { from: 'JPY', to: 'INR' }
        ];

        elements.popularPairsGrid.innerHTML = popularList.map(pair => {
            const fromRate = state.rates[pair.from] || 1;
            const toRate = state.rates[pair.to] || 1;
            const rate = toRate / fromRate;

            const fromInfo = state.currencies[pair.from] || { flag: '🏳️' };
            const toInfo = state.currencies[pair.to] || { flag: '🏳️' };

            return `
                <div class="popular-card" onclick="setConversionPair('${pair.from}', '${pair.to}')">
                    <div class="popular-card-pair">
                        <span>${fromInfo.flag} ${pair.from} → ${toInfo.flag} ${pair.to}</span>
                        <i class="fa-solid fa-chevron-right text-muted"></i>
                    </div>
                    <div class="popular-card-rate">1 ${pair.from} = ${formatNumber(rate, 4)} ${pair.to}</div>
                </div>
            `;
        }).join('');

        renderFavoriteChips();
    }

    function renderFavoriteChips() {
        elements.favChipsContainer.innerHTML = state.favorites.map(code => {
            const info = state.currencies[code] || { name: code, flag: '🏳️' };
            return `
                <div class="fav-chip" onclick="setConversionPair('USD', '${code}')">
                    <span>⭐ ${info.flag} ${code}</span>
                </div>
            `;
        }).join('');
    }

    window.setConversionPair = function(from, to) {
        state.fromCurrency = from;
        state.toCurrency = to;
        updateSelectedCurrencyButtons();
        performConversion(true);
        loadChartData();
        switchTab('converter');
    };

    // --- LIVE RATES TABLE ---
    function renderLiveRatesTable() {
        const query = (elements.ratesTableSearch.value || '').toLowerCase();

        const rows = Object.keys(state.currencies)
            .filter(code => {
                const info = state.currencies[code];
                return code.toLowerCase().includes(query) ||
                       info.name.toLowerCase().includes(query) ||
                       info.country.toLowerCase().includes(query);
            })
            .map(code => {
                const info = state.currencies[code];
                const rateUsd = state.rates[code] || 1;
                const unitInUsd = rateUsd !== 0 ? 1 / rateUsd : 0;

                return `
                    <tr>
                        <td><strong>${info.flag} ${info.name}</strong></td>
                        <td><span class="badge-code">${code}</span></td>
                        <td><strong>${info.symbol} ${formatNumber(rateUsd, 4)}</strong></td>
                        <td>$${formatNumber(unitInUsd, 4)}</td>
                        <td>
                            <button class="btn btn-outline btn-sm" onclick="setConversionPair('USD', '${code}')">
                                <i class="fa-solid fa-arrow-right-arrow-left"></i> Convert
                            </button>
                        </td>
                    </tr>
                `;
            });

        elements.ratesTableBody.innerHTML = rows.join('');
    }

    // --- MULTI CURRENCY CALCULATOR ---
    function renderMultiCurrencyGrid() {
        const amount = parseFloat(elements.multiBaseAmount.value) || 0;
        const baseCode = state.multiBaseCurrencyBtn.getAttribute('data-currency') || 'USD';
        const baseRate = state.rates[baseCode] || 1;

        elements.multiResultsGrid.innerHTML = state.multiTargets.map(targetCode => {
            const targetInfo = state.currencies[targetCode] || { name: targetCode, flag: '🏳️', symbol: targetCode };
            const targetRate = state.rates[targetCode] || 1;
            const converted = (amount / baseRate) * targetRate;

            return `
                <div class="multi-card">
                    <div>
                        <div class="multi-card-title">${targetInfo.flag} <strong>${targetCode}</strong> - ${targetInfo.name}</div>
                        <div class="multi-card-val">${targetInfo.symbol} ${formatNumber(converted, state.precision)}</div>
                    </div>
                    <button class="icon-btn text-danger" onclick="removeMultiTarget('${targetCode}')" title="Remove">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>
            `;
        }).join('');
    }

    window.removeMultiTarget = function(code) {
        state.multiTargets = state.multiTargets.filter(c => c !== code);
        renderMultiCurrencyGrid();
    };

    // --- CHART.JS ---
    function setupChart() {
        const ctx = elements.fxTrendChartCanvas.getContext('2d');
        state.fxChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Exchange Rate',
                    data: [],
                    borderColor: '#4f46e5',
                    backgroundColor: 'rgba(79, 70, 229, 0.08)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.35,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        padding: 12,
                        backgroundColor: 'rgba(15, 23, 42, 0.9)'
                    }
                },
                scales: {
                    x: { grid: { display: false } },
                    y: { grid: { color: 'rgba(0,0,0,0.05)' } }
                }
            }
        });
        loadChartData();
    }

    async function loadChartData() {
        state.chartPair = `${state.fromCurrency}_${state.toCurrency}`;
        elements.chartPairTitle.textContent = `${state.fromCurrency} to ${state.toCurrency} Trend (${state.chartTimeframe})`;

        try {
            const res = await fetch(`/api/historical-rates?pair=${state.chartPair}&range=${state.chartTimeframe}`);
            const data = await res.json();

            state.fxChart.data.labels = data.labels || [];
            state.fxChart.data.datasets[0].data = data.values || [];
            state.fxChart.update();

            const toInfo = state.currencies[state.toCurrency] || { symbol: state.toCurrency };
            elements.chartStatHigh.textContent = `${toInfo.symbol} ${formatNumber(data.high, 4)}`;
            elements.chartStatLow.textContent = `${toInfo.symbol} ${formatNumber(data.low, 4)}`;
            elements.chartStatAvg.textContent = `${toInfo.symbol} ${formatNumber(data.avg, 4)}`;

            const pct = data.changePct || 0;
            elements.chartStatChange.textContent = `${pct >= 0 ? '+' : ''}${formatNumber(pct, 2)}%`;
            elements.chartStatChange.className = `stat-val ${pct >= 0 ? 'text-success' : 'text-danger'}`;

        } catch (e) {}
    }

    // --- RATE ALERTS ---
    async function fetchAlerts() {
        try {
            const res = await fetch('/api/alerts');
            const alerts = await res.json();

            elements.activeAlertsList.innerHTML = alerts.map(alert => `
                <div class="alert-item">
                    <div>
                        <strong>${alert.from} → ${alert.to}</strong>
                        <span class="badge ${alert.condition === 'above' ? 'badge-success' : 'badge-warning'}">
                            ${alert.condition} ${alert.targetRate}
                        </span>
                    </div>
                    <button class="icon-btn text-danger" onclick="deleteAlert('${alert.id}')"><i class="fa-solid fa-trash"></i></button>
                </div>
            `).join('') || '<p class="text-muted">No active rate alerts created yet.</p>';
        } catch (e) {}
    }

    window.deleteAlert = async function(id) {
        await fetch(`/api/alerts?id=${id}`, { method: 'DELETE' });
        fetchAlerts();
        showToast('Alert deleted', 'info');
    };

    // --- HISTORY ---
    async function fetchHistory() {
        try {
            const res = await fetch('/api/history');
            const history = await res.json();

            elements.historyTableBody.innerHTML = history.map(item => {
                const date = new Date(item.timestamp).toLocaleString();
                return `
                    <tr>
                        <td>${date}</td>
                        <td><strong>${item.amount} ${item.from} → ${item.to}</strong></td>
                        <td>1 ${item.from} = ${formatNumber(item.rate, 4)} ${item.to}</td>
                        <td><strong class="text-primary">${formatNumber(item.convertedAmount, state.precision)} ${item.to}</strong></td>
                        <td>
                            <button class="icon-btn text-danger" onclick="deleteHistoryItem('${item.id}')"><i class="fa-solid fa-trash"></i></button>
                        </td>
                    </tr>
                `;
            }).join('') || '<tr><td colspan="5" class="text-center text-muted">No conversion history recorded yet.</td></tr>';
        } catch (e) {}
    }

    window.deleteHistoryItem = async function(id) {
        await fetch(`/api/history?id=${id}`, { method: 'DELETE' });
        fetchHistory();
    };

    // --- NOTIFICATIONS ---
    async function fetchNotifications() {
        try {
            const res = await fetch('/api/notifications');
            const notifs = await res.json();

            elements.notifCountBadge.textContent = notifs.length;
            elements.notifCountBadge.style.display = notifs.length > 0 ? 'inline-block' : 'none';

            elements.notifListContainer.innerHTML = notifs.map(n => `
                <div class="notif-card">
                    <strong>${n.title}</strong>
                    <p>${n.message}</p>
                    <small class="text-muted">${new Date(n.timestamp).toLocaleTimeString()}</small>
                </div>
            `).join('') || '<p class="text-muted text-center p-3">No notifications.</p>';
        } catch (e) {}
    }

    // --- SELECT DROPDOWNS & MODAL ---
    function populateSelectDropdowns() {
        const codes = Object.keys(state.currencies);
        const options = codes.map(code => {
            const info = state.currencies[code];
            return `<option value="${code}">${info.flag} ${code} - ${info.name}</option>`;
        }).join('');

        elements.alertFromSelect.innerHTML = options;
        elements.alertToSelect.innerHTML = options;
        elements.settingDefaultCurrency.innerHTML = options;
        elements.feeCalcFromSelect.innerHTML = options;
        elements.feeCalcToSelect.innerHTML = options;
        elements.travelHomeCurrSelect.innerHTML = options;
        elements.travelDestCurrSelect.innerHTML = options;
        elements.compareBaseSelect.innerHTML = options;

        elements.alertFromSelect.value = 'USD';
        elements.alertToSelect.value = 'INR';
        elements.feeCalcFromSelect.value = 'USD';
        elements.feeCalcToSelect.value = 'EUR';
        elements.travelHomeCurrSelect.value = 'USD';
        elements.travelDestCurrSelect.value = 'EUR';
        elements.compareBaseSelect.value = 'USD';
    }

    function openCurrencyModal(targetType) {
        state.activeModalTarget = targetType;
        elements.modalSearchInput.value = '';
        renderModalCurrencyList('');
        elements.currencyModal.classList.add('show');
        elements.modalSearchInput.focus();
    }

    function closeModal() {
        elements.currencyModal.classList.remove('show');
    }

    function renderModalCurrencyList(filter) {
        const query = filter.toLowerCase();
        const items = Object.keys(state.currencies)
            .filter(code => {
                const info = state.currencies[code];
                return code.toLowerCase().includes(query) ||
                       info.name.toLowerCase().includes(query) ||
                       info.country.toLowerCase().includes(query);
            })
            .map(code => {
                const info = state.currencies[code];
                return `
                    <div class="currency-option-item" onclick="selectModalCurrency('${code}')">
                        <span class="currency-flag">${info.flag}</span>
                        <div>
                            <strong>${code}</strong> - ${info.name}
                            <div class="text-muted" style="font-size:0.75rem">${info.country}</div>
                        </div>
                    </div>
                `;
            }).join('');

        elements.modalCurrencyList.innerHTML = items;
    }

    window.selectModalCurrency = function(code) {
        closeModal();
        if (state.activeModalTarget === 'from') state.fromCurrency = code;
        else if (state.activeModalTarget === 'to') state.toCurrency = code;
        else if (state.activeModalTarget === 'multiBase') {
            state.multiBaseCurrencyBtn.setAttribute('data-currency', code);
            const info = state.currencies[code];
            elements.multiBaseFlag.textContent = info.flag;
            elements.multiBaseCode.textContent = code;
            renderMultiCurrencyGrid();
            return;
        } else if (state.activeModalTarget === 'addMultiTarget') {
            if (!state.multiTargets.includes(code)) {
                state.multiTargets.push(code);
                renderMultiCurrencyGrid();
            }
            return;
        }

        updateSelectedCurrencyButtons();
        performConversion(true);
        loadChartData();
    };

    function updateSelectedCurrencyButtons() {
        const fromInfo = state.currencies[state.fromCurrency] || { flag: '🇺🇸', name: 'US Dollar' };
        const toInfo = state.currencies[state.toCurrency] || { flag: '🇮🇳', name: 'Indian Rupee' };

        elements.fromFlag.textContent = fromInfo.flag;
        elements.fromCode.textContent = state.fromCurrency;
        elements.fromName.textContent = fromInfo.name;

        elements.toFlag.textContent = toInfo.flag;
        elements.toCode.textContent = state.toCurrency;
        elements.toName.textContent = toInfo.name;
    }

    // --- EVENT LISTENERS ---
    function setupEventListeners() {
        // Tab Nav
        elements.navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                switchTab(link.getAttribute('data-tab'));
            });
        });

        // Main Converter listeners
        elements.amountInput.addEventListener('input', () => performConversion(false));
        elements.amountInput.addEventListener('change', () => performConversion(true));
        elements.convertBtn.addEventListener('click', () => performConversion(true));

        document.querySelectorAll('.quick-amt-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.quick-amt-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                elements.amountInput.value = btn.getAttribute('data-amt');
                performConversion(true);
            });
        });

        elements.swapBtn.addEventListener('click', () => {
            const temp = state.fromCurrency;
            state.fromCurrency = state.toCurrency;
            state.toCurrency = temp;
            updateSelectedCurrencyButtons();
            performConversion(true);
            loadChartData();
        });

        // Modals
        elements.fromCurrencyBtn.addEventListener('click', () => openCurrencyModal('from'));
        elements.toCurrencyBtn.addEventListener('click', () => openCurrencyModal('to'));
        elements.multiBaseCurrencyBtn.addEventListener('click', () => openCurrencyModal('multiBase'));
        elements.addMultiTargetBtn.addEventListener('click', () => openCurrencyModal('addMultiTarget'));
        elements.closeModalBtn.addEventListener('click', closeModal);
        elements.modalSearchInput.addEventListener('input', (e) => renderModalCurrencyList(e.target.value));

        // Fee Calc listeners
        elements.feeCalcAmount.addEventListener('input', updateFeeSummary);
        elements.feeCalcFromSelect.addEventListener('change', updateFeeSummary);
        elements.feeCalcToSelect.addEventListener('change', updateFeeSummary);
        elements.feePctSlider.addEventListener('input', updateFeeSummary);

        document.querySelectorAll('.fee-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                document.querySelectorAll('.fee-chip').forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                elements.feePctSlider.value = chip.getAttribute('data-fee');
                updateFeeSummary();
            });
        });

        // Travel Mode listeners
        elements.travelHomeCurrSelect.addEventListener('change', renderExpensesUI);
        elements.travelDestCurrSelect.addEventListener('change', renderExpensesUI);
        elements.travelBudgetInput.addEventListener('input', renderExpensesUI);

        elements.addExpenseForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = elements.expenseTitleInput.value;
            const category = elements.expenseCategorySelect.value;
            const amountTravel = parseFloat(elements.expenseAmountInput.value);
            const travelCurr = elements.travelDestCurrSelect.value;
            const homeCurr = elements.travelHomeCurrSelect.value;

            const travelRate = state.rates[travelCurr] || 1;
            const homeRate = state.rates[homeCurr] || 1;
            const amountHome = (amountTravel / travelRate) * homeRate;

            await fetch('/api/expenses', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, category, amountTravel, travelCurr, amountHome, homeCurr })
            });

            elements.expenseTitleInput.value = '';
            elements.expenseAmountInput.value = '';
            showToast('Expense logged successfully', 'success');
            fetchExpenses();
        });

        elements.exportExpensesCsvBtn.addEventListener('click', () => {
            let csv = 'Category,Title,AmountTravel,TravelCurr,AmountHome,HomeCurr,Timestamp\n';
            state.expenses.forEach(e => {
                csv += `"${e.category}","${e.title}",${e.amountTravel},"${e.travelCurr}",${e.amountHome},"${e.homeCurr}","${e.timestamp}"\n`;
            });
            const blob = new Blob([csv], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `travel-expenses-${Date.now()}.csv`;
            a.click();
        });

        // Comparison Matrix
        elements.compareBaseSelect.addEventListener('change', renderComparisonMatrix);

        // AI Chatbot
        elements.aiChatForm.addEventListener('submit', (e) => {
            e.preventDefault();
            sendAiMessage(elements.aiChatInput.value);
        });

        document.querySelectorAll('.ai-chip').forEach(chip => {
            chip.addEventListener('click', () => sendAiMessage(chip.getAttribute('data-prompt')));
        });

        // Auth Tabs & Forms
        elements.authTabLogin.addEventListener('click', () => {
            elements.authTabLogin.classList.add('active');
            elements.authTabRegister.classList.remove('active');
            elements.loginForm.classList.add('active');
            elements.registerForm.classList.remove('active');
        });

        elements.authTabRegister.addEventListener('click', () => {
            elements.authTabRegister.classList.add('active');
            elements.authTabLogin.classList.remove('active');
            elements.registerForm.classList.add('active');
            elements.loginForm.classList.remove('active');
        });

        elements.closeAuthModalBtn.addEventListener('click', () => elements.authModal.classList.remove('show'));

        elements.loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('loginEmail').value;
            const password = document.getElementById('loginPassword').value;

            try {
                const res = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                const data = await res.json();
                if (data.success) {
                    state.authToken = data.token;
                    state.currentUser = data.user;
                    localStorage.setItem('currencyx_token', data.token);
                    renderAuthHeader();
                    elements.authModal.classList.remove('show');
                    showToast(`Welcome back, ${data.user.name}!`, 'success');
                } else {
                    showToast(data.error || 'Login failed', 'danger');
                }
            } catch (err) {
                showToast('Login request failed', 'danger');
            }
        });

        elements.registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('regName').value;
            const email = document.getElementById('regEmail').value;
            const password = document.getElementById('regPassword').value;

            try {
                const res = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password })
                });
                const data = await res.json();
                if (data.success) {
                    state.authToken = data.token;
                    state.currentUser = data.user;
                    localStorage.setItem('currencyx_token', data.token);
                    renderAuthHeader();
                    elements.authModal.classList.remove('show');
                    showToast(`Account created! Welcome, ${data.user.name}!`, 'success');
                } else {
                    showToast(data.error || 'Registration failed', 'danger');
                }
            } catch (err) {
                showToast('Registration request failed', 'danger');
            }
        });

        // Other UI
        elements.ratesTableSearch.addEventListener('input', renderLiveRatesTable);
        elements.multiBaseAmount.addEventListener('input', renderMultiCurrencyGrid);

        elements.timeframeBtnGroup.querySelectorAll('.range-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                elements.timeframeBtnGroup.querySelectorAll('.range-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.chartTimeframe = btn.getAttribute('data-range');
                loadChartData();
            });
        });

        elements.createAlertForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const from = elements.alertFromSelect.value;
            const to = elements.alertToSelect.value;
            const targetRate = parseFloat(elements.alertTargetRateInput.value);
            const condition = elements.alertConditionSelect.value;

            await fetch('/api/alerts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ from, to, targetRate, condition })
            });

            showToast('Rate alert created!', 'success');
            elements.alertTargetRateInput.value = '';
            fetchAlerts();
        });

        elements.clearAllHistoryBtn.addEventListener('click', async () => {
            if (confirm('Clear all conversion history?')) {
                await fetch('/api/history', { method: 'DELETE' });
                fetchHistory();
                showToast('History cleared', 'info');
            }
        });

        elements.exportHistoryCsvBtn.addEventListener('click', async () => {
            const res = await fetch('/api/history');
            const history = await res.json();
            let csv = 'Date,From,To,Amount,Rate,ConvertedAmount\n';
            history.forEach(h => {
                csv += `"${h.timestamp}","${h.from}","${h.to}",${h.amount},${h.rate},${h.convertedAmount}\n`;
            });
            const blob = new Blob([csv], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `conversion-history-${Date.now()}.csv`;
            a.click();
        });

        elements.themeToggleBtn.addEventListener('click', toggleTheme);
        elements.langSelect.addEventListener('change', (e) => window.i18nEngine.setLanguage(e.target.value));

        elements.notifBellBtn.addEventListener('click', () => elements.notifDrawer.classList.add('show'));
        elements.closeNotifDrawerBtn.addEventListener('click', () => elements.notifDrawer.classList.remove('show'));
        elements.clearNotifsBtn.addEventListener('click', async () => {
            await fetch('/api/notifications', { method: 'DELETE' });
            fetchNotifications();
        });

        elements.settingsBtn.addEventListener('click', () => elements.settingsModal.classList.add('show'));
        elements.closeSettingsBtn.addEventListener('click', () => elements.settingsModal.classList.remove('show'));
        elements.settingsForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            state.precision = parseInt(elements.settingPrecision.value);
            state.refreshInterval = parseInt(elements.settingRefreshInterval.value);

            await fetch('/api/settings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    defaultCurrency: elements.settingDefaultCurrency.value,
                    precision: state.precision,
                    refreshInterval: state.refreshInterval,
                    pushNotifications: elements.settingPushNotifications.checked
                })
            });

            elements.settingsModal.classList.remove('show');
            showToast('Settings saved', 'success');
            performConversion();
        });

        elements.copyResultBtn.addEventListener('click', () => {
            const text = `${elements.resultFromDisplay.textContent} ${elements.resultAmountDisplay.textContent}`;
            navigator.clipboard.writeText(text);
            showToast('Copied to clipboard! 📋', 'success');
        });

        elements.shareResultBtn.addEventListener('click', () => {
            const text = `${elements.resultFromDisplay.textContent} ${elements.resultAmountDisplay.textContent}`;
            if (navigator.share) navigator.share({ title: 'Currency Conversion', text });
            else {
                navigator.clipboard.writeText(text);
                showToast('Conversion link copied!', 'info');
            }
        });

        elements.printResultBtn.addEventListener('click', () => window.print());

        window.addEventListener('online', () => {
            state.isOffline = false;
            updateConnectionBadge();
            fetchLiveRates();
            showToast('Network online. Live rates active.', 'success');
        });

        window.addEventListener('offline', () => {
            state.isOffline = true;
            updateConnectionBadge();
            showToast('You are offline. Showing cached rates.', 'warning');
        });
    }

    function switchTab(tabId) {
        elements.navLinks.forEach(l => l.classList.remove('active'));
        elements.tabSections.forEach(s => s.classList.remove('active'));

        const activeNav = document.querySelector(`.nav-link[data-tab="${tabId}"]`);
        const activeSec = document.getElementById(tabId);

        if (activeNav) activeNav.classList.add('active');
        if (activeSec) activeSec.classList.add('active');

        if (tabId === 'charts') {
            if (state.fxChart) {
                state.fxChart.resize();
            }
            loadChartData();
        }
    }

    function updateConnectionBadge() {
        if (state.isOffline) {
            elements.connectionStatus.className = 'status-badge cached';
            elements.statusText.textContent = 'CACHED';
        } else {
            elements.connectionStatus.className = 'status-badge live';
            elements.statusText.textContent = 'LIVE';
        }
    }

    function toggleTheme() {
        state.theme = state.theme === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', state.theme);
        elements.themeToggleBtn.innerHTML = state.theme === 'dark' ? '<i class="fa-solid fa-sun"></i>' : '<i class="fa-solid fa-moon"></i>';
        localStorage.setItem('currencyx_theme', state.theme);
    }

    function loadSavedTheme() {
        const saved = localStorage.getItem('currencyx_theme') || 'light';
        state.theme = saved;
        document.documentElement.setAttribute('data-theme', saved);
        elements.themeToggleBtn.innerHTML = saved === 'dark' ? '<i class="fa-solid fa-sun"></i>' : '<i class="fa-solid fa-moon"></i>';
    }

    async function loadSettings() {
        try {
            const res = await fetch('/api/settings');
            const data = await res.json();
            state.precision = data.precision || 2;
            state.refreshInterval = data.refreshInterval || 5;
            elements.settingPrecision.value = state.precision;
            elements.settingRefreshInterval.value = state.refreshInterval;
        } catch (e) {}
    }

    function formatNumber(num, decimals = 2) {
        if (isNaN(num)) return '0.00';
        return num.toLocaleString(undefined, {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `<i class="fa-solid fa-circle-info"></i> ${message}`;
        document.getElementById('toastContainer').appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    }

    function registerServiceWorker() {
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('sw.js').catch(err => console.log('SW registration error:', err));
        }
    }

    // START
    init();
});
