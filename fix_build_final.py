with open("app/build.gradle.kts", "r") as f:
    lines = f.readlines()

new_lines = []
in_signing = False
for line in lines:
    if "signingConfigs {" in line:
        in_signing = True
        new_lines.append('''  signingConfigs {
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
''')
    elif in_signing:
        if "buildTypes {" in line:
            in_signing = False
            new_lines.append(line)
    else:
        new_lines.append(line)

with open("app/build.gradle.kts", "w") as f:
    f.write("".join(new_lines))
