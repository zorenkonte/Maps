package ph.jeepfare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ph.jeepfare.domain.FareRules
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.ui.components.PamBarBack
import ph.jeepfare.ui.components.PamBarTitle
import ph.jeepfare.ui.components.PamCallout
import ph.jeepfare.ui.components.PamCard
import ph.jeepfare.ui.components.PamChip
import ph.jeepfare.ui.components.PamPinnedHeader
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.PamTone
import ph.jeepfare.ui.theme.pamEnter

@Composable
fun RatesScreen(onBack: () -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current

    Scaffold(containerColor = pal.bg) { padding ->
        PamPinnedHeader(
            modifier = Modifier.padding(padding),
            bar = {
                PamBarBack(onBack)
                PamBarTitle(Strings.RATES_TITLE, Modifier.pamEnter(index = 0))
            },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.pamEnter(index = 1)) {
                    PamChip(Strings.RATES_EFFECTIVE_CHIP, tone = PamTone.YELLOW, icon = PamIcons.Event)
                }

                PamCard(
                    overline = Strings.RATES_OVERLINE,
                    stripe = true,
                    modifier = Modifier.pamEnter(index = 2),
                ) {
                    Column {
                        RateTableHeader()
                        HorizontalDivider(thickness = 1.5.dp, color = pal.line)
                        RateTableRow(
                            Strings.jeepneyTypeLabel(JeepneyType.TRADITIONAL), JeepneyType.TRADITIONAL,
                            iconColor = pal.red,
                        )
                        HorizontalDivider(thickness = 1.5.dp, color = pal.line)
                        RateTableRow(
                            Strings.jeepneyTypeLabel(JeepneyType.MODERN), JeepneyType.MODERN,
                            iconColor = pal.blue,
                        )
                    }
                }

                PamCard(overline = Strings.RATES_DISCOUNT_OVERLINE, modifier = Modifier.pamEnter(index = 3)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DiscountRow(
                            PamIcons.School, Strings.passengerTypeLabel(PassengerType.STUDENT),
                            pal.blueDeep, pal.blueTint,
                        )
                        DiscountRow(
                            PamIcons.Elderly, Strings.passengerTypeLabel(PassengerType.SENIOR),
                            pal.yellowDeep, pal.yellowTint,
                        )
                        DiscountRow(
                            PamIcons.Accessible, Strings.passengerTypeLabel(PassengerType.PWD),
                            pal.red, pal.redTint,
                        )
                        Text(
                            Strings.RATES_DISCOUNT_NOTE,
                            fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp,
                            color = pal.ink2,
                        )
                    }
                }

                PamCallout(
                    Strings.RATES_CALLOUT,
                    tone = PamTone.BLUE,
                    icon = PamIcons.Info,
                    modifier = Modifier.pamEnter(index = 4),
                )

                Text(
                    androidx.compose.ui.text.buildAnnotatedString {
                        append(Strings.RATES_SOURCE_PREFIX)
                        withStyle(androidx.compose.ui.text.SpanStyle(fontFamily = fonts.mono)) {
                            append(Strings.RATES_SOURCE_FILE)
                        }
                        append(Strings.RATES_SOURCE_SUFFIX)
                    },
                    fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = pal.ink2,
                    modifier = Modifier.padding(horizontal = 4.dp).pamEnter(index = 5),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RateTableHeader() {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val style = androidx.compose.ui.text.TextStyle(
        fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, letterSpacing = 0.05.em,
        color = pal.ink3,
    )
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(Strings.RATES_COL_TYPE.uppercase(), style = style, modifier = Modifier.weight(1f))
        Text(Strings.RATES_COL_BASE.uppercase(), style = style, textAlign = TextAlign.End, modifier = Modifier.width(92.dp))
        Text(Strings.RATES_COL_PER_KM.uppercase(), style = style, textAlign = TextAlign.End, modifier = Modifier.width(92.dp))
    }
}

@Composable
private fun RateTableRow(label: String, type: JeepneyType, iconColor: Color) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val rate = FareRules.rateFor(type)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(PamIcons.AirportShuttle, contentDescription = null, tint = iconColor, modifier = Modifier.size(19.dp))
        Text(
            label,
            fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = pal.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            rate.baseFare.peso(),
            fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = pal.ink,
            textAlign = TextAlign.End, modifier = Modifier.width(92.dp),
        )
        Text(
            rate.perKm.peso(),
            fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = pal.ink,
            textAlign = TextAlign.End, modifier = Modifier.width(92.dp),
        )
    }
}

@Composable
private fun DiscountRow(icon: ImageVector, label: String, deep: Color, tint: Color) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(34.dp).background(tint, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = deep, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            fontFamily = fonts.body, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = pal.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            "−20%",
            fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = pal.ink2,
        )
    }
}
