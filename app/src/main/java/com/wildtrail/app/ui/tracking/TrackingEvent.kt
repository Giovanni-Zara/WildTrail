package com.wildtrail.app.ui.tracking

/**
 * One-shot events emitted by [TrackingViewModel] over a `Channel` (not state),
 * so each is handled exactly once and never replayed on recomposition or
 * configuration change. This is the "NavigationEvent" channel called for by
 * the fall-detection spec.
 *
 * Note the overlay's *visibility* is driven by [TrackingUiState.emergency]
 * (durable state) rather than by an event — that keeps it correct across
 * rotation. These events cover the genuinely transient signals.
 */
sealed interface TrackingEvent {

    /** A fall was confirmed: the UI should surface the full-screen emergency
     *  overlay (see also [TrackingUiState.emergency]). */
    data object ShowEmergencyOverlay : TrackingEvent

    /** The emergency countdown elapsed and the (placeholder) call was placed —
     *  the UI shows a Toast naming the contact. */
    data class EmergencyCallPlaced(val contactName: String) : TrackingEvent
}
