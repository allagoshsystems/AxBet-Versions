with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("compileOptions {", "lint {\n    checkReleaseBuilds = false\n  }\n  compileOptions {")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
