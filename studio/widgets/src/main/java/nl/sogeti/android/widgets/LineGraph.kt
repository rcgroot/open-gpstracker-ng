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
import nl.sogeti.android.widgets.model.GraphPoint

class LineGraph : View {

    interface ValueDescriptor {
        fun describeXvalue(context: Context, xValue: Float): String {
            return ""
        }

        fun describeYvalue(context: Context, yValue: Float): String {
            return ""
        }
    }

    @Size(multiple = 2) var data: List<GraphPoint> = listOf()
        set(value) {
            field = value
            clearCachedPoints()
            invalidate()
        }
    var xUnit = ""
        set(value) {
            field = value
            invalidate()
        }
    var description = object : ValueDescriptor {}
        set(value) {
            field = value
            invalidate()
        }
    var yUnit = ""
        set(value) {
            field = value
            invalidate()
        }
    var topGradientColor: Int = Color.DKGRAY
        set(value) {
            field = value
            createLineShader()
            invalidate()
        }
    var bottomGradientColor = Color.TRANSPARENT
        set(value) {
            field = value
            createLineShader()
            invalidate()
        }
    var lineColor
        set(value) {
            linePaint.color = value
            axisPaint.color = value
            gridPaint.color = value
            textPaint.color = value
            valueTextPaint.color = value
            invalidate()
        }
        get() = linePaint.color
    private var cachedPoints: List<PointF>? = null
    private val axisPaint = Paint()
    private val gridPaint = Paint()
    private val textPaint = Paint()
    private val valueTextPaint = Paint()
    private val linePaint = Paint()
    private val belowLinePaint = Paint()
    private val linePath = Path()
    private val drawLinePath = Path()
    private var h = 0f
    private var w = 0f
    private var sectionHeight = 0f
    private var sectionWidth = 0f
    private var unitTextSideMargin = 0f
    private val graphSideMargin = dp2px(8)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (attrs != null) appyStyle(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) appyStyle(attrs)
    }

    init {
        axisPaint.color = Color.WHITE
        gridPaint.color = Color.WHITE
        gridPaint.style = Paint.Style.STROKE
        val dash = dp2px(2)
        gridPaint.pathEffect = DashPathEffect(floatArrayOf(dash, dash), dash)
        textPaint.textSize = dp2px(18)
        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
        valueTextPaint.textSize = dp2px(12)
        valueTextPaint.color = Color.WHITE
        valueTextPaint.isAntiAlias = true
        linePaint.color = Color.BLACK
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.setShadowLayer(3f, -2f, 1f, Color.BLACK)
        belowLinePaint.isAntiAlias = true
        unitTextSideMargin = textPaint.textHeight() + valueTextPaint.textHeight()
        if (isInEditMode) {
            xUnit = "time"
            yUnit = "speed"
            data = listOf(GraphPoint(1f, 12F), GraphPoint(2F, 24F), GraphPoint(3F, 36F), GraphPoint(4F, 23F), GraphPoint(5F, 65F), GraphPoint(6F, 10F),
                    GraphPoint(7F, 80F), GraphPoint(8F, 65F), GraphPoint(9F, 13F))
            description = object : ValueDescriptor {
                override fun describeXvalue(context: Context, xValue: Float): String {
                    return "X value"
                }

                override fun describeYvalue(context: Context, yValue: Float): String {
                    return "Y value"
                }
            }
        }
    }

    fun appyStyle(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LineGraphStyle, 0, 0)
        try {
            xUnit = ta.getString(R.styleable.LineGraphStyle_x_unit) ?: xUnit
            yUnit = ta.getString(R.styleable.LineGraphStyle_y_unit) ?: yUnit
            topGradientColor = ta.getColor(R.styleable.LineGraphStyle_top_gradient, topGradientColor)
            bottomGradientColor = ta.getColor(R.styleable.LineGraphStyle_bottom_gradient, bottomGradientColor)
            lineColor = ta.getColor(R.styleable.LineGraphStyle_line_color, lineColor)
        } finally {
            ta.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w.toFloat()
        this.h = h.toFloat()
        this.sectionHeight = (h - unitTextSideMargin - graphSideMargin) / 4f
        this.sectionWidth = (w - unitTextSideMargin - graphSideMargin) / 4f
        clearCachedPoints()
        fillePointsCache()
        createLineShader()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawGraphLine(canvas)
        drawAxis(canvas)
        drawText(canvas)
    }

    private fun createLineShader() {
        belowLinePaint.shader = LinearGradient(0f, 0f, 0f, h, topGradientColor, bottomGradientColor, Shader.TileMode.CLAMP)
    }

    private fun drawGraphLine(canvas: Canvas) {
        if (cachedPoints == null) {
            fillePointsCache()
        }
        // Gradient below
        linePath.reset()
        linePath.moveTo(unitTextSideMargin, h - unitTextSideMargin)
        cachedPoints?.forEach { linePath.lineTo(it.x, it.y) }
        linePath.lineTo(w - graphSideMargin - 1, h - unitTextSideMargin)
        linePath.close()
        canvas.drawPath(linePath, belowLinePaint)

        // Top line
        linePath.rewind()
        linePath.moveTo(unitTextSideMargin, h - unitTextSideMargin)
        cachedPoints?.forEach { linePath.lineTo(it.x, it.y) }
        linePath.lineTo(w - graphSideMargin, h - unitTextSideMargin)
        canvas.drawPath(linePath, linePaint)
    }

    private fun drawAxis(canvas: Canvas) {
        // X-axis
        canvas.drawLine(unitTextSideMargin, h - unitTextSideMargin, w - graphSideMargin, h - unitTextSideMargin, axisPaint)
        // Y-axis
        canvas.drawLine(unitTextSideMargin, h - unitTextSideMargin, unitTextSideMargin, graphSideMargin, axisPaint)
    }

    private fun drawGrid(canvas: Canvas) {
        // Dotted X-axes
        drawLine(canvas, unitTextSideMargin, h - unitTextSideMargin - 1 * sectionHeight, w - graphSideMargin, h - unitTextSideMargin - 1 * sectionHeight, gridPaint)
        drawLine(canvas, unitTextSideMargin, h - unitTextSideMargin - 2 * sectionHeight, w - graphSideMargin, h - unitTextSideMargin - 2 * sectionHeight, gridPaint)
        drawLine(canvas, unitTextSideMargin, h - unitTextSideMargin - 3 * sectionHeight, w - graphSideMargin, h - unitTextSideMargin - 3 * sectionHeight, gridPaint)
        drawLine(canvas, unitTextSideMargin, h - unitTextSideMargin - 4 * sectionHeight, w - graphSideMargin, h - unitTextSideMargin - 4 * sectionHeight, gridPaint)
        // Dotted Y-axes
        drawLine(canvas, unitTextSideMargin + 1 * sectionWidth, h - unitTextSideMargin, unitTextSideMargin + 1 * sectionWidth, graphSideMargin, gridPaint)
        drawLine(canvas, unitTextSideMargin + 2 * sectionWidth, h - unitTextSideMargin, unitTextSideMargin + 2 * sectionWidth, graphSideMargin, gridPaint)
        drawLine(canvas, unitTextSideMargin + 3 * sectionWidth, h - unitTextSideMargin, unitTextSideMargin + 3 * sectionWidth, graphSideMargin, gridPaint)
        drawLine(canvas, unitTextSideMargin + 4 * sectionWidth, h - unitTextSideMargin, unitTextSideMargin + 4 * sectionWidth, graphSideMargin, gridPaint)
    }

    private fun drawText(canvas: Canvas) {
        if (cachedPoints == null) {
            fillePointsCache()
        }
        val x1 = description.describeXvalue(context, minX)
        val x2 = description.describeXvalue(context, (minX + maxX) / 2)
        val x3 = description.describeXvalue(context, maxX)
        val y1 = description.describeYvalue(context, minY)
        val y2 = description.describeYvalue(context, (minY + maxY) / 2)
        val y3 = description.describeYvalue(context, maxY)

        canvas.rotate(-90f)
        // Y unit
        val verticalTextWidth = textPaint.measureText(yUnit)
        canvas.drawText(yUnit, -verticalTextWidth / 2 - h / 2, -textPaint.fontMetrics.top, textPaint)
        // Y values
        canvas.drawText(y1, -graphSideMargin - 4 * sectionHeight, unitTextSideMargin - valueTextPaint.fontMetrics.descent, valueTextPaint)
        val middleTextHeight = valueTextPaint.measureText(y2)
        canvas.drawText(y2, -graphSideMargin - 2 * sectionHeight - middleTextHeight / 2, unitTextSideMargin - valueTextPaint.fontMetrics.descent, valueTextPaint)
        val endTextHeight = valueTextPaint.measureText(y3)
        canvas.drawText(y3, -graphSideMargin - endTextHeight, unitTextSideMargin - valueTextPaint.fontMetrics.descent, valueTextPaint)
        canvas.rotate(90f)

        // X unit
        val horizontalTextWidth = textPaint.measureText(yUnit)
        canvas.drawText(xUnit, w / 2 - horizontalTextWidth / 2, h - textPaint.fontMetrics.bottom, textPaint)
        // X values
        canvas.drawText(x1, unitTextSideMargin, h - textPaint.textHeight(), valueTextPaint)
        val middleTextWidth = valueTextPaint.measureText(x2)
        canvas.drawText(x2, unitTextSideMargin + 2 * sectionWidth - middleTextWidth / 2f, h - textPaint.textHeight(), valueTextPaint)
        val endTextWidth = valueTextPaint.measureText(x3)
        canvas.drawText(x3, unitTextSideMargin + 4 * sectionWidth - endTextWidth, h - textPaint.textHeight(), valueTextPaint)
    }

    private var minY: Float = 0f
    private var maxY: Float = 1f
    private var minX: Float = 0f
    private var maxX: Float = 1f

    private fun fillePointsCache() {
        minY = data.minBy { it.y }?.y ?: 0f
        maxY = data.maxBy { it.y }?.y ?: 100f
        val sorted = data.sortedBy { it.x }
        minX = sorted.firstOrNull()?.x ?: 0f
        maxX = sorted.lastOrNull()?.x ?: 100f
        fun convertDataToPoint(point: GraphPoint): PointF {
            val y = (point.y - minY) / (maxY - minY) * (sectionHeight * 4)
            val x = (point.x - minX) / (maxX - minX) * (sectionWidth * 4)
            return PointF(x + unitTextSideMargin, h - unitTextSideMargin - y)
        }

        cachedPoints = sorted.map { convertDataToPoint(it) }
    }

    private fun clearCachedPoints() {
        cachedPoints = null
    }

    private fun drawLine(canvas: Canvas, x: Float, y: Float, x2: Float, y2: Float, paint: Paint) {
        drawLinePath.rewind()
        drawLinePath.moveTo(x, y)
        drawLinePath.lineTo(x2, y2)
        canvas.drawPath(drawLinePath, paint)
    }

    private fun Paint.textHeight() = this.fontMetrics.descent - this.fontMetrics.ascent + this.fontMetrics.leading

    private fun dp2px(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    private fun drawOutline(canvas: Canvas) {
        canvas.drawLines(floatArrayOf(0f, 0f, w, h,
                w, 0f, 0f, h,
                0f, 0f, w, 0f,
                0f, 0f, 0f, h,
                w - 1, h - 1, 0f, h - 1,
                w - 1, h - 1, w - 1, 0f), axisPaint)
    }
}
