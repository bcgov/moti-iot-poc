(function() {
  freeboard.loadDatasourcePlugin({
    "type_name"   : "Open511",
    "display_name": "Open511",
    "description" : "This API is DriveBC's implementation of the Open511 specification",
    "settings"    : [
    {
      "name"         : "severity",
      "display_name" : "Severity",
      "type"         : "text",
      "description"  : "[MINOR, MODERATE, MAJOR]"
    },
    {
      "name"        : "eventType",
      "display_name": "Event Type",
      "required"    : true,
      "type"        : "option",
      "options"     : [
      {
        "name" : "Any",
        "value": ""
      },
      {
        "name" : "Construction",
        "value": "CONSTRUCTION"
      },
      {
        "name" : "Special Event",
        "value": "SPECIAL_EVENT"
      },
      {
        "name" : "Incident",
        "value": "INCIDENT"
      },
      {
        "name" : "Weather Condition",
        "value": "WEATHER_CONDITION"
      },
      {
        "name" : "Road Condition",
        "value": "ROAD_CONDITION"
      }
      ]
    },
    {
      "name"        : "roadName",
      "display_name": "Road Name",
      "type"        : "text"
    },
    {
      "name"        : "jurisdiction",
      "display_name": "Jurisdiction",
      "type"        : "text"
    },
    {
      "name"        : "status",
      "display_name": "Status",
      "required"    : true,
      "type"        : "option",
      "options"     : [
      {
        "name" : "All",
        "value": "ALL"
      },
      {
        "name" : "Active",
        "value": "ACTIVE"
      },
      {
        "name" : "Archived",
        "value": "ARCHIVED"
      }
      ]
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
      var data;
      var url = "http://api.open511.gov.bc.ca/events?format=json";
      url += "&status=" + currentSettings.status;
      if(currentSettings.severity !== undefined) {
        url += "&severity=" + currentSettings.severity;
      }

      if(currentSettings.jurisdiction !== undefined) {
        url += "&jurisdiction=" + currentSettings.jurisdiction;
      }

      if(currentSettings.eventType !== '') {
        url += "&event_type=" + currentSettings.eventType;
      }

      if(currentSettings.roadName !== undefined) {
        url += "&road_name=" + currentSettings.roadName;
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
    "type_name"   : "511 Plugin",
    "display_name": "511 Test Plugin",
    "description" : "A plugin for viewing 511 data",
    "fill_size" : false,
    "settings"    : [
    {
      "name"        : "the_text",
      "display_name": "Some Text",
      // We'll use a calculated setting because we want what's displayed in this widget to be dynamic based on something changing (like a datasource).
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
