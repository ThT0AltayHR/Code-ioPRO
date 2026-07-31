package com.codeioPRO.app;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_CHAT    = "tab_chat";
    private static final String TAG_FILES   = "tab_files";
    private static final String TAG_SHELL   = "tab_shell";
    private static final String TAG_MARKET  = "tab_market";
    private static final String TAG_SECRETS = "tab_secrets";
    private static final String TAG_AGENTS  = "tab_agents";

    private ChatFragment       chatFragment;
    private FilesFragment      filesFragment;
    private ShellFragment      shellFragment;
    private AiMarketFragment   marketFragment;
    private SecretsFragment    secretsFragment;
    private SubAgentFragment   agentsFragment;

    // Custom bottom nav — LinearLayout (BottomNavigationView max 5 sınırını aşar)
    private LinearLayout customBottomNav;
    private android.widget.FrameLayout fragmentContainer;
    private String activeTag = TAG_CHAT;

    // Tab root views (6 adet LinearLayout)
    private LinearLayout tabChat, tabFiles, tabShell, tabMarket, tabSecrets, tabAgents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.nav_bar, getTheme()));
        setContentView(R.layout.activity_main);

        fragmentContainer  = findViewById(R.id.fragment_container);
        customBottomNav    = findViewById(R.id.bottom_navigation_custom);

        tabChat    = findViewById(R.id.nav_chat);
        tabFiles   = findViewById(R.id.nav_files);
        tabShell   = findViewById(R.id.nav_shell);
        tabMarket  = findViewById(R.id.nav_market);
        tabSecrets = findViewById(R.id.nav_secrets);
        tabAgents  = findViewById(R.id.nav_agents);

        setupFragments(savedInstanceState);
        setupCustomBottomNav();
        setupBackPress();

        fragmentContainer.setAlpha(0f);
        fragmentContainer.animate().alpha(1f).setDuration(400).start();

        // İlk seçili sekmeyi işaretle
        updateTabSelection(activeTag);

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

    private void setupFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            chatFragment    = (ChatFragment)     fm.findFragmentByTag(TAG_CHAT);
            filesFragment   = (FilesFragment)    fm.findFragmentByTag(TAG_FILES);
            shellFragment   = (ShellFragment)    fm.findFragmentByTag(TAG_SHELL);
            marketFragment  = (AiMarketFragment) fm.findFragmentByTag(TAG_MARKET);
            secretsFragment = (SecretsFragment)  fm.findFragmentByTag(TAG_SECRETS);
            agentsFragment  = (SubAgentFragment) fm.findFragmentByTag(TAG_AGENTS);
        }

        if (chatFragment    == null) chatFragment    = new ChatFragment();
        if (filesFragment   == null) filesFragment   = new FilesFragment();
        if (shellFragment   == null) shellFragment   = new ShellFragment();
        if (marketFragment  == null) marketFragment  = new AiMarketFragment();
        if (secretsFragment == null) secretsFragment = new SecretsFragment();
        if (agentsFragment  == null) agentsFragment  = new SubAgentFragment();

        FragmentTransaction ft = fm.beginTransaction();
        addIfNeeded(ft, fm, chatFragment,    TAG_CHAT);
        addIfNeeded(ft, fm, filesFragment,   TAG_FILES);
        addIfNeeded(ft, fm, shellFragment,   TAG_SHELL);
        addIfNeeded(ft, fm, marketFragment,  TAG_MARKET);
        addIfNeeded(ft, fm, secretsFragment, TAG_SECRETS);
        addIfNeeded(ft, fm, agentsFragment,  TAG_AGENTS);

        // Hepsini gizle, sadece aktif sekmeyi göster
        ft.hide(filesFragment).hide(shellFragment).hide(marketFragment)
          .hide(secretsFragment).hide(agentsFragment);
        if (savedInstanceState != null) {
            // Kayıtlı durumdaki aktif sekme
            Fragment active = fm.findFragmentByTag(activeTag);
            if (active != null) { ft.show(active); }
            else ft.show(chatFragment);
        } else {
            ft.show(chatFragment);
        }
        ft.commitAllowingStateLoss();
    }

    private void addIfNeeded(FragmentTransaction ft, FragmentManager fm,
                              Fragment fragment, String tag) {
        if (fm.findFragmentByTag(tag) == null) {
            ft.add(R.id.fragment_container, fragment, tag);
        }
    }

    private void setupCustomBottomNav() {
        tabChat.setOnClickListener(v    -> onTabSelected(TAG_CHAT));
        tabFiles.setOnClickListener(v   -> onTabSelected(TAG_FILES));
        tabShell.setOnClickListener(v   -> onTabSelected(TAG_SHELL));
        tabMarket.setOnClickListener(v  -> onTabSelected(TAG_MARKET));
        tabSecrets.setOnClickListener(v -> onTabSelected(TAG_SECRETS));
        tabAgents.setOnClickListener(v  -> onTabSelected(TAG_AGENTS));
    }

    private void onTabSelected(String tag) {
        if (!switchToTab(tag)) return;
        // Haptic feedback
        View tab = getTabView(tag);
        if (tab != null) tab.performHapticFeedback(
            android.view.HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private boolean switchToTab(String tag) {
        if (tag.equals(activeTag)) return false;

        Fragment current = getSupportFragmentManager().findFragmentByTag(activeTag);
        Fragment next    = getSupportFragmentManager().findFragmentByTag(tag);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        if (current != null) ft.hide(current);
        if (next    != null) ft.show(next);
        ft.commitAllowingStateLoss();

        activeTag = tag;
        updateTabSelection(tag);
        animateTabIcon(tag);
        return true;
    }

    /**
     * Seçili sekmenin rengi accent (mavi), diğerleri gri.
     */
    private void updateTabSelection(String selected) {
        String[] allTags = {TAG_CHAT, TAG_FILES, TAG_SHELL, TAG_MARKET, TAG_SECRETS, TAG_AGENTS};
        for (String tag : allTags) {
            boolean isSelected = tag.equals(selected);
            setTabSelected(tag, isSelected);
        }
    }

    private void setTabSelected(String tag, boolean selected) {
        int iconViewId = getTabIconViewId(tag);
        int textViewId = getTabTextViewId(tag);

        View iconView = customBottomNav.findViewById(iconViewId);
        View textView = customBottomNav.findViewById(textViewId);

        int colorRes = selected ? R.color.bottom_nav_selected : R.color.bottom_nav_unselected;
        int color    = ContextCompat.getColor(this, colorRes);

        if (iconView instanceof ImageView) {
            ((ImageView) iconView).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        if (textView instanceof TextView) {
            ((TextView) textView).setTextColor(color);
        }
    }

    private void animateTabIcon(String tag) {
        View tab = getTabView(tag);
        if (tab != null) {
            tab.animate()
               .scaleX(1.2f).scaleY(1.2f).setDuration(80)
               .withEndAction(() ->
                   tab.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
               ).start();
        }
    }

    private View getTabView(String tag) {
        switch (tag) {
            case TAG_CHAT:    return tabChat;
            case TAG_FILES:   return tabFiles;
            case TAG_SHELL:   return tabShell;
            case TAG_MARKET:  return tabMarket;
            case TAG_SECRETS: return tabSecrets;
            case TAG_AGENTS:  return tabAgents;
            default:          return tabChat;
        }
    }

    private int getTabIconViewId(String tag) {
        switch (tag) {
            case TAG_CHAT:    return R.id.nav_chat_icon;
            case TAG_FILES:   return R.id.nav_files_icon;
            case TAG_SHELL:   return R.id.nav_shell_icon;
            case TAG_MARKET:  return R.id.nav_market_icon;
            case TAG_SECRETS: return R.id.nav_secrets_icon;
            case TAG_AGENTS:  return R.id.nav_agents_icon;
            default:          return R.id.nav_chat_icon;
        }
    }

    private int getTabTextViewId(String tag) {
        switch (tag) {
            case TAG_CHAT:    return R.id.nav_chat_text;
            case TAG_FILES:   return R.id.nav_files_text;
            case TAG_SHELL:   return R.id.nav_shell_text;
            case TAG_MARKET:  return R.id.nav_market_text;
            case TAG_SECRETS: return R.id.nav_secrets_text;
            case TAG_AGENTS:  return R.id.nav_agents_text;
            default:          return R.id.nav_chat_text;
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!activeTag.equals(TAG_CHAT)) {
                    switchToTab(TAG_CHAT);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ── Public Navigation API ────────────────────────────────────────────────
    public void navigateToFiles()           { switchToTab(TAG_FILES); }
    public void navigateToShell(String cmd) {
        switchToTab(TAG_SHELL);
        if (shellFragment != null && cmd != null) shellFragment.runCommand(cmd);
    }
    public void navigateToChat()    { switchToTab(TAG_CHAT); }
    public void navigateToMarket()  { switchToTab(TAG_MARKET); }
    public void navigateToSecrets() { switchToTab(TAG_SECRETS); }
    public void navigateToAgents()  { switchToTab(TAG_AGENTS); }

    // ── Fragment Getters ─────────────────────────────────────────────────────
    public ChatFragment     getChatFragment()    { return chatFragment; }
    public FilesFragment    getFilesFragment()   { return filesFragment; }
    public ShellFragment    getShellFragment()   { return shellFragment; }
    public AiMarketFragment getMarketFragment()  { return marketFragment; }
    public SecretsFragment  getSecretsFragment() { return secretsFragment; }
    public SubAgentFragment getAgentsFragment()  { return agentsFragment; }
}
