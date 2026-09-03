with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# I will just replace the specific compileSdk block with the one from the original
content = content.replace("compileSdk = 36", "compileSdk { version = release(36) { minorApiLevel = 1 } }")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
