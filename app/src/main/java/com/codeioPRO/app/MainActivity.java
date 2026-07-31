package com.codeioPRO.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    // Fragment tags
    private static final String TAG_DASHBOARD = "tab_dashboard";
    private static final String TAG_CHAT      = "tab_chat";
    private static final String TAG_FILES     = "tab_files";
    private static final String TAG_SHELL     = "tab_shell";
    private static final String TAG_MARKET    = "tab_market";
    private static final String TAG_SECRETS   = "tab_secrets";
    private static final String TAG_AGENTS    = "tab_agents";

    // Fragment instances
    private DashboardFragment  dashboardFragment;
    private ChatFragment       chatFragment;
    private FilesFragment      filesFragment;
    private ShellFragment      shellFragment;
    private AiMarketFragment   marketFragment;
    private SecretsFragment    secretsFragment;
    private SubAgentFragment   agentsFragment;

    private DrawerLayout drawerLayout;
    private String activeTag = TAG_DASHBOARD;

    // Header tabs
    private TextView htabChat, htabEditor, htabMarket, htabSettings;

    // Sidebar items
    private LinearLayout sidebarDashboard, sidebarChat, sidebarEditor, sidebarShell;
    private LinearLayout sidebarFiles, sidebarMarket, sidebarAgents, sidebarApi, sidebarSettings;
    private LinearLayout sidebarDb, sidebarTasks;
    private LinearLayout shortcutNewProject, shortcutOpenFile, shortcutZip, shortcutTerminal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.nav_bar, getTheme()));
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        // Header tabs
        htabChat     = findViewById(R.id.htab_chat);
        htabEditor   = findViewById(R.id.htab_editor);
        htabMarket   = findViewById(R.id.htab_market);
        htabSettings = findViewById(R.id.htab_settings);

        // Sidebar items
        sidebarDashboard  = findViewById(R.id.sidebar_dashboard);
        sidebarChat       = findViewById(R.id.sidebar_chat);
        sidebarEditor     = findViewById(R.id.sidebar_editor);
        sidebarShell      = findViewById(R.id.sidebar_shell);
        sidebarFiles      = findViewById(R.id.sidebar_files);
        sidebarMarket     = findViewById(R.id.sidebar_market);
        sidebarAgents     = findViewById(R.id.sidebar_agents);
        sidebarApi        = findViewById(R.id.sidebar_api);
        sidebarSettings   = findViewById(R.id.sidebar_settings);
        sidebarDb         = findViewById(R.id.sidebar_db);
        sidebarTasks      = findViewById(R.id.sidebar_tasks);

        shortcutNewProject = findViewById(R.id.shortcut_new_project);
        shortcutOpenFile   = findViewById(R.id.shortcut_open_file);
        shortcutZip        = findViewById(R.id.shortcut_zip);
        shortcutTerminal   = findViewById(R.id.shortcut_terminal);

        setupFragments(savedInstanceState);
        setupHeaderListeners();
        setupSidebarListeners();
        setupBackPress();

        // Fade in
        View container = findViewById(R.id.fragment_container);
        container.setAlpha(0f);
        container.animate().alpha(1f).setDuration(350).start();

        updateHeaderTabs(activeTag);
        updateSidebarSelection(activeTag);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String runCmd = intent.getStringExtra("run_command");
        if (runCmd != null) {
            intent.removeExtra("run_command");
            navigateToShell(runCmd);
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            switchToTab(TAG_CHAT);
            if (chatFragment != null) chatFragment.handleExternalIntent(intent);
        }
    }

    // ── Fragments ────────────────────────────────────────────────────────────

    private void setupFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            dashboardFragment = (DashboardFragment)  fm.findFragmentByTag(TAG_DASHBOARD);
            chatFragment      = (ChatFragment)        fm.findFragmentByTag(TAG_CHAT);
            filesFragment     = (FilesFragment)       fm.findFragmentByTag(TAG_FILES);
            shellFragment     = (ShellFragment)       fm.findFragmentByTag(TAG_SHELL);
            marketFragment    = (AiMarketFragment)    fm.findFragmentByTag(TAG_MARKET);
            secretsFragment   = (SecretsFragment)     fm.findFragmentByTag(TAG_SECRETS);
            agentsFragment    = (SubAgentFragment)    fm.findFragmentByTag(TAG_AGENTS);
        }

        if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
        if (chatFragment      == null) chatFragment      = new ChatFragment();
        if (filesFragment     == null) filesFragment     = new FilesFragment();
        if (shellFragment     == null) shellFragment     = new ShellFragment();
        if (marketFragment    == null) marketFragment    = new AiMarketFragment();
        if (secretsFragment   == null) secretsFragment   = new SecretsFragment();
        if (agentsFragment    == null) agentsFragment    = new SubAgentFragment();

        FragmentTransaction ft = fm.beginTransaction();
        addIfNeeded(ft, fm, dashboardFragment, TAG_DASHBOARD);
        addIfNeeded(ft, fm, chatFragment,      TAG_CHAT);
        addIfNeeded(ft, fm, filesFragment,     TAG_FILES);
        addIfNeeded(ft, fm, shellFragment,     TAG_SHELL);
        addIfNeeded(ft, fm, marketFragment,    TAG_MARKET);
        addIfNeeded(ft, fm, secretsFragment,   TAG_SECRETS);
        addIfNeeded(ft, fm, agentsFragment,    TAG_AGENTS);

        ft.hide(chatFragment).hide(filesFragment).hide(shellFragment)
          .hide(marketFragment).hide(secretsFragment).hide(agentsFragment);

        if (savedInstanceState != null) {
            Fragment active = fm.findFragmentByTag(activeTag);
            if (active != null) ft.show(active);
            else ft.show(dashboardFragment);
        } else {
            ft.show(dashboardFragment);
        }
        ft.commitAllowingStateLoss();
    }

    private void addIfNeeded(FragmentTransaction ft, FragmentManager fm, Fragment f, String tag) {
        if (fm.findFragmentByTag(tag) == null) ft.add(R.id.fragment_container, f, tag);
    }

    // ── Header tab listeners ─────────────────────────────────────────────────

    private void setupHeaderListeners() {
        // Hamburger
        View btnMenu = findViewById(R.id.btn_menu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Close drawer button inside sidebar
        View btnClose = findViewById(R.id.btn_close_drawer);
        if (btnClose != null) btnClose.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Header tabs
        if (htabChat     != null) htabChat.setOnClickListener(v     -> onHeaderTabSelected(TAG_CHAT));
        if (htabEditor   != null) htabEditor.setOnClickListener(v   -> onHeaderTabSelected(TAG_FILES));
        if (htabMarket   != null) htabMarket.setOnClickListener(v   -> onHeaderTabSelected(TAG_MARKET));
        if (htabSettings != null) htabSettings.setOnClickListener(v -> onHeaderTabSelected(TAG_SECRETS));
    }

    private void onHeaderTabSelected(String tag) {
        switchToTab(tag);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    // ── Sidebar listeners ────────────────────────────────────────────────────

    private void setupSidebarListeners() {
        if (sidebarDashboard != null)
            sidebarDashboard.setOnClickListener(v -> closeSidebarAndSwitch(TAG_DASHBOARD));
        if (sidebarChat != null)
            sidebarChat.setOnClickListener(v -> closeSidebarAndSwitch(TAG_CHAT));
        if (sidebarEditor != null)
            sidebarEditor.setOnClickListener(v -> closeSidebarAndSwitch(TAG_FILES));
        if (sidebarShell != null)
            sidebarShell.setOnClickListener(v -> closeSidebarAndSwitch(TAG_SHELL));
        if (sidebarFiles != null)
            sidebarFiles.setOnClickListener(v -> closeSidebarAndSwitch(TAG_FILES));
        if (sidebarMarket != null)
            sidebarMarket.setOnClickListener(v -> closeSidebarAndSwitch(TAG_MARKET));
        if (sidebarAgents != null)
            sidebarAgents.setOnClickListener(v -> closeSidebarAndSwitch(TAG_AGENTS));
        if (sidebarApi != null)
            sidebarApi.setOnClickListener(v -> closeSidebarAndSwitch(TAG_SECRETS));
        if (sidebarSettings != null)
            sidebarSettings.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                // Open settings (uses existing secrets/settings mechanism)
                if (chatFragment != null) {
                    switchToTab(TAG_CHAT);
                    chatFragment.showSettingsDialog();
                }
            });
        if (sidebarDb != null)
            sidebarDb.setOnClickListener(v -> closeSidebarAndSwitch(TAG_FILES));
        if (sidebarTasks != null)
            sidebarTasks.setOnClickListener(v -> closeSidebarAndSwitch(TAG_AGENTS));

        // Shortcuts
        if (shortcutNewProject != null)
            shortcutNewProject.setOnClickListener(v -> closeSidebarAndSwitch(TAG_FILES));
        if (shortcutOpenFile != null)
            shortcutOpenFile.setOnClickListener(v -> closeSidebarAndSwitch(TAG_FILES));
        if (shortcutZip != null)
            shortcutZip.setOnClickListener(v -> closeSidebarAndSwitch(TAG_SHELL));
        if (shortcutTerminal != null)
            shortcutTerminal.setOnClickListener(v -> closeSidebarAndSwitch(TAG_SHELL));
    }

    private void closeSidebarAndSwitch(String tag) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switchToTab(tag);
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private boolean switchToTab(String tag) {
        if (tag.equals(activeTag)) {
            return false;
        }
        Fragment current = getSupportFragmentManager().findFragmentByTag(activeTag);
        Fragment next    = getSupportFragmentManager().findFragmentByTag(tag);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        if (current != null) ft.hide(current);
        if (next    != null) ft.show(next);
        ft.commitAllowingStateLoss();

        activeTag = tag;
        updateHeaderTabs(tag);
        updateSidebarSelection(tag);
        return true;
    }

    private void updateHeaderTabs(String selected) {
        int activeColor   = getResources().getColor(R.color.text_primary, getTheme());
        int inactiveColor = getResources().getColor(R.color.text_secondary, getTheme());

        // Map fragment tags to header tabs
        if (htabChat     != null) htabChat.setTextColor(selected.equals(TAG_CHAT)     ? activeColor : inactiveColor);
        if (htabEditor   != null) htabEditor.setTextColor(selected.equals(TAG_FILES)  ? activeColor : inactiveColor);
        if (htabMarket   != null) htabMarket.setTextColor(selected.equals(TAG_MARKET) ? activeColor : inactiveColor);
        if (htabSettings != null) htabSettings.setTextColor(selected.equals(TAG_SECRETS) ? activeColor : inactiveColor);
    }

    private void updateSidebarSelection(String selected) {
        // Reset all sidebar items to default appearance
        resetSidebarItem(sidebarDashboard);
        resetSidebarItem(sidebarChat);
        resetSidebarItem(sidebarEditor);
        resetSidebarItem(sidebarShell);
        resetSidebarItem(sidebarFiles);
        resetSidebarItem(sidebarMarket);
        resetSidebarItem(sidebarAgents);
        resetSidebarItem(sidebarApi);
        resetSidebarItem(sidebarSettings);

        // Highlight the selected one
        LinearLayout toHighlight = null;
        switch (selected) {
            case TAG_DASHBOARD: toHighlight = sidebarDashboard; break;
            case TAG_CHAT:      toHighlight = sidebarChat;      break;
            case TAG_FILES:     toHighlight = sidebarFiles;     break;
            case TAG_SHELL:     toHighlight = sidebarShell;     break;
            case TAG_MARKET:    toHighlight = sidebarMarket;    break;
            case TAG_SECRETS:   toHighlight = sidebarApi;       break;
            case TAG_AGENTS:    toHighlight = sidebarAgents;    break;
        }
        if (toHighlight != null) {
            toHighlight.setBackgroundColor(getResources().getColor(R.color.sidebar_active_bg, getTheme()));
        }
    }

    private void resetSidebarItem(LinearLayout item) {
        if (item != null) {
            item.setBackgroundColor(0x00000000); // transparent
        }
    }

    // ── Back press ───────────────────────────────────────────────────────────

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (!activeTag.equals(TAG_DASHBOARD)) {
                    switchToTab(TAG_DASHBOARD);
                } else if (chatFragment != null && chatFragment.canGoBack()) {
                    chatFragment.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ── Public Navigation API ─────────────────────────────────────────────────

    public void navigateToFiles()           { switchToTab(TAG_FILES); }
    public void navigateToShell(String cmd) {
        switchToTab(TAG_SHELL);
        if (shellFragment != null && cmd != null) shellFragment.runCommand(cmd);
    }
    public void navigateToChat()    { switchToTab(TAG_CHAT); }
    public void navigateToMarket()  { switchToTab(TAG_MARKET); }
    public void navigateToSecrets() { switchToTab(TAG_SECRETS); }
    public void navigateToAgents()  { switchToTab(TAG_AGENTS); }

    // ── Fragment Getters ──────────────────────────────────────────────────────

    public ChatFragment     getChatFragment()    { return chatFragment; }
    public FilesFragment    getFilesFragment()   { return filesFragment; }
    public ShellFragment    getShellFragment()   { return shellFragment; }
    public AiMarketFragment getMarketFragment()  { return marketFragment; }
    public SecretsFragment  getSecretsFragment() { return secretsFragment; }
    public SubAgentFragment getAgentsFragment()  { return agentsFragment; }
}
