document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    menuToggle.addEventListener('click', toggleNav);
    overlay.addEventListener('click', closeNav);

    function toggleNav() {
        sidebar.classList.toggle('open');
        overlay.style.display = sidebar.classList.contains('open') ? "block" : "none";
    }

    function closeNav() {
        sidebar.classList.remove('open');
        overlay.style.display = "none";
    }
});