import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Add permission
content = content.replace(
    "<application",
    "    <uses-permission android:name=\"android.permission.REQUEST_INSTALL_PACKAGES\" />\n    <application"
)

# Add provider
provider_xml = """        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>"""

content = content.replace("    </application>", provider_xml)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
