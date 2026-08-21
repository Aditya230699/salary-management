/**
 * Formats a Date as a calendar date (yyyy-MM-dd) using its local components.
 *
 * `toISOString()` converts to UTC first, so for any timezone behind UTC a date picked as
 * the 1st is sent as the previous month's last day. Salary effective dates drive which
 * record is in force, so an off-by-one day here silently mis-dates compensation history.
 */
export function toLocalDateString(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Parses a yyyy-MM-dd string into local midnight of that calendar date.
 *
 * `new Date('yyyy-MM-dd')` interprets the string as UTC midnight, which renders on the
 * previous calendar day in any timezone behind UTC. The salary dialog derives its minimum
 * effective date from this value, so the drift let users pick a day the backend rejects.
 */
export function parseLocalDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

/** The calendar day after the given date, staying in local time. */
export function nextLocalDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1);
}
