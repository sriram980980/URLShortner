export interface ShortenRequest {
  longUrl: string;
  customTtlSeconds?: number;
}

export interface ShortenResponse {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  expiresAt?: string;
}

export interface ClickEvent {
  shortCode: string;
  longUrl: string;
  timestamp: string;
  userAgent: string;
  ipAddress: string;
}

export interface ClickCount {
  shortCode: string;
  count: number;
}
