// Dependencies
var express = require('express');
var mongoose = require('mongoose');
var bodyParser = require('body-parser');
var cors = require('cors');

// MongoDB
mongoose.connect('mongodb://localhost/location');

// Express
var app = express();
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(cors());

// Routes
app.use('/locations', require('./routes/locations.js'));

// Start server
app.listen(3000);
console.log('Listening on port 3000...');