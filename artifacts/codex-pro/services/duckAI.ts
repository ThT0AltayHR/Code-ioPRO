// DuckDuckGo AI Chat API (free, no API key needed)
// Based on duck.ai API used in duckAssist Android app

export interface AIModel {
  id: string;
  name: string;
  provider: string;
  duckId: string;
  accentColor: string;
  bgColor: string;
  description: string;
  iconFamily: 'Feather' | 'MaterialCommunityIcons' | 'Ionicons';
  icon: string;
}

export const AI_MODELS: AIModel[] = [
  {
    id: 'gpt-4o',
    name: 'GPT-4o',
    provider: 'OpenAI',
    duckId: 'gpt-4o-mini',
    accentColor: '#10A37F',
    bgColor: '#10a37f22',
    description: 'OpenAI\'nin en gelişmiş çok modlu modeli',
    iconFamily: 'Feather',
    icon: 'zap',
  },
  {
    id: 'claude-sonnet',
    name: 'Claude 3.5 Sonnet',
    provider: 'Anthropic',
    duckId: 'claude-3-5-sonnet-20241022',
    accentColor: '#D97757',
    bgColor: '#D9775722',
    description: 'Anthropic\'in zeka ve hız dengeli modeli',
    iconFamily: 'Feather',
    icon: 'feather',
  },
  {
    id: 'claude-haiku',
    name: 'Claude 3 Haiku',
    provider: 'Anthropic',
    duckId: 'claude-3-haiku-20240307',
    accentColor: '#C86442',
    bgColor: '#C8644222',
    description: 'Hızlı ve verimli Claude modeli',
    iconFamily: 'Feather',
    icon: 'feather',
  },
  {
    id: 'gpt-4-turbo',
    name: 'GPT-4 Turbo',
    provider: 'OpenAI',
    duckId: 'o3-mini',
    accentColor: '#10A37F',
    bgColor: '#10a37f22',
    description: 'Gelişmiş akıl yürütme yetenekli model',
    iconFamily: 'Feather',
    icon: 'cpu',
  },
  {
    id: 'gemini-pro',
    name: 'Gemini 1.5 Pro',
    provider: 'Google',
    duckId: 'meta-llama/Llama-3.3-70B-Instruct-Turbo',
    accentColor: '#4285F4',
    bgColor: '#4285F422',
    description: 'Google\'ın uzun bağlam anlama modeli',
    iconFamily: 'Feather',
    icon: 'star',
  },
  {
    id: 'mistral',
    name: 'Mistral Large',
    provider: 'Mistral',
    duckId: 'mistralai/Mistral-Small-3.1-24B-Instruct-2503',
    accentColor: '#FF8205',
    bgColor: '#FF820522',
    description: 'Avrupa\'nın güçlü açık kaynak modeli',
    iconFamily: 'Feather',
    icon: 'wind',
  },
];

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  model?: string;
}

const DUCKAI_STATUS_URL = 'https://duckduckgo.com/duckchat/v1/status';
const DUCKAI_CHAT_URL = 'https://duckduckgo.com/duckchat/v1/chat';

const UA = 'Mozilla/5.0 (Linux; Android 14; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36';

let cachedVqd: string | null = null;

async function getVQD(): Promise<string> {
  try {
    const response = await fetch(DUCKAI_STATUS_URL, {
      method: 'GET',
      headers: {
        'x-vqd-accept': '1',
        'User-Agent': UA,
        'Accept': '*/*',
        'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7',
        'Referer': 'https://duck.ai/',
        'Origin': 'https://duck.ai',
      },
    });
    const vqd = response.headers.get('x-vqd-4');
    if (vqd) {
      cachedVqd = vqd;
      return vqd;
    }
    throw new Error('VQD token alınamadı');
  } catch (e) {
    if (cachedVqd) return cachedVqd;
    throw e;
  }
}

export async function sendChatMessage(
  messages: Array<{ role: string; content: string }>,
  modelId: string,
): Promise<string> {
  const vqd = await getVQD();

  const response = await fetch(DUCKAI_CHAT_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-vqd-4': vqd,
      'User-Agent': UA,
      'Accept': 'text/event-stream',
      'Accept-Language': 'tr-TR,tr;q=0.9,en-US;q=0.8',
      'Referer': 'https://duck.ai/',
      'Origin': 'https://duck.ai',
    },
    body: JSON.stringify({
      model: modelId,
      messages: messages,
    }),
  });

  if (!response.ok) {
    // Try refreshing VQD
    cachedVqd = null;
    throw new Error(`API hatası: ${response.status}`);
  }

  // Update VQD from response if available
  const newVqd = response.headers.get('x-vqd-4');
  if (newVqd) cachedVqd = newVqd;

  const text = await response.text();
  let result = '';

  const lines = text.split('\n');
  for (const line of lines) {
    const trimmed = line.trim();
    if (trimmed.startsWith('data: ')) {
      const data = trimmed.slice(6);
      if (data === '[DONE]') break;
      try {
        const parsed = JSON.parse(data);
        if (parsed.message) result += parsed.message;
      } catch { /* skip malformed */ }
    }
  }

  return result || 'Yanıt alınamadı. Tekrar deneyin.';
}

export function getModelById(id: string): AIModel {
  return AI_MODELS.find(m => m.id === id) ?? AI_MODELS[0];
}

export function generateId(): string {
  return Date.now().toString() + Math.random().toString(36).substr(2, 9);
}
