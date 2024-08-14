document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    const main = document.querySelector('main');

    menuToggle.addEventListener('click', toggleNav);
    overlay.addEventListener('click', closeNav);

    function toggleNav() {
        if (sidebar.style.width === "250px") {
            closeNav();
        } else {
            openNav();
        }
    }

    function openNav() {
        sidebar.style.width = "250px";
        main.style.marginLeft = "250px";
        overlay.style.display = "block";
    }

    function closeNav() {
        sidebar.style.width = "0";
        main.style.marginLeft = "0";
        overlay.style.display = "none";
    }
});