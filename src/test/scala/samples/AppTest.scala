package samples

import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference

import com.github.cuzfrog.excel.Workbook
import utest._


object AppTest extends TestSuite {

  val workbookRef = new AtomicReference[Workbook](null)

  val tests = this {
    'OpenWorkbook {
      workbookRef.set(Workbook("./src/test/resources/sample.xlsx"))
    }
    'PopulateSheet {
      workbookRef.get().sheets.foreach { sheet =>
        sheet.rows.foreach { row =>
          row.cells.foreach { cell =>
            cell.setValue(cell.getValue + "_p")
          }
        }
      }
    }
    'SaveWorkbook {
      workbookRef.get().saveAs(Files.createTempFile("tmp", ".xlsx").toString)
    }
  }

}


