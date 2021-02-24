package dev.marcelpinto.permissionktx

/**
 * Defines the rational status when a [Permission] status is [PermissionStatus.Revoked]
 */
enum class PermissionRational {
    /**
     * The permission requires further explanation
     */
    REQUIRED,

    /**
     * The permission can be requested directly without further explanation
     */
    OPTIONAL,

    /**
     * Could not be determined. This can happen when checking the status before the first
     * activity is created.
     */
    UNDEFINED
}