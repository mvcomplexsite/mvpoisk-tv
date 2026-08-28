# MVPoisk TV Android v2

Android TV WebView shell for MVPoisk TV mode.

Production URL:
`https://mvcomplexsite.github.io/mvpoisk/?tv=1`

## v2 changes

- dedicated 1920x1080 CSS viewport for stable scaling on 4K Android TV;
- full-screen MVPoisk player shell when pressing Watch;
- partner iframe receives D-pad keys instead of MVPoisk stealing them;
- the first OK while the partner iframe is focused emulates a center tap for players whose big Play button is not keyboard-focusable;
- Back closes the TV player state before navigating away;
- hardware-accelerated WebView and fixed text zoom;
- versionCode 2 / versionName 2.0;
- release workflow uses a persistent MVPoisk signing key through GitHub repository secrets.

## Required GitHub Actions secrets

Create these repository secrets before running the workflow:

- `MVPOISK_KEYSTORE_BASE64`
- `MVPOISK_KEYSTORE_PASSWORD`
- `MVPOISK_KEY_ALIAS`
- `MVPOISK_KEY_PASSWORD`

Do not commit the `.jks` signing key or the secrets text file to the public repository.

The workflow produces artifact `mvpoisk-tv-v2-release-apk` containing `app-release.apk`.
