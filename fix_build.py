with open("app/build.gradle.kts", "r") as f:
    content = f.read()

import re
# Find the signingConfigs block
content = re.sub(r'signingConfigs\s*\{[\s\S]*?buildTypes', '''signingConfigs {
    create("release") {
      storeFile = file("${rootDir}/release.keystore")
      storePassword = "release123"
      keyAlias = "release"
      keyPassword = "release123"
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes''', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
