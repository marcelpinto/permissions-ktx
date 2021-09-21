# permissions-ktx ![Maven Central](https://img.shields.io/maven-central/v/dev.marcelpinto/permissions-ktx?style=for-the-badge)

Kotlin Lightweight Android permissions library that follows the permission request principles
and its Jetpack Compose friendly.

Learn more about best practices at
[https://developer.android.com/guide/topics/permissions/overview](https://developer.android.com/guide/topics/permissions/overview)

> **Disclaimer:** This is not an officially supported Google product, it's an experimental personal project, the API is constantly changing, use at your own risk.

# Overview

This library provides a wrapper around the existing Jetpack Activity Contracts to solve the 
following problems:

- Android Lifecycle
- Abstraction and testability
- Permission rejection

This is done with the combination of
[Jetpack Startup](https://developer.android.com/jetpack/androidx/releases/startup)
and the 
[Jetpack Activity and Fragment KTX](https://developer.android.com/jetpack/androidx/releases/activity)
by abstracting the access to the Permission status and enforcing best practices to improve 
permission acceptance rate.

* [How to include in your project](#how-to-include-in-your-project)
* [Check Permission Status](#check-permission-status)
* [Register Permission Request](#register-permission-request)
* [Launch Permission Request](#launch-permission-request)
    + [via safeLaunch(..)](#via-safelaunch)
    + [via launch()](#via-launch)
* [Multiple permissions launch](#multiple-permissions-launch)
* [Observe Permission Status](#observe-permission-status)
* [Self Initialization](#self-initialization)
* [Testing](#testing)
    + [Unit Tests](#unit-tests)
    + [Integration/UI Tests](#integrationui-tests)

## How to include in your project

The library is available via MavenCentral:

```groovy
allprojects {
    repositories {
        // ...
        mavenCentral()
    }
}
```

Add it to your module dependencies:

```groovy
dependencies {
    // base module
    implementation 'dev.marcelpinto:permissions-ktx:$version'
    // for compose projects
    implementation 'dev.marcelpinto:permissions-compose-ktx:$version'
}
```

## Check Permission Status

The [Permission](lib/src/main/java/dev/marcelpinto/permissionktx/Permission.kt) inline class
provides type safety and access to quickly check the status of a given permission:

```kotlin
val finePermission = Permission(Manifest.permission.ACCESS_FINE_LOCATION)
when (val status = finePermission.status) {
    is PermissionStatus.Granted -> // Do something
    is PermissionStatus.Revoked -> if (status.rationale == PermissionRational.REQUIRED) {
        // Show something
    } else {
        // Do something else
    }
}
```

## Register Permission Request

The library follows the same mechanism as 
[ActivityResultContracts](https://developer.android.com/reference/androidx/activity/result/contract/package-summary)
by registering in your Fragment or Activity a variable for result but instead of using 
[registerForActivityResult()](https://developer.android.com/reference/androidx/activity/result/ActivityResultCaller#registerForActivityResult(androidx.activity.result.contract.ActivityResultContract%3CI,%20O%3E,%20androidx.activity.result.ActivityResultCallback%3CO%3E))
you should use [registerForPermissionResult(permissionName)](lib/src/main/java/dev/marcelpinto/permissionktx/PermissionKtx.kt)

```kotlin
class MainFragment : Fragment() {
    
    private val locationPermissionRequest =
        registerForPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
            // do something when permission is granted or rejected
        }
}
```
This creates a [PermissionRequest](lib/src/main/java/dev/marcelpinto/permissionktx/PermissionResultLauncher.kt)
instance that can be used to launch the permission request flow.

## Launch Permission Request

There are two ways to launch a permission request:

### via safeLaunch(..)

This is the desired way to launch since it enforces the permission recommendation flow by:

1. Checking if the permission was already granted --> onAlreadyGranted
2. Then if further explanation is required --> onRequireRational
3. Otherwise launching the permission request --> onRequirePermissions

```kotlin
locationPermissionRequest.safeLaunch(
    onRequirePermissions = {
        // Optional:update your UI if needed and return true to launch 
        // the permission request
        true
    },
    onRequireRational = {
        // Show a rational (i.e snackbar/dialog) and call
        // locationPermissionRequest.launch() if user acknowledges 
        // the rational
    },
    onAlreadyGranted = {
        // Optional: perform action since permission was already granted 
    }
)
```
> Note: only ``onRequireRational`` lambda is required.

Check the [Simple sample](app/src/main/java/dev/marcelpinto/permissionktx/simple/SimpleFragment.kt)
or the [Compose Sample](app/src/main/java/dev/marcelpinto/permissionktx/compose)
for more.

### via launch()

For other case (or for backwards compatibility with your existing code) where you
want to launch directly the permission request, the ``launch()`` method provided
by the Jetpack Activity/Fragment library can still be used.

```kotlin
// this will launch the Android Permission request directly
locationPermissionRequest.launch()
```

## Multiple permissions launch

The library support launching multiple permissions at the same time, although this is only encourage
for specific cases, for example, a videochat (CAMERA and MIC). Otherwise is always better to request
only the necessary single permission for each use case, instead of requesting all at once.

To launch multiple permissions simply pass an array following the same mechanism explained above:

```kotlin
locationPermissionsRequest = registerForMultiplePermissionResult(
  arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
  )
) { resultMap ->
    // a map of Permission and the result as boolean.
}
```

> Check the [MultipleActivity sample](app/src/main/java/dev/marcelpinto/permissionktx/multiple/MultipleActivity.kt).

## Observe Permission Status

The library adds an observability pattern to the current Android Permissions API
by providing a Flow that emits every time a [declared permission](https://developer.android.com/training/permissions/declaring)
status changes.

This can be used to update UI in a reactive way or to enable/disable certain APIs
that requires a permission (i.e LocationManager).

```kotlin
val finePermission = Permission(Manifest.permission.ACCESS_FINE_LOCATION)
lifecycleScope.launch {
    finePermission.statusFlow.collect { status ->
        // Based on the status update UI or enable/disable another component
        // that requires the permission
    }   
}
```
> Note: for an example of this check the [Advance sample](app/src/main/java/dev/marcelpinto/permissionktx/advance)

## Self Initialization

The library uses the [Jetpack Startup](https://developer.android.com/jetpack/androidx/releases/startup)
library to self-initialize in the right moment.

In case you want to use your own mechanism you can initialize it by calling:

```kotlin
PermissionProvider.init(context)
```

And disabling the self-initialization on you AndroidManifest.xml adding the following tag:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup">
    <meta-data
        android:name="dev.marcelpinto.permissionktx.PermissionInitializer"
        android:value="androidx.startup"
        tools:node="remove"/>
</provider>
```

## Testing

The library is built with testability in mind to ensure that the permission flow can be
tested without Android dependencies and it's fully controllable.

### Unit Tests

For Unit Testing the library provides an overload of the Permission.init method
that allows to provide custom implementation of the Permission.Checker and Permission.Observer
allowing the test to control the status of the permission without Android dependencies.

```kotlin
// Using a StateFlow to change the values provided by the Observer and Checker
// You could use other mechanisms or directly a simple variable
private var permissionStatus = MutableStateFlow<Permission.Status>(
    Permission.Status.Revoked(
        type = Permission(Manifest.permission.ACCESS_FINE_LOCATION),
        rationale = Permission.Rational.OPTIONAL
    )
)

@Before
fun setUp() {
    val checker = object : Permission.Checker {
        // Returns the defined value in our StateFlow variable
        override fun getStatus(type: Permission) = permissionStatus.value
    }
    val observer = object : Permission.Observer {
        override fun getStatusFlow(type: Permission) = permissionStatus
    
        override fun refreshStatus() {
            permissionStatus.value = permissionStatus.value
        }
    }
    // Override the Permission initialization with the "fake" implementations
    PermissionProvider.init(checker, observer)
}

@Test
fun test() {
    // Emit new PermissionStatus to the permissionStatus flow to 
    // test different scenarios
}
```
> Check the [AdvanceViewModelTest](app/src/test/java/dev/marcelpinto/permissionktx/advance/AdvanceViewModelTest.kt)
> for a complete example

### Integration/UI Tests

To allow control of the permission flow without having to grant/revoke Android permissions
the library provides an overload of the Permission.Init method that allows to provide
custom implementation for Checker, Observer and the ActivityResultRegistry to use
when launching the permission request
(see [Testing ActivityResult](https://developer.android.com/training/basics/intents/result#test)).

This allow full control and customization of the Permission status
and permission request results, allowing to fully test the permission flow
without interacting with the Android framework.

```kotlin
private var permissionStatus: Permission.Status = Permission.Status.Revoked(
    type = Permission(Manifest.permission.ACCESS_FINE_LOCATION),
    rationale = Permission.Rational.OPTIONAL
)

@Before
fun setUp() {
    // Provide a custom init that returns the values of the defined permissionStatus
    // and when request is launched it returns true or false depending on the permissionStatus
    PermissionProvider.init(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        checker = object : Permission.Checker {
            override fun getStatus(type: Permission) = permissionStatus
        },
        registry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, permissionStatus.isGranted())
            }
        }
    )
}
```

> Check the [SimpleActivityTest](app/src/androidTest/java/dev/marcelpinto/permissionktx/simple/SimpleActivityTest.kt)
> for a complete example.

test