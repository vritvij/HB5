var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var Locations = require('../models/location.js');


/* GET /locations listing. */
router.get('/', function(req, res, next) {
  Locations.find(function (err, arr) {
    if (err) return next(err);
    res.json(arr);
  });
});


/* POST /locations */
router.post('/', function(req, res, next) {
  Locations.create(req.body, function (err, post) {
    if (err) return next(err);
    res.json(post);
  });
});


/* PUT /locations/:id */
router.put('/:id', function(req, res, next) {
  Locations.findOneAndUpdate({'user_id':req.params.id}, req.body, function (err, post) {
    if (err) return next(err);
    res.json(post);
  });
});

/* DELETE /locations/:id */
router.delete('/:id', function(req, res, next) {
  Locations.findByIdAndRemove(req.params.id, req.body, function (err, post) {
    if (err) return next(err);
    res.json(post);
  });
});

module.exports = router;
