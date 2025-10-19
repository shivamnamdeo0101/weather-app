// src/components/__tests__/SearchBar.test.tsx
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import SearchBar from '../SearchBar';

describe('SearchBar', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    mockOnSearch.mockClear();
  });

  it('renders input and button correctly', () => {
    render(<SearchBar onSearch={mockOnSearch} isLoading={false} />);
    
    const input = screen.getByPlaceholderText(/Enter city name/i);
    const button = screen.getByRole('button', { name: /Search/i });

    expect(input).toBeInTheDocument();
    expect(button).toBeInTheDocument();
    expect(button).toBeDisabled(); // button disabled when input is empty
  });

  it('enables button when input is not empty', () => {
    render(<SearchBar onSearch={mockOnSearch} isLoading={false} />);
    const input = screen.getByPlaceholderText(/Enter city name/i);
    const button = screen.getByRole('button', { name: /Search/i });

    fireEvent.change(input, { target: { value: 'London' } });
    expect(button).not.toBeDisabled();
  });

  it('does not call onSearch if input is empty', () => {
    render(<SearchBar onSearch={mockOnSearch} isLoading={false} />);
    const button = screen.getByRole('button', { name: /Search/i });

    fireEvent.click(button);
    expect(mockOnSearch).not.toHaveBeenCalled();
  });

  it('calls onSearch with trimmed input when submitted', async () => {
    render(<SearchBar onSearch={mockOnSearch} isLoading={false} />);
    
    const input = screen.getByPlaceholderText(/Enter city name/i);
    const button = screen.getByRole('button', { name: /Search/i });

    fireEvent.change(input, { target: { value: '  London  ' } });
    fireEvent.click(button);

    // wait for the promise to resolve if onSearch is async
    await new Promise(process.nextTick);

    expect(mockOnSearch).toHaveBeenCalledTimes(1);
    expect(mockOnSearch).toHaveBeenCalledWith('London');
    expect(input).toHaveValue(''); // input cleared after submit
  });

  it('disables input and button when isLoading is true', () => {
    render(<SearchBar onSearch={mockOnSearch} isLoading={true} />);
    const input = screen.getByPlaceholderText(/Enter city name/i);
    const button = screen.getByRole('button', { name: /Searching/i });

    expect(input).toBeDisabled();
    expect(button).toBeDisabled();
  });
});
