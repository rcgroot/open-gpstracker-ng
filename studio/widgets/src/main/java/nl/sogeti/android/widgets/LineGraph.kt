/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.widgets

import android.content.Context
import android.graphics.*
import android.support.annotation.Size
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import timber.log.Timber

class LineGraph : View {

    var xUnit = ""
    var yUnit = ""
    @Size(multiple = 2) var data: List<Point> = listOf()
        set(value) {
            field = value
            cachedPoints = null
        }
    var topGradientColor: Int = Color.DKGRAY
        set(value) {
            field = value
            createLineShader()
        }
    var bottomGradientColor = Color.TRANSPARENT
        set(value) {
            field = value
            createLineShader()
        }
    var lineColor
        set(value) {
            linePaint.color = value
        }
        get() = linePaint.color
    private var cachedPoints: List<PointF>? = null
    private val axisPaint = Paint()
    private val gridPaint = Paint()
    private val textPaint = Paint()
    private val linePaint = Paint()
    private val belowLinePaint = Paint()
    private val linePath = Path()
    private var h = 0f
    private var w = 0f
    private var sectionHeight = 0f
    private var sectionWidth = 0f
    private var unitTextMargin = 25f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        axisPaint.color = Color.WHITE
        gridPaint.color = Color.WHITE
        gridPaint.style = Paint.Style.STROKE
        gridPaint.pathEffect = DashPathEffect(floatArrayOf(2f, 2f), 2f)
        textPaint.textSize = dp2px(18)
        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
        linePaint.color = Color.BLACK
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.setShadowLayer(3f, -2f, 1f, Color.BLACK)
        belowLinePaint.isAntiAlias = true
        unitTextMargin = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent + textPaint.fontMetrics.leading
        if (true) {
            xUnit = "time"
            yUnit = "speed"
            data = listOf(Point(1, 12), Point(2, 24), Point(3, 36), Point(4, 23), Point(5, 65), Point(6, 12),
                    Point(7, 80), Point(8, 65), Point(9, 12))
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w.toFloat()
        this.h = h.toFloat()
        this.sectionHeight = (h - 2 * unitTextMargin) / 4f
        this.sectionWidth = (w - 2 * unitTextMargin) / 4f
        cachedPoints = null
        createLineShader()
    }

    private fun createLineShader() {
        belowLinePaint.shader = LinearGradient(0f, 0f, 0f, h, topGradientColor, bottomGradientColor, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Timber.d("Drawing on canvas ${canvas.width}x${canvas.height}")
        drawText(canvas)
        drawAxis(canvas)
        drawGraphLine(canvas)
    }

    private fun drawGraphLine(canvas: Canvas) {
        if (cachedPoints == null) {
            fillePointsCache()
        }
        linePath.reset()
        linePath.moveTo(unitTextMargin + 1, h - unitTextMargin - 1)
        cachedPoints?.forEach { linePath.lineTo(it.x, it.y) }
        linePath.lineTo(w - unitTextMargin - 1, h - unitTextMargin - 1)
        linePath.close()
        canvas.drawPath(linePath, belowLinePaint)

        linePath.rewind()
        linePath.moveTo(unitTextMargin + 1, h - unitTextMargin - 1)
        cachedPoints?.forEach { linePath.lineTo(it.x, it.y) }
        linePath.lineTo(w - unitTextMargin - 1, h - unitTextMargin - 1)
        canvas.drawPath(linePath, linePaint)
    }

    private fun drawAxis(canvas: Canvas) {
        // X-axis
        canvas.drawLine(unitTextMargin, h - unitTextMargin, w - unitTextMargin, h - unitTextMargin, axisPaint)
        // Dotted X-axes
        canvas.drawLine(unitTextMargin, h - unitTextMargin - 1 * sectionHeight, w - unitTextMargin, h - unitTextMargin - 1 * sectionHeight, gridPaint)
        canvas.drawLine(unitTextMargin, h - unitTextMargin - 2 * sectionHeight, w - unitTextMargin, h - unitTextMargin - 2 * sectionHeight, gridPaint)
        canvas.drawLine(unitTextMargin, h - unitTextMargin - 3 * sectionHeight, w - unitTextMargin, h - unitTextMargin - 3 * sectionHeight, gridPaint)
        canvas.drawLine(unitTextMargin, h - unitTextMargin - 4 * sectionHeight, w - unitTextMargin, h - unitTextMargin - 4 * sectionHeight, gridPaint)
        // Y-axis
        canvas.drawLine(unitTextMargin, h - unitTextMargin, unitTextMargin, unitTextMargin, axisPaint)
        // Dotted Y-axes
        canvas.drawLine(unitTextMargin + 1 * sectionWidth, h - unitTextMargin, unitTextMargin + 1 * sectionWidth, unitTextMargin, gridPaint)
        canvas.drawLine(unitTextMargin + 2 * sectionWidth, h - unitTextMargin, unitTextMargin + 2 * sectionWidth, unitTextMargin, gridPaint)
        canvas.drawLine(unitTextMargin + 3 * sectionWidth, h - unitTextMargin, unitTextMargin + 3 * sectionWidth, unitTextMargin, gridPaint)
        canvas.drawLine(unitTextMargin + 4 * sectionWidth, h - unitTextMargin, unitTextMargin + 4 * sectionWidth, unitTextMargin, gridPaint)
    }

    private fun drawText(canvas: Canvas) {
        canvas.rotate(-90f)
        val verticalTextWidth = textPaint.measureText(yUnit)
        canvas.drawText(yUnit, -verticalTextWidth / 2 - h / 2, -textPaint.fontMetrics.top, textPaint)
        canvas.rotate(90f)

        val horizontalTextWidth = textPaint.measureText(yUnit)
        canvas.drawText(xUnit, w / 2 - horizontalTextWidth / 2, h - textPaint.fontMetrics.bottom, textPaint)
    }

    private fun fillePointsCache() {
        val minY = data.minBy { it.y }?.y ?: 0
        val maxY = data.maxBy { it.y }?.y ?: 100
        val sorted = data.sortedBy { it.x }
        val minX = sorted.firstOrNull()?.x ?: 0
        val maxX = sorted.lastOrNull()?.x ?: 100
        fun convertDataToPoint(point: Point): PointF {
            val y = ((point.y - minY).toFloat() / (maxY - minY).toFloat()) * (sectionHeight * 4)
            val x = ((point.x - minX).toFloat() / (maxX - minX).toFloat()) * (sectionWidth * 4)
            return PointF(x + unitTextMargin + 1, h - unitTextMargin - y - 1)
        }

        cachedPoints = data.map { convertDataToPoint(it) }
    }

    fun dp2px(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    fun drawOutline(canvas: Canvas) {
        canvas.drawLines(floatArrayOf(0f, 0f, w, h,
                w, 0f, 0f, h,
                0f, 0f, w, 0f,
                0f, 0f, 0f, h,
                w - 1, h - 1, 0f, h - 1,
                w - 1, h - 1, w - 1, 0f), axisPaint)
    }
}
