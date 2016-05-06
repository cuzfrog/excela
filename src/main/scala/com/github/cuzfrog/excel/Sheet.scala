package com.github.cuzfrog.excel

import scala.collection.JavaConversions._
import com.typesafe.scalalogging.LazyLogging

sealed trait Sheet {
  val name: String
  val index: Int
  val workbook: Workbook
  /**
   * Get all rows from this sheet. If a row is not defined in excel sheet, a placeholder row will substitute.
   * Empty rows in the tail, though defined in excel sheet, will be dropped.
   */
  def rows: Seq[Row]
  def getRow(rowIdx: Int): Row
  def cells: Seq[Seq[Cell]]
  def getCell(rowIdx: Int, columnIdx: Int): Cell
  def setValue(rowIdx: Int, columnIdx: Int, value: Any): Cell
  def setStyle(rowIdx: Int, columnIdx: Int, style: Style): Cell
  /**
   * Copy content from a range to a range in this sheet specified by row/column, or with the same row/column.
   * Content flag: use 8 bit to specify what should be copied.
   * 0x01=value,0x02=style
   *
   * @return the range to which contents are copied.
   */
  def setRange(fromRange: Range, rowBegin: Int = (-1), columnBegin: Int = (-1), content: Byte = 0x01): Range
  def getRange(rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int): Range
  def getRange(preRange: Range.PreRange): Range
  /**
   * If input is Style, setStyle. Otherwise setValue.
   */
  def update(data: List[List[Any]]): Sheet
}

private object Sheet extends LazyLogging {
  def apply(sheet: org.apache.poi.ss.usermodel.Sheet, workbook: Workbook): Sheet = {
    new PoiSheet(sheet, workbook)
  }

  private class PoiSheet(sheet: org.apache.poi.ss.usermodel.Sheet, val workbook: Workbook) extends Sheet {
    private val entity = sheet
    override val name = entity.getSheetName
    override val index = 0

    override lazy val rows = {
      val indexedRows = {
        val cc = entity.rowIterator().toSeq.map(Row(_, this)).filter(_.isEmpty.unary_!)
        cc.indices zip cc
      }
      val bottomRowNum = indexedRows.last._2.index
      val rowsMap = indexedRows.toMap
      val rows = (0 to bottomRowNum).map {
        rowIdx =>
          rowsMap.get(rowIdx) match {
            case Some(row) => row
            case None      => Row(rowIdx, this) //interpolate with placeholder
          }
      }
      rows
    }
    override def getRow(rowIdx: Int) = entity.getRow(rowIdx) match {
      case null => Row(rowIdx, this)
      case poir => Row(poir, this)
    }

    override lazy val cells = {
      var time1 = System.currentTimeMillis()
      val rightEndColumn = rows.map(_.maxColumnIdx).max
      var time2 = System.currentTimeMillis()
      //logger.info(s"Initiated lazy rows,row size:${rows.size}, and get maxColumnIdx:${rightEndColumn} Time consumed: ${(time2 - time1)}")

      val cellsMatrix = rows.map {
        row =>
          val cellsInRow = {
            val cr = row.cells
            cr.map(_.columnIdx) zip cr
          }.toMap
          (0 to rightEndColumn).map {
            colIdx =>
              cellsInRow.get(colIdx) match {
                case Some(cell) => cell //return Cell in row
                case None       => Cell(row.index, colIdx, this) //when no column index, create placeholder.
              }
          }
      }
      time1 = System.currentTimeMillis()
      //logger.info(s"Initiated cells matrix, Time consumed: ${(time1 - time2)}")

      cellsMatrix.map(_.toList).toList
    }

    override def setValue(rowIdx: Int, columnIdx: Int, value: Any) = {
      this.getCell(rowIdx: Int, columnIdx: Int).setValue(value)
    }
    override def setStyle(rowIdx: Int, columnIdx: Int, style: Style) = {
      this.getCell(rowIdx: Int, columnIdx: Int).setStyle(style)
    }
    override def setRange(fromRange: Range, rowBegin: Int, columnBegin: Int, content: Byte): Range = {
      val rBegin = if (rowBegin < 0) fromRange.rowBegin else rowBegin
      val cBegin = if (columnBegin < 0) fromRange.columnBegin else columnBegin
      val rowCnt = fromRange.rowEnd - fromRange.rowBegin + 1
      val colCnt = fromRange.columnEnd - fromRange.columnBegin + 1
      val range = this.getRange(rBegin, cBegin, rBegin + rowCnt - 1, cBegin + colCnt - 1)
      range.copy(fromRange, content)
    }

    override def getRange(rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int): Range = {
      Range(this, rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int)
    }
    override def getRange(preRange: Range.PreRange): Range = {
      Range(this, preRange)
    }

    override def getCell(rowIdx: Int, columnIdx: Int) = getRow(rowIdx).getCell(columnIdx)

    override def update(data: List[List[Any]]) = {

      (data.indices zip data).foreach {
        row =>
          val rowIdx = row._1
          (row._2.indices zip row._2).foreach {
            value =>
              val columnIdx = value._1
              val cell = this.getCell(rowIdx, columnIdx)
              value._2 match {
                case st: Style => cell.setStyle(st)
                case v         => cell.setValue(v)
              }
          }
      }
      this
    }
  }
}