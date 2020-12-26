# permissions-ktx

[![](https://jitpack.io/v/skimarxall/permissions-ktx.svg)](https://jitpack.io/#skimarxall/permissions-ktx)

Kotlin Lightweight Android permissions library that follows the permission request principles  
and it's Jetpack Compose friendly.

Learn more about best practices at
[https://developer.android.com/guide/topics/permissions/overview](https://developer.android.com/guide/topics/permissions/overview)

# Overview

This library provides a wrapper around the existing Jetpack Activity Contracts to solve the 
following problems:

- Android Lifecycle
- Abstraction
- Permission rejection

This is done with the combination of
[Jetpack Startup](https://developer.android.com/jetpack/androidx/releases/startup)
and the 
[Jetpack Activity and Fragment KTX](https://developer.android.com/jetpack/androidx/releases/activity)
by abstracting the access to the Permission status and enforcing best practices to improve 
permission acceptance rate.

## How to include in your project

Currently providing the library via Jitpack (maven coming soon)

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
dependencies {
        implementation 'com.github.marcelpinto:permissions-ktx:0.1'
}
```

## Register Permission Request

The library follows the same mechanism as 
[ActivityResultContracts](https://developer.android.com/reference/androidx/activity/result/contract/package-summary)
by registering in your Fragment or Activity a variable for result but instead of using 
[registerForActivityResult()](https://developer.android.com/reference/androidx/activity/result/ActivityResultCaller#registerForActivityResult(androidx.activity.result.contract.ActivityResultContract%3CI,%20O%3E,%20androidx.activity.result.ActivityResultCallback%3CO%3E))
you can use [registerForPermissionResult(permissionName)](lib/src/main/java/dev/marcelpinto/permissionktx/PermissionKtx)

```kotlin
class MainFragment : Fragment() {
    
    private val locationPermissionRequest =
        registerForPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
            // do something when permission is granted or rejected
        }
}
```
This creates a [PermissionRequest](lib/src/main/java/dev/marcelpinto/permissionktx/PermissionRequest.kt)
variable that can be used to launch a permission request.

## Launch Permission Request

There are two ways to launch a permission request:

### via safeLaunch

This is the desired way to launch since it enforces the permission recommendation flow by:

1. Checking if the permission was already granted --> onAlreadyGranted
2. Then if further explanation is required --> onRequireRational
3. Otherwise launching the permission request --> onRequirePermission

```kotlin
locationPermissionRequest.safeLaunch(
    onRequirePermission = {
        // update your UI if needed and return true to launch 
        // the permission request
        true
    },
    onRequireRational = {
        // Show a rational (i.e snackbar/dialog) and call
        // locationPermissionRequest.launch() if user acknowledges 
        // the rational
    },
    onAlreadyGranted = {
        // perform action since permission was already granted 
    }
)
```
> Note: only ``onRequireRational`` lambda is required.

## Jetpack Activity/Fragment compatible

For other case (or for backwards compatibility with your existing code) where you
want to launch directly the permission request, ``launch()`` can still be used.

```kotlin
locationPermissionRequest.launch()
```

## Observe Permission Status

The library adds an observability pattern to the current Android Permissions API
by providing a Flow that emits every time a
[declared permission](https://developer.android.com/training/permissions/declaring)
status changes.

This can be used to update UI in a reactive way or to enable/disable certain APIs
that requires a permission (i.e LocationManager).

The PermissionsKtx provides an extension method for Permissions strings so you can
directly call it like:

```kotlin
lifecycleScope.launch {
    Manifest.permission.ACCESS_FINE_LOCATION.observePermissionStatus().collect {
        // update UI
    }   
}
```

## Self Initialization

The library uses the [Jetpack Startup](https://developer.android.com/jetpack/androidx/releases/startup)
library to self-initialize in the right moment.

In case you want to use your own mechanism you can initialize it by calling:

```kotlin
Permission.PermissionInitializer.create(context) 
```