const cityForm = document.querySelector('form');
const details = document.querySelector('.details');
const card = document.querySelector('.card');
const time = document.querySelector('img.time');
const icon = document.querySelector('.icon img');
const forecast = new Forecast();


// console.log(forecast);

const updateUI = (data) => {

  const cityDets = data.cityDets;
  const weather = data.weather;


  details.innerHTML = `
      <h5 class="my-3">${cityDets.LocalizedName}</h5>
      <div class="my-3">${weather.WeatherText}</div>
      <div class="display-4 my-4">
          <span>${weather.Temperature.Metric.Value}</span>
          <span>&deg;C</span>
      </div>    
  `

  // Remove the d-none and show the detail of the weather
  card.classList.contains('d-none') ? card.classList.remove('d-none') : null;

  let timeSrc = null;

  weather.IsDayTime == true ? timeSrc = 'img/day.svg' : timeSrc = 'img/night.svg';

  time.setAttribute('src', timeSrc);

  const iconSrc = `img/icons/${weather.WeatherIcon}.svg`;

  icon.setAttribute('src', iconSrc);
}


cityForm.addEventListener('submit', e => {

  // Prevent default action
  e.preventDefault();

  // push input from the input field

  const city = cityForm.city.value.trim();
  cityForm.reset();

  forecast.updateCity(city)
        .then(data => updateUI(data))
        .catch(err => console.log(err))

        localStorage.setItem('city', city);
})

if(localStorage.getItem('city')){
  forecast.updateCity(localStorage.getItem('city'))
    .then(data => updateUI(data))
    .catch(err => console.log(err))
}

// localStorage.setItem('name', 'Yonas');

// const name = localStorage.getItem('name');

// console.log(name);

// const updateCity = async (city) => {

//   const cityDets = await getCity(city);
//   const weather = await getWeather(cityDets.Key);

//   return {
//     cityDets,
//     weather
//   }
  
// }
