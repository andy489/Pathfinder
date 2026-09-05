fetch('/api/weather')
    .then(res => res.json())
    .then(cities => {
        cities.forEach((city, index) => {
            const tempEl = document.getElementById('box-' + index + '-temp');
            const imgEl = document.getElementById('box-' + index + '-img');
            if (tempEl) tempEl.innerText = city.tempCelsius;
            if (imgEl) imgEl.src = '/images/weather-icons/' + city.icon + '.png';
        });
    })
    .catch(() => {
        // Weather unavailable — silently leave placeholder values
    });
