import { Logger } from "@/utils/LogLevel";

describe('Logger', () => {
  const infoSpy = jest.spyOn(console, 'info').mockImplementation(() => {});
  const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
  const errorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

  afterEach(() => {
    jest.clearAllMocks();
    Logger.setEnabled(true);
  });

  it('should log info message when enabled', () => {
    Logger.info('Test info', { id: 1 });
    expect(infoSpy).toHaveBeenCalledWith(
      '[WeatherApiService][INFO] Test info',
      { id: 1 }
    );
  });

  it('should log warning message when enabled', () => {
    Logger.warn('Test warning');
    expect(warnSpy).toHaveBeenCalledWith('[WeatherApiService][WARN] Test warning');
  });

  it('should log error message when enabled', () => {
    Logger.error('Test error');
    expect(errorSpy).toHaveBeenCalledWith('[WeatherApiService][ERROR] Test error');
  });

  it('should not log anything when disabled', () => {
    Logger.setEnabled(false);
    Logger.info('No log');
    Logger.warn('No log');
    Logger.error('No log');

    expect(infoSpy).not.toHaveBeenCalled();
    expect(warnSpy).not.toHaveBeenCalled();
    expect(errorSpy).not.toHaveBeenCalled();
  });
});
