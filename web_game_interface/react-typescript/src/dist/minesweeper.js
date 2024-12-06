"use strict";
exports.__esModule = true;
exports.startGame = exports.init = void 0;
/* eslint-disable no-loop-func */
var width = 16;
var height = 16;
var mineCount = 40;
var size = 40;
var gameover = false;
var leftCount = 0;
// Initialize the board
var board = Array.from({ length: height }, function () {
    return Array.from({ length: width }, function () { return ({ text: "" }); });
});
// Set mines
var resetMines = function () {
    leftCount = width * height;
    for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
            board[y][x] = { text: "" };
        }
    }
    for (var i = 0; i < mineCount; i++) {
        var x = void 0, y = void 0;
        do {
            x = Math.floor(Math.random() * width);
            y = Math.floor(Math.random() * height);
        } while (board[y][x].mine);
        board[y][x].mine = true;
        leftCount--;
    }
};
var openTarget = [];
var init = function () {
    var container = document.getElementById("gamePivot");
    if (!(container instanceof HTMLDivElement)) {
        console.error("Container element not found or is not a valid div element");
        return;
    }
    container.style.width = "".concat(width * size, "px");
    container.style.height = "".concat(height * size, "px");
    container.innerHTML = ""; // 既存の要素をクリア
    var _loop_1 = function (y) {
        var _loop_2 = function (x) {
            var div = document.createElement("div");
            container.appendChild(div);
            div.style.position = "absolute";
            div.style.width = "".concat(size, "px");
            div.style.height = "".concat(size, "px");
            div.style.left = "".concat(x * size, "px");
            div.style.top = "".concat(y * size, "px");
            div.style.backgroundColor = "#ccc";
            div.style.border = "3px outset #ddd";
            div.style.boxSizing = "border-box";
            div.onpointerdown = function () {
                if (gameover)
                    return;
                var flagInput = document.getElementById("flag");
                if (flagInput && flagInput.checked) {
                    flag(x, y);
                }
                else {
                    openTarget.push([x, y]);
                    openCells();
                }
            };
            board[y][x].element = div;
        };
        for (var x = 0; x < width; x++) {
            _loop_2(x);
        }
    };
    for (var y = 0; y < height; y++) {
        _loop_1(y);
    }
};
exports.init = init;
// Set the flag
var flag = function (x, y) {
    var cell = board[y][x];
    if (cell.open)
        return;
    cell.text = cell.text === "\uD83D\uDEA9" ? "" : "🚩";
    update();
};
// Update the board
var update = function () {
    for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
            var cell = board[y][x];
            if (cell.element) {
                cell.element.textContent = cell.text;
                if (cell.open) {
                    cell.element.style.border = "1px solid #888";
                }
            }
        }
    }
};
// Show all mines
var showAllMine = function () {
    for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
            var cell = board[y][x];
            if (cell.mine)
                cell.text = "\uD83D\uDCA3";
        }
    }
};
// Open cell
var openCells = function () {
    while (openTarget.length) {
        var _a = openTarget.pop(), x = _a[0], y = _a[1];
        var cell = board[y][x];
        if (cell.open)
            continue;
        cell.open = true;
        leftCount--;
        if (cell.mine) {
            gameover = true;
            showAllMine();
            cell.text = "\uD83D\uDCA5";
            update();
            return;
        }
        var counter = 0;
        var target = [];
        for (var dy = -1; dy <= 1; dy++) {
            for (var dx = -1; dx <= 1; dx++) {
                var cx = x + dx;
                var cy = y + dy;
                if (cx < 0 || cy < 0 || cx >= width || cy >= height)
                    continue;
                target.push([cx, cy]);
                if (board[cy][cx].mine)
                    counter++;
            }
        }
        cell.text = counter ? counter.toString() : "";
        if (!counter)
            openTarget.push.apply(openTarget, target);
        if (leftCount === 0) {
            showAllMine();
            gameover = true;
        }
    }
    update();
};
// Initialize
var startGame = function () {
    gameover = false;
    resetMines();
    (0, exports.init)();
    var startTime = Date.now();
    var tick = function () {
        if (gameover)
            return;
        var time = ((Date.now() - startTime) / 1000).toFixed(2);
        var timer = document.getElementById("timer");
        if (timer)
            timer.textContent = time;
        requestAnimationFrame(tick);
    };
    tick();
};
exports.startGame = startGame;
