# URL Shortener Frontend - Setup Guide

Complete setup instructions for the Angular 17 frontend application.

## Quick Start

### 1. Prerequisites
- Node.js 20+ (download from https://nodejs.org/)
- npm 10+ (comes with Node.js)
- Angular CLI 17+ (optional, but recommended)

Verify installations:
```bash
node --version
npm --version
```

### 2. Install Dependencies

```bash
cd frontend
npm install
```

This will install all required packages including:
- Angular 17 framework
- Angular Material 17
- RxJS
- TypeScript 5.2

### 3. Configure Environment

Copy the environment template:
```bash
cp .env.example .env.local
```

The default configuration points to `http://localhost:8080` which is suitable for local development.

### 4. Start Development Server

```bash
npm start
```

The application will start at `http://localhost:4200/`

### 5. Verify Backend Connection

Ensure the backend API is running on `http://localhost:8080`:
- Test the connection by attempting to shorten a URL
- Check browser console (F12) for any CORS or connection errors

## Development Workflow

### File Structure
```
frontend/
├── src/
│   ├── app/                      # Application source
│   │   ├── components/           # Angular components
│   │   ├── services/             # HTTP services
│   │   ├── models/               # TypeScript interfaces
│   │   └── app.component.ts      # Root component
│   ├── environments/             # Environment configs
│   ├── styles.scss               # Global styles
│   └── index.html                # HTML entry point
├── package.json                  # Dependencies
├── angular.json                  # Angular config
└── tsconfig.json                 # TypeScript config
```

### Common Development Tasks

#### Add a New Component
```bash
ng generate component features/my-component --standalone
```

#### Add a New Service
```bash
ng generate service services/my-service
```

#### Run Tests
```bash
npm test
```

#### Build for Production
```bash
npm run build
```

#### Watch Mode (Auto-rebuild)
```bash
npm run watch
```

## Build for Production

### Standard Build
```bash
npm run build
```

Output: `dist/url-shortener-frontend/browser/`

### Docker Build
```bash
docker build -t url-shortener-frontend .
docker run -p 80:80 url-shortener-frontend
```

The application will be available at `http://localhost/`

## Configuration

### API Configuration
Edit `src/environments/environment.ts` for development API URL:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

Edit `src/environments/environment.prod.ts` for production:
```typescript
export const environment = {
  production: true,
  apiUrl: ''  // Use relative URLs in production
};
```

### Material Theme
The application uses Angular Material's "Indigo & Pink" theme.
To change the theme, update in `angular.json`:
- Replace theme in styles array
- Available themes: indigo-pink, deeppurple-amber, purple-green, pink-bluegrey

Themes are in: `node_modules/@angular/material/prebuilt-themes/`

## Troubleshooting

### Port 4200 Already in Use
```bash
ng serve --port 4300
```

### CORS Errors
Ensure the backend is running and configured to accept requests from `http://localhost:4200`

### Node Modules Issues
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Build Size Too Large
```bash
# Analyze bundle size
npm run build -- --stats-json
```

## Testing

### Unit Tests
```bash
npm test
```

### Coverage Report
```bash
ng test --code-coverage
```

Coverage report: `coverage/url-shortener-frontend/index.html`

## IDE Setup

### VS Code
1. Install "Angular Language Service" extension
2. Install "Prettier - Code formatter" extension
3. Extensions will provide:
   - TypeScript IntelliSense
   - Template highlighting
   - Linting

### WebStorm
- Built-in Angular support
- No additional setup required

## Performance Optimization

### Lazy Loading
Routes are lazy-loaded for better performance:
- `/shorten` - loads UrlShortenerComponent on demand
- `/analytics` - loads AnalyticsComponent on demand

### Change Detection
Components use OnPush strategy where applicable (defined in component metadata).

### Bundle Analysis
```bash
npm run build -- --configuration production --stats-json
npm install -g webpack-bundle-analyzer
webpack-bundle-analyzer dist/url-shortener-frontend/browser/stats.json
```

## Deployment

### Vercel
```bash
npm install -g vercel
vercel
```

### Netlify
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist/url-shortener-frontend/browser
```

### Traditional Web Server (Apache, Nginx)
1. Build: `npm run build`
2. Copy `dist/url-shortener-frontend/browser/*` to web root
3. Configure server to route all requests to `index.html` (SPA)

### Docker (Production)
See Dockerfile for multi-stage build setup with Nginx serving.

## Monitoring

### Console Errors
Open DevTools (F12) → Console tab to check for:
- Network errors
- TypeScript errors
- Component warnings

### Network Tab
Check Network tab for:
- API response times
- Failed requests
- Bundle sizes

### Lighthouse Audit
Run Chrome DevTools → Lighthouse to check:
- Performance
- Accessibility
- Best Practices
- SEO

## Security Considerations

1. **HTTPS**: Always use HTTPS in production
2. **CORS**: Ensure backend CORS is properly configured
3. **XSS Protection**: Angular sanitizes HTML by default
4. **CSP Headers**: Configure Content Security Policy
5. **API Keys**: Never commit secrets to version control

## Support

For issues or questions:
1. Check browser console for error messages
2. Review application logs
3. Check backend API status
4. Verify network connectivity

## Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [RxJS Documentation](https://rxjs.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
