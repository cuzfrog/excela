package com.github.cuzfrog.excel

import java.io.File
import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileOutputStream
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import com.github.cuzfrog.utils.FileAssistant

sealed trait Workbook {
  private[excel] val entity: org.apache.poi.ss.usermodel.Workbook
  val sheets: List[Sheet]
  val path: String
  def close: Unit
  def save: Workbook
  def saveAs(path: String): Workbook
}

object Workbook {
  /**
   * @param renameSurfix if not specified, the excel file will not be saved as a new file.
   */
  def apply(path: String, renameSurfix: String = ""): Workbook = {
    require(path.matches(""".*\.xlsx?"""), "Not excel file:" + path)
    val (p, fn, s) = FileAssistant.pathParse(path)
    val toPath = p + fn + renameSurfix + s
    val poiWb = new PoiWorkbook(path)
    renameSurfix match {
      case "" => poiWb
      case s => poiWb.saveAs(toPath)
    }
  }

  private class PoiWorkbook(override val path: String) extends Workbook {
    val entity = {
      val fileis = new FileInputStream(path)
      val workbook = WorkbookFactory.create(fileis)
      fileis.close
      workbook
    }
    lazy val evaluator = entity.getCreationHelper().createFormulaEvaluator()
    override val sheets = entity.iterator.toList.map(Sheet(_, this))
    override def close = entity.close()
    override def save = {
      val outS = new FileOutputStream(path)
      evaluator.setIgnoreMissingWorkbooks(true)
      evaluator.evaluateAll()
      entity.write(outS)
      this
    }

    override def saveAs(path: String) = {
      val outS = new FileOutputStream(path)
      entity.write(outS)
      outS.close //close output stream
      this.close //close old book
      Workbook(path) //open new book
    }
  }
}