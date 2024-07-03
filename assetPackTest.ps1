cd "C:\Users\lorde\Documents\GitHub\game31\"
Remove-Item ".\android\build\outputs\bundle\release\android-release.aab" -Verbose
Remove-Item ".\android\build\outputs\bundle\release\android-release.apks" -Verbose
.\gradlew clean
.\gradlew bundle
java -jar $Env:JAVA_HOME\bin\bundletool-all.jar build-apks --bundle=".\android\build\outputs\bundle\release\android-release.aab" --output=".\android\build\outputs\bundle\release\android-release.apks" --local-testing --ks="C:/Users/lorde/Downloads/kaigan_managed_simulacra1.jks" --ks-pass=pass:"projectISO@03" --ks-key-alias="kaigan" --key-pass=pass:"projectISO@03"
adb uninstall com.kaigan.pipedreams
java -jar $Env:JAVA_HOME\bin\bundletool-all.jar install-apks --apks=".\android\build\outputs\bundle\release\android-release.apks"
adb shell am start -n com.kaigan.pipedreams/com.kaigan.pipedreams.MainActivity
Read-Host -Prompt "Press Enter to exit"