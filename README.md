# MVPoisk TV Android v5

Android TV WebView shell for MVPoisk TV v33.

Changes from v3:
- loads `?tv=1&app=5`;
- virtual player cursor fades out after 2.4 seconds of inactivity;
- any D-pad movement immediately restores the cursor;
- OK also briefly restores the cursor before sending the touch;
- remote Back remains the only TV playback exit;
- uses the same permanent MVPoisk release signing secrets as v2/v3.

Build with the included GitHub Actions workflow. Artifact: `mvpoisk-tv-v5-release-apk`.
