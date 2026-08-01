import React from 'react';
import {
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { AI_MODELS, AIModel } from '@/services/duckAI';
import { useApp } from '@/context/AppContext';

const CAPABILITIES = {
  'gpt-4o': ['Çoklu Modal', 'Kod Üretimi', 'Mantık', 'Analiz'],
  'claude-sonnet': ['Uzun Bağlam', 'Yazı', 'Kod', 'Emniyet'],
  'claude-haiku': ['Hız', 'Verimlilik', 'Özetleme', 'Sohbet'],
  'gpt-4-turbo': ['Derin Mantık', 'Matematik', 'Analiz', 'Arama'],
  'gemini-pro': ['Uzun Bağlam', 'Multimodal', 'Arama', 'Genel'],
  'mistral': ['Açık Kaynak', 'Avrupa', 'Hız', 'Gizlilik'],
};

function ModelCard({ model, isSelected, onSelect }: { model: AIModel; isSelected: boolean; onSelect: () => void }) {
  const caps = CAPABILITIES[model.id as keyof typeof CAPABILITIES] ?? [];

  return (
    <TouchableOpacity
      style={[styles.card, isSelected && { borderColor: model.accentColor, borderWidth: 2 }]}
      onPress={onSelect}
      activeOpacity={0.8}
    >
      {isSelected && (
        <View style={[styles.activeBadge, { backgroundColor: model.accentColor }]}>
          <Text style={styles.activeBadgeText}>Aktif</Text>
        </View>
      )}
      <View style={styles.cardHeader}>
        <View style={[styles.modelIcon, { backgroundColor: model.bgColor }]}>
          <Feather name={model.icon as any} size={22} color={model.accentColor} />
        </View>
        <View style={styles.cardInfo}>
          <Text style={styles.cardName}>{model.name}</Text>
          <Text style={styles.cardProvider}>{model.provider}</Text>
        </View>
        <View style={[styles.freeTag]}>
          <Text style={styles.freeTagText}>ÜCRETSİZ</Text>
        </View>
      </View>
      <Text style={styles.cardDesc}>{model.description}</Text>
      <View style={styles.capsRow}>
        {caps.map((cap, i) => (
          <View key={i} style={[styles.capChip, { backgroundColor: model.bgColor }]}>
            <Text style={[styles.capText, { color: model.accentColor }]}>{cap}</Text>
          </View>
        ))}
      </View>
      <TouchableOpacity
        style={[
          styles.selectBtn,
          isSelected
            ? { backgroundColor: model.bgColor, borderColor: model.accentColor }
            : { backgroundColor: '#111A2C', borderColor: '#1E293B' },
        ]}
        onPress={onSelect}
        activeOpacity={0.8}
      >
        <Feather name={isSelected ? 'check-circle' : 'plus-circle'} size={14} color={isSelected ? model.accentColor : '#8B95A7'} />
        <Text style={[styles.selectBtnText, { color: isSelected ? model.accentColor : '#8B95A7' }]}>
          {isSelected ? 'Aktif Model' : 'Seç'}
        </Text>
      </TouchableOpacity>
    </TouchableOpacity>
  );
}

export default function MarketView() {
  const { selectedModel, setSelectedModel } = useApp();
  const insets = useSafeAreaInsets();
  const topPad = Platform.OS === 'web' ? 67 : insets.top;

  const handleSelect = (model: AIModel) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    setSelectedModel(model);
  };

  return (
    <ScrollView
      style={[styles.root, { paddingTop: topPad }]}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 80 }]}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.header}>
        <Text style={styles.title}>AI Model Market</Text>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{AI_MODELS.length} Model</Text>
        </View>
      </View>
      <Text style={styles.subtitle}>
        DuckDuckGo AI ile ücretsiz, gizlilik öncelikli modeller
      </Text>

      <View style={[styles.infoCard]}>
        <Feather name="shield" size={16} color="#22C55E" />
        <View style={styles.infoText}>
          <Text style={styles.infoTitle}>Tamamen Ücretsiz & Gizli</Text>
          <Text style={styles.infoDesc}>DuckDuckGo AI tüm modellere ücretsiz erişim sağlar. Sohbet geçmişi kaydedilmez.</Text>
        </View>
      </View>

      {AI_MODELS.map(model => (
        <ModelCard
          key={model.id}
          model={model}
          isSelected={model.id === selectedModel.id}
          onSelect={() => handleSelect(model)}
        />
      ))}
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
    gap: 16,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginTop: 4,
  },
  title: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  badge: {
    backgroundColor: '#0F2044',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#1E3A6E',
  },
  badgeText: {
    fontSize: 11,
    color: '#3B82F6',
    fontFamily: 'Inter_500Medium',
  },
  subtitle: {
    fontSize: 13,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    marginTop: -4,
  },
  infoCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
    backgroundColor: '#16A34A11',
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: '#16A34A33',
  },
  infoText: {
    flex: 1,
    gap: 2,
  },
  infoTitle: {
    fontSize: 13,
    fontWeight: '600' as const,
    color: '#22C55E',
    fontFamily: 'Inter_600SemiBold',
  },
  infoDesc: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    lineHeight: 16,
  },
  card: {
    backgroundColor: '#0B1120',
    borderRadius: 16,
    padding: 16,
    gap: 12,
    borderWidth: 1,
    borderColor: '#1E293B',
    position: 'relative',
  },
  activeBadge: {
    position: 'absolute',
    top: 12,
    right: 12,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  activeBadgeText: {
    fontSize: 10,
    fontWeight: '700' as const,
    color: '#FFFFFF',
    fontFamily: 'Inter_700Bold',
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  modelIcon: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardInfo: {
    flex: 1,
  },
  cardName: {
    fontSize: 15,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  cardProvider: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  freeTag: {
    backgroundColor: '#16A34A22',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#16A34A44',
    marginRight: 48,
  },
  freeTagText: {
    fontSize: 9,
    fontWeight: '700' as const,
    color: '#22C55E',
    fontFamily: 'Inter_700Bold',
    letterSpacing: 0.5,
  },
  cardDesc: {
    fontSize: 13,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    lineHeight: 18,
  },
  capsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  capChip: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 6,
  },
  capText: {
    fontSize: 11,
    fontWeight: '500' as const,
    fontFamily: 'Inter_500Medium',
  },
  selectBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingVertical: 10,
    borderRadius: 10,
    borderWidth: 1,
  },
  selectBtnText: {
    fontSize: 13,
    fontWeight: '600' as const,
    fontFamily: 'Inter_600SemiBold',
  },
});
