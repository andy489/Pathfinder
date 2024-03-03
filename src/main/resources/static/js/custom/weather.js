// https://openweathermap.org/weather-conditions

const boxImgAA = document.getElementById('box-a-img')
const boxImgBB = document.getElementById('box-b-img')

const boxTempA = document.getElementById('box-a-temp')
const boxTempB = document.getElementById('box-b-temp')

const KELVIN_ZERO = -273.15;

fetch('http://api.openweathermap.org/data/2.5/weather?q=Sofia&appid=8dd1b8c6c70655b59ef4f75b4d9fb753')
    .then(data => data.json())
    .then(info => {
        // console.log(info)

        // Formula Kelvin to Celsius 299K - 273.15 = 25.85°C
        boxTempA.innerText = Math.round(info.main.temp + KELVIN_ZERO);
        boxImgAA.src = '/images/weather-icons/' + info.weather[0].icon + '.png'
    })


fetch('http://api.openweathermap.org/data/2.5/weather?q=Sozopol&appid=8dd1b8c6c70655b59ef4f75b4d9fb753')
    .then(data => data.json())
    .then(info => {
        // console.log(info)

        // Formula Kelvin to Celsius 299K - 273.15 = 25.85°C
        boxTempB.innerText = Math.round(info.main.temp + KELVIN_ZERO);
        boxImgBB.src = '/images/weather-icons/' + info.weather[0].icon + '.png'
    })