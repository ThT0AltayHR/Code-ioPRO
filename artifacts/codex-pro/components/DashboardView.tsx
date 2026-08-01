import React, { useRef, useState } from 'react';
import {
  Animated,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  Platform,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useApp } from '@/context/AppContext';
import BrainAnimation from './BrainAnimation';

const QUICK_COMMANDS = [
  { icon: 'code', label: 'Kod Yaz', color: '#3B82F6', prompt: 'Benim için basit bir React Native bileşeni yaz' },
  { icon: 'search', label: 'Hata Bul', color: '#EF4444', prompt: 'Bu kodda hata var mı kontrol et ve düzelt' },
  { icon: 'layers', label: 'Refactor', color: '#8B5CF6', prompt: 'Bu kodu daha temiz ve okunabilir hale getir' },
  { icon: 'book', label: 'Açıkla', color: '#22C55E', prompt: 'Bu kodu satır satır açıkla' },
  { icon: 'zap', label: 'Optimize', color: '#FBBF24', prompt: 'Bu kodun performansını nasıl iyileştirebilirim?' },
  { icon: 'file-text', label: 'Dokümante', color: '#06B6D4', prompt: 'Bu kod için JSDoc yorumları ekle' },
];

const STATS = [
  { label: 'Tamamlanan', value: '24', unit: 'görev', icon: 'check-circle', color: '#22C55E' },
  { label: 'Yazılan', value: '1.2k', unit: 'satır', icon: 'code', color: '#3B82F6' },
  { label: 'Sohbet', value: '87', unit: 'mesaj', icon: 'message-square', color: '#8B5CF6' },
  { label: 'Verimlilik', value: '94', unit: '%', icon: 'trending-up', color: '#FBBF24' },
];

const AGENTS = [
  { name: 'Kod Analiz', status: 'active', color: '#22C55E' },
  { name: 'Hata Tespiti', status: 'active', color: '#22C55E' },
  { name: 'Dokümantasyon', status: 'idle', color: '#FBBF24' },
  { name: 'Test Üretici', status: 'idle', color: '#FBBF24' },
];

