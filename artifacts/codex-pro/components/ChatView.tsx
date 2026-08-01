import React, { useRef, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { KeyboardAvoidingView } from 'react-native-keyboard-controller';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { ChatMessage } from '@/services/duckAI';
import { useApp } from '@/context/AppContext';

function MessageBubble({ msg, modelName }: { msg: ChatMessage; modelName: string }) {
  const isUser = msg.role === 'user';
  const time = new Date(msg.timestamp).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });

  return (
    <View style={[styles.messageRow, isUser ? styles.messageRowUser : styles.messageRowAI]}>
      {!isUser && (
        <View style={styles.avatarAI}>
          <Feather name="cpu" size={12} color="#3B82F6" />
        </View>
      )}
      <View style={[styles.bubble, isUser ? styles.bubbleUser : styles.bubbleAI]}>
        {!isUser && (
          <Text style={styles.modelLabel}>{modelName}</Text>
        )}
        <Text style={[styles.bubbleText, isUser ? styles.bubbleTextUser : styles.bubbleTextAI]}>
          {msg.content}
        </Text>
        <Text style={styles.timeText}>{time}</Text>
      </View>
      {isUser && (
        <View style={styles.avatarUser}>
          <Feather name="user" size={12} color="#8B95A7" />
        </View>
      )}
    </View>
  );
}

function TypingIndicator({ modelName }: { modelName: string }) {
  return (
    <View style={[styles.messageRow, styles.messageRowAI]}>
      <View style={styles.avatarAI}>
        <Feather name="cpu" size={12} color="#3B82F6" />
      </View>
      <View style={[styles.bubble, styles.bubbleAI, styles.typingBubble]}>
        <Text style={styles.modelLabel}>{modelName}</Text>
        <View style={styles.typingDots}>
          <ActivityIndicator size="small" color="#3B82F6" />
          <Text style={styles.typingText}>Düşünüyor...</Text>
        </View>
      </View>
    </View>
  );
}

