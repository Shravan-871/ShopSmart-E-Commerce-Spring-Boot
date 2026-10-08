(function () {
  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return Array.from((root || document).querySelectorAll(sel)); }

  function closeAllDropdowns() {
    qsa('.nav-dropdown.open').forEach(function (el) { el.classList.remove('open'); });
  }

  function closeDrawer() {
    var nav = qs('.navbar');
    var overlay = qs('.nav-overlay');
    if (nav) nav.classList.remove('nav-open');
    if (overlay) overlay.classList.remove('show');
  }

  window.toggleDarkMode = function () {
    document.body.classList.toggle('dark-mode');
    localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
  };

  if (localStorage.getItem('darkMode') === 'true') {
    document.body.classList.add('dark-mode');
  }

  document.addEventListener('DOMContentLoaded', function () {
    var nav = qs('.navbar');
    if (!nav) return;

    var toggle = qs('.nav-toggle', nav);
    var overlay = qs('.nav-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.className = 'nav-overlay';
      document.body.appendChild(overlay);
    }

    if (toggle) {
      toggle.addEventListener('click', function (e) {
        e.stopPropagation();
        nav.classList.toggle('nav-open');
        overlay.classList.toggle('show', nav.classList.contains('nav-open'));
        if (!nav.classList.contains('nav-open')) closeAllDropdowns();
      });
    }

    overlay.addEventListener('click', function () {
      closeDrawer();
      closeAllDropdowns();
    });

    qsa('.nav-dropdown-btn', nav).forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.stopPropagation();
        var dropdown = btn.closest('.nav-dropdown');
        var wasOpen = dropdown.classList.contains('open');
        closeAllDropdowns();
        if (!wasOpen) dropdown.classList.add('open');
      });
    });

    document.addEventListener('click', function () {
      closeAllDropdowns();
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') {
        closeAllDropdowns();
        closeDrawer();
      }
    });

    qsa('.nav-menu a', nav).forEach(function (link) {
      link.addEventListener('click', function () {
        if (window.matchMedia('(max-width: 960px)').matches) closeDrawer();
      });
    });
  });
})();
