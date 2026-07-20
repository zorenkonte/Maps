package ph.jeepfare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamBorderWidth

/** One line on the receipt. */
data class ResiboRow(
    val label: String,
    val value: String,
    val strong: Boolean = false,
    val muted: Boolean = false,
)

/** Dashed horizontal rule (2px dashed --line-2). */
@Composable
fun ResiboDivider(modifier: Modifier = Modifier) {
    val pal = LocalPamPalette.current
    Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        drawLine(
            color = pal.line2,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
    }
}

/**
 * Receipt surface: perforated top/bottom edges (punched holes in page-bg color),
 * dashed rules, mono money rows with dotted leaders, Baloo total. Mirrors Resibo.jsx.
 */
@Composable
fun Resibo(
    modifier: Modifier = Modifier,
    header: String? = null,
    sub: String? = null,
    rows: List<ResiboRow>,
    dividerBeforeIndex: Int? = null,
    totalLabel: String? = null,
    totalValue: String? = null,
    footer: String? = null,
    pop: Boolean = false,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = if (pop) 24f else 4f; shape = RoundedCornerShape(14.dp); clip = false }
            .background(pal.surface, RoundedCornerShape(14.dp))
            .border(PamBorderWidth, pal.line, RoundedCornerShape(14.dp))
            .drawBehind {
                // Punched-hole perforation: page-bg dots straddling the top and bottom edges.
                val r = 4.5.dp.toPx()
                val step = 20.dp.toPx()
                val inset = 10.dp.toPx() + r
                var x = inset
                while (x <= size.width - inset + 0.5f) {
                    drawCircle(pal.bg, radius = r, center = Offset(x, 0f))
                    drawCircle(pal.bg, radius = r, center = Offset(x, size.height))
                    x += step
                }
            }
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
    ) {
        if (header != null) {
            Text(
                header,
                fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.04.em,
                color = pal.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
        if (sub != null) {
            Text(
                sub,
                fontFamily = fonts.mono, fontSize = 11.5.sp, color = pal.ink2,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
        if (header != null || sub != null) ResiboDivider(Modifier.padding(vertical = 12.dp))

        rows.forEachIndexed { index, row ->
            if (index == dividerBeforeIndex) ResiboDivider(Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    row.label,
                    fontFamily = fonts.mono, fontSize = 12.5.sp,
                    color = if (row.muted) pal.ink3 else pal.ink2,
                )
                // Dotted leader between label and value.
                Canvas(modifier = Modifier.weight(1f).widthIn(min = 12.dp).height(2.dp).padding(horizontal = 4.dp)) {
                    drawLine(
                        color = pal.line2,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.dp.toPx(), 5.dp.toPx())),
                    )
                }
                Text(
                    row.value,
                    fontFamily = fonts.mono,
                    fontWeight = if (row.strong) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (row.strong) 14.sp else 12.5.sp,
                    color = if (row.muted) pal.ink3 else pal.ink,
                )
            }
        }

        if (totalLabel != null && totalValue != null) {
            ResiboDivider(Modifier.padding(top = 12.dp, bottom = 10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    totalLabel,
                    fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.05.em,
                    color = pal.ink, modifier = Modifier.weight(1f),
                )
                Text(
                    totalValue,
                    fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 26.sp,
                    color = pal.ink,
                )
            }
        }
        if (footer != null) {
            Text(
                footer,
                fontFamily = fonts.mono, fontSize = 11.5.sp, color = pal.ink3,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }
    }
}
