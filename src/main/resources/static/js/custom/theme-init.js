(function () {
    var saved = localStorage.getItem('theme');
    var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    var dark = saved === 'dark' || (!saved && prefersDark);
    if (dark) document.documentElement.setAttribute('data-theme', 'dark');
})();
