package com.github.cuzfrog.excel

import scala.collection.JavaConversions._
import com.typesafe.scalalogging.LazyLogging

sealed trait Sheet {
  val name: String
  val index: Int
  val workbook: Workbook
  def cells: List[List[Cell]]
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
    override lazy val cells = {
      //var time1 = System.currentTimeMillis()
      val cellsCompact = {
        val cc = entity.rowIterator().toList
        cc.indices zip cc
      }
      val bottomRowNum = cellsCompact.last._2.getRowNum
      //var time2 = System.currentTimeMillis()
      //logger.info("1 Time consumed:"+(time2-time1))
      val rowSize = cellsCompact.size
      val rightEndColumn = rowSize match {
        case 0 => -1
        case n =>
          val maxSize = cellsCompact.map(_._2.size).max
          cellsCompact.find(_._2.size == maxSize).map(_._2.last.getColumnIndex).get
      }
      //time1 = System.currentTimeMillis()
      //logger.info("2 Time consumed:"+(time1-time2))

      val cellsCompactMap=cellsCompact.toMap
      val cellsMatrix = (0 to bottomRowNum).map {
        rowIdx =>
          val row = cellsCompactMap.get(rowIdx) match {
            case Some(poir) => poir
            case None       => this.entity.createRow(rowIdx)
          }
          val cellsInRow = {
            val cr = row.cellIterator.toList
            cr.indices zip cr
          }.toMap
          (0 to rightEndColumn).map {
            colIdx =>
              cellsInRow.get(colIdx) match {
                case Some(poic) => Cell(poic, this) //create poi cell
                case None       => Cell(rowIdx, colIdx, this) //when no column index, create placeholder.
              }
          }
      }
      //time2 = System.currentTimeMillis()
      //logger.info("3 Time consumed:" + (time2 - time1))
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

    def getRange(rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int): Range = {
      Range(this, rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int)
    }
    def getRange(preRange: Range.PreRange): Range = {
      Range(this, preRange)
    }

    private def getCell(rowIdx: Int, columnIdx: Int) = {
      val row = entity.getRow(rowIdx) match {
        case null => entity.createRow(rowIdx)
        case r    => r
      }
      val cell = row.getCell(columnIdx) match {
        case null => row.createCell(columnIdx)
        case c    => c
      } //above: get poi cell
      Cell(cell, this)
    }

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