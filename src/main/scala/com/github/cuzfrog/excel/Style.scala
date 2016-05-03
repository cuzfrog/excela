package com.github.cuzfrog.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors

sealed trait Style {
  private[excel] val entity: CellStyle
  def numberFormat: String
}

object Style {
  private[excel] def apply(poiCellStyle: CellStyle): Style = {
    new PoiCellStyle(poiCellStyle)
  }

  private class PoiCellStyle(poiCellStyle: CellStyle) extends Style {
    override val entity = poiCellStyle
    lazy val numberFormat = entity.getDataFormatString
  }
}
//===============StyleBuilder===============
class StyleBuilder(val workbook: Workbook) {
  var fontSize: Short = 10
  var fontName: String = "宋体"
  var fontIsBold: Boolean = false
  var hasBorder: Boolean = true
  var wrapText: Boolean = false
  var fgColor: Short = IndexedColors.AUTOMATIC.getIndex
  var isFill: Boolean = false
  var dataFormatString: String = "0"
  def newStyle(): Style = {

    val cellStyle = workbook.entity.createCellStyle()
    val dataFormat = workbook.entity.createDataFormat()
    def setBorderStyle(on: Boolean) = {
      val bs = if (on) CellStyle.BORDER_THIN else CellStyle.BORDER_NONE
      val bc = IndexedColors.BLACK.getIndex()
      cellStyle.setBorderBottom(bs);
      cellStyle.setBottomBorderColor(bc);
      cellStyle.setBorderLeft(bs);
      cellStyle.setLeftBorderColor(bc);
      cellStyle.setBorderRight(bs);
      cellStyle.setRightBorderColor(bc);
      cellStyle.setBorderTop(bs);
      cellStyle.setTopBorderColor(bc);
    }

    val font = workbook.entity.createFont
    font.setFontName(fontName)
    font.setFontHeightInPoints(fontSize)
    font.setBold(fontIsBold)
    cellStyle.setFont(font)
    setBorderStyle(hasBorder)
    cellStyle.setWrapText(wrapText)
    cellStyle.setFillForegroundColor(fgColor)
    val fillStyle = if (isFill) CellStyle.SOLID_FOREGROUND else CellStyle.NO_FILL
    cellStyle.setFillPattern(fillStyle);
    cellStyle.setDataFormat(dataFormat.getFormat(this.dataFormatString))
    Style(cellStyle)

  }
}