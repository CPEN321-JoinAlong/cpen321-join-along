const axios = require("axios");
const API_KEY = "AIzaSyDFO8v4uud6-k0s_T_25pJzTV_qvMndDBk";

module.exports = async function (originAddr, destAddr) {
    let config = {
        method: "get",
        url:
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
            originAddr +
            "&destinations=" +
            destAddr +
            "&units=metric&key=" +
            API_KEY,
        headers: {},
    };
    try {
        let response = await axios(config);
        // console.log("from: " + originAddr + " to: " + destAddr);
        // console.log(response.data.rows[0].elements[0].distance.text);
        return response.data.rows[0].elements[0].distance.text
    } catch (e) {
        // console.log("from: " + originAddr + " to: " + destAddr);
        // console.log(e);
        return "Travel distance can't be calculated"
    }
    // console.log(response)
    // return response.data.rows[0].elements[0].distance.text
};
