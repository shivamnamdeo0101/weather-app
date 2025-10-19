import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import PredictionBadge from '../PredictionBadge';

describe('PredictionBadge', () => {
  const testCases = [
    { prediction: 'Rain expected', colorClass: 'border-blue-500/50 bg-blue-500/10' },
    { prediction: 'Clear skies', colorClass: 'border-yellow-500/50 bg-yellow-500/10' },
    { prediction: 'Cloudy day', colorClass: 'border-gray-500/50 bg-gray-500/10' },
    { prediction: 'Thunderstorm', colorClass: 'border-purple-500/50 bg-purple-500/10' },
    { prediction: 'Unexpected weather', colorClass: 'border-orange-500/50 bg-orange-500/10' },
  ];

  testCases.forEach(({ prediction, colorClass }) => {
    it(`renders correct icon and color for "${prediction}"`, () => {
      render(<PredictionBadge prediction={prediction} />);

      // Check the icon exists
      const icon = screen.getByTestId('prediction-icon') as unknown as SVGElement;
      expect(icon).toBeInTheDocument();

      // Check the wrapper has the correct color classes
      const wrapper = icon.parentElement as HTMLElement;
      expect(wrapper).toHaveClass(colorClass);

      // Check the text
      expect(screen.getByText(prediction)).toBeInTheDocument();
    });
  });

  it('applies custom className if provided', () => {
    render(<PredictionBadge prediction="Rain expected" className="custom-class" />);
    const icon = screen.getByTestId('prediction-icon') as unknown as SVGElement;
    const wrapper = icon.parentElement as HTMLElement;
    expect(wrapper).toHaveClass('custom-class');
  });
});
