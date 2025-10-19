// src/components/__tests__/PredictionsSection.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import PredictionsSection from '../PredictionsSection';

// Mock PredictionBadge so we don't test its internal rendering
jest.mock('../PredictionBadge', () => ({
  __esModule: true,
  default: ({ prediction }: { prediction: string }) => (
    <div data-testid="prediction-badge">{prediction}</div>
  ),
}));

describe('PredictionsSection', () => {
  const dtTxt = '2025-10-19 10:00:00';

  it('renders PredictionBadge components for each prediction', () => {
    const predictions = ['Sunny', 'Rain expected', 'Cloudy'];
    render(<PredictionsSection predictions={predictions} dtTxt={dtTxt} />);

    // There should be the "Predictions" header
    expect(screen.getByText('Predictions')).toBeInTheDocument();

    // All PredictionBadge components rendered
    const badges = screen.getAllByTestId('prediction-badge');
    expect(badges).toHaveLength(predictions.length);

    predictions.forEach((prediction) => {
      expect(screen.getByText(prediction)).toBeInTheDocument();
    });
  });

  it('renders fallback message when there are no predictions', () => {
    render(<PredictionsSection predictions={[]} dtTxt={dtTxt} />);

    // Should display "No special predictions"
    expect(screen.getByText('No special predictions')).toBeInTheDocument();

    // The alert triangle header should NOT exist
    expect(screen.queryByText('Predictions')).not.toBeInTheDocument();
  });

  it('renders fallback message when predictions is undefined', () => {
    // @ts-ignore Testing runtime undefined
    render(<PredictionsSection predictions={undefined} dtTxt={dtTxt} />);

    expect(screen.getByText('No special predictions')).toBeInTheDocument();
    expect(screen.queryByText('Predictions')).not.toBeInTheDocument();
  });
});
