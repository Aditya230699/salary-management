import { toLocalDateString } from './date.util';

describe('toLocalDateString', () => {

  it('formats a date from its local components', () => {
    // Local midnight on the 1st. toISOString() would report the previous day for any
    // timezone behind UTC, which would mis-date a salary record by one day.
    const firstOfMonth = new Date(2024, 3, 1, 0, 0, 0);

    expect(toLocalDateString(firstOfMonth)).toBe('2024-04-01');
  });

  it('pads single digit months and days', () => {
    expect(toLocalDateString(new Date(2024, 0, 5))).toBe('2024-01-05');
  });

  it('keeps the calendar date for a late evening time', () => {
    // A timezone ahead of UTC would roll this forward under toISOString().
    expect(toLocalDateString(new Date(2024, 11, 31, 23, 30))).toBe('2024-12-31');
  });

  it('handles a leap day', () => {
    expect(toLocalDateString(new Date(2024, 1, 29))).toBe('2024-02-29');
  });
});
