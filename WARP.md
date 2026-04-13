# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Commands

### Development
- `npm run dev` - Start Vite development server with HMR
- `npm run build` - Type-check with TypeScript and build for production
- `npm run preview` - Preview production build locally
- `npm run lint` - Run ESLint on all TypeScript/TSX files

### TypeScript
- `tsc -b` - Build TypeScript (included in build command)
- TypeScript configuration uses project references (`tsconfig.app.json` for app code, `tsconfig.node.json` for Vite config)

## Architecture

### Tech Stack
- **React 19** with TypeScript
- **Vite** for build tooling and dev server
- **React Router v7** for client-side routing
- **ESLint** with TypeScript, React Hooks, and React Refresh plugins

### Project Structure
- **`src/main.tsx`** - Application entry point, sets up React root with BrowserRouter
- **`src/App.tsx`** - Main app component with route definitions
- **`src/pages/`** - Route components (Home, Listings, Auth)
- **`src/components/`** - Reusable UI components (Header, Footer, ProductCard)
- **`src/assets/`** - Static assets

### Routing
All routes defined in `App.tsx`:
- `/` - Home page with featured products
- `/listings` - Browse all product listings
- `/auth` - Login/registration page with toggle between modes
- `*` - 404 fallback

### Component Patterns
- Functional components with TypeScript
- Props typed with TypeScript interfaces/types (e.g., `Product` type in ProductCard)
- Inline styles used alongside CSS classes
- No state management library - using React hooks (e.g., `useState` in Auth)

### Data Model
Current `Product` type structure:
```typescript
{ id: string; title: string; price: number; image: string; size?: string; brand?: string }
```

### Language
UI text is in **French** - maintain this for all user-facing strings.

## Development Notes

### TypeScript Configuration
- Strict mode enabled with `noUnusedLocals` and `noUnusedParameters`
- Uses `bundler` module resolution (Vite-specific)
- JSX compiled to `react-jsx` (new JSX transform)

### ESLint
- Configuration in `eslint.config.js` (flat config format)
- Enforces React Hooks rules and React Refresh patterns
- Ignores `dist` directory

### Styling
- Global styles in `src/index.css` and `src/App.css`
- Uses CSS classes like `.container`, `.grid`, `.card`, `.site-header`, `.site-footer`
- No CSS-in-JS or utility framework

### Current Limitations
- Mock data only (no backend integration)
- No persistent state or storage
- Search functionality not implemented
- Authentication is UI-only (no actual auth logic)
- "Vendre" (Sell) button has no functionality

## Security

### Security Utilities
Security functions available in `src/utils/security.ts`:
- `sanitizeInput()` - XSS protection
- `validateEmail()` / `validatePassword()` - Input validation
- `hashPassword()` - Password hashing (SHA-256)
- `loginRateLimiter` - Brute force protection (5 attempts / 15 min)
- `validateFileSize()` / `validateFileType()` - File upload validation
- `detectScriptInjection()` - Script injection detection

### Security Considerations
- **Frontend only**: No backend = limited security
- **localStorage**: Data visible in browser (not production-ready)
- See `SECURITY.md` for full security audit and production recommendations
- For production: Backend API + Database + JWT + HTTPS required