export default function DashboardView() {
  const { userName, selectedModel, setShowModelSelector, sendMessage, setActiveTab, setShowVoiceModal } = useApp();
  const insets = useSafeAreaInsets();
  const [quickInput, setQuickInput] = useState('');
  const sendBtnScale = useRef(new Animated.Value(1)).current;

  const handleQuickSend = async () => {
    if (!quickInput.trim()) return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    Animated.sequence([
      Animated.timing(sendBtnScale, { toValue: 0.9, duration: 100, useNativeDriver: true }),
      Animated.timing(sendBtnScale, { toValue: 1, duration: 100, useNativeDriver: true }),
    ]).start();
    const msg = quickInput.trim();
    setQuickInput('');
    await sendMessage(msg);
    setActiveTab('chat');
  };

  const handleQuickCommand = async (cmd: typeof QUICK_COMMANDS[0]) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    await sendMessage(cmd.prompt);
    setActiveTab('chat');
  };

  const topPad = Platform.OS === 'web' ? 67 : insets.top;

  return (
    <ScrollView
      style={styles.root}
      contentContainerStyle={[styles.content, { paddingTop: topPad, paddingBottom: insets.bottom + 80 }]}
      showsVerticalScrollIndicator={false}
    >
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.logoRow}>
          <View style={styles.logoGrid}>
            <View style={[styles.logoSq, { backgroundColor: '#3B82F6' }]} />
            <View style={[styles.logoSq, { backgroundColor: '#8B5CF6' }]} />
            <View style={[styles.logoSq, { backgroundColor: '#8B5CF6' }]} />
            <View style={[styles.logoSq, { backgroundColor: '#3B82F6' }]} />
          </View>
          <View>
            <Text style={styles.logoText}>CODEX PRO</Text>
            <Text style={styles.logoVer}>v2.0.0</Text>
          </View>
        </View>
        <View style={styles.headerRight}>
          <TouchableOpacity style={styles.modelBtn} onPress={() => setShowModelSelector(true)} activeOpacity={0.7}>
            <Feather name="cpu" size={12} color="#3B82F6" />
            <Text style={styles.modelBtnText} numberOfLines={1}>{selectedModel.name}</Text>
            <Feather name="chevron-down" size={12} color="#8B95A7" />
          </TouchableOpacity>
        </View>
      </View>

      {/* Welcome */}
      <View style={styles.welcomeSection}>
        <View style={styles.welcomeLeft}>
          <Text style={styles.greeting}>Merhaba,</Text>
          <Text style={styles.userName}>{userName} 👋</Text>
          <Text style={styles.welcomeSub}>Bugün ne kodlayalım?</Text>
          <View style={styles.statusBadge}>
            <View style={styles.statusDot} />
            <Text style={styles.statusText}>Sistem Hazır</Text>
          </View>
        </View>
        <View style={styles.welcomeRight}>
          <BrainAnimation />
        </View>
      </View>

      {/* Stats */}
      <View style={styles.statsGrid}>
        {STATS.map((s, i) => (
          <View key={i} style={styles.statCard}>
            <View style={[styles.statIcon, { backgroundColor: s.color + '22' }]}>
              <Feather name={s.icon as any} size={14} color={s.color} />
            </View>
            <Text style={styles.statValue}>{s.value}<Text style={styles.statUnit}>{s.unit}</Text></Text>
            <Text style={styles.statLabel}>{s.label}</Text>
          </View>
        ))}
      </View>

      {/* Quick AI Chat */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Hızlı AI Sohbet</Text>
        <View style={styles.quickInput}>
          <TextInput
            style={styles.quickInputField}
            placeholder="Bir soru sor veya kod yaz..."
            placeholderTextColor="#5B6472"
            value={quickInput}
            onChangeText={setQuickInput}
            multiline
            maxLength={1000}
            selectionColor="#3B82F6"
            returnKeyType="send"
            onSubmitEditing={handleQuickSend}
          />
          <View style={styles.quickInputActions}>
            <TouchableOpacity style={styles.voiceBtn} onPress={() => setShowVoiceModal(true)} activeOpacity={0.7}>
              <Feather name="mic" size={16} color="#8B95A7" />
            </TouchableOpacity>
            <Animated.View style={{ transform: [{ scale: sendBtnScale }] }}>
              <TouchableOpacity
                style={[styles.sendBtn, !quickInput.trim() && styles.sendBtnDisabled]}
                onPress={handleQuickSend}
                activeOpacity={0.8}
              >
                <Feather name="send" size={16} color="#FFFFFF" />
              </TouchableOpacity>
            </Animated.View>
          </View>
        </View>
      </View>

      {/* Quick Commands */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Hızlı Komutlar</Text>
        <View style={styles.commandsGrid}>
          {QUICK_COMMANDS.map((cmd, i) => (
            <TouchableOpacity
              key={i}
              style={styles.commandCard}
              onPress={() => handleQuickCommand(cmd)}
              activeOpacity={0.7}
            >
              <View style={[styles.commandIcon, { backgroundColor: cmd.color + '22' }]}>
                <Feather name={cmd.icon as any} size={16} color={cmd.color} />
              </View>
              <Text style={styles.commandLabel}>{cmd.label}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Agent Status */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>AI Ajanlar</Text>
          <View style={styles.activeBadge}>
            <View style={[styles.statusDot, { width: 6, height: 6 }]} />
            <Text style={styles.activeBadgeText}>2 Aktif</Text>
          </View>
        </View>
        <View style={styles.agentsGrid}>
          {AGENTS.map((agent, i) => (
            <View key={i} style={styles.agentCard}>
              <View style={[styles.agentDot, { backgroundColor: agent.color }]} />
              <Text style={styles.agentName}>{agent.name}</Text>
              <Text style={[styles.agentStatus, { color: agent.color }]}>
                {agent.status === 'active' ? '●' : '○'}
              </Text>
            </View>
          ))}
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#050810',
  },
  content: {
    paddingHorizontal: 16,
    gap: 20,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  logoGrid: {
    width: 30,
    height: 30,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 2,
  },
  logoSq: {
    width: 13,
    height: 13,
    borderRadius: 2,
  },
  logoText: {
    fontSize: 16,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    letterSpacing: 1.5,
    fontFamily: 'Inter_700Bold',
  },
  logoVer: {
    fontSize: 10,
    color: '#3B82F6',
    letterSpacing: 1,
    fontFamily: 'Inter_400Regular',
  },
  headerRight: {
    gap: 8,
  },
  modelBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: '#0F2044',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#1E3A6E',
    maxWidth: 140,
  },
  modelBtnText: {
    fontSize: 11,
    color: '#3B82F6',
    fontFamily: 'Inter_500Medium',
    flex: 1,
  },
  welcomeSection: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#0B1120',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#1E293B',
    overflow: 'hidden',
  },
  welcomeLeft: {
    flex: 1,
    gap: 4,
  },
  welcomeRight: {
    opacity: 0.7,
    transform: [{ scale: 0.55 }],
    marginRight: -30,
  },
  greeting: {
    fontSize: 14,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  userName: {
    fontSize: 22,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  welcomeSub: {
    fontSize: 13,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    marginBottom: 8,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    alignSelf: 'flex-start',
    backgroundColor: '#16A34A22',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#16A34A44',
  },
  statusDot: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#22C55E',
  },
  statusText: {
    fontSize: 11,
    color: '#22C55E',
    fontFamily: 'Inter_500Medium',
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  statCard: {
    flex: 1,
    minWidth: '44%',
    backgroundColor: '#0B1120',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#1E293B',
    gap: 6,
  },
  statIcon: {
    width: 30,
    height: 30,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statValue: {
    fontSize: 22,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  statUnit: {
    fontSize: 12,
    fontWeight: '400' as const,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  statLabel: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  section: {
    gap: 12,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
    letterSpacing: 0.5,
  },
  activeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: '#16A34A22',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  activeBadgeText: {
    fontSize: 10,
    color: '#22C55E',
    fontFamily: 'Inter_500Medium',
  },
  quickInput: {
    backgroundColor: '#0B1120',
    borderWidth: 1,
    borderColor: '#1E293B',
    borderRadius: 14,
    padding: 12,
    gap: 8,
  },
  quickInputField: {
    fontSize: 14,
    color: '#F3F4F6',
    fontFamily: 'Inter_400Regular',
    minHeight: 44,
    maxHeight: 100,
    padding: 0,
  },
  quickInputActions: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: 8,
  },
  voiceBtn: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  sendBtn: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: '#3B82F6',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendBtnDisabled: {
    backgroundColor: '#1E3A6E',
  },
  commandsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  commandCard: {
    minWidth: '30%',
    flex: 1,
    backgroundColor: '#0B1120',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    gap: 8,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  commandIcon: {
    width: 36,
    height: 36,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  commandLabel: {
    fontSize: 11,
    color: '#8B95A7',
    fontFamily: 'Inter_500Medium',
    textAlign: 'center',
  },
  agentsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  agentCard: {
    flex: 1,
    minWidth: '44%',
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#0B1120',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    gap: 8,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  agentDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  agentName: {
    flex: 1,
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  agentStatus: {
    fontSize: 10,
  },
});
