(function () {
    var btn = document.getElementById('theme-toggle');

    function isDark() {
        return document.documentElement.getAttribute('data-theme') === 'dark';
    }

    function applyTheme(dark) {
        if (dark) {
            document.documentElement.setAttribute('data-theme', 'dark');
            localStorage.setItem('theme', 'dark');
        } else {
            document.documentElement.removeAttribute('data-theme');
            localStorage.setItem('theme', 'light');
        }
    }

    btn.addEventListener('click', function () {
        applyTheme(!isDark());
    });
})();
