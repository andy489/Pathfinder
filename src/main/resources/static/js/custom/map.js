fetch(`http://localhost:8080/api/routes/coordinates/${routeId}`)
    .then(res => res.json())
    .then(res => {
        let coordinates = res.coordinates
        // center = coordinates[Math.floor(coordinates.length / 2)] // dummy center

        let centerAndRadius = getCenterAndRadius(coordinates);
        // let center = centerAndRadius[0]
        // let radius = centerAndRadius[1]

        const middleLat = (res.minLat + res.maxLat) / 2
        const middleLon = (res.minLon + res.maxLon) / 2

        let center = [middleLat, middleLon]

        let zoom = res.zoom

        let totalDistanceInKm = calcTotalDist(coordinates);

        document.getElementById('totalDistance').textContent = totalDistanceInKm.toFixed(2)

        // TO MAKE THE MAP APPEAR YOU MUST
        // ADD YOUR ACCESS TOKEN FROM
        // https://account.mapbox.com
        // mapboxgl.accessToken = 'YOUR_MAPBOX_ACCESS_TOKEN';
        mapboxgl.accessToken = 'pk.eyJ1IjoiYW5keTQ4OSIsImEiOiJjbHJ4eHdpdXkxYmdrMmptdWJpN3Rweml5In0.Wa6J4RI23xY0CyWD-UxZjw';
        const map = new mapboxgl.Map({
            container: 'map',
            // Choose from Mapbox's core styles, or make your own style with Mapbox Studio
            style: 'mapbox://styles/mapbox/streets-v12',
            center: center,
            zoom: zoom
        });

        map.on('load', () => {
            map.addSource('route', {
                'type': 'geojson',
                'data': {
                    'type': 'Feature',
                    'properties': {},
                    'geometry': {
                        'type': 'LineString',
                        'coordinates': coordinates
                        // [
                        //     [-122.483696, 37.833818], [-122.483482, 37.833174], [-122.483396, 37.8327],
                        //     [-122.483568, 37.832056], [-122.48404, 37.831141], [-122.48404, 37.830497]
                        // ]
                    }
                }
            });
            map.addLayer({
                'id': 'route',
                'type': 'line',
                'source': 'route',
                'layout': {
                    'line-join': 'round',
                    'line-cap': 'round'
                },
                'paint': {
                    'line-color': '#a82835',
                    'line-width': 3
                }
            });
        });

    })

function getCenterAndRadius(coordinates) {
    const latCoordinates = coordinates.map(latAndLon => latAndLon[0])
    const minLat = Math.min(...latCoordinates)
    const maxLat = Math.max(...latCoordinates)

    const lonCoordinates = coordinates.map(latAndLon => latAndLon[1])
    const minLon = Math.min(...lonCoordinates)
    const maxLon = Math.max(...lonCoordinates)

    // console.log('minLat: ' + minLat + ', maxLat: ' + maxLat + ', minLon: ' + minLon + ', maxLon: ' + maxLon)

    const middleLat = (minLat + maxLat) / 2
    const middleLon = (minLon + maxLon) / 2

    let center = [middleLat, middleLon]

    let radiusToMin = calcTotalDist([center, [minLat, minLon]]);
    let radiusToMax = calcTotalDist([center, [maxLat, maxLon]]);

    let radius = Math.max(radiusToMin, radiusToMax)

    return [center, radius]
}


function calcTotalDist(coordinates) {
    let totalDist = 0

    if (coordinates.length > 1) {
        for (let i = 0; i < coordinates.length - 1; i++) {

            let currPoint = coordinates[i]
            let nextPoint = coordinates[i + 1]

            let lat1 = currPoint[0]
            let lon1 = currPoint[1]
            let lat2 = nextPoint[0]
            let lon2 = nextPoint[1]

            let lat1Radians = (lat1 / 180) * Math.PI
            let lon1Radians = (lon1 / 180) * Math.PI
            let lat2Radians = (lat2 / 180) * Math.PI
            let lon2Radians = (lon2 / 180) * Math.PI

            const IN_MILES = 3959
            const IN_KM = 6371

            let currDist = Math.acos(Math.sin(lat1Radians) * Math.sin(lat2Radians) +
                Math.cos(lat1Radians) * Math.cos(lat2Radians) * Math.cos(lon2Radians - lon1Radians)) * IN_KM

            totalDist += currDist
        }
    }

    return totalDist
}
