# Kid News

A kid-safe news page. Stories come from kid-focused newsrooms and open **right
in the page**: nothing in the app links out to other websites.

- **Web:** https://kiri0082.github.io/kids-news-app/ (GitHub Pages, served
  from `index.html` on `main`)
- **TV:** `tvapp/`, an Android TV app ("Kid News", `com.kidnews.tv`). It is a
  full-screen WebView loading the page in TV mode (`?tv=1`). It's separate from
  the JARVIS HUD TV app on purpose.

## Sources

| Source | Feed |
|---|---|
| TIME for Kids | `timeforkids.com/feed/` |
| Science News Explores | `snexplores.org/feed` |
| NASA | `nasa.gov/feed/` |

- **Freshness:** stories older than **7 days** are dropped.
- **Fetching:** feeds go through the rss2json proxy. A story's full text comes
  through the allorigins proxy, so the story can open in the page.
- **Rejected sources:** BBC Newsround (not kid-friendly enough), CBC Kids News
  (no RSS) and News for Kids (teasers only).

## Versioning

Every change bumps `PAGE_VERSION` in `index.html`. You can read it off a
running device in three places:
- the page footer
- `window.KIDNEWS_VERSION`
- the console line, which reaches logcat with the tag `KidNews` on the TV

The TV app's own version is `versionName` in `tvapp/AndroidManifest.xml`. It
logs that version at start-up.

## TV app: build and install

There's no Gradle: `tvapp/build.sh` runs aapt2, javac, d8 and apksigner. It
uses the toolchain in `~/jarvis-hud/tools/`. It signs with
`~/.kidnews/kidnews.keystore`, which is created on the first build. Keep that
key, or later updates won't install over the existing app.

```bash
cd tvapp && ./build.sh          # -> build/kid-news.apk
adb install -r build/kid-news.apk
adb shell am start -n com.kidnews.tv/.MainActivity
```

The page itself deploys by pushing `index.html` to `main`. The TV app loads the
live page with normal web caching, so a new version reaches it within about 10
minutes, GitHub Pages' cache time. There's no need to rebuild the APK.
