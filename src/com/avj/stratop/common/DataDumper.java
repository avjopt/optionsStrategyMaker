package com.avj.stratop.common;

//Java program to write data in excel sheet using java code

import java.io.File;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.avj.stratop.shortstrangle.ShortStrangleOutput;

import lombok.Getter;
import lombok.Setter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Getter
@Setter
public class DataDumper {

	private String filename;
	List<ShortStrangleOutput> shortStrangleList = null;

	public void setShortStrangleResult(List<ShortStrangleOutput> ssolist) {
		this.shortStrangleList = ssolist;
	}

	private void setFilename(String strategyName) {
		Date date = new Date();
		String dstr = date.toString();
		this.filename = strategyName + "_" + dstr.replace(" ", "_").replace(":", "_");
	}

	public void dump(String stanalysis) {
		setFilename(stanalysis);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet spreadsheet = workbook.createSheet(stanalysis);
		XSSFRow row;
		Map<String, Object[]> ssd = new TreeMap<>();
		ssd.put("Spot", new Object [] {"Spot","CallAtExpiry","Aya","Diya","PL","PutAtExpiry","Aya","Diya","PL","NetPL","PLPerLot","ROI"});
		for (ShortStrangleOutput sso : shortStrangleList) {
			ssd.put(sso.getSpot().toString(),
					new Object[] { sso.getSpot(), sso.getCalldes(), sso.getCallAya(), sso.getCallDiya(),
							sso.getCallPandL(), sso.getPutdes(), sso.getPutAya(), sso.getPutDiya(), sso.getPutPandL(),
							sso.getSpotPandL(), sso.getSpotPandLPerLot(), sso.getRoi() });
		}
		Set<String> keyid = ssd.keySet();
		int rowid = 0;
		for (String key : keyid) {
			row = spreadsheet.createRow(rowid++);
			Object[] objectArr = ssd.get(key);
			int cellid = 0;
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				if (obj instanceof Double) {
					cell.setCellValue(((Double) obj).doubleValue());
				} else {
					cell.setCellValue(obj.toString());
				}
			}
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(new File("C:\\chartjs\\" + filename + ".xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
