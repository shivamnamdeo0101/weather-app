'use client';

import { useState, useEffect, useCallback } from 'react';
import { OfflineCacheService } from '@/services/offlineCache';
import { Logger } from '@/utils/LogLevel';

const OFFLINE_MODE_KEY = 'weather_app_offline_mode';
const USER_PREFERENCE_SET_KEY = 'weather_app_mode_preference_set';

/**
 * Check if device is online
 */
function getIsOnline(): boolean {
  if (typeof globalThis.navigator === 'undefined') return false;
  return globalThis.navigator.onLine;
}

/**
 * Get default mode based on connectivity and user preference
 */
function getDefaultMode(hasUserPreference: boolean, userPreference: string | null): boolean {
  // If user has explicitly set a preference, use it
  if (hasUserPreference && userPreference !== null) {
    return userPreference === 'true';
  }
  
  // Otherwise, base default on connectivity: offline if no internet, online if connected
  return !getIsOnline();
}

export function useOfflineMode() {
  // Initialize with default (offline mode) - same on server and client to prevent hydration mismatch
  const [isOfflineMode, setIsOfflineModeState] = useState<boolean>(true);
  const [cachedCitiesCount, setCachedCitiesCount] = useState<number>(0);

  // Update cached cities count
  const updateCachedCount = useCallback(() => {
    if (typeof globalThis.window === 'undefined') return;
    const cities = OfflineCacheService.getCachedCities();
    setCachedCitiesCount(cities.length);
  }, []);

  // Load from localStorage and check connectivity after hydration (client-side only)
  useEffect(() => {
    // Check initial connectivity status
    const initialOnlineStatus = getIsOnline();
    
    // Read user preference from localStorage
    try {
      if (typeof globalThis.window !== 'undefined') {
        const hasUserPreference = globalThis.window.localStorage.getItem(USER_PREFERENCE_SET_KEY) === 'true';
        const stored = globalThis.window.localStorage.getItem(OFFLINE_MODE_KEY);
        
        // Determine default mode based on connectivity and user preference
        const defaultMode = getDefaultMode(hasUserPreference, stored);
        setIsOfflineModeState(defaultMode);
      }
    } catch (error) {
      Logger.warn('Failed to read offline mode from localStorage', error);
      // Fallback to connectivity-based default
      setIsOfflineModeState(!initialOnlineStatus);
    }
    
    // Load cached cities count
    updateCachedCount();
  }, [updateCachedCount]);

  // Listen to online/offline events and auto-switch mode based on connectivity
  useEffect(() => {
    if (typeof globalThis.window === 'undefined') return;

    const handleOnline = () => {
      // If user hasn't set a preference, auto-switch to online mode when connectivity is restored
      const hasUserPreference = globalThis.window.localStorage.getItem(USER_PREFERENCE_SET_KEY) === 'true';
      if (!hasUserPreference) {
        setIsOfflineModeState(false);
      }
    };

    const handleOffline = () => {
      // If user hasn't set a preference, auto-switch to offline mode when connectivity is lost
      const hasUserPreference = globalThis.window.localStorage.getItem(USER_PREFERENCE_SET_KEY) === 'true';
      if (!hasUserPreference) {
        setIsOfflineModeState(true);
      }
    };

    globalThis.window.addEventListener('online', handleOnline);
    globalThis.window.addEventListener('offline', handleOffline);

    return () => {
      globalThis.window.removeEventListener('online', handleOnline);
      globalThis.window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Set offline mode and persist to localStorage
  const setIsOfflineMode = useCallback((enabled: boolean) => {
    try {
      if (typeof globalThis.window !== 'undefined') {
        // Mark that user has explicitly set a preference
        globalThis.window.localStorage.setItem(USER_PREFERENCE_SET_KEY, 'true');
        globalThis.window.localStorage.setItem(OFFLINE_MODE_KEY, enabled.toString());
      }
      setIsOfflineModeState(enabled);
    } catch (error) {
      Logger.error('Failed to save offline mode preference', error);
    }
  }, []);

  return {
    isOfflineMode,
    setIsOfflineMode,
    cachedCitiesCount,
    updateCachedCount,
  };
}

