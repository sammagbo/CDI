const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const axios = require('axios'); // We might need to install axios or use fetch if node version supports it

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require('electron-squirrel-startup')) {
  app.quit();
}

let mainWindow;
let backendProcess;
const BACKEND_PORT = 8081;
const BACKEND_URL = `http://localhost:${BACKEND_PORT}/api/actuator/health`; // Assuming actuator is present or just check root

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false, // For simple migration, though not recommended for security in public apps
    },
    title: "CDI SafeTrack",
    icon: path.join(__dirname, 'src/assets/icon.png') // Placeholder
  });

  // Load the backend URL or local file?
  // Strategy: We load the local HTML file, which communicates with localhost:8081
  mainWindow.loadFile(path.join(__dirname, 'src/index.html'));

  // Open the DevTools.
  // mainWindow.webContents.openDevTools();
}

function startBackend() {
  const jarPath = path.join(__dirname, 'resources', 'backend.jar');
  console.log(`Starting backend from ${jarPath}`);

  backendProcess = spawn('java', ['-jar', jarPath]);

  backendProcess.stdout.on('data', (data) => {
    console.log(`Backend stdout: ${data}`);
  });

  backendProcess.stderr.on('data', (data) => {
    console.error(`Backend stderr: ${data}`);
  });

  backendProcess.on('close', (code) => {
    console.log(`Backend process exited with code ${code}`);
  });
}

function stopBackend() {
  if (backendProcess) {
    console.log('Stopping backend...');
    // backendProcess.kill(); // Might not kill child processes on Windows
      // On Windows, we might need a more forceful kill if the Java process spawns others, but usually simple kill works for jar
      const kill = require('tree-kill'); // Optional dependency if simple kill fails
      backendProcess.kill();
      backendProcess = null;
  }
}


app.whenReady().then(() => {
  startBackend();
  // We should wait for backend to be ready, but for now we just launch the UI
  // The UI might need retry logic if it hits the API before it's ready
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('will-quit', () => {
    stopBackend();
});
