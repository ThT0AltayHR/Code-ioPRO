import React, { useEffect, useRef } from 'react';
import {
  Animated,
  Modal,
  Platform,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import * as WebBrowser from 'expo-web-browser';
import { useApp } from '@/context/AppContext';

export default function VoiceChatModal() {
  const { showVoiceModal, setShowVoiceModal, selectedModel } = useApp();
  const insets = useSafeAreaInsets();
  const pulseAnim = useRef(new Animated.Value(1)).current;
  const ring1Anim = useRef(new Animated.Value(1)).current;
  const ring2Anim = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    if (!showVoiceModal) return;

    const pulse = Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, { toValue: 1.1, duration: 600, useNativeDriver: true }),
        Animated.timing(pulseAnim, { toValue: 1, duration: 600, useNativeDriver: true }),
      ])
    );

    const ring1 = Animated.loop(
      Animated.sequence([
        Animated.timing(ring1Anim, { toValue: 1.5, duration: 1000, useNativeDriver: true }),
        Animated.timing(ring1Anim, { toValue: 1, duration: 0, useNativeDriver: true }),
      ])
    );

    const ring2 = Animated.loop(
      Animated.sequence([
        Animated.delay(400),
        Animated.timing(ring2Anim, { toValue: 1.5, duration: 1000, useNativeDriver: true }),
        Animated.timing(ring2Anim, { toValue: 1, duration: 0, useNativeDriver: true }),
      ])
    );

    pulse.start();
    ring1.start();
    ring2.start();

    return () => {
      pulse.stop();
      ring1.stop();
      ring2.stop();
    };
  }, [showVoiceModal]);

  const openDuckAI = async () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    const url = 'https://duck.ai/chat';
    if (Platform.OS !== 'web') {
      await WebBrowser.openBrowserAsync(url, {
        presentationStyle: WebBrowser.WebBrowserPresentationStyle.FORM_SHEET,
        toolbarColor: '#0B1120',
        controlsColor: '#3B82F6',
        createTask: false,
      });
    }
    setShowVoiceModal(false);
  };

  return (
    <Modal
      visible={showVoiceModal}
      animationType="fade"
      transparent
      onRequestClose={() => setShowVoiceModal(false)}
    >
      <View style={styles.overlay}>
        <View style={[styles.container, { paddingTop: insets.top + 16, paddingBottom: insets.bottom + 16 }]}>
          {/* Header */}
          <View style={styles.header}>
            <TouchableOpacity onPress={() => setShowVoiceModal(false)} style={styles.closeBtn}>
              <Feather name="x" size={20} color="#8B95A7" />
            </TouchableOpacity>
            <Text style={styles.headerTitle}>Sesli Sohbet</Text>
            <View style={styles.modelBadge}>
              <Feather name="zap" size={10} color="#3B82F6" />
              <Text style={styles.modelBadgeText}>{selectedModel.name}</Text>
            </View>
          </View>

          {/* Animated mic */}
          <View style={styles.animationArea}>
            <Animated.View style={[styles.ring, styles.ring2, { transform: [{ scale: ring2Anim }] }]} />
            <Animated.View style={[styles.ring, styles.ring1, { transform: [{ scale: ring1Anim }] }]} />
            <Animated.View style={[styles.micContainer, { transform: [{ scale: pulseAnim }] }]}>
              <Feather name="mic" size={36} color="#FFFFFF" />
            </Animated.View>
          </View>

          <Text style={styles.title}>DuckDuckGo Sesli Sohbet</Text>
          <Text style={styles.subtitle}>
            duck.ai üzerinden güvenli,{'\n'}gizlilik öncelikli sesli AI sohbeti
          </Text>

          {/* Features */}
          <View style={styles.features}>
            {[
              { icon: 'shield', text: 'Sohbet geçmişi kaydedilmez' },
              { icon: 'mic', text: 'Doğal sesli konuşma' },
              { icon: 'cpu', text: `${selectedModel.name} modeli aktif` },
            ].map((f, i) => (
              <View key={i} style={styles.featureRow}>
                <View style={styles.featureIcon}>
                  <Feather name={f.icon as any} size={14} color="#3B82F6" />
                </View>
                <Text style={styles.featureText}>{f.text}</Text>
              </View>
            ))}
          </View>

          {/* Open duck.ai button */}
          <TouchableOpacity style={styles.openBtn} onPress={openDuckAI} activeOpacity={0.85}>
            <Feather name="external-link" size={18} color="#FFFFFF" />
            <Text style={styles.openBtnText}>duck.ai'yi Aç</Text>
          </TouchableOpacity>

          <TouchableOpacity onPress={() => setShowVoiceModal(false)} style={styles.cancelBtn}>
            <Text style={styles.cancelBtnText}>İptal</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: '#000000CC',
    justifyContent: 'flex-end',
  },
  container: {
    backgroundColor: '#0B1120',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 24,
    alignItems: 'center',
    borderTopWidth: 1,
    borderColor: '#1E293B',
  },
  header: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 32,
  },
  closeBtn: {
    padding: 4,
  },
  headerTitle: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
  },
  modelBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#0F2044',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#1E3A6E',
  },
  modelBadgeText: {
    fontSize: 10,
    color: '#3B82F6',
    fontFamily: 'Inter_500Medium',
  },
  animationArea: {
    width: 140,
    height: 140,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 24,
  },
  ring: {
    position: 'absolute',
    borderRadius: 100,
    borderWidth: 1,
    borderColor: '#3B82F6',
  },
  ring1: {
    width: 100,
    height: 100,
    opacity: 0.3,
  },
  ring2: {
    width: 130,
    height: 130,
    opacity: 0.15,
  },
  micContainer: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#3B82F6',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#3B82F6',
    shadowOpacity: 0.5,
    shadowRadius: 20,
    shadowOffset: { width: 0, height: 0 },
    elevation: 12,
  },
  title: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 28,
  },
  features: {
    width: '100%',
    gap: 12,
    marginBottom: 32,
    backgroundColor: '#111A2C',
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  featureRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  featureIcon: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: '#0F2044',
    alignItems: 'center',
    justifyContent: 'center',
  },
  featureText: {
    fontSize: 13,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  openBtn: {
    width: '100%',
    backgroundColor: '#3B82F6',
    borderRadius: 12,
    paddingVertical: 15,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 12,
  },
  openBtnText: {
    fontSize: 15,
    fontWeight: '600' as const,
    color: '#FFFFFF',
    fontFamily: 'Inter_600SemiBold',
  },
  cancelBtn: {
    paddingVertical: 12,
  },
  cancelBtnText: {
    fontSize: 14,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
});
