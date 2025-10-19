import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import AppHeader from '../AppHeader';

// Mock lucide-react icon to avoid SVG rendering issues in tests
jest.mock('lucide-react', () => ({
  Cloud: jest.fn(() => <svg data-testid="cloud-icon" />),
}));

describe('AppHeader Component', () => {
  it('renders with default title and subtitle', () => {
    render(<AppHeader />);

    // Default title
    expect(
      screen.getByRole('heading', { name: /weather forecast/i })
    ).toBeInTheDocument();

    // Default subtitle
    expect(
      screen.getByText(/get detailed weather forecasts and predictions/i)
    ).toBeInTheDocument();

    // Cloud icon
    expect(screen.getByTestId('cloud-icon')).toBeInTheDocument();
  });

  it('renders with custom title and subtitle', () => {
    render(
      <AppHeader
        title="Delhi Weather"
        subtitle="Detailed updates for Delhi"
      />
    );

    expect(screen.getByRole('heading', { name: /delhi weather/i })).toBeInTheDocument();
    expect(screen.getByText(/detailed updates for delhi/i)).toBeInTheDocument();
  });

  it('has proper accessibility roles and layout', () => {
    render(<AppHeader />);
    const banner = screen.getByRole('banner');
    expect(banner).toBeInTheDocument();
    expect(banner).toHaveClass('text-center');
  });
});
