package ph.jeepfare.ui

import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType

/** Taglish UI strings, kept in one place for easy tweaking/translation. */
object Strings {
    const val APP_TITLE = "Pamasahe"
    const val APP_SUBTITLE = "Jeepney Fare Calculator"

    const val JEEP_TYPE_QUESTION = "Anong jeep ang sakay mo?"
    const val DISTANCE_QUESTION = "Gaano kalayo ang biyahe?"
    const val PASSENGERS_QUESTION = "Ilan kayong sasakay?"

    const val TAB_MAP = "Mapa"
    const val TAB_MANUAL = "Manual"
    const val PICK_ON_MAP = "Pumili sa mapa"
    const val CHANGE_ON_MAP = "Palitan sa mapa"
    const val MANUAL_DISTANCE_LABEL = "Distansya (km)"
    const val NO_DISTANCE_YET = "Wala pang napipiling ruta"
    const val ESTIMATE_NOTE = "Tinatayang distansya lang (hindi eksaktong ruta)"

    const val BREAKDOWN_TITLE = "Kuwenta ng pamasahe"
    const val BASE_FARE_LABEL = "Base fare (unang %d km)"
    const val EXTRA_LABEL = "Dagdag"
    const val DISCOUNT_LABEL = "May 20% discount"
    const val PER_HEAD = "bawat isa"
    const val TOTAL_LABEL = "Kabuuang pamasahe"
    const val ENTER_DISTANCE_PROMPT = "Ilagay ang distansya para makuwenta ang pamasahe"
    const val NO_PASSENGERS_PROMPT = "Maglagay ng kahit isang pasahero"

    const val FARE_MATRIX_NOTE = "Batay sa LTFRB fare matrix simula Marso 19, 2026"

    const val MAP_TAP_ORIGIN = "I-tap sa mapa ang iyong sakayan"
    const val MAP_TAP_DESTINATION = "I-tap ang iyong babaan"
    const val MAP_ROUTE_LOADING = "Kinukuha ang ruta…"
    const val MAP_ROUTE_FAILED = "Hindi ma-abot ang routing service — tinatayang distansya ang ipapakita"
    const val ORIGIN_LABEL = "Sakayan"
    const val DESTINATION_LABEL = "Babaan"
    const val USE_DISTANCE = "Gamitin ang distansyang ito"
    const val RESET = "I-reset"
    const val BACK = "Bumalik"

    fun jeepneyTypeLabel(type: JeepneyType): String = when (type) {
        JeepneyType.TRADITIONAL -> "Traditional"
        JeepneyType.MODERN -> "Modern"
    }

    fun passengerTypeLabel(type: PassengerType): String = when (type) {
        PassengerType.REGULAR -> "Regular"
        PassengerType.STUDENT -> "Estudyante"
        PassengerType.SENIOR -> "Senior Citizen"
        PassengerType.PWD -> "PWD"
    }
}
