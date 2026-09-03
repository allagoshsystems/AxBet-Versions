with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("MissingGoogleServicesStrategyplugins {", "MissingGoogleServicesStrategy\nplugins {")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
