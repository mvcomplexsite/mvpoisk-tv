# MVPoisk TV Android v3

Android TV WebView shell for MVPoisk TV v32.

Changes from v2:
- no WebView overview auto-scaling (fixes 4K zoom mismatch);
- loads `?tv=1&app=3`;
- D-pad virtual cursor while the partner player is open;
- OK sends a real touch at the cursor position, so cross-origin Play / season / episode / voice controls can be clicked;
- Back closes the fullscreen MVPoisk player shell;
- explicit Android TV keyboard request for search;
- uses the same permanent MVPoisk release signing secrets as v2.

Build with the included GitHub Actions workflow. Artifact: `mvpoisk-tv-v3-release-apk`.
