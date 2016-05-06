package com.github.cuzfrog.excel

import scala.collection.JavaConversions

trait Row {
  def sheet: Sheet
  def index: Int
  def cells: Seq[Cell]
  def getCell(columnIdx: Int): Cell
  def cellsCnt: Int = cells.size
  def maxColumnIdx: Int
}

object Row {
  def apply(entity: org.apache.poi.ss.usermodel.Row, sheet: Sheet): Row = new PoiRow(entity, sheet)
  def apply(rowIdx: Int, sheet: Sheet): Row = new PlaceHolderRow(rowIdx, sheet)

  private class PoiRow(private val entity: org.apache.poi.ss.usermodel.Row, val sheet: Sheet) extends Row {
    val index: Int = entity.getRowNum
    import scala.collection.JavaConversions._
    val cells: Seq[Cell] = entity.cellIterator().toSeq.map(Cell(_, this))
    lazy val maxColumnIdx = cells.map(_.columnIdx).max
    def getCell(columnIdx: Int) = cells.find(_.columnIdx == columnIdx) match {
      case None       => Cell(index, columnIdx, sheet)
      case Some(cell) => cell
    }
  }

  private class PlaceHolderRow(val index: Int, val sheet: Sheet) extends Row {
    val cells = Seq()
    val maxColumnIdx = (-1)
    def getCell(columnIdx: Int) = Cell(index, columnIdx, sheet) //return a place holder
  }
}