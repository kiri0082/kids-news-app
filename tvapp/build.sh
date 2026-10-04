#!/bin/bash
# Builds kid-news.apk without Gradle: aapt2 -> javac -> d8 -> sign.
# Uses the Android toolchain already installed for jarvis-hud (JDK 17 + SDK 34).
set -euo pipefail
cd "$(dirname "$0")"

TOOLS=$HOME/jarvis-hud/tools
export JAVA_HOME="$TOOLS/jdk-17.0.20.1+1/Contents/Home"
BT="$TOOLS/sdk/build-tools/34.0.0"
ANDROID_JAR="$TOOLS/sdk/platforms/android-34/android.jar"
KEYSTORE="$HOME/.kidnews/kidnews.keystore"

rm -rf build gen classes
mkdir -p build gen classes "$(dirname "$KEYSTORE")"

if [ ! -f "$KEYSTORE" ]; then
  "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$KEYSTORE" -storepass kidnews \
    -keypass kidnews -alias kidnews -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=kid-news"
fi

"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -o build/base.apk -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml -R build/res.zip --java gen \
  --min-sdk-version 24 --target-sdk-version 34

"$JAVA_HOME/bin/javac" -source 11 -target 11 -classpath "$ANDROID_JAR" \
  -d classes gen/com/kidnews/tv/R.java src/com/kidnews/tv/*.java

"$BT/d8" --release --lib "$ANDROID_JAR" --output build \
  "classes/com/kidnews/tv/"*.class

cd build && zip -qj base.apk classes.dex && cd ..

"$BT/zipalign" -f 4 build/base.apk build/aligned.apk
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass pass:kidnews \
  --out build/kid-news.apk build/aligned.apk

echo "Built: $(pwd)/build/kid-news.apk"
