import React, { useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useApp } from '@/context/AppContext';

const LANGUAGES = ['JavaScript', 'TypeScript', 'Python', 'Rust', 'Go', 'Java', 'C++', 'Swift'];

const TOKEN_COLORS: Record<string, string> = {
  keyword: '#8B5CF6',
  string: '#22C55E',
  comment: '#5B6472',
  number: '#FBBF24',
  function: '#3B82F6',
  default: '#CBD5E1',
};

function SimpleCodeLine({ line, lineNumber }: { line: string; lineNumber: number }) {
  // Simple syntax color logic
  const trimmed = line.trim();
  let color = TOKEN_COLORS.default;
  if (trimmed.startsWith('//') || trimmed.startsWith('#')) color = TOKEN_COLORS.comment;
  else if (trimmed.startsWith('"') || trimmed.startsWith("'") || trimmed.startsWith('`')) color = TOKEN_COLORS.string;
  else if (/^(function|const|let|var|return|if|else|for|while|import|export|class|async|await|def|print|fn|pub|mod|use|struct|enum)/.test(trimmed)) {
    color = TOKEN_COLORS.keyword;
  } else if (/^\d/.test(trimmed)) color = TOKEN_COLORS.number;
  else if (/^\w+\s*\(/.test(trimmed)) color = TOKEN_COLORS.function;

  return (
    <View style={styles.codeLine}>
      <Text style={styles.lineNumber}>{lineNumber}</Text>
      <Text style={[styles.codeText, { color }]}>{line}</Text>
    </View>
  );
}

export default function EditorView() {
  const { editorCode, setEditorCode, editorLanguage, setEditorLanguage, terminalOutput, isRunning, runCode, sendMessage, setActiveTab } = useApp();
  const insets = useSafeAreaInsets();
  const [showLangPicker, setShowLangPicker] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const topPad = Platform.OS === 'web' ? 67 : insets.top;

  const handleAIHelp = async () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    const prompt = `Bu ${editorLanguage} kodunu incele ve iyileştirmeler öner:\n\n\`\`\`${editorLanguage.toLowerCase()}\n${editorCode}\n\`\`\``;
    await sendMessage(prompt);
    setActiveTab('chat');
  };

  const lines = editorCode.split('\n');

  return (
    <View style={[styles.root, { paddingTop: topPad }]}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <View style={styles.fileDot} />
          <Text style={styles.headerTitle}>main.{editorLanguage === 'JavaScript' ? 'js' : editorLanguage === 'TypeScript' ? 'ts' : editorLanguage === 'Python' ? 'py' : 'txt'}</Text>
        </View>
        <View style={styles.headerActions}>
          <TouchableOpacity
            style={styles.langBtn}
            onPress={() => setShowLangPicker(!showLangPicker)}
            activeOpacity={0.7}
          >
            <Feather name="code" size={12} color="#8B95A7" />
            <Text style={styles.langBtnText}>{editorLanguage}</Text>
            <Feather name="chevron-down" size={12} color="#8B95A7" />
          </TouchableOpacity>
          <TouchableOpacity style={styles.iconBtn} onPress={() => setIsEditMode(!isEditMode)} activeOpacity={0.7}>
            <Feather name={isEditMode ? 'eye' : 'edit-2'} size={15} color={isEditMode ? '#3B82F6' : '#8B95A7'} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.iconBtn} onPress={handleAIHelp} activeOpacity={0.7}>
            <Feather name="cpu" size={15} color="#8B5CF6" />
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.runBtn, isRunning && styles.runBtnActive]}
            onPress={() => { Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium); runCode(); }}
            activeOpacity={0.8}
            disabled={isRunning}
          >
            {isRunning ? (
              <ActivityIndicator size="small" color="#FFFFFF" />
            ) : (
              <Feather name="play" size={14} color="#FFFFFF" />
            )}
            <Text style={styles.runBtnText}>{isRunning ? 'Çalışıyor...' : 'Çalıştır'}</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Language picker dropdown */}
      {showLangPicker && (
        <ScrollView
          horizontal
          style={styles.langPicker}
          contentContainerStyle={styles.langPickerContent}
          showsHorizontalScrollIndicator={false}
        >
          {LANGUAGES.map(lang => (
            <TouchableOpacity
              key={lang}
              style={[styles.langChip, lang === editorLanguage && styles.langChipActive]}
              onPress={() => { setEditorLanguage(lang); setShowLangPicker(false); }}
              activeOpacity={0.7}
            >
              <Text style={[styles.langChipText, lang === editorLanguage && styles.langChipTextActive]}>
                {lang}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      )}

      {/* Code Editor */}
      <ScrollView style={styles.editorArea} horizontal showsHorizontalScrollIndicator={false}>
        <ScrollView showsVerticalScrollIndicator={false}>
          {isEditMode ? (
            <TextInput
              style={styles.editInput}
              value={editorCode}
              onChangeText={setEditorCode}
              multiline
              scrollEnabled={false}
              autoCapitalize="none"
              autoCorrect={false}
              spellCheck={false}
              selectionColor="#3B82F6"
            />
          ) : (
            <View style={styles.codeLines}>
              {lines.map((line, i) => (
                <SimpleCodeLine key={i} line={line} lineNumber={i + 1} />
              ))}
            </View>
          )}
        </ScrollView>
      </ScrollView>

      {/* Terminal Output */}
      <View style={[styles.terminal, { paddingBottom: insets.bottom + 70 }]}>
        <View style={styles.terminalHeader}>
          <View style={styles.terminalDots}>
            <View style={[styles.termDot, { backgroundColor: '#EF4444' }]} />
            <View style={[styles.termDot, { backgroundColor: '#FBBF24' }]} />
            <View style={[styles.termDot, { backgroundColor: '#22C55E' }]} />
          </View>
          <Text style={styles.terminalTitle}>Terminal</Text>
          {isRunning && <ActivityIndicator size="small" color="#22C55E" style={{ marginLeft: 8 }} />}
        </View>
        <ScrollView style={styles.terminalBody} showsVerticalScrollIndicator={false}>
          {terminalOutput ? (
            terminalOutput.split('\n').map((line, i) => (
              <Text key={i} style={[
                styles.terminalLine,
                line.startsWith('✓') ? styles.termSuccess :
                line.startsWith('>') ? styles.termCommand :
                line.startsWith('---') ? styles.termSeparator : null
              ]}>
                {line}
              </Text>
            ))
          ) : (
            <Text style={styles.terminalPlaceholder}>
              {isRunning ? 'Çalışıyor...' : '// Çalıştırmak için ▶ butonuna basın'}
            </Text>
          )}
        </ScrollView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#050810',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderColor: '#1E293B',
    backgroundColor: '#080C16',
    flexWrap: 'wrap',
    gap: 8,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  fileDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#FBBF24',
  },
  headerTitle: {
    fontSize: 13,
    color: '#F3F4F6',
    fontFamily: 'Inter_500Medium',
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  langBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#111A2C',
    paddingHorizontal: 8,
    paddingVertical: 5,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  langBtnText: {
    fontSize: 11,
    color: '#8B95A7',
    fontFamily: 'Inter_500Medium',
  },
  iconBtn: {
    width: 30,
    height: 30,
    borderRadius: 7,
    backgroundColor: '#111A2C',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  runBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: '#22C55E',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  runBtnActive: {
    backgroundColor: '#16A34A',
  },
  runBtnText: {
    fontSize: 11,
    fontWeight: '600' as const,
    color: '#FFFFFF',
    fontFamily: 'Inter_600SemiBold',
  },
  langPicker: {
    backgroundColor: '#0B1120',
    borderBottomWidth: 1,
    borderColor: '#1E293B',
    maxHeight: 46,
  },
  langPickerContent: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    gap: 6,
    alignItems: 'center',
  },
  langChip: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 6,
    backgroundColor: '#111A2C',
    borderWidth: 1,
    borderColor: '#1E293B',
    marginRight: 6,
  },
  langChipActive: {
    backgroundColor: '#0F2044',
    borderColor: '#3B82F6',
  },
  langChipText: {
    fontSize: 11,
    color: '#8B95A7',
    fontFamily: 'Inter_500Medium',
  },
  langChipTextActive: {
    color: '#3B82F6',
  },
  editorArea: {
    flex: 1,
    backgroundColor: '#080C16',
  },
  codeLines: {
    paddingVertical: 8,
    minWidth: '100%',
  },
  codeLine: {
    flexDirection: 'row',
    paddingVertical: 1,
    paddingHorizontal: 8,
    alignItems: 'flex-start',
  },
  lineNumber: {
    width: 32,
    fontSize: 12,
    color: '#5B6472',
    fontFamily: 'Inter_400Regular',
    textAlign: 'right',
    marginRight: 12,
    lineHeight: 18,
  },
  codeText: {
    fontSize: 12,
    lineHeight: 18,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  editInput: {
    fontSize: 12,
    color: '#CBD5E1',
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    lineHeight: 18,
    padding: 12,
    paddingLeft: 44,
    minHeight: 200,
    width: 400,
  },
  terminal: {
    maxHeight: 160,
    backgroundColor: '#030609',
    borderTopWidth: 1,
    borderColor: '#1E293B',
  },
  terminalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderColor: '#1E293B',
    gap: 8,
  },
  terminalDots: {
    flexDirection: 'row',
    gap: 5,
  },
  termDot: {
    width: 9,
    height: 9,
    borderRadius: 5,
  },
  terminalTitle: {
    fontSize: 11,
    color: '#5B6472',
    fontFamily: 'Inter_500Medium',
    flex: 1,
  },
  terminalBody: {
    padding: 10,
  },
  terminalLine: {
    fontSize: 12,
    color: '#8B95A7',
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    lineHeight: 18,
  },
  termSuccess: {
    color: '#22C55E',
  },
  termCommand: {
    color: '#3B82F6',
  },
  termSeparator: {
    color: '#1E293B',
  },
  terminalPlaceholder: {
    fontSize: 12,
    color: '#5B6472',
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    lineHeight: 18,
  },
});
