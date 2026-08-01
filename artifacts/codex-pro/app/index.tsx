import React from 'react';
import { Platform, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useApp } from '@/context/AppContext';
import Onboarding from '@/components/Onboarding';
import DashboardView from '@/components/DashboardView';
import ChatView from '@/components/ChatView';
import EditorView from '@/components/EditorView';
import MarketView from '@/components/MarketView';
import SettingsView from '@/components/SettingsView';
import ModelSelectorModal from '@/components/ModelSelectorModal';
import VoiceChatModal from '@/components/VoiceChatModal';

type TabDef = {
  key: 'dashboard' | 'chat' | 'editor' | 'market' | 'settings';
  icon: string;
  label: string;
  activeColor: string;
};

const TABS: TabDef[] = [
  { key: 'dashboard', icon: 'home', label: 'Ana', activeColor: '#3B82F6' },
  { key: 'chat', icon: 'message-circle', label: 'Sohbet', activeColor: '#8B5CF6' },
  { key: 'editor', icon: 'code', label: 'Editör', activeColor: '#22C55E' },
  { key: 'market', icon: 'cpu', label: 'Market', activeColor: '#FBBF24' },
  { key: 'settings', icon: 'settings', label: 'Ayarlar', activeColor: '#8B95A7' },
];

function BottomTabBar() {
  const { activeTab, setActiveTab, messages, isLoading } = useApp();
  const insets = useSafeAreaInsets();
  const bottomPad = Platform.OS === 'web' ? 34 : insets.bottom;

  return (
    <View style={[styles.tabBar, { paddingBottom: bottomPad }]}>
      {TABS.map(tab => {
        const isActive = activeTab === tab.key;
        const hasBadge = tab.key === 'chat' && messages.length > 0;
        return (
          <TouchableOpacity
            key={tab.key}
            style={styles.tabItem}
            onPress={() => {
              Haptics.selectionAsync();
              setActiveTab(tab.key);
            }}
            activeOpacity={0.7}
          >
            <View style={[styles.tabIconWrap, isActive && { backgroundColor: tab.activeColor + '22' }]}>
              <Feather
                name={tab.icon as any}
                size={20}
                color={isActive ? tab.activeColor : '#5B6472'}
              />
              {hasBadge && (
                <View style={[styles.badge, { backgroundColor: isLoading ? '#FBBF24' : tab.activeColor }]} />
              )}
            </View>
            <Text style={[styles.tabLabel, { color: isActive ? tab.activeColor : '#5B6472' }]}>
              {tab.label}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

export default function App() {
  const { isOnboarding, activeTab } = useApp();

  if (isOnboarding) {
    return <Onboarding />;
  }

  return (
    <View style={styles.root}>
      <View style={styles.content}>
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'chat' && <ChatView />}
        {activeTab === 'editor' && <EditorView />}
        {activeTab === 'market' && <MarketView />}
        {activeTab === 'settings' && <SettingsView />}
      </View>
      <BottomTabBar />
      <ModelSelectorModal />
      <VoiceChatModal />
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#050810',
  },
  content: {
    flex: 1,
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#080C16',
    borderTopWidth: 1,
    borderTopColor: '#1E293B',
    paddingTop: 8,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    gap: 3,
  },
  tabIconWrap: {
    width: 40,
    height: 34,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  badge: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 8,
    height: 8,
    borderRadius: 4,
    borderWidth: 1.5,
    borderColor: '#080C16',
  },
  tabLabel: {
    fontSize: 10,
    fontFamily: 'Inter_500Medium',
  },
});
