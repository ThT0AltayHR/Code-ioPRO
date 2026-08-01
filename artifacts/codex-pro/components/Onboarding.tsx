import React, { useRef, useState } from 'react';
import {
  Animated,
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';
import { useApp } from '@/context/AppContext';
import BrainAnimation from './BrainAnimation';

export default function Onboarding() {
  const { completeOnboarding } = useApp();
  const insets = useSafeAreaInsets();
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const scaleAnim = useRef(new Animated.Value(1)).current;

  const handleStart = () => {
    if (!name.trim()) {
      setError('Lütfen adınızı girin');
      return;
    }
    if (name.trim().length < 2) {
      setError('Ad en az 2 karakter olmalı');
      return;
    }
    setError('');
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);

    Animated.sequence([
      Animated.timing(scaleAnim, { toValue: 0.95, duration: 100, useNativeDriver: true }),
      Animated.timing(scaleAnim, { toValue: 1, duration: 100, useNativeDriver: true }),
    ]).start(() => {
      completeOnboarding(name.trim());
    });
  };

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={[styles.container, { paddingTop: insets.top + 20, paddingBottom: insets.bottom + 20 }]}>
        {/* Logo */}
        <View style={styles.logoRow}>
          <View style={styles.logoGrid}>
            <View style={[styles.logoSquare, { backgroundColor: '#3B82F6' }]} />
            <View style={[styles.logoSquare, { backgroundColor: '#8B5CF6' }]} />
            <View style={[styles.logoSquare, { backgroundColor: '#8B5CF6' }]} />
            <View style={[styles.logoSquare, { backgroundColor: '#3B82F6' }]} />
          </View>
          <View>
            <Text style={styles.logoTitle}>CODEX PRO</Text>
            <Text style={styles.logoVersion}>v2.0.0</Text>
          </View>
        </View>

        {/* Brain Animation */}
        <View style={styles.brainContainer}>
          <BrainAnimation />
        </View>

        {/* Tagline */}
        <Text style={styles.tagline}>
          Yapay Zeka Destekli{'\n'}
          <Text style={styles.taglineAccent}>Kodlama Asistanı</Text>
        </Text>
        <Text style={styles.subtitle}>
          GPT-4o • Claude • Gemini • Mistral
        </Text>

        {/* Name Input */}
        <View style={styles.inputSection}>
          <Text style={styles.inputLabel}>Adınız nedir?</Text>
          <TextInput
            style={[styles.input, error ? styles.inputError : null]}
            placeholder="Adınızı girin..."
            placeholderTextColor="#5B6472"
            value={name}
            onChangeText={t => { setName(t); setError(''); }}
            autoCapitalize="words"
            returnKeyType="done"
            onSubmitEditing={handleStart}
            selectionColor="#3B82F6"
          />
          {error ? <Text style={styles.errorText}>{error}</Text> : null}
        </View>

        {/* Start Button */}
        <Animated.View style={{ transform: [{ scale: scaleAnim }], width: '100%' }}>
          <TouchableOpacity
            style={[styles.startBtn, !name.trim() && styles.startBtnDisabled]}
            onPress={handleStart}
            activeOpacity={0.85}
          >
            <Text style={styles.startBtnText}>Başla →</Text>
          </TouchableOpacity>
        </Animated.View>

        <Text style={styles.footer}>
          DuckDuckGo AI · Gizlilik Öncelikli
        </Text>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#050810',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  logoGrid: {
    width: 36,
    height: 36,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 2,
  },
  logoSquare: {
    width: 16,
    height: 16,
    borderRadius: 3,
  },
  logoTitle: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    letterSpacing: 2,
    fontFamily: 'Inter_700Bold',
  },
  logoVersion: {
    fontSize: 11,
    color: '#3B82F6',
    letterSpacing: 1,
    fontFamily: 'Inter_500Medium',
  },
  brainContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  tagline: {
    fontSize: 26,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    textAlign: 'center',
    lineHeight: 34,
    fontFamily: 'Inter_700Bold',
  },
  taglineAccent: {
    color: '#3B82F6',
  },
  subtitle: {
    fontSize: 12,
    color: '#5B6472',
    letterSpacing: 1.5,
    fontFamily: 'Inter_400Regular',
  },
  inputSection: {
    width: '100%',
    gap: 8,
  },
  inputLabel: {
    fontSize: 14,
    color: '#8B95A7',
    fontFamily: 'Inter_500Medium',
    marginLeft: 4,
  },
  input: {
    backgroundColor: '#0B1120',
    borderWidth: 1,
    borderColor: '#1E293B',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 16,
    color: '#F3F4F6',
    fontFamily: 'Inter_400Regular',
  },
  inputError: {
    borderColor: '#EF4444',
  },
  errorText: {
    fontSize: 12,
    color: '#EF4444',
    marginLeft: 4,
    fontFamily: 'Inter_400Regular',
  },
  startBtn: {
    backgroundColor: '#3B82F6',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
  },
  startBtnDisabled: {
    backgroundColor: '#1E3A6E',
  },
  startBtnText: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: '#FFFFFF',
    fontFamily: 'Inter_600SemiBold',
    letterSpacing: 0.5,
  },
  footer: {
    fontSize: 11,
    color: '#5B6472',
    fontFamily: 'Inter_400Regular',
  },
});
