sudo apt-get update
sudo apt install gradle
gradle wrapper --gradle-version 6.5
chmod +x gradlew
./gradlew bundleDebug
./gradlew assembleAndroidTest

sudo apt-get install jq
chmod +x jq

echo 'Uploading aab'
app_url=$(curl -u "pratimagauns1:oyfxyAVRCVmFurpJ5yfg" -X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/app" -F "file=@/mnt/d/Git/BlendNet/bine-android/app/build/outputs/bundle/debug/app-debug.aab" | jq --raw-output '.app_url')

echo $app_url

echo 'Uploading test suite'

test_suite_url=$(curl -u "pratimagauns1:oyfxyAVRCVmFurpJ5yfg" -X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/test-suite" -F "file=@/mnt/d/Git/BlendNet/bine-android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" | jq --raw-output '.test_suite_url')

echo $test_suite_url

echo 'Starting test run'

curl -X POST "https://api-cloud.browserstack.com/app-automate/espresso/build" -d  "{\"singleRunnerInvocation\": \"true\", \"devices\": [\"Google Pixel 3-9.0\"], \"app\": \"$app_url\", \"deviceLogs\" : true, \"testSuite\": \"$test_suite_url\"}" -H "Content-Type: application/json" -u "pratimagauns1:oyfxyAVRCVmFurpJ5yfg"

echo 'Complete!'

