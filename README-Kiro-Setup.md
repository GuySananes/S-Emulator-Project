# S-Emulator Project - Kiro IDE Setup

## Project Overview
This is a Java-based S language IDE/compiler with server-client architecture featuring:
- **JavaFX Client**: Desktop application with local and server modes
- **Tomcat Web Server**: RESTful API backend with servlet-based architecture  
- **Web Client**: HTML/CSS/JS frontend for browser-based access
- **Core Modules**: DTO, Engine, Exception handling

## Prerequisites
- **Java 17+** with JAVA_HOME environment variable set
- **Apache Tomcat 10.1.26** (included in lib/apache-tomcat-10.1.26/)
- **JavaFX SDK 22.0.2** (included in lib/javafx-sdk-22.0.2/)
- **Gson 2.11.0** (included in lib/)
- **Jakarta Servlet API** (included in lib/tomcat/)

## Quick Start

### 1. Build Everything
```bash
# PowerShell (recommended)
powershell -ExecutionPolicy Bypass -File build.ps1

# Or use VS Code task
Ctrl+Shift+P → "Tasks: Run Task" → "Compile All Modules"
```

### 2. Run JavaFX Desktop Client
```bash
# Batch file
run-client.bat

# Or use VS Code debug
F5 → "Launch JavaFX Client"
```

### 3. Run Web Server
```bash
# Development server with auto-build and deployment (recommended)
run-server-dev.bat

# Or use VS Code task
Ctrl+Shift+P → "Tasks: Run Task" → "Start Tomcat Server"
```

### 4. Access Applications
- **JavaFX Client**: Runs locally with both Local Mode and Server Mode tabs
- **Web Client**: http://localhost:8080/web_SEmulator_Web_exploded
- **API Base**: http://localhost:8080/web_SEmulator_Web_exploded/api/

## Project Structure
```
S-Emulator-Project/
├── client/src/javafxUI/          # JavaFX desktop application
├── server/src/sserver/           # Tomcat servlet backend
├── web-SEmulator/web/            # HTML/CSS/JS web frontend
├── engine/src/core/              # S language execution engine
├── DTO/src/                      # Data transfer objects
├── exception/src/                # Custom exceptions
├── lib/                          # External libraries
├── run/                          # Compiled JARs and deployment
└── out/production/               # Compiled classes
```

## Development Workflow

### Building
- **Full Build**: `build.ps1` compiles all modules in correct dependency order
- **VS Code Integration**: Tasks and launch configurations are pre-configured
- **Auto-compilation**: Launch configurations trigger builds automatically

### Running & Debugging
- **JavaFX Client**: Use F5 or run `run-client.bat`
- **Server Development**: Use `run-server-dev.bat` for integrated build+deploy+run
- **Web Client**: Access via browser after server is running

### Key Features
- **Hybrid Mode**: JavaFX client supports both local and server execution
- **Real-time Monitoring**: Server mode shows live user statistics
- **RESTful API**: Complete HTTP API for S language operations
- **Credit System**: User credit management for server execution

## API Endpoints
- `POST /api/login` - User authentication
- `GET /api/users/live` - Live user statistics  
- `POST /api/execute/*` - S language program execution
- `POST /api/credits/charge` - Credit management
- `POST /api/file/load` - Program file upload
- `GET /api/programs` - Available programs

## Troubleshooting
- **Build Issues**: Ensure all JARs in lib/ are accessible
- **JavaFX Issues**: Verify JavaFX module path in launch configs  
- **Server Issues**: Local Tomcat is used automatically (lib/apache-tomcat-10.1.26/)
- **Path Issues**: Windows paths with spaces are handled in build scripts
- **Port Conflicts**: Default port is 8080, change in Tomcat's server.xml if needed

## Local Tomcat Configuration
The project uses a local Tomcat installation at `lib/apache-tomcat-10.1.26/`. This eliminates the need for system-wide Tomcat setup and ensures consistent deployment across different development environments.

The project is now fully configured for Kiro IDE with proper build automation, debugging support, and development workflows.