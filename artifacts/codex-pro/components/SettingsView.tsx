import React, { useState } from 'react';
import {
  Alert,
  Platform,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useApp } from '@/context/AppContext';

interface SettingRowProps {
  icon: string;
  iconColor?: string;
  title: string;
  subtitle?: string;
  right?: React.ReactNode;
  onPress?: () => void;
  danger?: boolean;
}

function SettingRow({ icon, iconColor = '#8B95A7', title, subtitle, right, onPress, danger }: SettingRowProps) {
  return (
    <TouchableOpacity
      style={styles.row}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      disabled={!onPress}
    >
      <View style={[styles.rowIcon, { backgroundColor: (iconColor + '22') }]}>
        <Feather name={icon as any} size={15} color={danger ? '#EF4444' : iconColor} />
      </View>
      <View style={styles.rowContent}>
        <Text style={[styles.rowTitle, danger && styles.rowTitleDanger]}>{title}</Text>
        {subtitle ? <Text style={styles.rowSubtitle}>{subtitle}</Text> : null}
      </View>
      {right ?? (onPress ? <Feather name="chevron-right" size={16} color="#5B6472" /> : null)}
    </TouchableOpacity>
  );
}

export default function SettingsView() {
  const { userName, setUserName, clearMessages, selectedModel, setShowModelSelector } = useApp();
  const insets = useSafeAreaInsets();
  const [editingName, setEditingName] = useState(false);
  const [tempName, setTempName] = useState(userName);
  const [darkMode] = useState(true);
  const [soundEnabled, setSoundEnabled] = useState(false);
  const topPad = Platform.OS === 'web' ? 67 : insets.top;

  const saveName = () => {
    if (tempName.trim().length >= 2) {
      setUserName(tempName.trim());
      setEditingName(false);
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    }
  };

  const handleClear = () => {
    if (Platform.OS === 'web') {
      clearMessages();
      return;
    }
    Alert.alert('Sohbet Geçmişini Sil', 'Tüm mesajlar silinecek. Emin misiniz?', [
      { text: 'İptal', style: 'cancel' },
      {
        text: 'Sil', style: 'destructive',
        onPress: () => { clearMessages(); Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success); }
      },
    ]);
  };

  return (
    <ScrollView
      style={[styles.root, { paddingTop: topPad }]}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 80 }]}
      showsVerticalScrollIndicator={false}
    >
      <Text style={styles.pageTitle}>Ayarlar</Text>

      {/* Profile */}
      <View style={styles.section}>
        <Text style={styles.sectionLabel}>KİŞİSEL</Text>
        <View style={styles.profileCard}>
          <View style={styles.profileAvatar}>
            <Text style={styles.profileAvatarText}>{userName.charAt(0).toUpperCase()}</Text>
          </View>
          <View style={styles.profileInfo}>
            {editingName ? (
              <View style={styles.nameEdit}>
                <TextInput
                  style={styles.nameInput}
                  value={tempName}
                  onChangeText={setTempName}
                  autoFocus
                  selectTextOnFocus
                  selectionColor="#3B82F6"
                />
                <TouchableOpacity onPress={saveName} style={styles.saveName} activeOpacity={0.7}>
                  <Feather name="check" size={16} color="#22C55E" />
                </TouchableOpacity>
              </View>
            ) : (
              <Text style={styles.profileName}>{userName}</Text>
            )}
            <Text style={styles.profileSub}>CODEX PRO Kullanıcısı</Text>
          </View>
          <TouchableOpacity onPress={() => setEditingName(!editingName)} style={styles.editBtn} activeOpacity={0.7}>
            <Feather name="edit-2" size={14} color="#8B95A7" />
          </TouchableOpacity>
        </View>
      </View>

      {/* AI Settings */}
      <View style={styles.section}>
        <Text style={styles.sectionLabel}>YAPAY ZEKA</Text>
        <View style={styles.sectionCard}>
          <SettingRow
            icon="cpu"
            iconColor="#3B82F6"
            title="Aktif Model"
            subtitle={`${selectedModel.name} · ${selectedModel.provider}`}
            onPress={() => setShowModelSelector(true)}
          />
          <View style={styles.divider} />
          <SettingRow
            icon="shield"
            iconColor="#22C55E"
            title="Gizlilik Modu"
            subtitle="Sohbet geçmişi sunucularda saklanmaz"
            right={<Switch value={true} disabled thumbColor="#FFFFFF" trackColor={{ true: '#22C55E', false: '#1E293B' }} />}
          />
          <View style={styles.divider} />
          <SettingRow
            icon="globe"
            iconColor="#3B82F6"
            title="API Sağlayıcı"
            subtitle="DuckDuckGo AI (Ücretsiz)"
          />
        </View>
      </View>

      {/* App Settings */}
      <View style={styles.section}>
        <Text style={styles.sectionLabel}>UYGULAMA</Text>
        <View style={styles.sectionCard}>
          <SettingRow
            icon="moon"
            iconColor="#8B5CF6"
            title="Karanlık Tema"
            subtitle="CODEX PRO dark theme"
            right={<Switch value={darkMode} disabled thumbColor="#FFFFFF" trackColor={{ true: '#8B5CF6', false: '#1E293B' }} />}
          />
          <View style={styles.divider} />
          <SettingRow
            icon="volume-2"
            iconColor="#FBBF24"
            title="Ses Efektleri"
            right={
              <Switch
                value={soundEnabled}
                onValueChange={v => { setSoundEnabled(v); Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light); }}
                thumbColor="#FFFFFF"
                trackColor={{ true: '#FBBF24', false: '#1E293B' }}
              />
            }
          />
        </View>
      </View>

      {/* Data */}
      <View style={styles.section}>
        <Text style={styles.sectionLabel}>VERİ</Text>
        <View style={styles.sectionCard}>
          <SettingRow
            icon="trash-2"
            iconColor="#EF4444"
            title="Sohbet Geçmişini Temizle"
            subtitle="Tüm AI sohbet mesajları silinir"
            onPress={handleClear}
            danger
          />
        </View>
      </View>

      {/* About */}
      <View style={styles.section}>
        <Text style={styles.sectionLabel}>HAKKINDA</Text>
        <View style={styles.sectionCard}>
          <SettingRow icon="info" iconColor="#3B82F6" title="CODEX PRO" subtitle="v2.0.0 · AI Destekli Kodlama Asistanı" />
          <View style={styles.divider} />
          <SettingRow icon="lock" iconColor="#22C55E" title="Gizlilik Politikası" subtitle="DuckDuckGo AI gizlilik standartları" />
        </View>
      </View>

      {/* Built with */}
      <Text style={styles.builtWith}>
        Powered by DuckDuckGo AI · GPT-4o · Claude · Gemini · Mistral
      </Text>
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
    gap: 24,
  },
  pageTitle: {
    fontSize: 22,
    fontWeight: '700' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_700Bold',
    marginTop: 4,
  },
  section: {
    gap: 8,
  },
  sectionLabel: {
    fontSize: 11,
    fontWeight: '600' as const,
    color: '#5B6472',
    fontFamily: 'Inter_600SemiBold',
    letterSpacing: 1,
    marginLeft: 4,
  },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#0B1120',
    borderRadius: 14,
    padding: 16,
    gap: 14,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  profileAvatar: {
    width: 50,
    height: 50,
    borderRadius: 14,
    backgroundColor: '#0F2044',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#3B82F6',
  },
  profileAvatarText: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: '#3B82F6',
    fontFamily: 'Inter_700Bold',
  },
  profileInfo: {
    flex: 1,
    gap: 2,
  },
  profileName: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
  },
  profileSub: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  nameEdit: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  nameInput: {
    flex: 1,
    fontSize: 15,
    color: '#F3F4F6',
    fontFamily: 'Inter_600SemiBold',
    backgroundColor: '#111A2C',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: '#3B82F6',
  },
  saveName: {
    width: 30,
    height: 30,
    borderRadius: 8,
    backgroundColor: '#16A34A22',
    alignItems: 'center',
    justifyContent: 'center',
  },
  editBtn: {
    width: 32,
    height: 32,
    borderRadius: 8,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  sectionCard: {
    backgroundColor: '#0B1120',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#1E293B',
    overflow: 'hidden',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 13,
    gap: 12,
  },
  rowIcon: {
    width: 32,
    height: 32,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowContent: {
    flex: 1,
    gap: 2,
  },
  rowTitle: {
    fontSize: 14,
    fontWeight: '500' as const,
    color: '#F3F4F6',
    fontFamily: 'Inter_500Medium',
  },
  rowTitleDanger: {
    color: '#EF4444',
  },
  rowSubtitle: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: 'Inter_400Regular',
  },
  divider: {
    height: 1,
    backgroundColor: '#1E293B',
    marginLeft: 60,
  },
  builtWith: {
    fontSize: 11,
    color: '#5B6472',
    fontFamily: 'Inter_400Regular',
    textAlign: 'center',
    paddingVertical: 8,
  },
});
