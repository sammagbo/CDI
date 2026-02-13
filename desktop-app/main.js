const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');
const http = require('http'); // Native http module for polling

// Prevent garbage collection
let backendProcess;
let splashWindow;
let mainWindow;

const BACKEND_PORT = 8081;
const POLL_INTERVAL = 500;
const MAX_RETRIES = 60; // 30 seconds timeout

function createSplash() {
    splashWindow = new BrowserWindow({
        width: 500,
        height: 300,
        frame: false,
        alwaysOnTop: true,
        transparent: true,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    splashWindow.loadFile(path.join(__dirname, 'src/splash.html'));
}

function createMainWindow() {
    mainWindow = new BrowserWindow({
        width: 1280,
        height: 800,
        show: false, // Wait until ready-to-show
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            nodeIntegration: false,
            contextIsolation: true,
            sandbox: false // Needed for some preload features? contextIsolation: true is safer.
        },
        title: "CDI SafeTrack"
    });

    mainWindow.maximize();
    mainWindow.loadFile(path.join(__dirname, 'src/index.html'));

    mainWindow.once('ready-to-show', () => {
        if (splashWindow) {
            splashWindow.close();
            splashWindow = null;
        }
        mainWindow.show();
    });
}

function startBackend() {
    const jarPath = path.join(__dirname, 'resources', 'backend.jar');

    // Check if JAR exists
    if (!fs.existsSync(jarPath)) {
        console.error("Backend JAR not found at:", jarPath);
        // For development, proceed to UI but show error? 
        // We'll proceed so the UI can at least show a connection error.
        createMainWindow();
        return;
    }

    console.log(`Spawning backend: java -jar "${jarPath}"`);
    backendProcess = spawn('java', ['-jar', jarPath], {
        cwd: path.dirname(jarPath) // Run in resources dir so logs go there? Or just keep CWD clean
    });

    backendProcess.stdout.on('data', (data) => console.log(`Backend: ${data}`));
    backendProcess.stderr.on('data', (data) => console.error(`Backend Error: ${data}`));

    backendProcess.on('close', (code) => {
        console.log(`Backend process exited with code ${code}`);
    });

    pollBackend();
}

function pollBackend(retryCount = 0) {
    if (retryCount > MAX_RETRIES) {
        console.error("Backend failed to start in time.");
        // Open window anyway, UI will handle error
        createMainWindow();
        return;
    }

    const req = http.get(`http://localhost:${BACKEND_PORT}/api/health`, (res) => {
        if (res.statusCode === 200) {
            console.log("Backend is ready!");
            createMainWindow();
        } else {
            setTimeout(() => pollBackend(retryCount + 1), POLL_INTERVAL);
        }
    });

    req.on('error', (err) => {
        setTimeout(() => pollBackend(retryCount + 1), POLL_INTERVAL);
    });

    req.end();
}

// Native Backup Handler
ipcMain.handle('save-backup', async (event, { data, filename }) => {
    try {
        const homeDir = app.getPath('documents');
        const backupDir = path.join(homeDir, 'CDI_Backups');

        if (!fs.existsSync(backupDir)) {
            fs.mkdirSync(backupDir, { recursive: true });
        }

        const filePath = path.join(backupDir, filename);
        fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf-8');
        console.log(`Backup saved to: ${filePath}`);
        return { success: true, path: filePath };
    } catch (error) {
        console.error("Backup failed:", error);
        return { success: false, error: error.message };
    }
});

app.whenReady().then(() => {
    createSplash();
    startBackend();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createSplash();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});

app.on('will-quit', () => {
    if (backendProcess) {
        console.log("Killing backend process...");
        // On Windows, use taskkill with /T to kill the entire process tree (Java spawns child processes)
        if (process.platform === 'win32') {
            const { execSync } = require('child_process');
            try {
                execSync(`taskkill /PID ${backendProcess.pid} /T /F`, { stdio: 'ignore' });
            } catch (e) {
                console.error('Failed to kill backend process tree:', e.message);
            }
        } else {
            backendProcess.kill();
        }
        backendProcess = null;
    }
});
