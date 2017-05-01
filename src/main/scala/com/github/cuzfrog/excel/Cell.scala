package com.github.cuzfrog.excel

import org.apache.poi.ss.usermodel.CellType

trait Cell {
  val rowIdx: Int
  val columnIdx: Int
  val row: Row
  def sheet: Sheet
  def getValue: Option[Any]
  def getStyle: Option[Style]
  def setValue(value: Any): Cell
  def setStyle(style: Style): Cell
}

object Cell {
  def apply(cell: org.apache.poi.ss.usermodel.Cell, row: Row): Cell = {
    new PoiCell(cell, row)
  }
  def apply(rowIdx: Int, columnIdx: Int, sheet: Sheet): Cell = {
    new PlaceHolderCell(rowIdx, columnIdx, sheet)
  }

  private class PoiCell(cell: org.apache.poi.ss.usermodel.Cell, override val row: Row) extends Cell {
    private val entity = cell
    override val rowIdx = cell.getRowIndex
    override val columnIdx = cell.getColumnIndex
    override val sheet: Sheet = row.sheet
    override def getValue = Option(fromCell(cell, cell.getCellTypeEnum))
    override def getStyle = Option(Style(entity.getCellStyle))

    private def fromCell(c: org.apache.poi.ss.usermodel.Cell, valueType: CellType): Any = valueType match {
      case CellType.NUMERIC => c.getNumericCellValue
      case CellType.BOOLEAN => c.getBooleanCellValue
      case CellType.FORMULA => fromCell(c, c.getCachedFormulaResultTypeEnum) //recursive call
      case _                 => c.getStringCellValue
    }

    override def setValue(value: Any): PoiCell = {
      value match {
        case null       => //do nothing
        case v: Boolean => cell.setCellValue(v)
        case v: Double  => cell.setCellValue(v)
        case v: Int     => cell.setCellValue(v)
        case v          => cell.setCellValue(v.toString)
      }
      //println(value+"|"+this.getValue+"|"+entity.getRowIndex+","+entity.getColumnIndex+"|")
      this
    }

    override def setStyle(style: Style): PoiCell = {

      this.entity.setCellStyle(style.entity)
      this
    }

  }

  private class PlaceHolderCell(override val rowIdx: Int, override val columnIdx: Int, override val sheet: Sheet) extends Cell {
    override final val getValue = None
    override final val getStyle = None
    override val row = sheet.getRow(rowIdx)
    override def setValue(value: Any): Cell = {
      sheet.realCell(rowIdx, columnIdx).setValue(value) //return a PoiCell with value set
    }
    override def setStyle(style: Style): Cell = {
      sheet.realCell(rowIdx, columnIdx).setStyle(style)
    }
  }
}