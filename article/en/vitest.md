# Introduction
Since creating tests for React took more time than expected, I decided to document it as a reference.
# Objective
To verify the switching of menu items displayed depending on whether the user is logged in or not.
# Environment
```typescript:package.json
"@types/react": "^18.3.12"
"@types/react-dom": "^18.3.1"

"react": "^18.3.1"
"react-dom": "^18.3.1"
"react-router-dom": "^6.28.0"
"react-scripts": "5.0.1"
"typescript": "^4.9.5"

"@testing-library/jest-dom": "^6.6.3"
"@testing-library/react": "^16.1.0"
"@types/jest": "^29.5.14"
"@types/node": "^22.10.2"

"vitest": "^2.1.8"
```
```typescript:tsconfig.json
{
  "compilerOptions": {
    "jsx": "react",
    "target": "es2017",
    "module": "es2020",
    "rootDir": "./",
    "moduleResolution": "node",
    "types": ["node", "vitest", "@testing-library/jest-dom"],
    "outDir": "./src/dist",
    "isolatedModules": true,
    "esModuleInterop": true,
    "forceConsistentCasingInFileNames": true,
    "strict": true,
    "noImplicitAny": false,
    "strictNullChecks": false,
    "skipLibCheck": true,
    "baseUrl": "./",
    "paths": {
      "@testing-library/react": ["node_modules/@testing-library/react"]
    }
  },
    "include": ["src/**/*.ts", "test/**/*"],
    "exclude": ["node_modules", "src/dist"]
}
```
# Directory Structure
root
├─ src
│   ├─ App.tsx
│   └─ components
│       └─ Header.tsx
├─ test
│   └─ Header.test.tsx
├─ vitest.config.ts
└─ vitest.setup.ts
# Installing Necessary Packages
```bash:command
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom
```
# Test code
```typescript:App.tsx
const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
          <Header />
      </Router>
    </AuthProvider>
  );
};
```
```typescript:Header.tsx
const Header: React.FC = () => {
  const { isLoggedIn } = useAuth();
  return (
    <header>
      <div>
        <ul>
          {isLoggedIn ? (
            <>
              <li><Link to="/logout">logout</Link></li>
            </>
          ) : (
            <>
              <li><Link to="/login">login</Link></li>
            </>
          )}
        </ul>
      </div>
    </header>
  );
};
```
```typescript:Header.test.tsx
describe("Header Component", () => {
  test("Displays login button if not logged in.", () => {
    render(
      <AuthContext.Provider value={{ isLoggedIn: false, setIsLoggedIn: () => {} }}>
        <Router>
          <Header />
        </Router>
      </AuthContext.Provider>
    );
    const loginLink = screen.getByText(/login/i);
    expect(loginLink).toBeInTheDocument();
  });

  test("Displays logout button if logged in.", () => {
    render(
      <AuthContext.Provider value={{ isLoggedIn: true, setIsLoggedIn: () => {} }}>
        <Router>
          <Header />
        </Router>
      </AuthContext.Provider>
    );
    const logoutLink = screen.getByText(/logout/i);
    expect(logoutLink).toBeInTheDocument();
  });
});
```
```typescript:vitest.config.ts
export default defineConfig({
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./vitest.setup.ts",
  },
});
```
```typescript:vitest.setup.ts
import "@testing-library/jest-dom";
```
# Test results
```bash:command
% npm vtest

✓ test/Header.test.tsx
    ✓ Header Component
        ✓ Displays login button if not logged in.
        ✓ Displays logout button if logged in.

Test Files  1 passed
     Tests  2 passed
```
# Points of confusion
##### Missing Configurations
```typescript:tsconfig.json
"moduleResolution": "node",
"types": ["node", "vitest", "@testing-library/jest-dom"],
"paths": {
  "@testing-library/react": ["node_modules/@testing-library/react"]
}
```
##### Regex Matching
```typescript:Header.tsx
const loginLink = screen.getByText(/login/i);
```
##### Header must be wrapped in router
```typescript:App.tsx
<Router>
  <Header />
</Router>
```
