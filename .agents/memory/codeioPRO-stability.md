---
name: Code-ioPRO stability fixes
description: Comprehensive stability pass — 8 bug categories fixed in v1.1.0
---

## Critical Bugs Fixed (v1.1.0, commit 059b839)

### EditorActivity.runFile() — Run button never worked
`getApplicationContext() instanceof MainActivity` is ALWAYS false — Application context is never an Activity instance.
**Fix:** Start MainActivity with `run_command` Intent extra + `FLAG_ACTIVITY_REORDER_TO_FRONT`. MainActivity handles in `handleIntent()`.
**Why:** Common Android mistake — Application context ≠ Activity.

### EditorActivity.saveContent() — FilesFragment never refreshed after save
`getParent()` returns null for normally launched Activities (only works with deprecated ActivityGroup).
**Fix:** Removed broken refresh call. FilesFragment refreshes in `onResume()` automatically.

### EditorActivity — OOM on large files
`byte[] bytes = new byte[(int) f.length()]` — no size guard.
**Fix:** Added 2 MB check, `finish()` if exceeded.

### SubAgentFragment — Unbounded thread leak
`Executors.newCachedThreadPool()` with no `shutdown()` in `onDestroy()`.
**Fix:** `newFixedThreadPool(4)` + `shutdownNow()` + `awaitTermination(500ms)` in `onDestroy()`.

### SubAgentFragment — FAKE FEATURE (not a crash, but misleading)
`runAgentSimulation()` / `dispatchProject()` use only `Thread.sleep()` delays — zero real AI calls.
**Fix:** Renamed to `dispatchAgents()`, added clear `[Demo]` labels in all log messages, JavaDoc warning.
**Why:** Real AI dispatch requires API keys + HTTP client integration; that's a future feature.

### MainActivity — deprecated onBackPressed()
Overriding `onBackPressed()` is deprecated on API 33+ and breaks Android 13 predictive back gesture.
**Fix:** `getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {...})`.

### ShellFragment — hardcoded PATH
`/data/data/com.codeioPRO.app/files/usr/bin` — breaks on any device where package name differs or relocates.
**Fix:** `requireContext().getFilesDir().getAbsolutePath() + "/usr/bin"`.

### FilesFragment — external storage crash on Android 13+
`ROOT_DIR` was `static final` pointing to `Environment.getExternalStoragePublicDirectory(DOWNLOADS)`.
On Android 13+ with revoked `READ_EXTERNAL_STORAGE`, `mkdirs()` fails silently → NPE.
**Fix:** `getRootDir()` with three-tier fallback: (1) Downloads/Code-ioPRO, (2) `getExternalFilesDir()`, (3) `getFilesDir()`.

### WebView memory leaks — all fragments
None of the WebView fragments called `webView.destroy()` in `onDestroyView()`.
**Fix:** Added `stopLoading() + clearHistory() + clearCache(true) + destroy()` in `onDestroyView()` for:
AiMarketFragment, SecretsFragment, EditorActivity, ShellFragment, SubAgentFragment.

## Stub services (intentional, no fix needed)
- `AssistantRecognitionService` — empty stub satisfying manifest entry; delegates to system STT.
- `AssistantService` (VoiceInteractionService) — body-less, correct.
- `AssistantSession.onShow()` — opens ChatActivity and hides, correct.

## Build status
- v1.1.0-stable tag @ 059b839 — Build #13 ✅ SUCCESS (2m1s)
