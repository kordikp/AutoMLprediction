// Generated by CoffeeScript 1.5.0
(function() {
  var module;

  module = angular.module('swing.directives.table');

  module.directive("showHeaderOnHover", function($parse) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var cachedColumnOuterWidths, cachedColumnWidths, createFauxTable, destroyFauxTable, fauxHoveredRow, fauxTable;
        fauxTable = null;
        fauxHoveredRow = null;
        cachedColumnWidths = [];
        cachedColumnOuterWidths = [];
        createFauxTable = function($td, $tr, e) {
          var cellWidth, cellsInRangeOuterWidths, cellsInRangeWidths, cellsToTheLeftOuterWidths, fauxHeader, fauxHeaderTH, fauxHoveredRowTD, firstContinousVisibleRange, header, headerCells, idx, left, rowCells, th, trOffset, visibleCellsFound, visibleRange, width, _i, _j, _ref, _ref1;
          trOffset = $tr.offset();
          firstContinousVisibleRange = function($elements) {
            var $el, $window, el, end, i, inRange, isVisible, scrollLeft, scrollTop, start, windowHeight, windowWidth, _i, _len;
            $window = $(window);
            scrollLeft = $window.scrollLeft();
            scrollTop = $window.scrollTop();
            windowWidth = $window.width();
            windowHeight = $window.height();
            i = 0;
            start = 0;
            end = 0;
            inRange = false;
            for (_i = 0, _len = $elements.length; _i < _len; _i++) {
              el = $elements[_i];
              $el = $(el);
              isVisible = $el.isOnScreen(scrollTop, scrollLeft, windowWidth, windowHeight);
              if (isVisible) {
                if (inRange) {
                  end = i + 1;
                } else if ($el.is(':visible')) {
                  start = i;
                  inRange = true;
                }
              } else {
                if (inRange) {
                  end = i;
                  if ($el.is(':visible')) {
                    break;
                  }
                }
              }
              i += 1;
            }
            return {
              start: start,
              end: end
            };
          };
          if (fauxTable == null) {
            cachedColumnWidths = [];
            cachedColumnOuterWidths = [];
            $tr.children().each(function(i, e) {
              var $e;
              $e = $(e);
              if ($e.is(':visible')) {
                cachedColumnWidths.push($e.width());
                return cachedColumnOuterWidths.push($e.outerWidth());
              } else {
                cachedColumnWidths.push(0);
                return cachedColumnOuterWidths.push(0);
              }
            });
            fauxTable = $('<table></table>');
            fauxTable.addClass('header-hover-table');
            fauxTable.addClass(element.attr('class'));
            fauxTable.css({
              position: "absolute"
            });
            $("body").append(fauxTable);
          }
          fauxTable.empty();
          header = element.find("tr").first();
          headerCells = $(header).children();
          rowCells = $tr.children();
          visibleRange = firstContinousVisibleRange(rowCells);
          if (cachedColumnWidths) {
            cellsInRangeWidths = cachedColumnWidths.slice(visibleRange.start, visibleRange.end);
            cellsInRangeOuterWidths = cachedColumnOuterWidths.slice(visibleRange.start, visibleRange.end);
            cellsToTheLeftOuterWidths = [];
            if (visibleRange.start) {
              cellsToTheLeftOuterWidths = cachedColumnOuterWidths.slice(0, visibleRange.start);
            }
          } else {
            cellsInRangeWidths = [];
            cellsInRangeOuterWidths = [];
            cellsToTheLeftOuterWidths = [];
          }
          left = cellsToTheLeftOuterWidths.sum();
          width = cellsInRangeOuterWidths.sum();
          fauxTable.css({
            top: trOffset.top,
            left: left,
            width: width
          });
          fauxHeader = $("<tr class=\"faux-header-black\"></tr>");
          fauxHoveredRow = $("<tr></tr>");
          fauxHeaderTH = $(headerCells.slice(visibleRange.start, visibleRange.end)).clone();
          fauxHoveredRowTD = $(rowCells.slice(visibleRange.start, visibleRange.end)).clone();
          fauxHeader.append(fauxHeaderTH);
          fauxHoveredRow.append(fauxHoveredRowTD);
          if (fauxHeaderTH.length) {
            visibleCellsFound = 0;
            for (idx = _i = _ref = fauxHeaderTH.length - 1; _ref <= 0 ? _i <= 0 : _i >= 0; idx = _ref <= 0 ? ++_i : --_i) {
              cellWidth = cellsInRangeWidths[idx];
              th = fauxHeaderTH[idx];
              $(th).width(cellWidth);
              if (visibleCellsFound < 2 && headerCells[visibleRange.start + idx].clientWidth) {
                if (visibleCellsFound === 1) {
                  $(th).addClass('penultimate');
                }
                visibleCellsFound += 1;
              }
            }
            for (idx = _j = 1, _ref1 = fauxHeaderTH.length; 1 <= _ref1 ? _j < _ref1 : _j > _ref1; idx = 1 <= _ref1 ? ++_j : --_j) {
              th = fauxHeaderTH[idx];
              if (headerCells[visibleRange.start + idx].clientWidth) {
                $(th).addClass('first');
                break;
              }
            }
          }
          fauxTable.append(fauxHeader);
          return fauxTable.append(fauxHoveredRow);
        };
        destroyFauxTable = function() {
          if (fauxTable) {
            fauxTable.detach();
          }
          return fauxTable = null;
        };
        element.on('mouseover', 'td', function(e) {
          var $td, $tr;
          $td = $(this);
          $tr = $($td.parent());
          if ($tr.data("type") === "data") {
            return createFauxTable($td, $tr, e);
          } else {
            return destroyFauxTable();
          }
        });
        element.bind('mouseout', function(e) {
          return destroyFauxTable();
        });
        element.bind('didReload', function(e) {
          return destroyFauxTable();
        });
        element.on('$destroy', function(e) {
          element.unbind('mouseout');
          element.unbind('didReload');
          return element.off('mouseover', 'td');
        });
      }
    };
  });

}).call(this);
