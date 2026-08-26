/* ============================================================
   SmartSchool — PWA: Service Worker Registration + Install Banner
   ============================================================ */

(function () {
  'use strict';

  let deferredInstallPrompt = null;

  /* ---- Register Service Worker ---- */
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/service-worker.js')
        .then(reg => {
          console.log('[PWA] Service Worker registered. Scope:', reg.scope);

          // Notify if update available
          reg.addEventListener('updatefound', () => {
            const newWorker = reg.installing;
            newWorker.addEventListener('statechange', () => {
              if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                if (typeof UI !== 'undefined') {
                  UI.toast('A new version of SmartSchool is available. Refresh to update.', 'info', 8000);
                }
              }
            });
          });
        })
        .catch(err => console.warn('[PWA] Service Worker registration failed:', err));
    });
  }

  /* ---- Capture Install Prompt ---- */
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredInstallPrompt = e;
    showInstallBanner();
  });

  function showInstallBanner() {
    const banner = document.getElementById('install-banner');
    if (!banner) return;
    banner.classList.add('visible');
  }

  /* ---- Install Button click ---- */
  window.installApp = async function () {
    const banner = document.getElementById('install-banner');
    if (!deferredInstallPrompt) return;
    deferredInstallPrompt.prompt();
    const { outcome } = await deferredInstallPrompt.userChoice;
    if (outcome === 'accepted') {
      console.log('[PWA] User accepted the install prompt');
      if (banner) banner.classList.remove('visible');
    }
    deferredInstallPrompt = null;
  };

  /* ---- Dismiss Banner ---- */
  window.dismissInstallBanner = function () {
    const banner = document.getElementById('install-banner');
    if (banner) banner.classList.remove('visible');
  };

  /* ---- Offline / Online Detection ---- */
  function updateOnlineStatus() {
    const offlineBanner = document.getElementById('offline-banner');
    if (offlineBanner) {
      offlineBanner.style.display = navigator.onLine ? 'none' : 'flex';
    }
    if (!navigator.onLine && typeof UI !== 'undefined') {
      UI.toast('You are offline. Some features may not be available.', 'warning', 6000);
    }
  }

  window.addEventListener('online',  updateOnlineStatus);
  window.addEventListener('offline', updateOnlineStatus);
  window.addEventListener('DOMContentLoaded', updateOnlineStatus);

})();
