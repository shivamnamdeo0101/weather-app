import { renderHook, act, waitFor } from '@testing-library/react';
import { useOfflineMode } from '../useOfflineMode';
import { OfflineCacheService } from '@/services/offlineCache';
import { Logger } from '@/utils/LogLevel';

jest.mock('@/services/offlineCache');
jest.mock('@/utils/LogLevel');

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

// Mock localStorage
Object.defineProperty(global, 'localStorage', {
  value: localStorageMock,
  writable: true,
  configurable: true,
});

// Mock navigator.onLine
const mockNavigator = {
  onLine: true,
};

Object.defineProperty(global, 'navigator', {
  value: mockNavigator,
  writable: true,
  configurable: true,
});

// Mock window events
const eventListeners: Record<string, Array<() => void>> = {};

// Create mock window with jest functions
const mockAddEventListener = jest.fn((event: string, handler: () => void) => {
  if (!eventListeners[event]) {
    eventListeners[event] = [];
  }
  eventListeners[event].push(handler);
});

const mockRemoveEventListener = jest.fn((event: string, handler: () => void) => {
  if (eventListeners[event]) {
    eventListeners[event] = eventListeners[event].filter(h => h !== handler);
  }
});

// Use a different approach - extend existing window or create new if doesn't exist
if (typeof (global as any).window === 'undefined') {
  (global as any).window = {
    addEventListener: mockAddEventListener,
    removeEventListener: mockRemoveEventListener,
    localStorage: localStorageMock,
  };
} else {
  // Spy on existing window methods instead of replacing
  const originalWindow = (global as any).window;
  originalWindow.addEventListener = mockAddEventListener;
  originalWindow.removeEventListener = mockRemoveEventListener;
  originalWindow.localStorage = localStorageMock;
}

// Also set globalThis
if (typeof (global as any).globalThis === 'undefined') {
  (global as any).globalThis = global as any;
}

const mockedOfflineCacheService = OfflineCacheService as jest.Mocked<typeof OfflineCacheService>;

describe('useOfflineMode hook', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    eventListeners.online = [];
    eventListeners.offline = [];
    mockNavigator.onLine = true;
    mockedOfflineCacheService.getCachedCities.mockReturnValue([]);
  });

  it('should initialize with offline mode by default', async () => {
    localStorage.clear(); // Ensure no stored preference
    mockNavigator.onLine = false; // Device is offline
    
    const { result } = renderHook(() => useOfflineMode());

    // Wait for effect to run
    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(true);
    });
    expect(result.current.cachedCitiesCount).toBe(0);
  });

  it('should load cached cities count on mount', async () => {
    mockedOfflineCacheService.getCachedCities.mockReturnValue(['london', 'paris']);

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.cachedCitiesCount).toBe(2);
    });
  });

  it('should read offline mode preference from localStorage', async () => {
    localStorage.setItem('weather_app_offline_mode', 'false');
    localStorage.setItem('weather_app_mode_preference_set', 'true');

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(false);
    });
  });

  it('should default to offline mode when device is offline', async () => {
    mockNavigator.onLine = false;
    localStorage.clear();

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(true);
    });
  });

  it('should default to online mode when device is online and no preference set', async () => {
    mockNavigator.onLine = true;
    localStorage.clear();

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(false);
    });
  });

  it('should set offline mode and persist to localStorage', async () => {
    const { result } = renderHook(() => useOfflineMode());

    await act(async () => {
      result.current.setIsOfflineMode(true);
    });

    expect(result.current.isOfflineMode).toBe(true);
    expect(localStorage.getItem('weather_app_offline_mode')).toBe('true');
    expect(localStorage.getItem('weather_app_mode_preference_set')).toBe('true');
  });

  it('should update cached cities count', async () => {
    const { result } = renderHook(() => useOfflineMode());

    mockedOfflineCacheService.getCachedCities.mockReturnValue(['london']);

    await act(async () => {
      result.current.updateCachedCount();
    });

    expect(result.current.cachedCitiesCount).toBe(1);
  });

  it('should listen to online events and auto-switch when no user preference', async () => {
    localStorage.clear();
    mockNavigator.onLine = false;

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(true);
    });

    // Simulate coming online
    mockNavigator.onLine = true;
    const onlineHandler = eventListeners.online?.[0];
    
    if (onlineHandler) {
      act(() => {
        onlineHandler();
      });

      await waitFor(() => {
        expect(result.current.isOfflineMode).toBe(false);
      });
    }
  });

  it('should listen to offline events and auto-switch when no user preference', async () => {
    localStorage.clear();
    mockNavigator.onLine = true;

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(false);
    });

    // Simulate going offline
    mockNavigator.onLine = false;
    const offlineHandler = eventListeners.offline?.[0];
    
    if (offlineHandler) {
      act(() => {
        offlineHandler();
      });

      await waitFor(() => {
        expect(result.current.isOfflineMode).toBe(true);
      });
    }
  });

  it('should not auto-switch when user has set preference', async () => {
    localStorage.setItem('weather_app_offline_mode', 'true');
    localStorage.setItem('weather_app_mode_preference_set', 'true');
    mockNavigator.onLine = true;

    const { result } = renderHook(() => useOfflineMode());

    await waitFor(() => {
      expect(result.current.isOfflineMode).toBe(true);
    });

    // Simulate coming online - should not auto-switch
    const onlineHandler = eventListeners.online?.[0];
    
    if (onlineHandler) {
      act(() => {
        onlineHandler();
      });

      // Wait a bit to ensure no change
      await new Promise(resolve => setTimeout(resolve, 100));

      expect(result.current.isOfflineMode).toBe(true);
    }
  });

  it('should handle localStorage errors gracefully', async () => {
    const { result } = renderHook(() => useOfflineMode());
    
    // Wait for initial hydration
    await waitFor(() => {
      expect(result.current.isOfflineMode).toBeDefined();
    });
    
    // Get initial state
    const initialState = result.current.isOfflineMode;
    
    const originalSetItem = localStorage.setItem;
    let setItemCalled = false;
    localStorage.setItem = jest.fn(() => {
      setItemCalled = true;
      throw new Error('Storage error');
    });

    await act(async () => {
      result.current.setIsOfflineMode(!initialState);
    });

    // Should still update state even if localStorage fails
    // The state should be toggled regardless of localStorage error
    expect(result.current.isOfflineMode).toBe(!initialState);
    expect(setItemCalled).toBe(true);

    localStorage.setItem = originalSetItem;
  });

  it('should cleanup event listeners on unmount', () => {
    const { unmount } = renderHook(() => useOfflineMode());

    expect(mockAddEventListener).toHaveBeenCalledWith('online', expect.any(Function));
    expect(mockAddEventListener).toHaveBeenCalledWith('offline', expect.any(Function));

    unmount();

    expect(mockRemoveEventListener).toHaveBeenCalled();
  });
});

