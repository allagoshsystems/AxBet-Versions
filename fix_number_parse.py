with open("app/src/main/java/com/example/AppUpdateManager.kt", "r") as f:
    content = f.read()

robust_parse = """                        val latestVersionCodeObj = snapshot.get("latest_version_code")
                        val latestVersionCode = when (latestVersionCodeObj) {
                            is Number -> latestVersionCodeObj.toInt()
                            is String -> latestVersionCodeObj.toIntOrNull() ?: 0
                            else -> 0
                        }"""

content = content.replace('                        val latestVersionCode = snapshot.getLong("latest_version_code")?.toInt() ?: 0', robust_parse)

with open("app/src/main/java/com/example/AppUpdateManager.kt", "w") as f:
    f.write(content)
