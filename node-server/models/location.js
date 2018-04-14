// Dependencies
var restful = require('node-restful');
var mongoose = restful.mongoose;

// Schema
var locationSchema = new mongoose.Schema({
    user_id: String,
    lat: String,
    lng: String,
    updated_at: { type: Date, default: Date.now },
    other_data: { isAlive: Boolean, heartRate: String},
});

// Return model
module.exports = restful.model('Locations', locationSchema);