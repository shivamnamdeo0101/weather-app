import React from 'react';
import { render, screen } from '@testing-library/react';
import EmptyState from '../EmptyState';

// ✅ Mock lucide-react icons to avoid parsing SVGs in Jest
jest.mock('lucide-react', () => ({
  Cloud: () => <svg data-testid="cloud-icon" />,
}));

describe('EmptyState', () => {
  it('renders the Cloud icon', () => {
    render(<EmptyState />);
    expect(screen.getByTestId('cloud-icon')).toBeInTheDocument();
  });

  it('renders the main heading correctly', () => {
    render(<EmptyState />);
    expect(
      screen.getByRole('heading', { name: /Welcome to Weather Forecast/i })
    ).toBeInTheDocument();
  });

  it('renders the descriptive paragraph', () => {
    render(<EmptyState />);
    expect(
      screen.getByText(/Enter a city name above to get detailed weather forecasts/i)
    ).toBeInTheDocument();
  });

  it('renders all feature bullet points', () => {
    render(<EmptyState />);
    const features = [
      'Real-time weather data',
      '5-day forecasts',
      'Smart predictions',
      'Detailed analytics',
    ];

    features.forEach((feature) => {
      expect(screen.getByText(new RegExp(feature, 'i'))).toBeInTheDocument();
    });
  });

  it('renders overall layout structure', () => {
    const { container } = render(<EmptyState />);
    // ensures top-level wrapper
    expect(container.firstChild).toHaveClass('max-w-4xl');
    // ensures gray background block exists
    expect(container.querySelector('.bg-gray-800')).toBeTruthy();
  });
});
