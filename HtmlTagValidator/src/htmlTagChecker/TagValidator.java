package htmlTagChecker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator.IsEmpty;

import excel.Excel;

public class TagValidator 
{
	static String test_case_file_name;
	static Logger logger = LogManager.getRootLogger();
	
	public void executeTestCase()
	{
		XSSFSheet Sheet = new Excel().getSheet(test_case_file_name);
		
		for (Row row : Sheet)
		{
			if ( row.getRowNum() == 0 ) 
			{
				logger.info("First row is header so skipping...");
			}
			
			if (row.getRowNum() > 0 && row.getCell(1).getStringCellValue().equalsIgnoreCase("Y"))//Check Test_Flag column no 1
			{
				try
				{
//					for (Cell cell : row)
//					{
//						if (cell.getCellType() == 0) logger.info(cell.getColumnIndex() + " " + cell.getNumericCellValue());
//						if (cell.getCellType() == 1) logger.info(cell.getColumnIndex() + " " + cell.getStringCellValue());
//					}
					logger.info("******************************************************************************************************");
					logger.info("Executing Test Case No : "+ row.getCell(0).getNumericCellValue());
					
					Document doc = Jsoup.connect(row.getCell(3).getStringCellValue()).get();//Gets the HTML code of the given URL column no 3
					logger.info("Checking Url : " + row.getCell(3).getStringCellValue());
					
					Elements parent = doc.getElementsByClass(row.getCell(4).getStringCellValue());//getElementsByClass no 4
					logger.info("Class Is : " + row.getCell(4).getStringCellValue());
					
					Elements child = parent.select(row.getCell(5).getStringCellValue());////select (element inside getElementsByClass) column no 5
					logger.info("Element Is : " + row.getCell(5).getStringCellValue());
					
					String attribute = child.attr(row.getCell(6).getStringCellValue());
					logger.info("Attribute Is : " + row.getCell(6).getStringCellValue());
					
					if(parent.isEmpty())
					{
						logger.info("Class Not Found");
						setTestCaseStatus(row,"Class Not Found","FAIL");
					}
					
					if(child.isEmpty() && !parent.isEmpty())
					{
						logger.info("Tag Not Found");
						setTestCaseStatus(row,"Tag Not Found","FAIL");
					}
					
					if(attribute.isEmpty() && !child.isEmpty())
					{
						logger.info("Attribute Not Found");
						setTestCaseStatus(row,"Attribute Not Found","FAIL");
					}
					
					if (!attribute.isEmpty())
					{
						for (Element ele : child)
						{
							if(ele.attr(row.getCell(6).getStringCellValue()).length()==0)//attribute column no 6 
							{
								logger.info("Actual Output : IS EMPTY");
								logger.info("Test Case Status : FAIL");
								setTestCaseStatus(row,"EMPTY","FAIL");
							}

							if(ele.attr(row.getCell(6).getStringCellValue()).length() > 0)
							{
								logger.info("Actual Output : " + ele.attr(row.getCell(6).getStringCellValue()));
								logger.info("Status : PASS");
								setTestCaseStatus(row,ele.attr(row.getCell(6).getStringCellValue()),"PASS");
							}
						}
					}

				}
				catch (Exception e)
				{
					logger.info("Exception while executing test case row no " + row.getRowNum());
					e.printStackTrace();
					setTestCaseStatus(row,"ERROR","FAIL",e.toString());
				}
			}
		}
		try
		{
			FileOutputStream ResultToFile = new FileOutputStream(new File(test_case_file_name));
			Sheet.getWorkbook().write(ResultToFile);
			ResultToFile.close();
			Sheet.getWorkbook().close();
		}
		catch (Exception e)
		{
			logger.info("Exception while writting result to file");
			e.printStackTrace();
		}
	}
	
	public void setTestCaseStatus(Row r,String s1,String s2)
	{
		Cell actual_cell = r.createCell(7);
		actual_cell.setCellValue(s1);
		
		Cell status_cell = r.createCell(8);
		status_cell.setCellValue(s2);
	}
	
	public void setTestCaseStatus(Row r,String s1,String s2,String s3)
	{
		Cell actual_cell = r.createCell(7);
		actual_cell.setCellValue(s1);
		
		Cell status_cell = r.createCell(8);
		status_cell.setCellValue(s2);
		
		Cell exception_cell = r.createCell(9);
		exception_cell.setCellValue(s3);
	}
	
	public static void main(String[] args) 
	{
		logger.info("Start");
		try 
		{
			test_case_file_name = args[0];
					
			TagValidator tv = new TagValidator();
			tv.executeTestCase();
		} 
		catch (Exception e) 
		{
			logger.info("Exception in main");
			e.printStackTrace();
		}
	}
}