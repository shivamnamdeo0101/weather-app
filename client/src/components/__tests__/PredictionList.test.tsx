// src/components/__tests__/PredictionBadge.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import PredictionBadge from '../PredictionBadge';

describe('PredictionBadge', () => {
  it('renders the prediction text', () => {
    render(<PredictionBadge prediction="Sunny" />);
    expect(screen.getByText('Sunny')).toBeInTheDocument();
  });

  it('applies correct color classes for different predictions', () => {
    const { rerender } = render(<PredictionBadge prediction="Clear sky" />);
    expect(screen.getByText('Clear sky').parentElement).toHaveClass(
      'border-yellow-500/50 bg-yellow-500/10'
    );

    rerender(<PredictionBadge prediction="Light rain" />);
    expect(screen.getByText('Light rain').parentElement).toHaveClass(
      'border-blue-500/50 bg-blue-500/10'
    );

    rerender(<PredictionBadge prediction="Cloudy" />);
    expect(screen.getByText('Cloudy').parentElement).toHaveClass(
      'border-gray-500/50 bg-gray-500/10'
    );

    rerender(<PredictionBadge prediction="Thunderstorm" />);
    expect(screen.getByText('Thunderstorm').parentElement).toHaveClass(
      'border-purple-500/50 bg-purple-500/10'
    );

    rerender(<PredictionBadge prediction="Foggy" />);
    expect(screen.getByText('Foggy').parentElement).toHaveClass(
      'border-orange-500/50 bg-orange-500/10'
    );
  });

  it('renders the correct icon component for predictions', () => {
    const { rerender } = render(<PredictionBadge prediction="Sunny" />);
    let icon = screen.getByTestId('prediction-icon'); // <-- directly
    expect(icon).toBeInTheDocument(); // ✅

    rerender(<PredictionBadge prediction="Light rain" />);
    icon = screen.getByTestId('prediction-icon');
    expect(icon).toBeInTheDocument();

    rerender(<PredictionBadge prediction="Cloudy" />);
    icon = screen.getByTestId('prediction-icon');
    expect(icon).toBeInTheDocument();

    rerender(<PredictionBadge prediction="Thunderstorm" />);
    icon = screen.getByTestId('prediction-icon');
    expect(icon).toBeInTheDocument();

    rerender(<PredictionBadge prediction="Foggy" />);
    icon = screen.getByTestId('prediction-icon');
    expect(icon).toBeInTheDocument();
  });

  it('merges custom className with default classes', () => {
    render(<PredictionBadge prediction="Sunny" className="custom-class" />);
    const badge = screen.getByText('Sunny').parentElement;
    expect(badge).toHaveClass('custom-class');
  });
});
