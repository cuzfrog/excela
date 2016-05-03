package com.github.cuzfrog.excel

sealed trait Range {
  val cells: List[List[Cell]]
  val rowBegin: Int
  val rowEnd: Int
  val columnBegin: Int
  val columnEnd: Int
  /**
   * Copy a range's content to this range.
   * Content flag: use 8 bit to specify what should be copied.
   * 0x01=value,0x02=style
   *
   * Note: From left-up corner, only the intersection of the two ranges will be affected, i.e. the overlapped parts.
   * @return The partial range of this range intersected by from-range.
   */
  def copy(fromRange: Range, content: Byte = 0x01): Range
}

object Range {
  private[excel] def apply(sheet: Sheet, rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int): Range = {
    new RangeImpl(sheet: Sheet, rowBegin: Int, rowEnd: Int, columnBegin: Int, columnEnd: Int)
  }

  case class PreRange(val rowBegin: Int, val rowEnd: Int, val columnBegin: Int, val columnEnd: Int)

  private[excel] def apply(sheet: Sheet, preRange: PreRange): Range = {
    new RangeImpl(sheet, preRange.rowBegin, preRange.rowEnd, preRange.columnBegin, preRange.columnEnd)
  }

  private class RangeImpl(val sheet: Sheet, val rowBegin: Int, val rowEnd: Int, val columnBegin: Int, val columnEnd: Int) extends Range {
    require(rowBegin >= 0 && rowEnd >= rowBegin && columnBegin >= 0 && columnEnd >= columnBegin,
      s"Illegal index:${rowBegin},${rowEnd},${columnBegin},${columnEnd}")
    private[this] val sheetCells = sheet.cells
    require(rowEnd <= sheetCells.size && columnEnd <= sheetCells.head.size, s"Out of sheet bound:row${rowEnd},${columnEnd}")
    val cells = sheet.cells.slice(rowBegin, rowEnd + 1).map(_.slice(columnBegin, columnEnd + 1))

    def copy(fromRange: Range, content: Byte) = {
      val fromRowCnt = fromRange.rowEnd - fromRange.rowBegin + 1
      val fromColumnCnt = fromRange.columnEnd - fromRange.columnBegin + 1
      val minRowCnt = Math.min(cells.size, fromRowCnt)
      val minColCnt = Math.min(columnEnd - columnBegin + 1, fromColumnCnt)
      (0 to minRowCnt - 1).foreach {
        rowIdx =>
          val row = cells(rowIdx)
          val fromRow = fromRange.cells(rowIdx)
          (0 to minColCnt - 1).foreach {
            colIdx =>
              val cell = row(colIdx)
              val fromCell = fromRow(colIdx)
              if ((content & 0x01) > 0) {
                fromCell.getValue match {
                  case Some(v) => cell.setValue(v)
                  case None    => //do nothing
                }
              }
              if ((content & 0x02) > 0) fromCell.getStyle match {
                case Some(st) => cell.setStyle(st)
                case None     => //do nothing
              }
          }
      }

      Range(sheet, rowBegin, rowBegin + minRowCnt - 1, columnBegin, columnBegin + minColCnt - 1)
    }
  }

}