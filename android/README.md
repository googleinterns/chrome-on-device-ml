## Overview
This is an Android app for emulating on-device machine learning in Google Chrome Android application. We use this app as an experimental setup to analyze delay and memory usage of running ML on device in different use cases.

## Build the App using Android Studio
### Prerequisites
- Install [Android Studio](https://developer.android.com/studio/index.html), following instruction on the website.
- Andriod Studio 3.2 or later
- You need and Android device or Android emulator and Android development environment.

### Building
- Open Android Studio, and from the Welcome screen, select Open an existing Android Studio project.
- From the Open File or Project window that appears, navigate to and select the ```chrome-on-device-ml/android``` directory from wherever you cloned this GitHub repo.
- You may also need to install various platforms and tools according to error messages.

### Running
- You need to have an Android device plugged in with developer options enabled at this point. See [here](https://developer.android.com/studio/run/device) for more details on setting up developer devices.
- If you already have Android emulator installed in Android Studio, select a virtual device with minimum API 15.
- Click ```Run``` to run the app on your Android device.
