// Generated by CoffeeScript 1.5.0
(function() {
  var module;

  module = angular.module('h2o.controllers.inspect');

  module.controller('InspectAppController', function($scope, InspectDataService, InspectColumnService, MenuService) {
    var _this = this;
    $scope.InspectDataService = InspectDataService;
    $scope.InspectColumnService = InspectColumnService;
    $scope.MenuService = MenuService;
    this.init = function() {
      return InspectDataService.fetch();
    };
    $scope.isKeySetInURI = function() {
      var queryDict;
      queryDict = JSONApiServerURI().query(true);
      return (queryDict != null) && (queryDict.key != null);
    };
    return this.init();
  });

  module.controller('InspectColumnController', function($scope, $log, InspectDataService, InspectColumnService) {
    var _this = this;
    $scope.InspectDataService = InspectDataService;
    $scope.InspectColumnService = InspectColumnService;
    this.init = function() {};
    $scope.sortByFunc = function(valueFunc, ascending) {
      var c, headers, sortFunc;
      if (ascending == null) {
        ascending = true;
      }
      headers = new Array();
      angular.forEach(InspectDataService.columns, function(c) {
        return headers.push({
          name: c.name,
          value: valueFunc(c)
        });
      });
      if (ascending) {
        sortFunc = function(a, b) {
          return a.value - b.value;
        };
      } else {
        sortFunc = function(a, b) {
          return b.value - a.value;
        };
      }
      headers.sort(sortFunc);
      return InspectColumnService.setNewColumnOrderByNames((function() {
        var _i, _len, _results;
        _results = [];
        for (_i = 0, _len = headers.length; _i < _len; _i++) {
          c = headers[_i];
          _results.push(c.name);
        }
        return _results;
      })());
    };
    $scope.sortByVariance = function() {
      return $scope.sortByFunc((function(c) {
        return c.variance;
      }), false);
    };
    $scope.showAllColumns = function() {
      var c, _i, _len, _ref;
      _ref = InspectColumnService.filterableTableHeaders;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        c = _ref[_i];
        InspectColumnService.shownTableHeaders[c.name] = true;
      }
      return InspectColumnService.refilter();
    };
    $scope.hideAllColumns = function() {
      var c, _i, _len, _ref;
      _ref = InspectColumnService.filterableTableHeaders;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        c = _ref[_i];
        InspectColumnService.shownTableHeaders[c.name] = false;
      }
      return InspectColumnService.refilter();
    };
    $scope.hideMissingColumns = function() {
      var c, noValues, _i, _len, _ref;
      _ref = InspectColumnService.filterableTableHeaders;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        c = _ref[_i];
        noValues = c.num_missing_values === InspectDataService.numRows;
        InspectColumnService.shownTableHeaders[c.name] = !noValues;
      }
      return InspectColumnService.refilter();
    };
    return this.init();
  });

  module.controller('InspectTableController', function($scope, $log, InspectDataService, InspectColumnService) {
    var _this = this;
    $scope.InspectDataService = InspectDataService;
    $scope.InspectColumnService = InspectColumnService;
    $scope.tableHeaders = [];
    $scope.tableData = [];
    this.init = function() {
      $scope.$watch('InspectColumnService.filteredTableHeaders', function(newVal, oldVal, scope) {
        return $scope.tableHeaders = newVal != null ? newVal : [];
      });
      $scope.$watch('InspectColumnService.tableData', function(newVal, oldVal, scope) {
        return $scope.tableData = newVal != null ? newVal : [];
      });
    };
    $scope.reorder = function(namesOrder) {
      namesOrder.unshift("row");
      return InspectColumnService.setNewColumnOrderByNames(namesOrder);
    };
    return this.init();
  });

  module.controller('InspectPaginationController', function($scope, $log, InspectDataService) {
    var _this = this;
    $scope.InspectDataService = InspectDataService;
    $scope.offset = 0;
    $scope.limit = 0;
    $scope.firstRow = 0;
    $scope.lastRow = 0;
    $scope.pageOffset = 0;
    $scope.firstPage = 0;
    $scope.lastPage = 0;
    $scope.canGoToNextPage = false;
    $scope.canGoToPrevPage = false;
    $scope.isLoading = false;
    this.init = function() {
      var _this = this;
      $scope.$watch('InspectDataService.numRows', function(newVal, oldVal, scope) {
        if (newVal) {
          $scope.lastRow = newVal;
        }
        return _this.recalculatePages();
      });
      $scope.$watch('InspectDataService.offset', function(newVal, oldVal, scope) {
        $scope.offset = newVal;
        return _this.recalculatePages();
      });
      $scope.$watch('InspectDataService.limit', function(newVal, oldVal, scope) {
        if (newVal) {
          $scope.limit = newVal;
        }
        return _this.recalculatePages();
      });
      return $scope.$watch('InspectDataService.status', function(newVal, oldVal, scope) {
        return _this.recalculatePages();
      });
    };
    this.offsetFromPage = function(page) {
      var _ref;
      return page * ((_ref = $scope.limit) != null ? _ref : InspectDataService.defaultLimit);
    };
    this.recalculatePages = function() {
      var newLastPage, newPageOffset;
      if (!$scope.limit) {
        newPageOffset = 0;
        newLastPage = 0;
      } else {
        newPageOffset = Math.ceil($scope.offset / $scope.limit);
        newLastPage = Math.floor($scope.lastRow / $scope.limit);
      }
      $scope.pageOffset = newPageOffset;
      $scope.lastPage = newLastPage;
      $scope.isLoading = $scope.InspectDataService.isLoading;
      $scope.canGoToNextPage = !$scope.isLoading && $scope.pageOffset < $scope.lastPage;
      return $scope.canGoToPrevPage = !$scope.isLoading && $scope.pageOffset > 0;
    };
    $scope.fetch = function() {
      InspectDataService.offset = _this.offsetFromPage($scope.pageOffset);
      InspectDataService.limit = $scope.limit;
      _this.recalculatePages();
      return InspectDataService.fetch();
    };
    $scope.nextPage = function() {
      if (!$scope.canGoToNextPage) {
        return;
      }
      $scope.pageOffset += 1;
      return $scope.fetch();
    };
    $scope.prevPage = function() {
      if (!$scope.canGoToPrevPage) {
        return;
      }
      $scope.pageOffset -= 1;
      return $scope.fetch();
    };
    $scope.pageSliderTooltipValue = function(pageOffset) {
      return "row " + (_this.offsetFromPage(pageOffset));
    };
    return this.init();
  });

}).call(this);
