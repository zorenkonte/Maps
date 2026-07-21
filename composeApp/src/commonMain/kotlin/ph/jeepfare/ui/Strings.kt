package ph.jeepfare.ui

import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType

/** Taglish UI strings per the design system's content rules (readme.md):
 * Filipino labels, English numbers/units, sentence case, informal ikaw/mo. */
object Strings {
    const val APP_TITLE = "Pamasahe"
    const val APP_TAGLINE = "Magkano ang pamasahe mo?"

    const val OVERLINE_JEEP = "Uri ng jeep"
    const val OVERLINE_DISTANCE = "Distansya"
    const val OVERLINE_PASSENGERS = "Mga pasahero"
    const val OVERLINE_BREAKDOWN = "Breakdown — laban sa sobrang singil"

    const val TAB_MAP = "Mapa"
    const val TAB_MANUAL = "Manual"
    const val OPEN_MAP = "Buksan ang mapa"
    const val MANUAL_DISTANCE_LABEL = "Distansya (km)"
    const val MANUAL_HINT = "Kasama ang unang 4 km sa base fare."
    const val NO_DISTANCE_YET = "Wala pang napipiling ruta"
    const val ROUTE_FROM_MAP = "Ruta mula sa mapa"
    const val CHIP_OSRM = "OSRM ruta"
    const val CHIP_ESTIMATE = "estimate"
    const val ESTIMATE_NOTE = "tinatayang distansya (estimate)"

    const val RESIBO_HEADER = "PAMASAHE"
    const val RESIBO_TOTAL = "KABUUAN"
    const val RESIBO_FOOTER = "Ingat sa biyahe!"
    const val BASE_FARE_LABEL = "Base fare · unang %d km"
    const val EXTRA_LABEL = "Dagdag · %d km × %s"
    const val PER_HEAD_LABEL = "Pamasahe bawat tao"
    const val DISCOUNT_SUFFIX = " (−20%)"
    const val DISCOUNT_CHIP = "20% diskwento"
    const val SHARE_RESIBO = "I-share ang resibo"
    const val ENTER_DISTANCE_PROMPT = "Ilagay ang distansya para makuwenta ang pamasahe"
    const val NO_PASSENGERS_PROMPT = "Maglagay ng kahit isang pasahero"

    const val FARE_MATRIX_NOTE = "Singil ng LTFRB · epektibo Marso 19, 2026"

    const val MAP_TITLE = "Pumili sa mapa"
    const val MAP_TAP_HINT = "1st tap: sakayan · 2nd: babaan · 3rd: reset"
    const val MAP_TAP_ON_MAP = "I-tap sa mapa"
    const val ORIGIN_LABEL = "Sakayan"
    const val DESTINATION_LABEL = "Babaan"
    const val MAP_ROUTE_FAILED = "tinatayang distansya (estimate) — walang network"
    const val MAP_LOAD_FAILED = "Hindi ma-load ang mapa"
    const val USE_DISTANCE = "Gamitin"
    const val PICK_FIRST = "Pumili"
    const val MY_LOCATION = "Lokasyon ko"
    const val BACK = "Bumalik"
    const val THEME = "Tema"

    const val RATES_TITLE = "Singil ng LTFRB"
    const val RATES_EFFECTIVE_CHIP = "Epektibo Marso 19, 2026"
    const val RATES_OVERLINE = "Pamasahe sa jeep"
    const val RATES_COL_TYPE = "Uri"
    const val RATES_COL_BASE = "Base · 4 km"
    const val RATES_COL_PER_KM = "Bawat km"
    const val RATES_DISCOUNT_OVERLINE = "Diskwento — 20%"
    const val RATES_DISCOUNT_NOTE = "Ang diskwento ay sa buong pamasahe, bago i-round."
    const val RATES_CALLOUT =
        "Ang dagdag na km ay sinisingil bawat nasimulang km (halimbawa: 7.5 km = 4 na dagdag). " +
            "Bawat pasahero, ni-round sa pinakamalapit na ₱0.25."
    const val RATES_SOURCE_PREFIX = "Source: LTFRB fare matrix. Kapag nagbago ang singil, i-update ang "
    const val RATES_SOURCE_FILE = "FareRules.kt"
    const val RATES_SOURCE_SUFFIX = " — isang lugar lang."

    const val RECEIPT_TITLE = "Resibo"
    const val SHARE = "I-share"
    const val SAVE = "I-save"
    const val SAVED = "Na-save!"
    const val RECEIPT_HINT = "Ipakita sa konduktor — malinaw ang math, iwas sobrang singil."
    const val TOO_FAR_NOTE = "Masyadong malayo para sa jeep — hanggang 500 km lang"

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
        PassengerType.STUDENT -> "Estudyante"
        PassengerType.SENIOR -> "Senior"
        PassengerType.PWD -> "PWD"
    }
}
