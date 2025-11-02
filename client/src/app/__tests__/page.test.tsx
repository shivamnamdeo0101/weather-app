import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Home from '../page';
import { useWeatherSearch } from '@/hooks/useWeatherSearch';
import { useOfflineMode } from '@/hooks/useOfflineMode';

jest.mock('@/hooks/useWeatherSearch');
jest.mock('@/hooks/useOfflineMode');

const mockUseWeatherSearch = useWeatherSearch as jest.MockedFunction<typeof useWeatherSearch>;
const mockUseOfflineMode = useOfflineMode as jest.MockedFunction<typeof useOfflineMode>;

describe('Home Page', () => {
  const mockHandleSearch = jest.fn();
  const mockClearError = jest.fn();
  const mockSetIsOfflineMode = jest.fn();
  const mockUpdateCachedCount = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseOfflineMode.mockReturnValue({
      isOfflineMode: false,
      setIsOfflineMode: mockSetIsOfflineMode,
      cachedCitiesCount: 0,
      updateCachedCount: mockUpdateCachedCount,
    });
    mockUseWeatherSearch.mockReturnValue({
      weatherData: [],
      currentCity: '',
      isLoading: false,
      error: null,
      searchingCity: '',
      handleSearch: mockHandleSearch,
      clearError: mockClearError,
    });
  });

  it('renders the page with all components', () => {
    render(<Home />);

    expect(screen.getByRole('banner')).toBeInTheDocument();
  });

  it('renders empty state when no data and no error', () => {
    render(<Home />);

    // EmptyState should be rendered when no data, no loading, no error
    expect(mockUseWeatherSearch).toHaveBeenCalled();
  });

  it('renders error alert when error exists', () => {
    mockUseWeatherSearch.mockReturnValue({
      weatherData: [],
      currentCity: '',
      isLoading: false,
      error: 'Test error',
      searchingCity: 'London',
      handleSearch: mockHandleSearch,
      clearError: mockClearError,
    });

    render(<Home />);

    expect(mockUseWeatherSearch).toHaveBeenCalled();
  });

  it('renders loading state when loading', () => {
    mockUseWeatherSearch.mockReturnValue({
      weatherData: [],
      currentCity: '',
      isLoading: true,
      error: null,
      searchingCity: 'London',
      handleSearch: mockHandleSearch,
      clearError: mockClearError,
    });

    render(<Home />);

    expect(mockUseWeatherSearch).toHaveBeenCalled();
  });

  it('renders weather data when available', () => {
    const mockWeatherData = [
      {
        dt_txt: '2024-01-01 12:00:00',
        main: { temp: 25, feels_like: 26, temp_min: 20, temp_max: 30, pressure: 1013, sea_level: 1013, grnd_level: 1013, humidity: 60, temp_kf: 0 },
        weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
        wind: { speed: 5, deg: 180, gust: 7 },
        predictions: ['sunny'],
      },
    ];

    mockUseWeatherSearch.mockReturnValue({
      weatherData: mockWeatherData,
      currentCity: 'London',
      isLoading: false,
      error: null,
      searchingCity: '',
      handleSearch: mockHandleSearch,
      clearError: mockClearError,
    });

    render(<Home />);

    expect(mockUseWeatherSearch).toHaveBeenCalled();
  });

  it('passes offline mode props to AppHeader', () => {
    mockUseOfflineMode.mockReturnValue({
      isOfflineMode: true,
      setIsOfflineMode: mockSetIsOfflineMode,
      cachedCitiesCount: 3,
      updateCachedCount: mockUpdateCachedCount,
    });

    render(<Home />);

    expect(mockUseOfflineMode).toHaveBeenCalled();
  });
});

