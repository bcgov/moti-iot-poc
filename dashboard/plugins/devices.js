(function() {
  freeboard.loadDatasourcePlugin({
    "type_name"   : "Devices",
    "display_name": "Devices",
    "description" : "This API displays devices.",
    "settings"    : [
    {
      "name": "deviceId",
      "display_name": "Device ID",
      "type": "number"
    },
    {
      "name": "sensor",
      "display_name": "Sensor Name",
      "type": "text"
    },
    {
      "name": "seeEvents",
      "display_name": "See Events",
      "type": "boolean"
    },
    {
      "name": "latestEvents",
      "display_name": "See Latest Event",
      "type": "boolean"
    },
    {
      "name": "host",
      "display_name": "Host name",
      "type": "text",
      "default_value": "https://oahu-api-tran-iot-dev.pathfinder.gov.bc.ca"
    },
    {
      "name"         : "refresh_time",
      "display_name" : "Refresh Time",
      "type"         : "text",
      "description"  : "In milliseconds",
      "default_value": 5000
    }
    ],
    newInstance: function(settings, newInstanceCallback, updateCallback) {
      newInstanceCallback(new myDatasourcePlugin(settings, updateCallback));
    }
  });

  var myDatasourcePlugin = function(settings, updateCallback) {
    var vm = this;

    var currentSettings = settings;

    function getData() {
      var url = currentSettings.host + "/api/devices/";

      if(currentSettings.deviceId) {
        url += currentSettings.deviceId;

        if(currentSettings.sensor) {
          url += '/' + currentSettings.sensor;
        }

        if(currentSettings.seeEvents) {
          url += "/events";
        }

        if(currentSettings.latestEvents) {
          url += "/latest/";
        }
      }

      $.ajax({
        url: url,
        type: 'GET',
        success: function(results) {
          updateCallback(results);
        },
        error: function(error) {
          console.log(error);
        }
      });
    }

    var refreshTimer;

    function createRefreshTimer(interval) {
      if(refreshTimer) {
        clearInterval(refreshTimer);
      }

      refreshTimer = setInterval(function() {
        getData();
      }, interval);
    }

    vm.onSettingsChanged = function(newSettings) {
      currentSettings = newSettings;
    }

    vm.updateNow = function() {
      getData();
    }

    vm.onDispose = function() {
      clearInterval(refreshTimer);
      refreshTimer = undefined;
    }

    createRefreshTimer(currentSettings.refresh_time);
  }

  freeboard.loadWidgetPlugin({
    "type_name"   : "Device Plugin",
    "display_name": "Device Plugin",
    "description" : "A plugin for viewing a device",
    "fill_size" : false,
    "settings"    : [
    {
      "name"        : "name",
      "display_name": "Device Name",
      "type"        : "calculated"
    },
    {
      "name"        : "lat",
      "display_name": "Latitude",
      "type"        : "calculated"
    },
    {
      "name"        : "lng",
      "display_name": "Longitude",
      "type"        : "calculated"
    },
    {
      "name"        : "size",
      "display_name": "Size",
      "type"        : "option",
      "options"     : [
      {
        "name" : "Regular",
        "value": "regular"
      },
      {
        "name" : "Big",
        "value": "big"
      }
      ]
    }
    ],
    newInstance: function(settings, newInstanceCallback) {
      newInstanceCallback(new myWidgetPlugin(settings));
    }
  });

  var myWidgetPlugin = function(settings) {
    var vm = this;
    var currentSettings = settings;

    var myTextElement = $("<span></span>");

    vm.render = function(containerElement) {
      $(containerElement).append(myTextElement);
    }

    vm.getHeight = function() {
      if(currentSettings.size === "big") {
        return 2;
      } else {
        return 1;
      }
    }

    vm.onSettingsChanged = function(newSettings) {
      currentSettings = newSettings;
    }

    vm.onCalculatedValueChanged = function(settingName, newValue) {
      if(settingName === "the_text") {
        $(myTextElement).html(newValue);
      }
    }

    vm.onDispose = function() { }
  }
}());
