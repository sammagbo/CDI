const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    saveBackup: (data) => ipcRenderer.invoke('save-backup', data)
});
