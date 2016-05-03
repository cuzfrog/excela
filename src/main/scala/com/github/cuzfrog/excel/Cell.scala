package com.github.cuzfrog.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors

trait Cell {
  val rowIdx: Int
  val columnIdx: Int
  val sheet: Sheet
  def getValue: Option[Any]
  def getStyle: Option[Style]
  def setValue(value: Any): Cell
  def setStyle(style: Style): Cell
}

object Cell {
  def apply(cell: org.apache.poi.ss.usermodel.Cell, sheet: Sheet): Cell = {
    new PoiCell(cell, sheet)
  }
  def apply(rowIdx: Int, columnIdx: Int, sheet: Sheet): Cell = {
    new PlaceHolderCell(rowIdx, columnIdx, sheet)
  }

  private class PoiCell(cell: org.apache.poi.ss.usermodel.Cell, override val sheet: Sheet) extends Cell {
    private val entity = cell
    override val rowIdx = cell.getRowIndex
    override val columnIdx = cell.getColumnIndex

    import org.apache.poi.ss.usermodel.Cell._
    override def getValue = Option(fromCell(cell, cell.getCellType))
    override lazy val getStyle = Option(Style(entity.getCellStyle))

    private def fromCell(c: org.apache.poi.ss.usermodel.Cell, valueType: Int): Any = valueType match {
      case CELL_TYPE_NUMERIC => c.getNumericCellValue
      case CELL_TYPE_BOOLEAN => c.getBooleanCellValue
      case CELL_TYPE_FORMULA => fromCell(c, c.getCachedFormulaResultType) //recursive call
      case _ => c.getStringCellValue
    }

    override def setValue(value: Any) = {
      value match {
        case null => //do nothing
        case v: Boolean => cell.setCellValue(v)
        case v: Double => cell.setCellValue(v)
        case v: Int => cell.setCellValue(v)
        case v => cell.setCellValue(v.toString)
      }
      //println(value+"|"+this.getValue+"|"+entity.getRowIndex+","+entity.getColumnIndex+"|")
      this
    }

    override def setStyle(style: Style) = {

      this.entity.setCellStyle(style.entity)
      this
    }

  }

  private class PlaceHolderCell(override val rowIdx: Int, override val columnIdx: Int, override val sheet: Sheet) extends Cell {
    override def getValue = None
    override def getStyle = None
    override def setValue(value: Any) = {
      //println(rowIdx+"|"+columnIdx+"|"+value)
      sheet.setValue(rowIdx, columnIdx, value) //return a PoiCell with value set
    }
    override def setStyle(style: Style) = {
      sheet.setStyle(rowIdx, columnIdx, style)
    }
  }
}