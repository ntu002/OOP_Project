package crawl.site;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class crawlHistorySite {
    private final ArrayList<String> siteLinks;

    public ArrayList<String> getSiteLinks() {
        return siteLinks;
    }

    // Lay tat ca cac link di tich de crawl
    public void getAllSiteLinks() {
        System.out.println("Lay cac link cua phan pagination: ");

        try {
            Document doc = Jsoup.connect("https://nguoikesu.com/di-tich-lich-su?types[0]=1").timeout(120000).get();

            // p tag nay cho biet co tong bnh trang trong pagination
            int numberOfPagination = 1;
            Element pTag = doc
                    .selectFirst("p[class=counter float-end pt-3 pe-2]");
            if (pTag != null) {
                String[] pTagContentArray = pTag.text().split(" ");
                int pTagContentArrSize = pTagContentArray.length;
                numberOfPagination = Integer.parseInt(pTagContentArray[pTagContentArrSize - 1]);
                System.out.println("Total number of paginate page: " + numberOfPagination);
            }

            // Lay link tu trang dau tien
            Element siteUl = doc.selectFirst("ul[class=com-tags-tag__category category list-group]");
            Elements siteLi = siteUl.select("li");

            for (Element li : siteLi) {
                // Lay the li dau tien co duoc
                Element liATag = li.selectFirst("a");
                if (liATag != null) {
                    String link = "https://nguoikesu.com" + liATag.attr("href");

                    if (!siteLinks.contains(link)) {
                        System.out.println(liATag.text() + " - " + link);
                        siteLinks.add(link);
                    }
                }
            }

            // 1 trang co <= 10 di tich
            // Format link: "https://nguoikesu.com/di-tich-lich-su?types[0]=1&start=..." => query
            if (numberOfPagination > 1) {
                for (int i = 2; i <= numberOfPagination; ++i) {
                    System.out.println("\nCurrent page: " + i);
                    String link = "https://nguoikesu.com/di-tich-lich-su?types[0]=1&start=" + (i - 1) * 10;

                    try {
                        Document pagiDoc = Jsoup.connect(link).timeout(120000).get();

                        // Lay tu trang thu 2 den het
                        Element pagiSiteUl = pagiDoc.selectFirst("ul[class=com-tags-tag__category category list-group]");
                        Elements pagiSiteLi = pagiSiteUl.select("li");

                        for (Element li : pagiSiteLi) {
                            // Lay the li dau tien co duoc
                            Element liATag = li.selectFirst("a");
                            if (liATag != null) {
                                String pagilink = "https://nguoikesu.com" + liATag.attr("href");

                                if (!siteLinks.contains(pagilink)) {
                                    System.out.println(liATag.text() + " - " + pagilink);
                                    siteLinks.add(pagilink);
                                }
                            }
                        }
                    } catch (IOException err) {
                        throw new RuntimeException(err);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Cac truong hop cua dia diem
    public boolean locationCheck(String location) {
        return location.equals("Khu v???c") ||
                location.equals("?????a ch???") ||
                location.equals("?????a ??i???m") ||
                location.equals("V??? tr??");
    }

    // Cac truong hop cua thoi gian xay dung, hoan thanh
    public boolean timeCheck(String time) {
        return time.equals("Kh???i c??ng") ||
                time.equals("Ho??n th??nh") ||
                time.equals("Th??nh l???p") ||
                time.equals("Kh???i l???p");
    }

    public void crawlSiteData(String link) {
        System.out.println("\nDang crawl di tich o link: " + link);

        try {
            Document doc = Jsoup.connect(link).timeout(120000).get();

            String name = "Ch??a r??"; // Ten di tich
            String time = "Ch??a r??"; // Thoi gian khoi cong / xay dung / xuat hien
            String description = "Ch??a r??"; // Mo ta ngan
            String location = "Ch??a r??"; // Dia diem
            String backgroundStory = "Ch??a r??"; // Truyen thuyet ???
            String founder = "Ch??a r??"; // Nguoi sang lap?
            String festival = "Ch??a r??"; // Le hoi (neu co)
            ArrayList<String> relatedChar = new ArrayList<>(); // Nhan vat lien quan
            ArrayList<String> relatedEvent = new ArrayList<>(); // Su kien / le hoi lien quan?

            // Lay bang thong tin (neu co)
            Element infoTable = doc.selectFirst("table[class=infobox]");

            if (infoTable != null) {
                Elements infoTableRows = infoTable.select("tr");
                int numberOfTr = infoTableRows.size();

                for (int i = 0; i < numberOfTr; ++i) {
                    Element tableHead = infoTableRows.get(i).selectFirst("th");

                    if (tableHead != null) {
                        String tableHeadContent = tableHead.text();
                        if (i == 0) {
                            // Ten cua dia danh
                            name = tableHeadContent;
                        } else {
                            // Neu chua co gia tri cho dia diem va th dag chi dia diem
                            if (locationCheck(tableHeadContent) && location.equals("Ch??a r??")) {
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                if (tableData != null) {
                                    location = tableData.text();
                                }
                            } else if (timeCheck(tableHeadContent)) {
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                if (tableData != null) {
                                    if (time.equals("Ch??a r??")) {
                                        // Thoi gian khoi tao
                                        time = tableData.text();
                                    } else {
                                        // Thoi gian hoan thanh (neu co)
                                        time = time + " - " + tableData.text();
                                    }
                                }
                            } else if (tableHeadContent.equals("Ng?????i s??ng l???p") && founder.equals("Ch??a r??")) {
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                if (tableData != null) {
                                    founder = tableData.text();
                                }
                            }
                        }
                    }
                }
            }

            // Loc text
            Element contentBody = doc.selectFirst("div[class=com-content-article__body]");
            // Lay cac the con cua contentBody => loc text tu cac the p
            Elements contentBodyElements = contentBody.children();
            boolean isFirstP = false; // Kiem tra xem co phai the p dau tien tim duoc k
            // Hau nhu lay thong tin tong quat the p dau tien

            for (Element item : contentBodyElements) {
                if (item.tagName().equals("p")) {
                    Element firstParagraph = item;
                    String firstPContent = firstParagraph.text();
                    Pattern p;
                    Matcher m;

                    // The b dau tien la ten cua nhan vat?
                    if (name.equals("Ch??a r??")) {
                        Element firstBTag = firstParagraph.selectFirst("b");
                        if (firstBTag != null) {
                            name = firstBTag.text();
                        }
                    }

                    // Lay mo ta ngan gon cua di tich
                    // Loai bo doan trong ngoac dau tien cho chac :D
                    if (description.equals("Ch??a r??")) {
                        int firstOpenParen = firstPContent.indexOf("(");
                        int firstCloseParen = firstPContent.indexOf(")");
                        if (firstOpenParen != -1 && firstCloseParen != -1) {
                            String firstParen = firstPContent.substring(firstOpenParen + 1, firstCloseParen);
                            if (firstParen.contains("l??")) {
                                firstPContent = firstPContent.substring(firstCloseParen + 1);
                            }
                        }

                        boolean outLoop = true;
                        while (outLoop) {
                            // kiem tra sau chu "la" la gi?
                            int start = firstPContent.indexOf("l??"); // tra ve vi tri chu l cua "la" dau tien tim thay
                            if (start != -1 && start < firstPContent.length() - 3) {
                                // Neu la chu in hoa || hoac la con, lang? ... => bo TH chu la nay di :v
                                if (
                                        Character.isUpperCase(firstPContent.charAt(start + 3)) ||
                                                firstPContent.charAt(start + 2) != ' '
                                ) {
                                    // Con truong hop la mot
                                    // Truong hop chu lam
                                    if (firstPContent.charAt(start + 2) == 'm' && firstPContent.charAt(start + 3) == ' ') {
                                        outLoop = false;
                                    } else firstPContent = firstPContent.substring(start + 3);
                                } else outLoop = false;
                            } else outLoop = false;
                        }

                        p = Pattern.compile("(l??|bao g???m|n??i)[^.]*[.]", Pattern.CASE_INSENSITIVE);
                        m = p.matcher(firstPContent);

                        if (m.find()) {
                            String result = m.group(0);
                            if (!result.contains(":")) {
                                description = result.substring(0, result.length() - 1);
                            }
                        }
                    }

                    // Lay ra dia chi cua di tich
                    if (location.equals("Ch??a r??")) {
                        firstPContent = firstParagraph.text();

                        do {
                            p = Pattern.compile("(((n???m|t???a l???c|d???ng|x??y d???ng) (???|tr??n))|(thu???c))[^.]*[.]", Pattern.CASE_INSENSITIVE);
                            m = p.matcher(firstPContent);

                            if (m.find()) {
                                String findResult = m.group(0);
                                if (findResult.contains("tri???u ?????i")) {
                                    // Bo qua chu thuoc hien tai?
                                    int skipIndex = firstPContent.indexOf("tri???u ?????i");
                                    firstPContent = firstPContent.substring(skipIndex + 9);
                                } else {
                                    location = findResult.substring(0, findResult.length() - 1);
                                    break;
                                }
                            } else break;
                        } while (true);
                    }

                    // Lay thoi gian xay dung
                    if (time.equals("Ch??a r??")) {
                        firstPContent = firstParagraph.text();
                        p = Pattern.compile("(x??y d???ng) (t???|v??o|trong)[^.]*[.(]", Pattern.CASE_INSENSITIVE);
                        m = p.matcher(firstPContent);

                        if (m.find()) {
                            String findResult = m.group(0);
                            time = findResult.substring(0, findResult.length() - 1);
                        }
                    }

                    // Lay ra le hoi (neu co)
                    if (festival.equals("Ch??a r??")) {
                        p = Pattern.compile("(L??? h???i)[^.]*[.]");
                        m = p.matcher(firstPContent);

                        if (m.find()) {
                            String findResult = m.group(0);
                            festival = findResult.substring(0, findResult.length() - 1);
                        }
                    }

                    // Lay ra truyen thuyet
                    if (backgroundStory.equals("Ch??a r??")) {
                        p = Pattern.compile("(truy???n thuy???t)[^.]*[.]", Pattern.CASE_INSENSITIVE);
                        m = p.matcher(firstPContent);

                        if (m.find()) {
                            String findResult = m.group(0);
                            backgroundStory = findResult.substring(0, findResult.length() - 1);
                        }
                    }

                    // Lay ra cac the a tim duoc trong p => tim nhan vat lien quan
                    Elements pATags = item.select("a");
                    for (Element a : pATags) {
                        String hrefValue = a.attr("href");
                        if (hrefValue.contains("/nhan-vat") && !hrefValue.contains("nha-")) {
                            String aTagValue = a.text();
                            if (!relatedChar.contains(aTagValue)) {
                                relatedChar.add(aTagValue);
                            }
                        }
                    }
                }
            }

            System.out.println("Name: " + name);
            System.out.println("Built date: " + time);
            System.out.println("Location: " + location);
            System.out.println("Founder: " + founder);
            System.out.println("Related Myth: " + backgroundStory);
            System.out.println("Related festival: " + festival);
            System.out.println("Description: " + description);
            System.out.print("Related characters: [");
            if (relatedChar.size() > 0) {
                for (int i = 0; i < relatedChar.size(); ++i) {
                    if (i < relatedChar.size() - 1) {
                        System.out.print(relatedChar.get(i) + ", ");
                    } else System.out.print(relatedChar.get(i) + "]\n");
                }
            } else {
                System.out.print("]\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getDataFromLink() {
        for (String link : siteLinks) {
            crawlSiteData(link);
        }
    }

    public void crawlData() {
        getAllSiteLinks();
        getDataFromLink();
    }

    public crawlHistorySite() {
        this.siteLinks = new ArrayList<>();
        crawlData();
    }
}