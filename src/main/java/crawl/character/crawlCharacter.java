package crawl.character;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.regex.*;

public class crawlCharacter {
    // Link luu trang cua nhan vat lich su
    private static ArrayList<String> charInfoLinks;

    public static ArrayList<String> getCharInfoLinks() {
        return charInfoLinks;
    }

    public crawlCharacter() {
        this.charInfoLinks = new ArrayList<>();
        crawlData();
    }

    // Lay link info cua cac nhan vat tu /nhan-vat
    public void getCharInfoPageLink() {
//        ArrayList<String> paginateLinks = new ArrayList<>();
//        paginateLinks.add("https://nguoikesu.com/nhan-vat");
        System.out.println("\nLay link nhan vat tu /nhan-vat: ");

        try {
            Document doc = Jsoup.connect("https://nguoikesu.com/nhan-vat").timeout(120000).get();

            // p tag nay cho biet co tong bnh trang trong pagination
            Element pTag = doc
                    .selectFirst("p[class=com-content-category-blog__counter counter float-end pt-3 pe-2]");
            String[] pTagContentArray = pTag.text().split(" ");
            int pTagContentArrSize = pTagContentArray.length;
            int numberOfPagination = Integer.parseInt(pTagContentArray[pTagContentArrSize - 1]);
            System.out.println("Total number of paginate page: " + numberOfPagination);

            // Lay cac link tu trang dau tien truoc => sau do lay tu trang thu 2 den het
            Elements pageHeaders = doc.select("div[class=page-header]");
//            System.out.println(pageHeaders.size());

            for (Element pageHeader : pageHeaders) {
                Element pageHeaderATag = pageHeader.selectFirst("a");
                if (pageHeaderATag != null) {
                    String link = "https://nguoikesu.com" + pageHeaderATag.attr("href");

                    // 1 so truong hop co cac trieu dai nen khong tinh vao nhan vat
                    if (!link.contains("nha-")) {
                        // Neu link nay chua co trong mang => tranh TH lap
                        if (!charInfoLinks.contains(link)) {
                            System.out.println(pageHeader.text() + " - " + link);
                            charInfoLinks.add(link);
                        }
                    }
                }
            }

            // 1 trang co <= 5 nhan vat
            // Format link: "https://nguoikesu.com/nhan-vat?start=..." => query
            for (int i = 2; i <= numberOfPagination; ++i) {
                System.out.println("\nCurrent page: " + i);
                String link = "https://nguoikesu.com/nhan-vat?start=" + String.valueOf((i - 1) * 5);

                try {
                    Document pagiDoc = Jsoup.connect(link).timeout(120000).get();

                    // Lay tu trang thu 2 den het
                    Elements pagiPageHeaders = pagiDoc.select("div[class=page-header]");

                    for (Element pagiPageHeader : pagiPageHeaders) {
                        Element pagiPHATag = pagiPageHeader.selectFirst("a");

                        if (pagiPHATag != null) {
                            String pagiLink = "https://nguoikesu.com" + pagiPHATag.attr("href");

                            // 1 so truong hop co cac trieu dai nen khong tinh vao nhan vat
                            if (!pagiLink.contains("nha-")) {
                                // Neu link nay chua co trong mang => tranh TH lap
                                if (!charInfoLinks.contains(pagiLink)) {
                                    System.out.println(pagiPageHeader.text() + " - " + pagiLink);
                                    charInfoLinks.add(pagiLink);
                                }
                            }
                        }
                    }
                } catch (IOException err) {
                    throw new RuntimeException(err);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Kiem tra chuc vu cua nhan vat
    public boolean positionCheck(String position) {
        return position.contains("Ho??ng ?????") ||
                position.contains("Ho??ng h???u") ||
                position.contains("Vua") ||
                position.contains("V????ng") ||
                position.equals("C??ng vi???c") ||
                position.equals("Ngh??? nghi???p") ||
                position.equals("C???p b???c") ||
                position.equals("????n v???") ||
                position.equals("Ch???c quan cao nh???t") ||
                position.equals("Ch???c v???") ||
                position.equals("V??? tr??");
    }

    public boolean workTimeCheck(String workTime) {
        return workTime.equals("Tr??? v??") ||
                workTime.equals("T???i v???") ||
                workTime.equals("Nhi???m k???") ||
                workTime.equals("N??m t???i ng??") ||
                workTime.equals("Ho???t ?????ng");
    }

    public boolean fatherCheck(String father) {
        return father.equals("Th??n ph???") ||
                father.equals("Cha") ||
                father.equals("B??? m???");
    }

    public boolean motherCheck(String mother) {
        return mother.equals("Th??n m???u") ||
                mother.equals("M???") ||
                mother.equals("B??? m???");
    }

    public boolean eraCheck(String era) {
        return era.equals("Ho??ng t???c") ||
                era.equals("Tri???u ?????i") ||
                era.equals("Gia t???c") ||
                era.equals("K??? nguy??n");
    }

    public boolean birthCheck(String birth) {
        return birth.equals("Ng??y sinh") ||
                birth.equals("Sinh");
    }

    // Truy cap vao link nhan vat va crawl
    public void crawlCharInfo(String link) {
        System.out.println("\nDang crawl nhan vat o link: " + link);
        try {
            Document doc = Jsoup.connect(link).timeout(120000).get();

            String charName = "Ch??a r??"; // ten
            String charMother = "Ch??a r??"; // me
            String charFather = "Ch??a r??"; // cha
            String dateOfBirth = "Ch??a r??"; // ngay sinh
            String lostDate = "Ch??a r??"; // ngay mat
            String preceeded = "Ch??a r??"; // tien nhiem
            String succeeded = "Ch??a r??"; // ke nhiem
            String era = "Ch??a r??"; // trieu dai ?
            String workTime = "Ch??a r??"; // thoi gian tai chuc
            String position = "Ch??a r??"; // chuc vu

            // Lay ra bang thong tin (neu co) => neu k co thi loc text
            Element infoTable = doc.selectFirst("table[class^=infobox]");

            if (infoTable != null) {
                Elements infoTableRows = infoTable.select("tr");
                int numberOfTr = infoTableRows.size();
                for (int i = 0; i < numberOfTr; ++i) {
                    // Chi muc dau tien la ten
                    if (i == 0) {
                        Element tableHead = infoTableRows.get(i).selectFirst("th");
                        // Chua co cach tach xau
                        if (tableHead != null) {
                            charName = tableHead.text();
                        }
                    } else {
                        Element tableHead = infoTableRows.get(i).selectFirst("th");

                        // Trong khi crawl co 1 so nhan vat co nhieu chuc vu, tien nhiem
                        // ke nhiem,... => lay nhung cai dau crawl duoc, bo nhung cai sau
                        if (tableHead != null) {
                            String tableHeadContent = tableHead.text();

                            // Chuc vu se dung truoc thoi gian tai vi hoac nhiem ky =>
                            // Check neu sau no la nhiem ky / tgian tai chuc thi do la chuc vu
                            // 1 khi co vi tri r thi thoi khong chay if nay nua
                            if (i < numberOfTr - 1 && (position.equals("Ch??a r??") || positionCheck(position))) {
                                Element nextTableHead = infoTableRows.get(i + 1).selectFirst("th");

                                if (nextTableHead != null) {
                                    if (workTimeCheck(nextTableHead.text())) {
                                        if (!tableHeadContent.equals("Thu???c")) {
                                            position = tableHeadContent;
                                        }
                                        continue;
                                    }
                                }
                            }

                            if (positionCheck(tableHeadContent) && (position.equals("Ch??a r??") || position.equals("Thu???c"))) { // Neu trong xau co hoang de,... => chuc vu
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                if (tableData != null) {
                                    position = tableData.text();
                                } else position = tableHeadContent;
                            } else if (workTimeCheck(tableHeadContent) && workTime.equals("Ch??a r??")) { // thoi gian tai chuc
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                workTime = tableData.text();
                            } else if (tableHeadContent.equals("Ti???n nhi???m") && preceeded.equals("Ch??a r??")) { // Preceeded
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                // Co cac truong hop
                                // Dau tien la co font
                                // 2 la co the a
                                // 3 chac la k co j?
                                // ...
                                preceeded = tableData.text();
                            } else if (tableHeadContent.equals("K??? nhi???m") && succeeded.equals("Ch??a r??")) { // Succeeded
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                succeeded = tableData.text();
                            } else if (eraCheck(tableHeadContent) && era.equals("Ch??a r??")) {
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                era = tableData.text();
                            } else if (fatherCheck(tableHeadContent) && charFather.equals("Ch??a r??")) { // Father
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                charFather = tableData.text();
                            } else if (motherCheck(tableHeadContent) && charMother.equals("Ch??a r??")) { // Mother
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                charMother = tableData.text();
                            } else if (birthCheck(tableHeadContent) && dateOfBirth.equals("Ch??a r??")) { // Birth
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                dateOfBirth = tableData.text();
                            } else if (tableHeadContent.equals("M???t") && lostDate.equals("Ch??a r??")) { // Lost
                                Element tableData = infoTableRows.get(i).selectFirst("td");
                                lostDate = tableData.text();
                            }
                        } else {
                            Elements numberOfTd = infoTableRows.get(i).select("td");

                            // Neu la the img
                            if (numberOfTd.size() < 2) {
                                continue;
                            }

                            // Co the co truong hop no la the td thay vi th
                            Element tableDataAlter = infoTableRows.get(i).selectFirst("td");

                            if (tableDataAlter != null) {
                                String tableDataAlterContent = tableDataAlter.text();

                                if (i < numberOfTr - 1 && (position.equals("Ch??a r??") || positionCheck(position))) {
                                    Element nextTableData = infoTableRows.get(i + 1).selectFirst("td");

                                    if (nextTableData != null) {
                                        if (workTimeCheck(nextTableData.text())) {
                                            if (!tableDataAlterContent.equals("Thu???c")) {
                                                position = tableDataAlterContent;
                                            }
                                            continue;
                                        }
                                    }
                                }

                                if (positionCheck(tableDataAlterContent) && (position.equals("Ch??a r??") || position.equals("Thu???c"))) { // Neu trong xau co hoang de,... => chuc vu
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    if (tableData != null) {
                                        position = tableData.text();
                                    } else position = tableDataAlterContent;
                                } else if (workTimeCheck(tableDataAlterContent) && workTime.equals("Ch??a r??")) { // thoi gian tai chuc
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    workTime = tableData.text();
                                } else if (tableDataAlterContent.equals("Ti???n nhi???m") && preceeded.equals("Ch??a r??")) { // Preceeded
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    // Co cac truong hop
                                    // Dau tien la co font
                                    // 2 la co the a
                                    // 3 chac la k co j?
                                    // ...
                                    preceeded = tableData.text();
                                } else if (tableDataAlterContent.equals("K??? nhi???m") && succeeded.equals("Ch??a r??")) { // Succeeded
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    succeeded = tableData.text();
                                } else if (eraCheck(tableDataAlterContent) && era.equals("Ch??a r??")) {
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    era = tableData.text();
                                } else if (fatherCheck(tableDataAlterContent) && charFather.equals("Ch??a r??")) { // Father
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    charFather = tableData.text();
                                } else if (motherCheck(tableDataAlterContent) && charMother.equals("Ch??a r??")) { // Mother
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    charMother = tableData.text();
                                } else if (birthCheck(tableDataAlterContent) && dateOfBirth.equals("Ch??a r??")) { // Birth
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    dateOfBirth = tableData.text();
                                } else if (tableDataAlterContent.equals("M???t") && lostDate.equals("Ch??a r??")) { // Lost
                                    Element tableData = infoTableRows.get(i).select("td").get(1);
                                    lostDate = tableData.text();
                                }
                            }
                        }
                    }
                }
            }

            Element contentBody = doc.selectFirst("div[class=com-content-article__body]");

            // Thuong thong tin se nam o the p dau tien
            Elements contentBodyElements = contentBody.children();
//                Element firstParagraph = contentBody.selectFirst("p");

            for (Element item : contentBodyElements) {
                if (item.tagName().equals("p")) {
                    Element firstParagraph = item;
                    // Lay cac the a la con the p
                    Elements pATags = firstParagraph.select("a");

                    // The b dau tien la ten cua nhan vat?
                    Element firstBTag = firstParagraph.selectFirst("b");
                    if (firstBTag != null) {
                        if (charName.equals("Ch??a r??")) charName = firstBTag.text();
                    }

                    // Tim ngay sinh
                    String firstPContent = firstParagraph.text();
                    Pattern birthRegex = Pattern.compile("\\(([^)]*)\\)", Pattern.UNICODE_CASE);
                    Matcher birthMatch = birthRegex.matcher(firstPContent);

                    while (birthMatch.find()) {
                        String firstResult = birthMatch.group(0);

                        // Lay ra doan xau co format (...) => lay ...
                        // Truong hop doan trong ngoac khong phai ngay sinh
                        Pattern checkValid = Pattern.compile("sinh|th??ng|n??m|-|???");
                        Matcher matchValid = checkValid.matcher(firstResult);

                        if (matchValid.find()) {
                            // Loai bo phan chu Han: ...,/; ... => lay phan ... sau
                            int startIndex = firstResult.lastIndexOf(',');
                            if (startIndex == -1) {
                                startIndex = firstResult.lastIndexOf(';');
                                if (startIndex == -1) {
                                    startIndex = firstResult.lastIndexOf('???');
                                    if (startIndex == -1) {
                                        startIndex = 1;
                                    } else startIndex++;
                                } else startIndex++;
                            } else startIndex++;

                            String contentInParen = firstResult.substring(startIndex, firstResult.length() - 1);
                            // Chia ra nam sinh voi nam mat
                            String[] splitString = {};
                            if (contentInParen.contains("-")) {
                                splitString = contentInParen.split("-");
                            } else {
                                splitString = contentInParen.split("???");
                            }

                            if (splitString.length == 1) {
                                if (dateOfBirth.equals("Ch??a r??")) dateOfBirth = splitString[0].trim();
                            } else {
                                if (dateOfBirth.equals("Ch??a r??")) dateOfBirth = splitString[0].trim();
                                if (lostDate.equals("Ch??a r??")) lostDate = splitString[1].trim();
                            }
                            break;
                        } else {
                            int start = firstPContent.indexOf(')');
                            firstPContent = firstPContent.substring(start + 1);
                            birthMatch = birthRegex.matcher(firstPContent);
                        }
                    }

                    // Lay chuc vu - nghe nghiep cua nhan vat
                    firstPContent = firstParagraph.text();
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

                    Pattern posiRegex = Pattern.compile("(l??|l??m)[^.]*[.]");
                    Matcher posiMatcher = posiRegex.matcher(firstPContent);

                    if (posiMatcher.find()) {
                        String result = posiMatcher.group(0);
                        if (!result.contains(":")) {
                            if (position.equals("Ch??a r??") || positionCheck(position) || position.equals("Thu???c")) {
                                position = result.substring(0, result.length() - 1);
                            }
                        }
                    }

                    // Lay trieu dai, nha???
                    for (Element a : pATags) {
                        String hrefValue = a.attr("href");
                        if (hrefValue.contains("nha-")) {
                            if (era.equals("Ch??a r??")) {
                                era = a.text();
                                break;
                            }
                        }
                    }
                    break;
                }
            }

            System.out.println("Name: " + charName);
            System.out.println("Date of birth and birth place: " + dateOfBirth);
            System.out.println("Lost date and lost place: " + lostDate);
            System.out.println("Position: " + position);
            System.out.println("Work time: " + workTime);
            System.out.println("Era: " + era);
            System.out.println("Father: " + charFather);
            System.out.println("Mother: " + charMother);
            System.out.println("Preceeded: " + preceeded);
            System.out.println("Succeeded: " + succeeded);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Crawl nhan vat
    public void getCharInfo() {
        for (String link : charInfoLinks) {
            crawlCharInfo(link);
        }
    }

    public void crawlData() {
//        getAllTimeStampLinks();
//        getAllChildLinkFromTimeStamp();
//        getAllCharInfoLinks();
        getCharInfoPageLink();
        getCharInfo();
    }
}