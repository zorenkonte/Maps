package ph.jeepfare.ui

import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType

/** UI strings. English only, sentence case, addressed to the commuter riding
 * the jeep ("you" / "your fare") rather than to a driver counting a load. */
object Strings {
    const val APP_TITLE = "Pamasahe"
    const val APP_TAGLINE = "How much should your ride cost?"

    const val OVERLINE_JEEP = "Jeep you're riding"
    const val OVERLINE_DISTANCE = "Your trip"
    const val OVERLINE_FARE_TYPE = "Your fare type"
    const val OVERLINE_COMPANIONS = "Paying for someone else?"
    const val OVERLINE_BREAKDOWN = "Breakdown — spot an overcharge"

    const val TAB_MAP = "Map"
    const val TAB_MANUAL = "Manual"
    const val OPEN_MAP = "Open map"
    const val MANUAL_DISTANCE_LABEL = "Distance (km)"
    const val MANUAL_HINT = "The first 4 km are covered by the base fare."
    const val NO_DISTANCE_YET = "No route picked yet"
    const val ROUTE_FROM_MAP = "Route from map"
    const val CHIP_OSRM = "Road route"
    const val CHIP_ESTIMATE = "Estimate"

    const val FARE_TYPE_HINT = "Pick the discount you can show the conductor."
    const val COMPANIONS_HINT = "Add anyone whose fare you're covering."
    const val ADD_COMPANIONS = "Add riders"
    const val HIDE_COMPANIONS = "Just me"
    const val COMPANIONS_CHIP = "You + %d more"

    const val RESIBO_HEADER = "PAMASAHE"
    const val RESIBO_TOTAL = "TOTAL"
    const val RESIBO_YOUR_FARE = "YOUR FARE"
    const val RESIBO_FOOTER = "Safe trip!"
    const val BASE_FARE_LABEL = "Base fare · first %d km"
    const val EXTRA_LABEL = "Extra · %d km × %s"
    const val FULL_FARE_LABEL = "Full fare · per rider"
    const val YOU_LABEL = "You · %s"
    const val DISCOUNT_SUFFIX = " (−20%)"
    const val DISCOUNT_CHIP = "20% off"
    const val SHARE_RESIBO = "Share receipt"
    const val ENTER_DISTANCE_PROMPT = "Set your distance to see the fare"

    const val FARE_MATRIX_NOTE = "LTFRB fare matrix · effective March 19, 2026"

    const val MAP_TITLE = "Pick your trip"
    const val MAP_TAP_HINT = "1st tap: pick-up · 2nd: drop-off · 3rd: reset"
    const val MAP_TAP_ON_MAP = "Tap the map"
    const val ORIGIN_LABEL = "Pick-up"
    const val DESTINATION_LABEL = "Drop-off"
    const val MAP_ROUTE_FAILED = "Straight-line estimate — no network"
    const val USE_DISTANCE = "Use this"
    const val PICK_FIRST = "Tap 2 points"
    const val MY_LOCATION = "Recenter map"
    const val BACK = "Back"
    const val THEME = "Theme"

    const val RATES_TITLE = "LTFRB fares"
    const val RATES_EFFECTIVE_CHIP = "Effective March 19, 2026"
    const val RATES_OVERLINE = "Jeepney fares"
    const val RATES_COL_TYPE = "Type"
    const val RATES_COL_BASE = "Base · 4 km"
    const val RATES_COL_PER_KM = "Per km"
    const val RATES_DISCOUNT_OVERLINE = "Discounts — 20%"
    const val RATES_DISCOUNT_NOTE = "The discount applies to the whole fare, before rounding."
    const val RATES_CALLOUT =
        "Extra kilometers are charged per started kilometer (for example: 7.5 km = 4 extra). " +
            "Each rider's fare is rounded to the nearest ₱0.25."
    const val RATES_SOURCE_PREFIX = "Source: LTFRB fare matrix. When fares change, update "
    const val RATES_SOURCE_FILE = "FareRules.kt"
    const val RATES_SOURCE_SUFFIX = " — one place only."

    const val RECEIPT_TITLE = "Receipt"
    const val SHARE = "Share"
    const val SAVE = "Download"
    const val SAVED = "Saved!"
    const val SAVE_FAILED = "Can't save"
    const val RECEIPT_HINT = "Show this to the conductor — the math is in the open, so an overcharge is easy to spot."
    const val TOO_FAR_NOTE = "Too far for a jeepney — 500 km max"

    const val STEPPER_DECREASE = "Remove one %s"
    const val STEPPER_INCREASE = "Add one %s"

    fun jeepneyTypeLabel(type: JeepneyType): String = when (type) {
        JeepneyType.TRADITIONAL -> "Traditional"
        JeepneyType.MODERN -> "Modern"
    }

    fun jeepneyTypeLong(type: JeepneyType): String = when (type) {
        JeepneyType.TRADITIONAL -> "Traditional PUJ"
        JeepneyType.MODERN -> "Modern PUJ"
    }

    fun passengerTypeLabel(type: PassengerType): String = when (type) {
        PassengerType.REGULAR -> "Regular"
        PassengerType.STUDENT -> "Student"
        PassengerType.SENIOR -> "Senior"
        PassengerType.PWD -> "PWD"
    }
}
