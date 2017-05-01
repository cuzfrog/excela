package com.github.cuzfrog.excel

import java.io.{FileInputStream, FileOutputStream}

import com.github.cuzfrog.utils.FileAssistant
import org.apache.poi.ss.usermodel.WorkbookFactory

import scala.collection.JavaConverters._

sealed trait Workbook {
  private[excel] val entity: org.apache.poi.ss.usermodel.Workbook
  val sheets: Seq[Sheet]
  val path: String
  def close(): Unit
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
      case _ => poiWb.saveAs(toPath)
    }
  }

  private class PoiWorkbook(override val path: String) extends Workbook {
    val entity = {
      val fileis = new FileInputStream(path)
      val workbook = WorkbookFactory.create(fileis)
      fileis.close()
      workbook
    }
    lazy val evaluator = entity.getCreationHelper.createFormulaEvaluator()
    override val sheets = entity.iterator.asScala.toSeq.map(Sheet(_, this))
    override def close(): Unit = entity.close()
    override def save: PoiWorkbook = {
      val outS = new FileOutputStream(path)
      evaluator.setIgnoreMissingWorkbooks(true)
      evaluator.evaluateAll()
      entity.write(outS)
      this
    }

    override def saveAs(path: String): Workbook = {
      val outS = new FileOutputStream(path)
      entity.write(outS)
      outS.close() //close output stream
      this.close() //close old book
      Workbook(path) //open new book
    }
  }
}