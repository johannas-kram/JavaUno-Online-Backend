
function loadTheme(){
    let theme = localStorage.getItem('theme');
    if(theme !== 'dark'){
        theme = 'light';
    } else if(app != null) {
        app.darkMode = true;
    }
    setTheme(theme);
}

function toggleTheme(){
    app.darkMode = !app.darkMode;
    const theme = app.darkMode ? 'dark' : 'light';
    setTheme(theme);
}

function setTheme(theme){
    document.querySelector('html').setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
}

loadTheme();