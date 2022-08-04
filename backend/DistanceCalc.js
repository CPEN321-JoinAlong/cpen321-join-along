//temporarity using to update all the events and User
// const axios = require("axios")
module.exports = function (coordUser, coordEvent) {
    // console.log(coordUser)
    // console.log(coordEvent)
    let userLat = parseFloat(coordUser.split(",")[0]);
    let userLng = parseFloat(coordUser.split(",")[0]);

    let eventLat = parseFloat(coordEvent.split(",")[0]);
    let eventLng = parseFloat(coordEvent.split(",")[0]);
    // console.log(eventLat + ", " + eventLng)
    if((userLat === 0 && userLng === 0) || (eventLat === 0 && eventLng === 0)){
        return -1
    }
    var R = 6371; // Radius of the earth in km
    var dLat = deg2rad(eventLat - userLat); // deg2rad below
    var dLon = deg2rad(eventLng - userLng);
    var a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(userLat)) *
            Math.cos(deg2rad(eventLat)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var d = R * c; // Distance in km
    // console.log(d)
    return d;
    // try {
    //     let response = await axios(
    //         "https://maps.googleapis.com/maps/api/geocode/json?address="+ location + "&key=AIzaSyDFO8v4uud6-k0s_T_25pJzTV_qvMndDBk"
    //     );
    //     let coordinates = response.data.results[0].geometry.location;
    //     console.log(coordinates)
    //     return`${coordinates.lat},${coordinates.lng}`;
    // } catch (e) {
    //     // console.log(e)
    //     return "0,0"
    // }
};

function deg2rad(deg) {
    return deg * (Math.PI / 180);
}

//["62e317ac77f7ad9a56ab886b", "62e48622b43f633a5a0e3860", "62e363a8e21c7698113dc974", "62db9dd9337ad5e08e29b562", "62dba10ae96413b41863b7c5", "62dba1bce96413b41863b7f2", "62d7ae2d010a82beb388b2ff", "62e8bd3d7080c5316bc7e32f", "62e8bd7d7080c5316bc7e33f", "62e98d597080c5316bc7e73e"]
//Test Event, Full Event, Very Specific, testnorth, testnorth, testnorth2
