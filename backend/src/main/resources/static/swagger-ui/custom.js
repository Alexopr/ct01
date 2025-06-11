// CT.01 Swagger UI Custom JavaScript
// Расширяет функциональность документации API

(function() {
    'use strict';

    // Ожидаем загрузки Swagger UI
    function waitForSwaggerUI() {
        if (typeof window.ui !== 'undefined' && window.ui) {
            initializeCustomizations();
        } else {
            setTimeout(waitForSwaggerUI, 100);
        }
    }

    function initializeCustomizations() {
        console.log('🚀 CT.01 Swagger UI customizations loaded');
        
        // Добавляем кастомные элементы
        addCustomHeader();
        addCustomFooter();
        addEnvironmentSelector();
        enhanceResponsesViewer();
        
        // Добавляем keyboard shortcuts
        addKeyboardShortcuts();
    }

    // Добавляет кастомный заголовок
    function addCustomHeader() {
        const topbar = document.querySelector('.swagger-ui .topbar');
        if (topbar && !document.querySelector('.ct01-custom-header')) {
            const customHeader = document.createElement('div');
            customHeader.className = 'ct01-custom-header';
            customHeader.innerHTML = `
                <div style="background: linear-gradient(135deg, #0ea5e9 0%, #a855f7 100%); 
                           padding: 0.5rem 1rem; color: white; font-weight: 600; 
                           border-radius: 6px; margin-left: 1rem; display: inline-block;">
                    📊 CT.01 API v1.0.0
                </div>
                <div style="margin-left: 1rem; color: #8b949e; font-size: 0.9rem; display: inline-block;">
                    Environment: Development
                </div>
            `;
            topbar.appendChild(customHeader);
        }
    }

    // Добавляет кастомный футер
    function addCustomFooter() {
        const wrapper = document.querySelector('.swagger-ui .wrapper');
        if (wrapper && !document.querySelector('.ct01-custom-footer')) {
            const footer = document.createElement('div');
            footer.className = 'ct01-custom-footer';
            footer.innerHTML = `
                <div style="margin-top: 3rem; padding: 2rem; background: #161b22; 
                           border: 1px solid #30363d; border-radius: 8px; text-align: center;">
                    <h3 style="color: #f0f6fc; margin-bottom: 1rem;">🔗 Полезные ссылки</h3>
                    <div style="display: flex; justify-content: center; gap: 2rem; flex-wrap: wrap;">
                        <a href="/api/v3/api-docs" target="_blank" 
                           style="color: #0ea5e9; text-decoration: none; font-weight: 500;">
                            📄 OpenAPI JSON
                        </a>
                        <a href="#" onclick="window.open('/actuator/health', '_blank')" 
                           style="color: #10b981; text-decoration: none; font-weight: 500;">
                            ❤️ Health Check
                        </a>
                        <a href="https://github.com/ct01-project" target="_blank" 
                           style="color: #a855f7; text-decoration: none; font-weight: 500;">
                            🐙 GitHub
                        </a>
                    </div>
                    <p style="color: #8b949e; margin-top: 1rem; font-size: 0.9rem;">
                        © 2024 CT.01 - Cryptocurrency Trading Dashboard
                    </p>
                </div>
            `;
            wrapper.appendChild(footer);
        }
    }

    // Добавляет селектор окружения
    function addEnvironmentSelector() {
        const servers = document.querySelector('.servers');
        if (servers && !document.querySelector('.ct01-env-selector')) {
            const envSelector = document.createElement('div');
            envSelector.className = 'ct01-env-selector';
            envSelector.innerHTML = `
                <div style="background: #161b22; border: 1px solid #30363d; 
                           border-radius: 6px; padding: 1rem; margin: 1rem 0;">
                    <label style="color: #f0f6fc; font-weight: 600; display: block; margin-bottom: 0.5rem;">
                        🌐 Environment:
                    </label>
                    <select id="environmentSelect" style="background: #0a0b0d; border: 1px solid #30363d; 
                                                         color: #f0f6fc; padding: 0.5rem; border-radius: 4px; width: 100%;">
                        <option value="http://localhost:8080">Development (localhost:8080)</option>
                        <option value="https://api-staging.ct01.com">Staging</option>
                        <option value="https://api.ct01.com">Production</option>
                    </select>
                </div>
            `;
            servers.parentNode.insertBefore(envSelector, servers);
            
            // Обработчик изменения окружения
            document.getElementById('environmentSelect').addEventListener('change', function(e) {
                console.log('🔄 Environment changed to:', e.target.value);
                // Здесь можно добавить логику смены сервера
            });
        }
    }

    // Улучшает отображение ответов
    function enhanceResponsesViewer() {
        // Добавляем обработчики для копирования ответов
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('copy-to-clipboard')) {
                const responseBody = e.target.closest('.response').querySelector('pre code');
                if (responseBody) {
                    navigator.clipboard.writeText(responseBody.textContent).then(() => {
                        showNotification('✅ Response copied to clipboard!');
                    });
                }
            }
        });
    }

    // Добавляет горячие клавиши
    function addKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ctrl + K - фокус на поиск
            if (e.ctrlKey && e.key === 'k') {
                e.preventDefault();
                const filterInput = document.querySelector('.filter input');
                if (filterInput) {
                    filterInput.focus();
                }
            }
            
            // Ctrl + Enter - выполнить первый найденный endpoint
            if (e.ctrlKey && e.key === 'Enter') {
                e.preventDefault();
                const firstTryOut = document.querySelector('.try-out__btn');
                if (firstTryOut && firstTryOut.textContent.includes('Try it out')) {
                    firstTryOut.click();
                }
            }
        });
    }

    // Показывает уведомление
    function showNotification(message) {
        const notification = document.createElement('div');
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed; top: 20px; right: 20px; z-index: 9999;
            background: #10b981; color: white; padding: 1rem;
            border-radius: 6px; font-weight: 600;
            animation: slideIn 0.3s ease-out;
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    // Добавляем CSS анимации
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
        
        .ct01-custom-header {
            display: flex;
            align-items: center;
            margin-left: auto;
        }
        
        @media (max-width: 768px) {
            .ct01-custom-header {
                flex-direction: column;
                gap: 0.5rem;
            }
        }
    `;
    document.head.appendChild(style);

    // Инициализация
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', waitForSwaggerUI);
    } else {
        waitForSwaggerUI();
    }
})(); 