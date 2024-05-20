const key = 'N3zIhmHxFCRMTI7RGm8uhTDUDVhgwKkG';

// http://dataservice.accuweather.com/currentconditions/v1/  ; This is the getWeather base api
// http://dataservice.accuweather.com/locations/v1/cities/search ; This is the getCity base api

const getWeather = async (id) => {

    // API Request for get the weather information
    const base = 'http://dataservice.accuweather.com/currentconditions/v1/';
    const query = `${id}?apikey=${key}`;
    const request = base + query;

    //API Response for get the weather information
    const response = await fetch (request);
    const data = await response.json();

    return data[0];
}

const getCity = async (city) => {

    // API Request for get city information
    const base = 'http://dataservice.accuweather.com/locations/v1/cities/search';
    const query = `?apikey=${key}&q=${city}`;
    const request = base + query;

    //API Response for get city information

    const response = await fetch(request);
    const data = await response.json();

    return data[0];   
}


// getCity('addis ababa')
//        .then(data => getWeather(data.Key))
//        .then(data => console.log(data))
//        .catch(err => console.log(err))