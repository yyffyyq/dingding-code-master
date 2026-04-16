# Getting Started

<cite>
**Referenced Files in This Document**
- [package.json](file://package.json)
- [vite.config.ts](file://vite.config.ts)
- [README.md](file://README.md)
- [index.html](file://index.html)
- [src/main.ts](file://src/main.ts)
- [src/App.vue](file://src/App.vue)
- [src/layout/index.vue](file://src/layout/index.vue)
- [src/router/index.ts](file://src/router/index.ts)
- [src/request.ts](file://src/request.ts)
- [openapi2ts.config.ts](file://openapi2ts.config.ts)
- [tsconfig.json](file://tsconfig.json)
- [tsconfig.app.json](file://tsconfig.app.json)
- [tsconfig.node.json](file://tsconfig.node.json)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Installation and Setup](#installation-and-setup)
4. [Development Workflow](#development-workflow)
5. [Environment Configuration](#environment-configuration)
6. [Build and Preview](#build-and-preview)
7. [Deployment Preparation](#deployment-preparation)
8. [Debugging and Hot Reload](#debugging-and-hot-reload)
9. [Common Issues and Troubleshooting](#common-issues-and-troubleshooting)
10. [Best Practices](#best-practices)
11. [Conclusion](#conclusion)

## Introduction
This guide helps you set up the development environment for the SSO frontend application. It covers prerequisites, dependency installation, development server startup, build process, environment configuration, and deployment preparation. Practical examples demonstrate typical development workflows, and troubleshooting tips address common setup issues.

## Prerequisites
- Node.js: Install a stable LTS version compatible with the project’s toolchain. The project uses modern tooling (Vite 6.x, Vue 3, TypeScript ~5.8.x), so ensure your Node.js version aligns with these tools’ documented compatibility.
- Package manager: Choose either npm or yarn. The scripts in the project are npm-focused but work identically with yarn.
- Git: Required for cloning the repository and managing branches.
- IDE: Recommended extensions for Vue 3 and TypeScript support are available via the official Vue TypeScript guide.

**Section sources**
- [README.md:1-6](file://README.md#L1-L6)
- [package.json:6-11](file://package.json#L6-L11)

## Installation and Setup
Follow these steps to prepare your local environment:

1. Clone the repository and navigate to the frontend directory.
2. Install dependencies:
   - Using npm: Run the install command to fetch production and development dependencies.
   - Using yarn: Run the equivalent yarn install command.
3. Verify installation by checking that node_modules is populated and package-lock.json/yarn.lock exists.

Key project dependencies include Vue 3, Vue Router, Pinia, Element Plus, Ant Design Vue, and TypeScript tooling. Development dependencies include Vite, Vue SFC plugin, TypeScript compiler, and related type definitions.

**Section sources**
- [package.json:12-29](file://package.json#L12-L29)

## Development Workflow
Start the development server and begin building:

1. Start the dev server:
   - Using npm: Run the dev script to launch Vite’s development server with hot module replacement.
   - Using yarn: Run the equivalent yarn dev command.
2. Open the application in your browser at the port indicated by the dev server.
3. Edit files under src; changes automatically reload thanks to Vite’s HMR.

Typical workflows:
- Add new pages: Create a new view component under src/views and register it in the router configuration.
- Integrate APIs: Use the shared request client to communicate with backend endpoints.
- Generate TypeScript models: Use the openapi2ts script to generate TS models from the backend OpenAPI spec.

**Section sources**
- [package.json:6-11](file://package.json#L6-L11)
- [vite.config.ts:5-12](file://vite.config.ts#L5-L12)
- [src/router/index.ts:1-40](file://src/router/index.ts#L1-L40)
- [src/request.ts:1-49](file://src/request.ts#L1-L49)
- [openapi2ts.config.ts:1-7](file://openapi2ts.config.ts#L1-L7)

## Environment Configuration
The project uses Vite’s environment handling and TypeScript configuration:

- Path aliases: The @ alias resolves to src for convenient imports across the app.
- TypeScript configuration:
  - tsconfig.json orchestrates app and node configurations.
  - tsconfig.app.json enables strict TypeScript checks and path mapping for the browser bundle.
  - tsconfig.node.json configures bundler-mode TypeScript for Vite config and Node-specific tooling.

Base URL and routing:
- The router uses HTML5 history mode with BASE_URL from import.meta.env. For local development, Vite injects this value automatically.

**Section sources**
- [vite.config.ts:7-11](file://vite.config.ts#L7-L11)
- [tsconfig.json:1-8](file://tsconfig.json#L1-L8)
- [tsconfig.app.json:3-18](file://tsconfig.app.json#L3-L18)
- [tsconfig.node.json:2-24](file://tsconfig.node.json#L2-L24)
- [src/router/index.ts:9](file://src/router/index.ts#L9)

## Build and Preview
Prepare the application for production:

1. Build:
   - Using npm: Run the build script to type-check and bundle the app.
   - Using yarn: Run the equivalent yarn build command.
2. Preview:
   - Using npm: Run the preview script to serve the production build locally.
   - Using yarn: Run the equivalent yarn preview command.

Build artifacts are emitted to the default dist directory. The preview server simulates production behavior to validate the build.

**Section sources**
- [package.json:9-10](file://package.json#L9-L10)
- [vite.config.ts:5-12](file://vite.config.ts#L5-L12)

## Deployment Preparation
Before deploying:

- Confirm base path: If hosting under a subpath, set Vite’s base option accordingly in the Vite config.
- Validate static assets: Ensure public assets and index.html references remain intact after build.
- Test preview: Use the preview command to confirm the production build behaves as expected.
- Backend connectivity: Verify the request client’s base URL matches your target backend endpoint.

**Section sources**
- [src/request.ts:6-10](file://src/request.ts#L6-L10)
- [index.html:11](file://index.html#L11)

## Debugging and Hot Reload
Hot reload:
- Vite enables automatic page refresh when you edit Vue SFCs, TypeScript files, or styles. No additional configuration is required.

Debugging tips:
- Browser DevTools: Set breakpoints in Vue components and inspect reactive state.
- Console logging: Use console.log statements in scripts and composables.
- Network tab: Inspect API requests/responses routed through the shared request client.
- Type errors: Fix TypeScript errors flagged by vue-tsc during build.

**Section sources**
- [src/request.ts:13-47](file://src/request.ts#L13-L47)
- [src/main.ts:1-19](file://src/main.ts#L1-L19)

## Common Issues and Troubleshooting
- Node.js version mismatch:
  - Symptom: Build fails with engine or module compatibility errors.
  - Fix: Install a Node.js version aligned with Vite 6.x and TypeScript ~5.8.x requirements.
- Missing dependencies:
  - Symptom: npm/yarn reports missing packages after install.
  - Fix: Re-run install with your chosen package manager; clear cache if needed.
- Port conflicts:
  - Symptom: Dev server fails to start due to port already in use.
  - Fix: Stop the conflicting service or configure Vite to use another port.
- Incorrect base URL:
  - Symptom: Routing or asset loading fails in production builds.
  - Fix: Set Vite’s base option and ensure import.meta.env.BASE_URL is correct in the router.
- API connectivity:
  - Symptom: Requests fail or redirect to login unexpectedly.
  - Fix: Verify the request client’s base URL matches the backend endpoint and credentials are enabled when required.

**Section sources**
- [vite.config.ts:5-12](file://vite.config.ts#L5-L12)
- [src/request.ts:6-10](file://src/request.ts#L6-L10)
- [src/router/index.ts:9](file://src/router/index.ts#L9)

## Best Practices
- Keep dependencies updated: Regularly update npm packages and TypeScript to benefit from bug fixes and performance improvements.
- Use strict TypeScript settings: Leverage the existing strict mode and unused local/parameter checks to catch issues early.
- Organize imports: Use the @ alias for cleaner imports and maintainable code.
- Centralize HTTP requests: Use the shared request client for consistent interceptors and error handling.
- Generate typed models: Periodically regenerate TypeScript models from the backend OpenAPI spec to keep contracts synchronized.

**Section sources**
- [tsconfig.app.json:7-12](file://tsconfig.app.json#L7-L12)
- [src/request.ts:1-49](file://src/request.ts#L1-L49)
- [openapi2ts.config.ts:1-7](file://openapi2ts.config.ts#L1-L7)

## Conclusion
You now have the essentials to develop, build, and deploy the SSO frontend application. Start the dev server, leverage hot reload, debug with familiar tools, and follow the best practices outlined here to maintain a smooth development experience.