// src/hooks/__tests__/useWeatherEmoji.test.ts
import { renderHook } from '@testing-library/react';
import { useWeatherEmoji } from '../useWeatherEmoji';

describe('useWeatherEmoji', () => {
  it('returns a function', () => {
    const { result } = renderHook(() => useWeatherEmoji());
    expect(typeof result.current).toBe('function');
  });

  it('returns correct emoji for known icon codes', () => {
    const { result } = renderHook(() => useWeatherEmoji());

    const emojiFn = result.current;

    expect(emojiFn('01d')).toBe('☀️');
    expect(emojiFn('01n')).toBe('🌙');
    expect(emojiFn('02d')).toBe('⛅');
    expect(emojiFn('02n')).toBe('☁️');
    expect(emojiFn('09d')).toBe('🌧️');
    expect(emojiFn('10n')).toBe('🌧️');
    expect(emojiFn('13d')).toBe('❄️');
    expect(emojiFn('50n')).toBe('🌫️');
  });

  it('returns default emoji for unknown icon codes', () => {
    const { result } = renderHook(() => useWeatherEmoji());
    const emojiFn = result.current;

    expect(emojiFn('unknown')).toBe('🌤️');
    expect(emojiFn('')).toBe('🌤️');
    expect(emojiFn('99x')).toBe('🌤️');
  });
});
