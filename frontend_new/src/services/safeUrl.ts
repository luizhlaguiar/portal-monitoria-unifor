/**
 * Só devolve a URL se for http(s). Qualquer outra coisa (javascript:, data:, etc.) vira '#',
 * para um anexo malicioso não executar código ao ser clicado.
 */
export function safeUrl(url?: string | null): string {
  if (!url) return '#';
  try {
    const parsed = new URL(url, window.location.origin);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:' ? parsed.href : '#';
  } catch {
    return '#';
  }
}
