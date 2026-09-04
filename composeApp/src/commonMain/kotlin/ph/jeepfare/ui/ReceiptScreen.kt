package ph.jeepfare.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.TripParty
import ph.jeepfare.rememberShareText
import ph.jeepfare.currentDateLabel
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamButtonVariant
import ph.jeepfare.ui.components.PamStripe
import ph.jeepfare.ui.components.PamTopBar
import ph.jeepfare.ui.components.Resibo
import ph.jeepfare.ui.components.ResiboRow
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.pamArriveEnter
import ph.jeepfare.ui.theme.pamEnter

@Composable
fun ReceiptScreen(breakdown: FareBreakdown, party: TripParty, onBack: () -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val shareText = rememberShareText()
    val clipboard = LocalClipboardManager.current
    val dateLabel = remember { currentDateLabel() }
    var saved by remember { mutableStateOf(false) }
    // "Copied!" is transient feedback, not a permanent label change.
    LaunchedEffect(saved) {
        if (saved) {
            kotlinx.coroutines.delay(2000)
            saved = false
        }
    }

    val (rows, dividerAt) = resiboRows(breakdown, party)
    val allRows = listOf(ResiboRow(dateLabel, "", muted = true)) + rows
    val shiftedDivider = dividerAt?.plus(1)

    Scaffold(containerColor = pal.bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            PamTopBar(Strings.RECEIPT_TITLE, onBack = onBack, modifier = Modifier.pamEnter(index = 0))
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PamStripe(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .pamEnter(index = 1),
                )
                // The receipt is what this screen is for: it is handed to you,
                // rising into place a beat after the header settles.
                var receiptShown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { receiptShown = true }
                AnimatedVisibility(visible = receiptShown, enter = pamArriveEnter()) {
                    Resibo(
                        header = Strings.RESIBO_HEADER,
                        sub = "${Strings.jeepneyTypeLong(breakdown.jeepneyType)} · ${formatKm(breakdown.distanceKm)} km",
                        rows = allRows,
                        dividerBeforeIndex = shiftedDivider,
                        totalLabel = totalLabelFor(party),
                        totalValue = breakdown.total.peso(),
                        footer = Strings.RESIBO_FOOTER,
                        pop = true,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.pamEnter(index = 3),
                ) {
                    PamButton(
                        Strings.SHARE,
                        onClick = { shareText(resiboShareText(breakdown, party, dateLabel)) },
                        icon = PamIcons.Share,
                        modifier = Modifier.weight(1f),
                    )
                    PamButton(
                        if (saved) Strings.SAVED else Strings.SAVE,
                        onClick = {
                            // "Copy" lands the receipt text on the clipboard — no storage
                            // permission needed, and it pastes anywhere.
                            clipboard.setText(AnnotatedString(resiboShareText(breakdown, party, dateLabel)))
                            saved = true
                        },
                        icon = PamIcons.Download,
                        variant = PamButtonVariant.SECONDARY,
                    )
                }
                Text(
                    Strings.RECEIPT_HINT,
                    fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = pal.ink2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).pamEnter(index = 4),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
