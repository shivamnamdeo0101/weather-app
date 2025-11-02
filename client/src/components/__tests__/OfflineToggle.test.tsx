import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import OfflineToggle from '../OfflineToggle';

// Mock lucide-react icons
jest.mock('lucide-react', () => ({
  WifiOff: jest.fn(({ className, 'aria-hidden': ariaHidden }) => (
    <svg data-testid="wifi-off-icon" className={className} aria-hidden={ariaHidden} />
  )),
  Wifi: jest.fn(({ className, 'aria-hidden': ariaHidden }) => (
    <svg data-testid="wifi-icon" className={className} aria-hidden={ariaHidden} />
  )),
}));

describe('OfflineToggle Component', () => {
  const mockOnToggle = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders in offline mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    expect(screen.getByText('Offline')).toBeInTheDocument();
    expect(screen.getByTestId('wifi-off-icon')).toBeInTheDocument();
    expect(screen.queryByTestId('wifi-icon')).not.toBeInTheDocument();
  });

  it('renders in online mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={false}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    expect(screen.getByText('Online')).toBeInTheDocument();
    expect(screen.getByTestId('wifi-icon')).toBeInTheDocument();
    expect(screen.queryByTestId('wifi-off-icon')).not.toBeInTheDocument();
  });

  it('displays cached cities count in offline mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={3}
      />
    );

    expect(screen.getByText('(3 cached)')).toBeInTheDocument();
  });

  it('does not display cached count in online mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={false}
        onToggle={mockOnToggle}
        cachedCitiesCount={5}
      />
    );

    expect(screen.queryByText(/cached/)).not.toBeInTheDocument();
  });

  it('calls onToggle when button is clicked', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    fireEvent.click(toggleButton);

    expect(mockOnToggle).toHaveBeenCalledTimes(1);
    expect(mockOnToggle).toHaveBeenCalledWith(false);
  });

  it('toggles from offline to online when clicked', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    fireEvent.click(toggleButton);

    expect(mockOnToggle).toHaveBeenCalledWith(false);
  });

  it('toggles from online to offline when clicked', () => {
    render(
      <OfflineToggle
        isOfflineMode={false}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    fireEvent.click(toggleButton);

    expect(mockOnToggle).toHaveBeenCalledWith(true);
  });

  it('has correct accessibility attributes in offline mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    expect(toggleButton).toHaveAttribute('aria-checked', 'false');
    expect(toggleButton).toHaveAttribute('aria-label', 'Enable online mode');
    expect(toggleButton).toHaveAttribute('title', 'Offline mode - Click to go online');
  });

  it('has correct accessibility attributes in online mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={false}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    expect(toggleButton).toHaveAttribute('aria-checked', 'true');
    expect(toggleButton).toHaveAttribute('aria-label', 'Disable online mode');
    expect(toggleButton).toHaveAttribute('title', 'Online mode - Click to go offline');
  });

  it('applies correct styling classes for offline mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    expect(toggleButton).toHaveClass('bg-gray-600');
  });

  it('applies correct styling classes for online mode', () => {
    render(
      <OfflineToggle
        isOfflineMode={false}
        onToggle={mockOnToggle}
        cachedCitiesCount={0}
      />
    );

    const toggleButton = screen.getByRole('switch');
    expect(toggleButton).toHaveClass('bg-blue-600');
  });

  it('handles missing cachedCitiesCount prop', () => {
    render(
      <OfflineToggle
        isOfflineMode={true}
        onToggle={mockOnToggle}
      />
    );

    expect(screen.getByText('Offline')).toBeInTheDocument();
    expect(screen.queryByText(/cached/)).not.toBeInTheDocument();
  });
});

