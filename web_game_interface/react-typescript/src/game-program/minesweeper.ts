/* eslint-disable no-loop-func */
const width = 16;
const height = 16;
const mineCount = 40;
const size = 40;

let gameover = false;
let leftCount = 0;

type Cell = {
  text: string;
  mine?: boolean;
  open?: boolean;
  element?: HTMLDivElement;
};

// Initialize the board
const board: Cell[][] = Array.from({ length: height }, () =>
  Array.from({ length: width }, () => ({ text: "" }))
);

// Set mines
const resetMines = () => {
  leftCount = width * height;
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      board[y][x] = { text: "" };
    }
  }
  for (let i = 0; i < mineCount; i++) {
    let x: number, y: number;
    do {
      x = Math.floor(Math.random() * width);
      y = Math.floor(Math.random() * height);
    } while (board[y][x].mine);
    board[y][x].mine = true;
    leftCount--;
  }
};

const openTarget: [number, number][] = [];

export const init = () => {
  const container = document.getElementById("gamePivot");
  if (!(container instanceof HTMLDivElement)) {
    console.error("Container element not found or is not a valid div element");
    return;
  }

  container.style.width = `${width * size}px`;
  container.style.height = `${height * size}px`;
  container.innerHTML = ""; // 既存の要素をクリア

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const div = document.createElement("div");
      container.appendChild(div);
      div.style.position = "absolute";
      div.style.width = `${size}px`;
      div.style.height = `${size}px`;
      div.style.left = `${x * size}px`;
      div.style.top = `${y * size}px`;
      div.style.backgroundColor = `#ccc`;
      div.style.border = `3px outset #ddd`;
      div.style.boxSizing = `border-box`;

      div.onpointerdown = () => {
        if (gameover) return;
        const flagInput = document.getElementById("flag") as HTMLInputElement;
        if (flagInput && flagInput.checked) {
          flag(x, y);
        } else {
          openTarget.push([x, y]);
          openCells();
        }
      };

      board[y][x].element = div;
    }
  }
};

// Set the flag
const flag = (x: number, y: number) => {
  const cell = board[y][x];
  if (cell.open) return;
  cell.text = cell.text === `🚩` ? "" : "🚩";
  update();
};

// Update the board
const update = () => {
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const cell = board[y][x];
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
const showAllMine = () => {
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const cell = board[y][x];
      if (cell.mine) cell.text = `💣`;
    }
  }
};

// Open cell
const openCells = () => {
  while (openTarget.length) {
    const [x, y] = openTarget.pop()!;
    const cell = board[y][x];
    if (cell.open) continue;
    cell.open = true;
    leftCount--;

    if (cell.mine) {
      gameover = true;
      showAllMine();
      cell.text = `💥`;
      update();
      return;
    }

    let counter = 0;
    const target: [number, number][] = [];
    for (let dy = -1; dy <= 1; dy++) {
      for (let dx = -1; dx <= 1; dx++) {
        const cx = x + dx;
        const cy = y + dy;
        if (cx < 0 || cy < 0 || cx >= width || cy >= height) continue;
        target.push([cx, cy]);
        if (board[cy][cx].mine) counter++;
      }
    }

    cell.text = counter ? counter.toString() : "";
    if (!counter) openTarget.push(...target);

    if (leftCount === 0) {
      showAllMine();
      gameover = true;
    }
  }
  update();
};

// Initialize
export const startGame = () => {
  gameover = false;
  resetMines();
  init();
  const startTime = Date.now();
  const tick = () => {
    if (gameover) return;
    const time = ((Date.now() - startTime) / 1000).toFixed(2);
    const timer = document.getElementById("timer");
    if (timer) timer.textContent = time;
    requestAnimationFrame(tick);
  };
  tick();
};
