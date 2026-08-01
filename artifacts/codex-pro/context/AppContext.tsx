import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ChatMessage, AI_MODELS, AIModel, generateId, sendChatMessage } from '@/services/duckAI';

type TabName = 'dashboard' | 'chat' | 'editor' | 'market' | 'settings';

interface AppContextType {
  // User
  userName: string;
  setUserName: (name: string) => void;
  isOnboarding: boolean;
  completeOnboarding: (name: string) => void;

  // Navigation
  activeTab: TabName;
  setActiveTab: (tab: TabName) => void;

  // AI Model
  selectedModel: AIModel;
  setSelectedModel: (model: AIModel) => void;
  showModelSelector: boolean;
  setShowModelSelector: (show: boolean) => void;

  // Chat
  messages: ChatMessage[];
  isLoading: boolean;
  sendMessage: (content: string) => Promise<void>;
  clearMessages: () => void;

  // Voice
  showVoiceModal: boolean;
  setShowVoiceModal: (show: boolean) => void;

  // Editor
  editorCode: string;
  setEditorCode: (code: string) => void;
  editorLanguage: string;
  setEditorLanguage: (lang: string) => void;
  terminalOutput: string;
  setTerminalOutput: (output: string) => void;
  isRunning: boolean;
  runCode: () => void;
}

const AppContext = createContext<AppContextType | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [userName, setUserNameState] = useState('');
  const [isOnboarding, setIsOnboarding] = useState(true);
  const [activeTab, setActiveTab] = useState<TabName>('dashboard');
  const [selectedModel, setSelectedModelState] = useState<AIModel>(AI_MODELS[0]);
  const [showModelSelector, setShowModelSelector] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showVoiceModal, setShowVoiceModal] = useState(false);
  const [editorCode, setEditorCode] = useState(
`// CODEX PRO - AI Destekli Kod Editörü
// Merhaba! Buraya kod yazın...

function merhaba(isim) {
  const mesaj = \`Merhaba, \${isim}! CODEX PRO'ya hoş geldiniz.\`;
  console.log(mesaj);
  return mesaj;
}

// AI asistanıyla sohbet ederek kod yazmaya başlayın
merhaba("Dünya");`
  );
  const [editorLanguage, setEditorLanguage] = useState('JavaScript');
  const [terminalOutput, setTerminalOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);

  const messagesRef = useRef(messages);
  messagesRef.current = messages;

  // Load saved state
  useEffect(() => {
    (async () => {
      try {
        const savedName = await AsyncStorage.getItem('codex_userName');
        const savedOnboarding = await AsyncStorage.getItem('codex_onboarding');
        const savedModel = await AsyncStorage.getItem('codex_model');
        const savedMessages = await AsyncStorage.getItem('codex_messages');

        if (savedName) setUserNameState(savedName);
        if (savedOnboarding === 'done') setIsOnboarding(false);
        if (savedModel) {
          const model = AI_MODELS.find(m => m.id === savedModel);
          if (model) setSelectedModelState(model);
        }
        if (savedMessages) {
          try {
            const parsed = JSON.parse(savedMessages);
            if (Array.isArray(parsed)) setMessages(parsed.slice(-50));
          } catch {}
        }
      } catch {}
    })();
  }, []);

  const setUserName = useCallback((name: string) => {
    setUserNameState(name);
    AsyncStorage.setItem('codex_userName', name).catch(() => {});
  }, []);

  const completeOnboarding = useCallback((name: string) => {
    setUserNameState(name);
    setIsOnboarding(false);
    AsyncStorage.multiSet([
      ['codex_userName', name],
      ['codex_onboarding', 'done'],
    ]).catch(() => {});
  }, []);

  const setSelectedModel = useCallback((model: AIModel) => {
    setSelectedModelState(model);
    AsyncStorage.setItem('codex_model', model.id).catch(() => {});
  }, []);

  const sendMessage = useCallback(async (content: string) => {
    if (!content.trim() || isLoading) return;

    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content: content.trim(),
      timestamp: Date.now(),
    };

    const currentMessages = messagesRef.current;
    const newMessages = [...currentMessages, userMsg];
    setMessages(newMessages);
    setIsLoading(true);

    try {
      const apiMessages = newMessages.map(m => ({
        role: m.role,
        content: m.content,
      }));

      const response = await sendChatMessage(apiMessages, selectedModel.duckId);

      const assistantMsg: ChatMessage = {
        id: generateId(),
        role: 'assistant',
        content: response,
        timestamp: Date.now(),
        model: selectedModel.id,
      };

      const finalMessages = [...newMessages, assistantMsg];
      setMessages(finalMessages);
      AsyncStorage.setItem('codex_messages', JSON.stringify(finalMessages.slice(-50))).catch(() => {});
    } catch (e: unknown) {
      const errMsg: ChatMessage = {
        id: generateId(),
        role: 'assistant',
        content: `⚠️ Hata: ${e instanceof Error ? e.message : 'Bağlantı hatası. İnternet bağlantınızı kontrol edin ve tekrar deneyin.'}`,
        timestamp: Date.now(),
        model: selectedModel.id,
      };
      setMessages(prev => [...prev, errMsg]);
    } finally {
      setIsLoading(false);
    }
  }, [isLoading, selectedModel]);

  const clearMessages = useCallback(() => {
    setMessages([]);
    AsyncStorage.removeItem('codex_messages').catch(() => {});
  }, []);

  const runCode = useCallback(() => {
    setIsRunning(true);
    setTerminalOutput('');
    
    // Simulate code execution
    setTimeout(() => {
      const lines = [
        `> Çalıştırılıyor: ${editorLanguage}...`,
        '> CODEX PRO v2.0 Runtime',
        '---',
        'Merhaba, Dünya! CODEX PRO\'ya hoş geldiniz.',
        '---',
        `✓ Başarıyla tamamlandı (${Math.floor(Math.random() * 200 + 50)}ms)`,
      ];
      setTerminalOutput(lines.join('\n'));
      setIsRunning(false);
    }, 1500);
  }, [editorCode, editorLanguage]);

  return (
    <AppContext.Provider
      value={{
        userName, setUserName, isOnboarding, completeOnboarding,
        activeTab, setActiveTab,
        selectedModel, setSelectedModel, showModelSelector, setShowModelSelector,
        messages, isLoading, sendMessage, clearMessages,
        showVoiceModal, setShowVoiceModal,
        editorCode, setEditorCode, editorLanguage, setEditorLanguage,
        terminalOutput, setTerminalOutput, isRunning, runCode,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}
