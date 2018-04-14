$(document).ready(function () {
    // Global variables
    var map;
    var heatmap;
    var pings = [];
    var markers = [];
    var heatmapData = new google.maps.MVCArray();

    var isHeatmapEnabled = false;

    var tempLatLang = new google.maps.LatLng(33.901237, -118.392678);

    var markerActive = {
        url: "./marker_active.png",
        size: new google.maps.Size(50, 50),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(25, 25),
        scaledSize: new google.maps.Size(20, 20)
    };
    var markerInactive = {
        url: "./marker_inactive.png",
        size: new google.maps.Size(50, 50),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(25, 25),
        scaledSize: new google.maps.Size(20, 20)
    };

    // Map initialization function
    function initMap() {
        var mapOptions = {
            zoom: 20,
            center: tempLatLang,
            disableDefaultUI: true,
            styles: [
                {elementType: 'geometry', stylers: [{color: '#242f3e'}]},
                {elementType: 'labels.text.stroke', stylers: [{color: '#242f3e'}]},
                {elementType: 'labels.text.fill', stylers: [{color: '#746855'}]},
                {
                    featureType: 'landscape',
                    elementType: 'geometry.stroke',
                    stylers: [{color: '#82605f'}]
                },
                {
                    featureType: 'administrative.locality',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#d59563'}]
                },
                {
                    featureType: 'poi',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#d59563'}]
                },
                {
                    featureType: 'poi.park',
                    elementType: 'geometry',
                    stylers: [{color: '#263c3f'}]
                },
                {
                    featureType: 'poi.park',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#6b9a76'}]
                },
                {
                    featureType: 'road',
                    elementType: 'geometry',
                    stylers: [{color: '#38414e'}]
                },
                {
                    featureType: 'road',
                    elementType: 'geometry.stroke',
                    stylers: [{color: '#212a37'}]
                },
                {
                    featureType: 'road',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#9ca5b3'}]
                },
                {
                    featureType: 'road.highway',
                    elementType: 'geometry',
                    stylers: [{color: '#746855'}]
                },
                {
                    featureType: 'road.highway',
                    elementType: 'geometry.stroke',
                    stylers: [{color: '#1f2835'}]
                },
                {
                    featureType: 'road.highway',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#f3d19c'}]
                },
                {
                    featureType: 'transit',
                    elementType: 'geometry',
                    stylers: [{color: '#2f3948'}]
                },
                {
                    featureType: 'transit.station',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#d59563'}]
                },
                {
                    featureType: 'water',
                    elementType: 'geometry',
                    stylers: [{color: '#17263c'}]
                },
                {
                    featureType: 'water',
                    elementType: 'labels.text.fill',
                    stylers: [{color: '#515c6d'}]
                },
                {
                    featureType: 'water',
                    elementType: 'labels.text.stroke',
                    stylers: [{color: '#17263c'}]
                }
            ]
        };

        // Create the map
        map = new google.maps.Map(document.getElementById('map'), mapOptions);

        // Create the heatmap layer
        heatmap = new google.maps.visualization.HeatmapLayer({
            data: heatmapData,
            radius: 30,
            opacity: 0.5
        });

        // Hide overlay when the tiles are laoded once
        google.maps.event.addListenerOnce(map, 'tilesloaded', function () {
            $("#overlay").addClass("hide");
        });
    }

    function toggleMarkers(shouldEnable) {
        if (shouldEnable) {
            markers.forEach(function (marker) {
                marker.setMap(map);
            });
        } else {
            markers.forEach(function (marker) {
                marker.setMap(null);
            });
        }
    }

    function updateMapMarkers() {
        // Make ajax request to server
        $.ajax({
            url: "http://18.217.115.154:3000/locations",
            cache: false,
            dataType: "json",
            success: function (data) {
                // Store ping data
                pings = [];
                heatmapData.clear();

                data.forEach(function(datum) {
                    datum["latLng"] = new google.maps.LatLng(datum["lat"], datum["lng"]);
                    delete datum["lat"];
                    delete datum["lng"];

                    pings.push(datum);
                    heatmapData.push(datum["latLng"]);
                });

                // Clear markers
                toggleMarkers(false);

                markers = [];

                // Draw new markers
                pings.forEach(function (ping) {
                    markers.push(new google.maps.Marker({
                        title: ping["user_id"],
                        position: ping["latLng"],
                        map: isHeatmapEnabled ? null : map,
                        icon: ping["other_data"]["isAlive"] ? markerActive : markerInactive
                    }));
                });
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("Error: " + textStatus + " - " + errorThrown);
            },
            complete: function () {
                setTimeout(updateMapMarkers, 3000);
            }
        });
    }

    function testMarker() {
        new google.maps.Marker({
            position: tempLatLang,
            map: map,
            icon: markerActive,
            optimized: false
        });
    }

    // Initialize the map
    initMap();

    // Test marker
    // testMarker();

    // Start to update markers
    updateMapMarkers();

    // Add click handler for heatmap
    $("#heatmap-btn").click(function () {
        if (!isHeatmapEnabled) {
            toggleMarkers(false);
            heatmap.setMap(map);
        } else {
            toggleMarkers(true);
            heatmap.setMap(null);
        }

        isHeatmapEnabled = !isHeatmapEnabled;
    });

});