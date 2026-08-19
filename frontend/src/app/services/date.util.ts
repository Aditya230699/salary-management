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
