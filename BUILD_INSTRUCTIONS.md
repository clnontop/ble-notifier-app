# 🚀 GitHub Actions APK Build Instructions

## Automatic APK Building with GitHub Actions

This repository is configured to automatically build APK files using GitHub Actions whenever you push code.

### 📋 **Setup Steps**

1. **Create GitHub Repository**
   - Go to [GitHub.com](https://github.com)
   - Click "New Repository"
   - Name it: `ble-notifier-app`
   - Make it **Public** (required for free GitHub Actions)

2. **Upload Your Code**
   ```bash
   # In your project folder
   git init
   git add .
   git commit -m "Initial BLE Notifier App"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/ble-notifier-app.git
   git push -u origin main
   ```

3. **GitHub Actions Will Auto-Build**
   - GitHub will automatically detect the workflow file
   - Build process starts immediately after push
   - Takes about 5-10 minutes to complete

### 📱 **Download Your APK**

1. **Go to Actions Tab**
   - Visit your GitHub repository
   - Click "Actions" tab at the top
   - Click on the latest workflow run

2. **Download APK**
   - Scroll down to "Artifacts" section
   - Click "BLE-Notifier-Debug-APK" to download
   - Unzip the downloaded file to get your APK

### 🔄 **Triggering New Builds**

- **Automatic**: Every time you push code changes
- **Manual**: Go to Actions tab → "Build Android APK" → "Run workflow"

### 📂 **What Gets Built**

- **Debug APK**: `app-debug.apk` (for testing)
- **Release APK**: `app-release-unsigned.apk` (optimized, but unsigned)

### 🛠️ **Troubleshooting**

If the build fails:
1. Check the "Actions" tab for error logs
2. Common issues:
   - Syntax errors in code
   - Missing dependencies
   - Gradle configuration problems

### 🎯 **Quick Start (No Git Knowledge Required)**

1. **Download GitHub Desktop**: https://desktop.github.com/
2. **Create repository** through GitHub Desktop
3. **Drag your project folder** into GitHub Desktop
4. **Publish repository** (make it public)
5. **Wait for build** to complete in Actions tab
6. **Download APK** from Artifacts

### ⚡ **Alternative: Use GitHub Web Interface**

1. Create new repository on GitHub.com
2. Upload files by dragging them to the web interface
3. Commit changes
4. Wait for automatic build
5. Download APK from Actions tab

---

**🎉 Your APK will be ready in 5-10 minutes after uploading!**
