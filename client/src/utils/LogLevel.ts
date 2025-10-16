export enum LogLevel {
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
}

export class Logger {
  private static isEnabled = true; // turn off in production

  static info(message: string, ...args: any[]) {
    if (this.isEnabled) console.info(`[WeatherApiService][INFO] ${message}`, ...args);
  }

  static warn(message: string, ...args: any[]) {
    if (this.isEnabled) console.warn(`[WeatherApiService][WARN] ${message}`, ...args);
  }

  static error(message: string, ...args: any[]) {
    if (this.isEnabled) console.error(`[WeatherApiService][ERROR] ${message}`, ...args);
  }

  static setEnabled(enabled: boolean) {
    this.isEnabled = enabled;
  }
}
