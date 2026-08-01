import React from 'react';
import {
  Modal,
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

export default function ModelSelectorModal() {
  const { showModelSelector, setShowModelSelector, selectedModel, setSelectedModel } = useApp();
  const insets = useSafeAreaInsets();

  const select = (model: AIModel) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    setSelectedModel(model);
    setShowModelSelector(false);
  };

  return (
    <Modal
      visible={showModelSelector}
      animationType="slide"
      transparent
      onRequestClose={() => setShowModelSelector(false)}
    >
      <TouchableOpacity
        style={styles.backdrop}
        activeOpacity={1}
        onPress={() => setShowModelSelector(false)}
      />
      <View style={[styles.sheet, { paddingBottom: insets.bottom + 16 }]}>
        <View style={styles.handle} />
        <View style={styles.header}>
          <Text style={styles.title}>AI Model Seç</Text>
          <TouchableOpacity onPress={() => setShowModelSelector(false)} style={styles.closeBtn}>
            <Feather name="x" size={20} color="#8B95A7" />
          </TouchableOpacity>
        </View>
        <Text style={styles.subtitle}>DuckDuckGo AI · Ücretsiz · Gizlilik Öncelikli</Text>
        <ScrollView showsVerticalScrollIndicator={false} style={styles.list}>
          {AI_MODELS.map(model => {
            const isSelected = model.id === selectedModel.id;
            return (
              <TouchableOpacity
                key={model.id}
                style={[styles.modelCard, isSelected && styles.modelCardActive, { borderColor: isSelected ? model.accentColor : '#1E293B' }]}
                onPress={() => select(model)}
                activeOpacity={0.8}
              >
                <View style={[styles.modelIcon, { backgroundColor: model.bgColor }]}>
                  <Feather name={model.icon as any} size={18} color={model.accentColor} />
                </View>
                <View style={styles.modelInfo}>
                  <View style={styles.modelNameRow}>
                    <Text style={styles.modelName}>{model.name}</Text>
                    {isSelected && (
                      <View style={[styles.activeBadge, { backgroundColor: model.accentColor + '33' }]}>
                        <Text style={[styles.activeBadgeText, { color: model.accentColor }]}>Aktif</Text>
                      </View>
                    )}
                  </View>
                  <Text style={styles.modelProvider}>{model.provider}</Text>
                  <Text style={styles.modelDesc}>{model.description}</Text>
                </View>
                {isSelected && <Feather name="check-circle" size={18} color={model.accentColor} />}
              </TouchableOpacity>
            );
          })}
        </ScrollView>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: '#00000080',
  },
  sheet: {
    backgroundColor: '#0B1120',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    maxHeight: '80%',
    paddingTop: 8,
    borderTopWidth: 1,
    borderColor: '#1E293B',
  },
  handle: {
    width: 40,
    height: 4,
    backgroundColor: '#1E293B',
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    marginBottom: 4,
  },
  title: {
    fontSize: 18,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
  },
  closeBtn: {
    padding: 4,
  },
  subtitle: {
    fontSize: 12,
    color: '#5B6472',
    paddingHorizontal: 20,
    marginBottom: 16,
    fontFamily: 'Inter_400Regular',
  },
  list: {
    paddingHorizontal: 16,
  },
  modelCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#111A2C',
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#1E293B',
    gap: 12,
  },
  modelCardActive: {
    backgroundColor: '#0F1D35',
  },
  modelIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modelInfo: {
    flex: 1,
    gap: 2,
  },
  modelNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  modelName: {
    fontSize: 14,
    fontWeight: '600' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
  },
  activeBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 6,
  },
  activeBadgeText: {
    fontSize: 10,
    fontWeight: '600' as const,
    fontFamily: 'Inter_600SemiBold',
  },
  modelProvider: {
    fontSize: 11,
    color: '#5B6472',
    fontFamily: 'Inter_400Regular',
  },
  modelDesc: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
    marginTop: 2,
  },
});
