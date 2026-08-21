# URL Shortener Frontend

Angular 17 frontend application for the URL Shortener service. Built with standalone components, Angular Material, and reactive forms.

## Features

- **URL Shortening**: Convert long URLs into short, memorable codes
- **Analytics**: Track clicks and view detailed analytics for shortened URLs
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Material UI**: Modern and intuitive user interface using Angular Material

## Prerequisites

- Node.js 20+ and npm 10+
- Angular CLI 17+

## Installation

```bash
# Install dependencies
npm install
```

## Development

```bash
# Start the development server
npm start

# Navigate to http://localhost:4200/
# The application will automatically reload when you change source files
```

## Build

```bash
# Build for production
npm run build

# Build output will be stored in `dist/url-shortener-frontend/browser/`
```

## Testing

```bash
# Run unit tests
npm test

# Run tests with code coverage
ng test --code-coverage
```

## Docker

```bash
# Build Docker image
docker build -t url-shortener-frontend .

# Run container
docker run -p 80:80 url-shortener-frontend
```

## Project Structure

```
src/
├── app/
│   ├── components/
│   │   ├── url-shortener/        # URL shortening component
│   │   └── analytics/            # Analytics component
│   ├── services/
│   │   ├── url.service.ts        # URL API service
│   │   └── analytics.service.ts  # Analytics API service
│   ├── models/
│   │   └── url.model.ts          # Data models and interfaces
│   ├── app.component.ts          # Root component
│   ├── app.config.ts             # Application configuration
│   └── app.routes.ts             # Route definitions
├── environments/
│   ├── environment.ts            # Development environment
│   └── environment.prod.ts       # Production environment
├── index.html                    # HTML entry point
├── styles.scss                   # Global styles
└── main.ts                       # Bootstrap file
```

## API Configuration

The frontend communicates with the backend API at:
- **Development**: `http://localhost:8080`
- **Production**: Configured via relative URLs

Configure the API URL in `src/environments/environment.ts` and `src/environments/environment.prod.ts`.

## Components

### URL Shortener Component
- Accept long URLs
- Optional TTL (Time To Live) configuration
- Display shortened URL with copy-to-clipboard functionality
- Error handling with snackbar notifications

### Analytics Component
- Search by short code
- Display total click count
- View detailed click events with timestamp, IP address, and user agent
- Sort and filter events

## Styling

The application uses:
- **Angular Material** for pre-built components
- **SCSS** for custom styling
- **Material Design** color scheme (Indigo & Pink theme)
- **Responsive CSS** for mobile and tablet support

## Environment Variables

Create `.env` file in the project root (not tracked in git):

```bash
# Backend API URL
NG_APP_API_URL=http://localhost:8080
```

## Build Configuration

- **Production optimization**: Enabled (tree-shaking, minification, AOT compilation)
- **Source maps**: Disabled in production
- **Lazy loading**: Routes are lazy-loaded for optimal performance
- **Bundle size budgets**: 500KB initial, 1MB total

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Android)

## License

Proprietary - URL Shortener Project