export default function ChatView() {
  const { messages, isLoading, sendMessage, clearMessages, selectedModel, setShowModelSelector, setShowVoiceModal } = useApp();
  const insets = useSafeAreaInsets();
  const [input, setInput] = useState('');
  const inputRef = useRef<TextInput>(null);

  const topPad = Platform.OS === 'web' ? 67 : insets.top;

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    const msg = input.trim();
    setInput('');
    await sendMessage(msg);
  };

  // inverted so newest messages are at bottom
  const reversedMessages = [...messages].reverse();

  return (
    <View style={[styles.root, { paddingTop: topPad }]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <View style={styles.aiDot} />
          <Text style={styles.headerTitle}>AI Sohbet</Text>
        </View>
        <View style={styles.headerActions}>
          <TouchableOpacity
            style={styles.headerBtn}
            onPress={() => setShowModelSelector(true)}
            activeOpacity={0.7}
          >
            <Feather name="cpu" size={13} color="#3B82F6" />
            <Text style={styles.modelBtnText} numberOfLines={1}>{selectedModel.name}</Text>
            <Feather name="chevron-down" size={12} color="#8B95A7" />
          </TouchableOpacity>
          {messages.length > 0 && (
            <TouchableOpacity style={styles.iconBtn} onPress={clearMessages} activeOpacity={0.7}>
              <Feather name="trash-2" size={16} color="#8B95A7" />
            </TouchableOpacity>
          )}
        </View>
      </View>

      <KeyboardAvoidingView
        style={styles.flex}
        behavior="padding"
        keyboardVerticalOffset={0}
      >
        {/* Messages */}
        {messages.length === 0 ? (
          <View style={styles.emptyState}>
            <View style={styles.emptyIcon}>
              <Feather name="message-circle" size={32} color="#3B82F6" />
            </View>
            <Text style={styles.emptyTitle}>Sohbete Başla</Text>
            <Text style={styles.emptyText}>
              {selectedModel.name} modeli ile{'\n'}kodlama sorularını sor
            </Text>
            <View style={styles.emptySuggestions}>
              {['Hızlı kod yaz', 'Hata ayıkla', 'Konsept açıkla'].map((s, i) => (
                <TouchableOpacity
                  key={i}
                  style={styles.suggestionChip}
                  onPress={() => sendMessage(s)}
                  activeOpacity={0.7}
                >
                  <Text style={styles.suggestionText}>{s}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        ) : (
          <FlatList
            data={reversedMessages}
            keyExtractor={item => item.id}
            renderItem={({ item }) => (
              <MessageBubble msg={item} modelName={selectedModel.name} />
            )}
            inverted
            ListHeaderComponent={isLoading ? <TypingIndicator modelName={selectedModel.name} /> : null}
            contentContainerStyle={[styles.messagesList, { paddingBottom: 12 }]}
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
            keyboardDismissMode="interactive"
          />
        )}

        {/* Input bar */}
        <View style={[styles.inputBar, { paddingBottom: insets.bottom + 8 }]}>
          <TouchableOpacity
            style={styles.voiceBtn}
            onPress={() => setShowVoiceModal(true)}
            activeOpacity={0.7}
          >
            <Feather name="mic" size={18} color="#8B95A7" />
          </TouchableOpacity>
          <TextInput
            ref={inputRef}
            style={styles.input}
            placeholder="Mesajınızı yazın..."
            placeholderTextColor="#5B6472"
            value={input}
            onChangeText={setInput}
            multiline
            maxLength={4000}
            selectionColor="#3B82F6"
          />
          <TouchableOpacity
            style={[styles.sendBtn, (!input.trim() || isLoading) && styles.sendBtnDisabled]}
            onPress={handleSend}
            activeOpacity={0.8}
            disabled={!input.trim() || isLoading}
          >
            {isLoading ? (
              <ActivityIndicator size="small" color="#FFFFFF" />
            ) : (
              <Feather name="send" size={18} color="#FFFFFF" />
            )}
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#050810',
  },
  flex: { flex: 1 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderColor: '#1E293B',
    backgroundColor: '#080C16',
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  aiDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#22C55E',
  },
  headerTitle: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  headerBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#0F2044',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#1E3A6E',
    maxWidth: 130,
  },
  modelBtnText: {
    fontSize: 11,
    color: '#3B82F6',
    fontFamily: 'Inter_500Medium',
    flex: 1,
  },
  iconBtn: {
    width: 34,
    height: 34,
    borderRadius: 8,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
    gap: 12,
  },
  emptyIcon: {
    width: 64,
    height: 64,
    borderRadius: 20,
    backgroundColor: '#0F2044',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  emptyText: {
    fontSize: 14,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    textAlign: 'center',
    lineHeight: 20,
  },
  emptySuggestions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    justifyContent: 'center',
    marginTop: 8,
  },
  suggestionChip: {
    backgroundColor: '#111A2C',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  suggestionText: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_500Medium',
  },
  messagesList: {
    paddingHorizontal: 12,
    paddingTop: 12,
    gap: 4,
  },
  messageRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 8,
    marginVertical: 4,
  },
  messageRowUser: {
    justifyContent: 'flex-end',
  },
  messageRowAI: {
    justifyContent: 'flex-start',
  },
  avatarAI: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: '#0F2044',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E3A6E',
    flexShrink: 0,
  },
  avatarUser: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
    flexShrink: 0,
  },
  bubble: {
    maxWidth: '78%',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 10,
    gap: 4,
  },
  bubbleUser: {
    backgroundColor: '#1E3A6E',
    borderBottomRightRadius: 4,
    borderWidth: 1,
    borderColor: '#2563EB33',
  },
  bubbleAI: {
    backgroundColor: '#0B1120',
    borderBottomLeftRadius: 4,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  modelLabel: {
    fontSize: 10,
    color: '#3B82F6',
    fontFamily: 'Inter_600SemiBold',
    letterSpacing: 0.5,
  },
  bubbleText: {
    fontSize: 14,
    lineHeight: 20,
    fontFamily: 'Inter_400Regular',
  },
  bubbleTextUser: {
    color: '#E2E8F0',
  },
  bubbleTextAI: {
    color: '#CBD5E1',
  },
  timeText: {
    fontSize: 10,
    color: '#5B6472',
    fontFamily: 'Inter_400Regular',
    alignSelf: 'flex-end',
  },
  typingBubble: {
    minWidth: 120,
  },
  typingDots: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  typingText: {
    fontSize: 13,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: 12,
    paddingTop: 10,
    gap: 8,
    backgroundColor: '#080C16',
    borderTopWidth: 1,
    borderColor: '#1E293B',
  },
  voiceBtn: {
    width: 40,
    height: 40,
    borderRadius: 10,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
    flexShrink: 0,
    marginBottom: 2,
  },
  input: {
    flex: 1,
    backgroundColor: '#111A2C',
    borderWidth: 1,
    borderColor: '#1E293B',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingTop: 10,
    paddingBottom: 10,
    fontSize: 14,
    color: '#F3F4F6',
    fontFamily: 'Inter_400Regular',
    maxHeight: 120,
    minHeight: 40,
  },
  sendBtn: {
    width: 40,
    height: 40,
    borderRadius: 10,
    backgroundColor: '#3B82F6',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    marginBottom: 2,
  },
  sendBtnDisabled: {
    backgroundColor: '#1E3A6E',
  },
});
