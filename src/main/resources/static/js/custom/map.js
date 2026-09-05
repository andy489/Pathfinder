const mapRouteId = document.getElementById('curr-route-id')?.value;

fetch(`/api/routes/coordinates/${mapRouteId}`)
    .then(res => {
        if (!res.ok) throw new Error('Failed to load route coordinates: ' + res.status);
        return res.json();
    })
    .then(res => {
        const coordinates = res.coordinates;

        const totalDistanceInKm = calcTotalDist(coordinates);
        document.getElementById('totalDistance').textContent = totalDistanceInKm.toFixed(2);

        // Leaflet uses [lat, lon]; coordinates from API are [lon, lat]
        const latLngs = coordinates.map(c => [c[1], c[0]]);

        const centerLat = (res.minLat + res.maxLat) / 2;
        const centerLon = (res.minLon + res.maxLon) / 2;

        const map = L.map('map').setView([centerLat, centerLon], Math.round(res.zoom));

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 19
        }).addTo(map);

        const polyline = L.polyline(latLngs, { color: '#a82835', weight: 3 }).addTo(map);
        map.fitBounds(polyline.getBounds(), { padding: [20, 20] });
    })
    .catch(err => console.error('Map error:', err.message));

function calcTotalDist(coordinates) {
    const R = 6371;
    let totalDist = 0;
    for (let i = 0; i < coordinates.length - 1; i++) {
        const lon1 = coordinates[i][0], lat1 = coordinates[i][1];
        const lon2 = coordinates[i + 1][0], lat2 = coordinates[i + 1][1];
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) ** 2 +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) ** 2;
        totalDist += 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    return totalDist;
}
