# Code-Morpher

## Overview

`Code-Morpher` is a **source‑to‑source transpiler** that translates code from one programming language to another. The current implementation focuses on converting **python** source files into **javascript,java,cpp,c** (with a modular design that can be extended to other target languages).

The project consists of two main parts:

1. **Backend (Java/Maven)** – Core transpilation engine, AST visitors, and generators.
2. **Frontend (React)** – A simple UI that lets users upload a Java file, select a target language, and view the generated code.

## Features
- Parse Java source with ANTLR grammar.
- Serialize AST to JSON for easy manipulation.
- Generate TypeScript code preserving semantics.
- Web UI for quick interactive testing.
- Well‑structured Maven build with unit tests.

## Prerequisites
| Tool | Minimum Version |
|------|-----------------|
| JDK  | 11 (or newer) |
| Maven| 3.6+ |
| Node.js | 18.x |
| npm | 9.x |

Make sure `java`, `mvn`, and `npm` are available on your `PATH`.

## Building the Backend
```bash
# Clone the repository (if you haven't already)
git clone https://github.com/AsmitBhandari/Code-Morpher.git
cd Code-Morpher
mvn clean install
```bash


## Running the Backend
'''bash
# backend
cd backend
mvn clean install
.\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
'''bash


## Running the Frontend
```bash
# From the project root
cd frontend
npm install   # installs dependencies
npm run dev     # starts the dev server (http://localhost:3000)
```
The UI communicates with the backend via REST endpoints (the default base URL is `http://localhost:8080`). Adjust the proxy settings in `frontend/package.json` if you change the backend port.

## Usage Example
1. Start the backend (`java -jar …`).
2. In another terminal, start the frontend (`npm start`).
3. Open `http://localhost:3000` in your browser.
4. Upload a Java file (or paste code), select **TypeScript** as the target, and click **Transpile**.
5. The generated TypeScript will appear in the output pane – you can copy it or download it.

## Project Structure
```
Code-Morpher/
├─ src/main/java/               # Java source
│   └─ com/transpiler/         # Core packages (controller, generator, visitor, dto)
├─ frontend/                    # React UI (src/components, src/App.jsx, etc.)
├─ pom.xml                      # Maven build configuration
├─ .gitignore                   # Ignored files (build artifacts, logs, IDE configs)
─ README.md                    # You are reading it!
```

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your‑feature`).
3. Make your changes and ensure `mvn test` passes.
4. Open a Pull Request describing the change.
5. Follow the code‑style guidelines (use `google-java-format` for Java and Prettier for the frontend).



