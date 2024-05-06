package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FelgiScraper {
    public static void main(String[] args) {
        String todayDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        System.out.println(todayDate);

        String velgenland_url = "https://www.velgenland.nl/velgen/";
        String bandenleader_url = "https://www.bandenleader.nl/merken-van-aluminium-velgen/";
        String oponeo_alu_url = "https://www.oponeo.nl/lichtmetalen-velgen";
        String oponeo_stal_url = "https://www.oponeo.nl/stalen-velgen";
        String excelFilePath = "Konkurencjoneo HOLANDIA " + todayDate + ".xlsx";

        try {
            Workbook workbook = new XSSFWorkbook();

            Sheet velgenland_sheet = workbook.createSheet("VelgenLand.nl");
            Sheet bandeleader_sheet = workbook.createSheet("Bandenleader.nl");
            Sheet oponeo_alu_sheet = workbook.createSheet("Felgi Aluminiowe Oponeo NL");
            Sheet oponeo_stal_sheet = workbook.createSheet("Felgi Stalowe Oponeo NL");

            int rowCountVelgenland = 0;
            Row headerRowVelgenland = velgenland_sheet.createRow(rowCountVelgenland++);
            headerRowVelgenland.createCell(0).setCellValue("Marka");
            headerRowVelgenland.createCell(1).setCellValue("Model");
            headerRowVelgenland.createCell(2).setCellValue("Rozmiar");
            headerRowVelgenland.createCell(3).setCellValue("Cena");

            int rowCountBandenleader = 0;
            Row headerRowBandenleader = bandeleader_sheet.createRow(rowCountBandenleader++);
            headerRowBandenleader.createCell(0).setCellValue("Marka");
            headerRowBandenleader.createCell(1).setCellValue("Model");
            headerRowBandenleader.createCell(2).setCellValue("Rozmiar");
            headerRowBandenleader.createCell(3).setCellValue("Cena");
            headerRowBandenleader.createCell(4).setCellValue("Link");

            int rowCountOponeoAlu = 0;
            Row headerRowOponeoAlu = oponeo_alu_sheet.createRow(rowCountOponeoAlu++);
            headerRowOponeoAlu.createCell(0).setCellValue("Marka");
            headerRowOponeoAlu.createCell(1).setCellValue("Model");
            headerRowOponeoAlu.createCell(2).setCellValue("Cena");
            headerRowOponeoAlu.createCell(3).setCellValue("Rozmiar");

            int rowCountOponeoStal = 0;
            Row headerRowOponeoStal = oponeo_stal_sheet.createRow(rowCountOponeoStal++);
            headerRowOponeoStal.createCell(0).setCellValue("Marka");
            headerRowOponeoStal.createCell(1).setCellValue("Model");
            headerRowOponeoStal.createCell(2).setCellValue("Cena");

            // Scraping Velgenland.nl
//            Document velgenland_doc = Jsoup.connect(velgenland_url).get();
//            Elements brandLinksVelgenland = velgenland_doc.select(".c-brands__link");
//
//            for (Element brandLink : brandLinksVelgenland) {
//                String brandURL = "https://www.velgenland.nl" + brandLink.attr("href");
//                String brandName = brandLink.select(".c-brands__title").text();
//
//                try {
//                    Document brandDoc = Jsoup.connect(brandURL).get();
//                    Elements brandRims = brandDoc.select(".c-results .c-card");
//
//                    System.out.println("Velgenland: Liczba felg dla marki " + brandName + ": " + brandRims.size());
//
//                    for (Element rim : brandRims) {
//                        String model = rim.select(".c-card__title").text();
//                        String priceContainer = rim.select(".c-card__price").text();
//                        String price = priceContainer;
//                        String sizes = rim.select(".c-card__specs .c-card__specs__item").text();
//
//                        Row row = velgenland_sheet.createRow(rowCountVelgenland++);
//                        row.createCell(0).setCellValue(brandName);
//                        row.createCell(1).setCellValue(model);
//                        row.createCell(2).setCellValue(sizes);
//                        row.createCell(3).setCellValue(price + "€");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            // Scraping Bandenleader.nl
            Document bandenleader_doc = Jsoup.connect(bandenleader_url).get();
            Elements rimLinksBandenleader = bandenleader_doc.select(".row.row-list > .col-xs-6.col-sm-3.col-md-2 > a");

            Set<String> addedModels = new HashSet<>();

            for (Element rimLink : rimLinksBandenleader) {
                String rimURL = rimLink.attr("abs:href");
                Document rimPage = Jsoup.connect(rimURL).get();
                Elements rimDetails = rimPage.select(".thumbnail.tCenter");
                Elements priceDetails = rimPage.select(".thumbnail.tCenter > a");

                for (int i = 0; i < priceDetails.size(); i++) {
                    Element rimLink1 = priceDetails.get(i);
                    String rimURL1 = rimLink1.attr("abs:href");
                    Document rimPage1 = Jsoup.connect(rimURL1).get();
                    String price = rimPage1.select(".prix").text().split(" ")[0];

                    System.out.println("Visiting rim page: " + rimURL1);
                    System.out.println("Rim page title: " + rimPage1.title());

                    Element rimDetail = rimDetails.get(i);
                    String brandName = rimDetail.select(".upper.bold").text();
                    String modelName = rimDetail.select(".upper.bold").text();
                    Elements sizes = rimDetail.select(".label.label-warning");

                    for (Element size : sizes) {
                        String sizeText = size.text();
                        if (!addedModels.contains(modelName + sizeText)) {
                            Row row = bandeleader_sheet.createRow(rowCountBandenleader++);
                            row.createCell(0).setCellValue(brandName);
                            row.createCell(1).setCellValue(modelName);
                            row.createCell(2).setCellValue(sizeText);
                            row.createCell(3).setCellValue("Zaczyna się od: "+price);
                            row.createCell(4).setCellValue("Link do felgi"+rimURL1); // Write the URL into the Excel file
                            addedModels.add(modelName + sizeText);
                        }
                    }
                }
            }
            // Scraping Oponeo.nl
            Document oponeoAluDoc = Jsoup.connect(oponeo_alu_url).get();
            Elements rimsOponeoAlu = oponeoAluDoc.select(".column-list .product");

            for (Element rim : rimsOponeoAlu) {
                String brand = rim.getElementsByClass("producer").text();
                String model = rim.getElementsByClass("model").text();
                String price = rim.getElementsByClass("price").text();
                Elements sizes = rim.getElementsByClass("diametersList").select("a");

                for (Element size : sizes) {
                    String sizeText = size.text();
                    Row row = oponeo_alu_sheet.createRow(rowCountOponeoAlu++);
                    row.createCell(0).setCellValue(brand);
                    row.createCell(1).setCellValue(model);
                    row.createCell(2).setCellValue(price);
                    row.createCell(3).setCellValue(sizeText);
                }
            }

            Document oponeoStalDoc = Jsoup.connect(oponeo_stal_url).get();
            Elements rimsOponeoStal = oponeoStalDoc.select(".column-list .product");

            for (Element rim : rimsOponeoStal) {
                String brand = rim.getElementsByClass("producer").text();
                String model = rim.getElementsByClass("model").text();
                String price = rim.getElementsByClass("price").text();

                Row row = oponeo_stal_sheet.createRow(rowCountOponeoStal++);
                row.createCell(0).setCellValue(brand);
                row.createCell(1).setCellValue(model);
                row.createCell(2).setCellValue(price);
            }

            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            System.out.println("Pobrano dane felg i zapisano do pliku Excel.");

        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas pobierania zawartości strony:");
            e.printStackTrace();
        }
    }
}
