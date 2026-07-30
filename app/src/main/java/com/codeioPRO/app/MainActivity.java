package com.codeioPRO.app;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    private BottomNavigationView bottomNav;
    private android.widget.FrameLayout fragmentContainer;
    private String activeTag = TAG_CHAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.nav_bar, getTheme()));
        setContentView(R.layout.activity_main);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav         = findViewById(R.id.bottom_navigation);

        setupFragments(savedInstanceState);
        setupBottomNav();
        setupBackPress();

        fragmentContainer.setAlpha(0f);
        fragmentContainer.animate().alpha(1f).setDuration(400).start();

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

        // EditorActivity.runFile() buraya yönlendirir
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
        if (!chatFragment.isAdded())    ft.add(R.id.fragment_container, chatFragment,    TAG_CHAT);
        if (!filesFragment.isAdded())   ft.add(R.id.fragment_container, filesFragment,   TAG_FILES);
        if (!shellFragment.isAdded())   ft.add(R.id.fragment_container, shellFragment,   TAG_SHELL);
        if (!marketFragment.isAdded())  ft.add(R.id.fragment_container, marketFragment,  TAG_MARKET);
        if (!secretsFragment.isAdded()) ft.add(R.id.fragment_container, secretsFragment, TAG_SECRETS);
        if (!agentsFragment.isAdded())  ft.add(R.id.fragment_container, agentsFragment,  TAG_AGENTS);

        ft.hide(filesFragment).hide(shellFragment).hide(marketFragment)
          .hide(secretsFragment).hide(agentsFragment);
        ft.show(chatFragment);
        ft.commit();

        activeTag = TAG_CHAT;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chat)    return switchToTab(TAG_CHAT);
            if (id == R.id.nav_files)   return switchToTab(TAG_FILES);
            if (id == R.id.nav_shell)   return switchToTab(TAG_SHELL);
            if (id == R.id.nav_market)  return switchToTab(TAG_MARKET);
            if (id == R.id.nav_secrets) return switchToTab(TAG_SECRETS);
            if (id == R.id.nav_agents)  return switchToTab(TAG_AGENTS);
            return false;
        });
    }

    /**
     * DÜZELTME: Deprecated onBackPressed() override, OnBackPressedCallback ile değiştirildi.
     * Bu Android 13+ predictive back gesture ile uyumlu çalışır.
     */
    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!TAG_CHAT.equals(activeTag)) {
                    bottomNav.setSelectedItemId(R.id.nav_chat);
                    switchToTab(TAG_CHAT);
                } else if (chatFragment != null && chatFragment.canGoBack()) {
                    chatFragment.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private boolean switchToTab(String tag) {
        if (tag.equals(activeTag)) return true;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

        Fragment current = fm.findFragmentByTag(activeTag);
        Fragment next    = fm.findFragmentByTag(tag);

        if (current != null) ft.hide(current);
        if (next    != null) ft.show(next);

        ft.commit();
        activeTag = tag;

        View v = bottomNav.findViewById(getNavItemId(tag));
        if (v != null) v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);

        animateTabIcon(tag);
        return true;
    }

    private int getNavItemId(String tag) {
        switch (tag) {
            case TAG_CHAT:    return R.id.nav_chat;
            case TAG_FILES:   return R.id.nav_files;
            case TAG_SHELL:   return R.id.nav_shell;
            case TAG_MARKET:  return R.id.nav_market;
            case TAG_SECRETS: return R.id.nav_secrets;
            case TAG_AGENTS:  return R.id.nav_agents;
            default:          return R.id.nav_chat;
        }
    }

    private void animateTabIcon(String tag) {
        View icon = bottomNav.findViewById(getNavItemId(tag));
        if (icon != null) {
            icon.animate()
                .scaleX(1.25f).scaleY(1.25f).setDuration(100)
                .withEndAction(() ->
                    icon.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
        }
    }

    public void navigateToFiles()            { bottomNav.setSelectedItemId(R.id.nav_files);   switchToTab(TAG_FILES); }
    public void navigateToShell(String cmd)  { bottomNav.setSelectedItemId(R.id.nav_shell);   switchToTab(TAG_SHELL); if (shellFragment != null && cmd != null) shellFragment.runCommand(cmd); }
    public void navigateToChat()             { bottomNav.setSelectedItemId(R.id.nav_chat);    switchToTab(TAG_CHAT); }
    public void navigateToMarket()           { bottomNav.setSelectedItemId(R.id.nav_market);  switchToTab(TAG_MARKET); }
    public void navigateToSecrets()          { bottomNav.setSelectedItemId(R.id.nav_secrets); switchToTab(TAG_SECRETS); }
    public void navigateToAgents()           { bottomNav.setSelectedItemId(R.id.nav_agents);  switchToTab(TAG_AGENTS); }

    public ChatFragment     getChatFragment()    { return chatFragment; }
    public FilesFragment    getFilesFragment()   { return filesFragment; }
    public ShellFragment    getShellFragment()   { return shellFragment; }
    public AiMarketFragment getMarketFragment()  { return marketFragment; }
    public SecretsFragment  getSecretsFragment() { return secretsFragment; }
    public SubAgentFragment getAgentsFragment()  { return agentsFragment; }
}
