export enum LogLevel {
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
}

export class Logger {
  static info(message: string, ...args: unknown[]) {
    console.info(`[WeatherApiService][INFO] ${message}`, ...args);
  }

  static warn(message: string, ...args: unknown[]) {
    console.warn(`[WeatherApiService][WARN] ${message}`, ...args);
  }

  static error(message: string, ...args: unknown[]) {
    console.error(`[WeatherApiService][ERROR] ${message}`, ...args);
  }
}
