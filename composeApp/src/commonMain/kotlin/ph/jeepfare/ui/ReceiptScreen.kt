package ph.jeepfare.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.jeepfare.currentDateLabel
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.TripParty
import ph.jeepfare.rememberSaveImage
import ph.jeepfare.rememberShareImage
import ph.jeepfare.ui.components.PamBarBack
import ph.jeepfare.ui.components.PamBarTitle
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamButtonVariant
import ph.jeepfare.ui.components.PamPinnedHeader
import ph.jeepfare.ui.components.PamStripe
import ph.jeepfare.ui.components.Resibo
import ph.jeepfare.ui.components.ResiboRow
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.pamArriveEnter
import ph.jeepfare.ui.theme.pamEnter

/** What the download button is currently reporting. */
private enum class SaveState { IDLE, SAVED, FAILED }

@Composable
fun ReceiptScreen(breakdown: FareBreakdown, party: TripParty, onBack: () -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val scope = rememberCoroutineScope()
    val shareImage = rememberShareImage()
    val saveImage = rememberSaveImage()
    val dateLabel = remember { currentDateLabel() }
    val fileName = remember(dateLabel) { receiptFileName(dateLabel) }

    // The receipt travels as a picture, so what is shared is literally what is
    // drawn here: this layer records the framed receipt, and the two buttons
    // hand the recorded bitmap to the share sheet or to the gallery.
    val receiptLayer = rememberGraphicsLayer()

    var saveState by remember { mutableStateOf(SaveState.IDLE) }
    // "Saved!" is transient feedback, not a permanent label change.
    LaunchedEffect(saveState) {
        if (saveState != SaveState.IDLE) {
            delay(2000)
            saveState = SaveState.IDLE
        }
    }

    val (rows, dividerAt) = resiboRows(breakdown, party)
    val allRows = listOf(ResiboRow(dateLabel, "", muted = true)) + rows
    val shiftedDivider = dividerAt?.plus(1)

    Scaffold(containerColor = pal.bg) { padding ->
        PamPinnedHeader(
            modifier = Modifier.padding(padding),
            bar = {
                PamBarBack(onBack)
                PamBarTitle(Strings.RECEIPT_TITLE, Modifier.pamEnter(index = 0))
            },
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // The receipt is what this screen is for: it is handed to you,
                // rising into place a beat after the header settles.
                var receiptShown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { receiptShown = true }
                AnimatedVisibility(visible = receiptShown, enter = pamArriveEnter()) {
                    Column(
                        modifier = Modifier
                            .drawWithContent {
                                receiptLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(receiptLayer)
                            }
                            // Opaque page color and a little breathing room, so the
                            // shared PNG is a framed receipt rather than a cut-out
                            // with a clipped shadow.
                            .background(pal.bg)
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        PamStripe(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp)),
                        )
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
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.pamEnter(index = 3),
                ) {
                    PamButton(
                        Strings.SHARE,
                        onClick = {
                            scope.launch { shareImage(receiptLayer.toImageBitmap(), fileName) }
                        },
                        icon = PamIcons.Share,
                        modifier = Modifier.weight(1f),
                    )
                    PamButton(
                        when (saveState) {
                            SaveState.SAVED -> Strings.SAVED
                            SaveState.FAILED -> Strings.SAVE_FAILED
                            SaveState.IDLE -> Strings.SAVE
                        },
                        onClick = {
                            scope.launch {
                                val image = receiptLayer.toImageBitmap()
                                saveImage(image, fileName) { ok ->
                                    saveState = if (ok) SaveState.SAVED else SaveState.FAILED
                                }
                            }
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
