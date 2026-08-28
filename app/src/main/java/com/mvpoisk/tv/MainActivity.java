package com.mvpoisk.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private static final String HOME = "https://mvcomplexsite.github.io/mvpoisk/?tv=1&app=3";

    private FrameLayout root;
    private WebView webView;
    private CursorView cursorView;
    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;
    private boolean playerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enterImmersive();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        cursorView = new CursorView(this);
        cursorView.setVisibility(View.GONE);
        root.addView(cursorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        // Do not shrink a 1920-wide web page again to "fit" the physical 4K panel.
        // This was one of the causes of the v1/v2 TV zoom/scale mismatch.
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(settings.getUserAgentString() + " MVPoiskTV/3.0 AndroidTV");

        webView.addJavascriptInterface(new TvBridge(), "MVPoiskAndroid");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && host.equalsIgnoreCase("mvcomplexsite.github.io")) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // If navigation returned to a normal page, ensure the cursor cannot
                // remain floating above the catalogue.
                view.evaluateJavascript(
                        "Boolean(window.MVPoiskTVPlayer&&window.MVPoiskTVPlayer.isOpen&&window.MVPoiskTVPlayer.isOpen())",
                        value -> {
                            if (!"true".equals(value)) setPlayerOpenUi(false);
                        });
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                fullscreenView = view;
                fullscreenCallback = callback;
                webView.setVisibility(View.GONE);
                root.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                if (playerOpen) cursorView.bringToFront();
                enterImmersive();
            }

            @Override
            public void onHideCustomView() {
                exitFullscreenPlayer();
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME);
        }
        webView.requestFocus();
    }

    private class TvBridge {
        @JavascriptInterface
        public void setPlayerOpen(boolean open) {
            runOnUiThread(() -> setPlayerOpenUi(open));
        }

        @JavascriptInterface
        public void requestKeyboard() {
            runOnUiThread(() -> {
                webView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT);
            });
        }
    }

    private void setPlayerOpenUi(boolean open) {
        playerOpen = open;
        if (cursorView == null) return;
        if (open) {
            cursorView.resetToCenter();
            cursorView.setVisibility(View.VISIBLE);
            cursorView.bringToFront();
        } else {
            cursorView.setVisibility(View.GONE);
        }
    }

    private void enterImmersive() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void tapAtCursor() {
        if (cursorView == null) return;
        View target = fullscreenView != null ? fullscreenView : webView;
        if (target == null || target.getWidth() <= 0 || target.getHeight() <= 0) return;
        float x = cursorView.getCursorX();
        float y = cursorView.getCursorY();
        long now = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent up = MotionEvent.obtain(now, now + 65, MotionEvent.ACTION_UP, x, y, 0);
        target.dispatchTouchEvent(down);
        target.dispatchTouchEvent(up);
        down.recycle();
        up.recycle();
    }

    private boolean closeWebPlayerIfOpen() {
        if (!playerOpen || webView == null) return false;
        webView.evaluateJavascript(
                "try{Boolean(window.MVPoiskTVPlayer&&window.MVPoiskTVPlayer.closeIfOpen&&window.MVPoiskTVPlayer.closeIfOpen())}catch(e){false}",
                value -> setPlayerOpenUi(false));
        return true;
    }

    private void exitFullscreenPlayer() {
        if (fullscreenView == null) return;
        root.removeView(fullscreenView);
        fullscreenView = null;
        webView.setVisibility(View.VISIBLE);
        if (fullscreenCallback != null) fullscreenCallback.onCustomViewHidden();
        fullscreenCallback = null;
        webView.requestFocus();
        if (playerOpen) cursorView.bringToFront();
        enterImmersive();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int code = event.getKeyCode();

        if (playerOpen) {
            if (code == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (fullscreenView != null) exitFullscreenPlayer();
                closeWebPlayerIfOpen();
                return true;
            }

            if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT
                    || code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    float dx = 0f;
                    float dy = 0f;
                    if (code == KeyEvent.KEYCODE_DPAD_LEFT) dx = -1f;
                    if (code == KeyEvent.KEYCODE_DPAD_RIGHT) dx = 1f;
                    if (code == KeyEvent.KEYCODE_DPAD_UP) dy = -1f;
                    if (code == KeyEvent.KEYCODE_DPAD_DOWN) dy = 1f;
                    cursorView.move(dx, dy, event.getRepeatCount());
                }
                return true;
            }

            if ((code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER)
                    && event.getAction() == KeyEvent.ACTION_UP) {
                tapAtCursor();
                return true;
            }
        }

        if (code == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (fullscreenView != null) {
                exitFullscreenPlayer();
                return true;
            }
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersive();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.removeJavascriptInterface("MVPoiskAndroid");
            webView.destroy();
        }
        super.onDestroy();
    }

    private static class CursorView extends View {
        private final Paint outer = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint inner = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float x;
        private float y;

        CursorView(Context context) {
            super(context);
            setWillNotDraw(false);
            setClickable(false);
            setFocusable(false);
            outer.setStyle(Paint.Style.FILL);
            outer.setColor(Color.argb(230, 10, 10, 14));
            inner.setStyle(Paint.Style.FILL);
            inner.setColor(Color.WHITE);
        }

        void resetToCenter() {
            post(() -> {
                x = getWidth() * 0.5f;
                y = getHeight() * 0.5f;
                invalidate();
            });
        }

        void move(float dx, float dy, int repeatCount) {
            float base = Math.max(32f, getWidth() * 0.018f);
            float acceleration = repeatCount >= 8 ? 2.2f : repeatCount >= 3 ? 1.55f : 1f;
            float step = base * acceleration;
            float margin = Math.max(18f, getWidth() * 0.008f);
            x = Math.max(margin, Math.min(getWidth() - margin, x + dx * step));
            y = Math.max(margin, Math.min(getHeight() - margin, y + dy * step));
            invalidate();
        }

        float getCursorX() { return x; }
        float getCursorY() { return y; }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float r = Math.max(8f, getWidth() * 0.0046f);
            canvas.drawCircle(x, y, r + 5f, outer);
            canvas.drawCircle(x, y, r, inner);
        }
    }
}
