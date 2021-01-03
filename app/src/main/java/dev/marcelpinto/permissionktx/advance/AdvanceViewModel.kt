package dev.marcelpinto.permissionktx.advance

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.getPermissionStatus
import dev.marcelpinto.permissionktx.observePermissionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class AdvanceViewModel(private val locationFlow: LocationFlow) : ViewModel() {

    private val finePermission = Manifest.permission.ACCESS_FINE_LOCATION

    private val locationRequested = MutableStateFlow(false)

    private val eventData = MutableSharedFlow<AdvanceEventData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    private val viewData = MutableLiveData<AdvanceViewData>()

    private var trackJob: Job? = null

    init {
        // Observe the location permission status changes in combination with the user input to
        // enable/disable tracking.
        finePermission.observePermissionStatus()
            .combine(locationRequested) { status, isEnabled ->
                status.isGranted() && isEnabled
            }
            .onEach { enable ->
                trackLocation(enable)
            }
            .launchIn(viewModelScope)
    }

    fun getViewData(): LiveData<AdvanceViewData> = viewData

    fun getEventData(): Flow<AdvanceEventData> = eventData

    fun onLocationClick() {
        locationRequested.value = !locationRequested.value
        val permissionStatus = finePermission.getPermissionStatus()
        if (locationRequested.value && permissionStatus is Permission.Status.Revoked
            && permissionStatus.rationale != Permission.Rational.REQUIRED
        ) {
            eventData.tryEmit(AdvanceEventData.RequestPermission)
        }
    }

    fun onRationalConfirmed() {
        eventData.tryEmit(AdvanceEventData.RequestPermission)
    }

    fun onRationalDeclined() {
        locationRequested.value = false
    }

    /**
     * Enable location tracking (if was not enabled already) or disable it by cancelling any
     * existing job.
     */
    private fun trackLocation(enable: Boolean) {
        if (enable) {
            if (trackJob?.isActive == true) return
            updateViewData("...")
            trackJob = locationFlow.getLocation().onEach { location ->
                updateViewData(location)
            }.launchIn(viewModelScope)
        } else {
            trackJob?.cancel()
            updateViewData("Location Disabled")
        }
    }

    private fun updateViewData(location: String) {
        val isLocationRequested = locationRequested.value
        val permissionStatus = finePermission.getPermissionStatus()
        viewData.value = if (permissionStatus.isGranted() && isLocationRequested) {
            AdvanceViewData(
                location = location,
                showPermissionHint = false,
                showRational = false
            )
        } else {
            AdvanceViewData(
                location = location,
                showPermissionHint = isLocationRequested,
                showRational = isLocationRequested && permissionStatus.requiresRational()
            )
        }
    }

    private fun Permission.Status.requiresRational() =
        (this as? Permission.Status.Revoked)?.rationale == Permission.Rational.REQUIRED
}

data class AdvanceViewData(
    val location: String,
    val showPermissionHint: Boolean,
    val showRational: Boolean
)

sealed class AdvanceEventData {
    object RequestPermission : AdvanceEventData()
}